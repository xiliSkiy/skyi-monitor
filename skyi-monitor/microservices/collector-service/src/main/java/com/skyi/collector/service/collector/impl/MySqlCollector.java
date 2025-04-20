package com.skyi.collector.service.collector.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.skyi.collector.dto.CollectorTaskDTO;
import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.service.collector.AbstractCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL数据库采集器
 */
@Slf4j
@Component
public class MySqlCollector extends AbstractCollector {
    
    private static final String TYPE = "database";
    private static final String PROTOCOL = "mysql";
    
    // MySQL监控SQL语句
    private static final String CONNECTION_COUNT_SQL = "SELECT COUNT(1) AS count FROM information_schema.processlist";
    private static final String SLOW_QUERY_COUNT_SQL = "SELECT COUNT(*) AS count FROM mysql.slow_log";
    private static final String UPTIME_SQL = "SHOW GLOBAL STATUS LIKE 'Uptime'";
    private static final String QUESTIONS_SQL = "SHOW GLOBAL STATUS LIKE 'Questions'";
    private static final String BYTES_RECEIVED_SQL = "SHOW GLOBAL STATUS LIKE 'Bytes_received'";
    private static final String BYTES_SENT_SQL = "SHOW GLOBAL STATUS LIKE 'Bytes_sent'";
    private static final String INNODB_ROW_LOCK_TIME_SQL = "SHOW GLOBAL STATUS LIKE 'Innodb_row_lock_time'";
    private static final String INNODB_BUFFER_POOL_PAGES_FREE_SQL = "SHOW GLOBAL STATUS LIKE 'Innodb_buffer_pool_pages_free'";
    private static final String INNODB_BUFFER_POOL_PAGES_TOTAL_SQL = "SHOW GLOBAL STATUS LIKE 'Innodb_buffer_pool_pages_total'";
    
    @Override
    public String getType() {
        return TYPE;
    }
    
    @Override
    public List<String> getSupportedProtocols() {
        return List.of(PROTOCOL);
    }
    
    @Override
    public boolean testConnection(Map<String, String> connectionParams) {
        try {
            String url = buildJdbcUrl(connectionParams);
            String username = connectionParams.get("username");
            String password = connectionParams.get("password");
            
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                log.debug("MySQL连接测试成功: {}", url);
                return true;
            }
        } catch (Exception e) {
            log.error("MySQL连接测试失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<MetricDataDTO> collect(CollectorTask task, Long instanceId) {
        List<MetricDataDTO> results = new ArrayList<>();
        Connection conn = null;
        
        try {
            // 解析连接参数和指标
            Map<String, String> connectionParams = parseConnectionParams(task);
            List<CollectorTaskDTO.MetricDTO> metrics = parseMetrics(task);
            
            if (metrics.isEmpty() || connectionParams.isEmpty()) {
                log.warn("采集任务配置不完整, taskId={}", task.getId());
                return results;
            }
            
            String url = buildJdbcUrl(connectionParams);
            String username = connectionParams.get("username");
            String password = connectionParams.get("password");
            
            // 创建数据库连接
            conn = DriverManager.getConnection(url, username, password);
            
            // 为所有支持的指标创建标签Map
            Map<String, String> labels = new HashMap<>();
            labels.put("host", connectionParams.get("host"));
            labels.put("port", connectionParams.get("port"));
            labels.put("database", connectionParams.get("database"));
            
            // 处理每个指标
            for (CollectorTaskDTO.MetricDTO metric : metrics) {
                if (!metric.getEnabled()) {
                    continue;
                }
                
                String metricName = metric.getName();
                String metricPath = metric.getPath();
                Double value = null;
                
                // 获取指标数据
                if (metricPath.equalsIgnoreCase("connection_count")) {
                    value = executeQueryAndGetDoubleValue(conn, CONNECTION_COUNT_SQL, "count");
                } else if (metricPath.equalsIgnoreCase("slow_query_count")) {
                    value = executeQueryAndGetDoubleValue(conn, SLOW_QUERY_COUNT_SQL, "count");
                } else if (metricPath.equalsIgnoreCase("uptime")) {
                    value = executeShowStatusAndGetDoubleValue(conn, UPTIME_SQL);
                } else if (metricPath.equalsIgnoreCase("questions")) {
                    value = executeShowStatusAndGetDoubleValue(conn, QUESTIONS_SQL);
                } else if (metricPath.equalsIgnoreCase("bytes_received")) {
                    value = executeShowStatusAndGetDoubleValue(conn, BYTES_RECEIVED_SQL);
                } else if (metricPath.equalsIgnoreCase("bytes_sent")) {
                    value = executeShowStatusAndGetDoubleValue(conn, BYTES_SENT_SQL);
                } else if (metricPath.equalsIgnoreCase("innodb_row_lock_time")) {
                    value = executeShowStatusAndGetDoubleValue(conn, INNODB_ROW_LOCK_TIME_SQL);
                } else if (metricPath.equalsIgnoreCase("innodb_buffer_pool_usage")) {
                    Double free = executeShowStatusAndGetDoubleValue(conn, INNODB_BUFFER_POOL_PAGES_FREE_SQL);
                    Double total = executeShowStatusAndGetDoubleValue(conn, INNODB_BUFFER_POOL_PAGES_TOTAL_SQL);
                    if (free != null && total != null && total > 0) {
                        value = (total - free) / total * 100; // 计算使用率百分比
                    }
                } else if (metricPath.startsWith("custom_sql:")) {
                    // 支持自定义SQL查询
                    String sql = metricPath.substring("custom_sql:".length());
                    value = executeQueryAndGetDoubleValue(conn, sql, "value");
                }
                
                if (value != null) {
                    // 创建指标数据
                    MetricDataDTO metricData = createMetricData(
                            task, instanceId, metricName, value, labels);
                    results.add(metricData);
                } else {
                    log.warn("获取MySQL指标数据失败, metric={}", metricName);
                }
            }
        } catch (Exception e) {
            log.error("MySQL采集出错: {}", e.getMessage(), e);
        } finally {
            // 关闭数据库连接
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("关闭MySQL连接出错: {}", e.getMessage(), e);
                }
            }
        }
        
        return results;
    }
    
    @Override
    public String validateMetrics(String metrics) {
        try {
            List<CollectorTaskDTO.MetricDTO> metricsList = 
                    objectMapper.readValue(metrics, new TypeReference<List<CollectorTaskDTO.MetricDTO>>() {});
            
            for (CollectorTaskDTO.MetricDTO metric : metricsList) {
                if (metric.getName() == null || metric.getName().isEmpty()) {
                    return "指标名称不能为空";
                }
                if (metric.getPath() == null || metric.getPath().isEmpty()) {
                    return "指标路径不能为空";
                }
            }
            
            return null;
        } catch (Exception e) {
            return "指标配置格式不正确: " + e.getMessage();
        }
    }
    
    /**
     * 构建JDBC URL
     *
     * @param connectionParams 连接参数
     * @return JDBC URL
     */
    private String buildJdbcUrl(Map<String, String> connectionParams) {
        String host = connectionParams.get("host");
        String port = connectionParams.getOrDefault("port", "3306");
        String database = connectionParams.getOrDefault("database", "");
        
        StringBuilder urlBuilder = new StringBuilder("jdbc:mysql://")
                .append(host).append(":").append(port);
        
        if (!database.isEmpty()) {
            urlBuilder.append("/").append(database);
        }
        
        // 添加连接参数
        urlBuilder.append("?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        
        return urlBuilder.toString();
    }
    
    /**
     * 执行SQL查询并获取指定列的Double值
     *
     * @param conn 数据库连接
     * @param sql SQL语句
     * @param columnName 列名
     * @return 列值
     * @throws SQLException SQL异常
     */
    private Double executeQueryAndGetDoubleValue(Connection conn, String sql, String columnName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(columnName);
            }
        }
        return null;
    }
    
    /**
     * 执行SHOW STATUS语句并获取结果的Double值
     *
     * @param conn 数据库连接
     * @param sql SQL语句
     * @return 值
     * @throws SQLException SQL异常
     */
    private Double executeShowStatusAndGetDoubleValue(Connection conn, String sql) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return Double.parseDouble(rs.getString("Value"));
            }
        } catch (NumberFormatException e) {
            log.warn("无法将MySQL状态值转换为数字: {}", e.getMessage());
        }
        return null;
    }
} 