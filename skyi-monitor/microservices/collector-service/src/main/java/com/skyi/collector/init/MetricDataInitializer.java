package com.skyi.collector.init;

import com.skyi.collector.dto.MetricAssetTypeMappingDTO;
import com.skyi.collector.dto.MetricDefinitionDTO;
import com.skyi.collector.dto.MetricProtocolMappingDTO;
import com.skyi.collector.service.MetricDefinitionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 指标数据初始化器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricDataInitializer implements CommandLineRunner {

    private final MetricDefinitionService metricService;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("开始初始化指标数据...");
            initSystemMetrics();
            initServerMetrics();
            initDatabaseMetrics();
            log.info("指标数据初始化完成");
        } catch (Exception e) {
            log.error("指标数据初始化失败", e);
        }
    }
    
    /**
     * 初始化系统指标
     */
    private void initSystemMetrics() {
        log.info("初始化系统指标...");
        List<MetricDefinitionDTO> systemMetrics = new ArrayList<>();
        
        // 系统状态指标
        systemMetrics.add(createMetric(
                "system.status", "系统状态", "系统", "gauge", "状态码",
                "http", "{\"url\":\"/api/system/status\",\"method\":\"GET\"}",
                Arrays.asList(new String[]{"http"}),
                Arrays.asList(new String[]{"system", "application"})
        ));
        
        // 系统响应时间指标
        systemMetrics.add(createMetric(
                "system.response.time", "系统响应时间", "系统", "gauge", "毫秒",
                "http", "{\"url\":\"/api/system/health\",\"method\":\"GET\"}",
                Arrays.asList(new String[]{"http"}),
                Arrays.asList(new String[]{"system", "application"})
        ));
        
        // 导入系统指标
        metricService.importMetrics(systemMetrics);
    }
    
    /**
     * 初始化服务器指标
     */
    private void initServerMetrics() {
        log.info("初始化服务器指标...");
        List<MetricDefinitionDTO> serverMetrics = new ArrayList<>();
        
        // CPU使用率指标
        serverMetrics.add(createMetric(
                "server.cpu.usage", "CPU使用率", "服务器", "gauge", "%",
                "snmp", "{\"oid\":\"1.3.6.1.4.1.2021.11.9.0\"}",
                Arrays.asList(new String[]{"snmp", "ssh"}),
                Arrays.asList(new String[]{"server"})
        ));
        
        // 内存使用率指标
        serverMetrics.add(createMetric(
                "server.memory.usage", "内存使用率", "服务器", "gauge", "%",
                "snmp", "{\"oid\":\"1.3.6.1.4.1.2021.4.11.0\"}",
                Arrays.asList(new String[]{"snmp", "ssh"}),
                Arrays.asList(new String[]{"server"})
        ));
        
        // 磁盘使用率指标
        serverMetrics.add(createMetric(
                "server.disk.usage", "磁盘使用率", "服务器", "gauge", "%",
                "snmp", "{\"oid\":\"1.3.6.1.4.1.2021.9.1.9.1\"}",
                Arrays.asList(new String[]{"snmp", "ssh"}),
                Arrays.asList(new String[]{"server"})
        ));
        
        // 网络流入流量指标
        serverMetrics.add(createMetric(
                "server.network.in", "网络流入流量", "服务器", "counter", "byte/s",
                "snmp", "{\"oid\":\"1.3.6.1.2.1.31.1.1.1.6.1\"}",
                Arrays.asList(new String[]{"snmp"}),
                Arrays.asList(new String[]{"server", "network"})
        ));
        
        // 网络流出流量指标
        serverMetrics.add(createMetric(
                "server.network.out", "网络流出流量", "服务器", "counter", "byte/s",
                "snmp", "{\"oid\":\"1.3.6.1.2.1.31.1.1.1.10.1\"}",
                Arrays.asList(new String[]{"snmp"}),
                Arrays.asList(new String[]{"server", "network"})
        ));
        
        // 导入服务器指标
        metricService.importMetrics(serverMetrics);
    }
    
    /**
     * 初始化数据库指标
     */
    private void initDatabaseMetrics() {
        log.info("初始化数据库指标...");
        List<MetricDefinitionDTO> dbMetrics = new ArrayList<>();
        
        // MySQL连接数指标
        dbMetrics.add(createMetric(
                "db.mysql.connections", "MySQL连接数", "数据库", "gauge", "个",
                "jdbc", "{\"sql\":\"SELECT COUNT(1) FROM information_schema.processlist\"}",
                Arrays.asList(new String[]{"jdbc"}),
                Arrays.asList(new String[]{"database", "mysql"})
        ));
        
        // MySQL QPS指标
        dbMetrics.add(createMetric(
                "db.mysql.qps", "MySQL每秒查询数", "数据库", "gauge", "次/秒",
                "jdbc", "{\"sql\":\"SHOW GLOBAL STATUS LIKE 'Questions'\"}",
                Arrays.asList(new String[]{"jdbc"}),
                Arrays.asList(new String[]{"database", "mysql"})
        ));
        
        // MySQL慢查询数指标
        dbMetrics.add(createMetric(
                "db.mysql.slow_queries", "MySQL慢查询数", "数据库", "counter", "次",
                "jdbc", "{\"sql\":\"SHOW GLOBAL STATUS LIKE 'Slow_queries'\"}",
                Arrays.asList(new String[]{"jdbc"}),
                Arrays.asList(new String[]{"database", "mysql"})
        ));
        
        // 导入数据库指标
        metricService.importMetrics(dbMetrics);
    }
    
    /**
     * 创建指标定义
     *
     * @param code 指标编码
     * @param name 指标名称
     * @param category 指标类别
     * @param dataType 数据类型
     * @param unit 单位
     * @param collectionMethod 采集方式
     * @param paramTemplate 参数模板
     * @param protocols 协议列表
     * @param assetTypes 资产类型列表
     * @return 指标定义
     */
    private MetricDefinitionDTO createMetric(String code, String name, String category, String dataType, String unit,
                                           String collectionMethod, String paramTemplate, 
                                           List<String> protocols, List<String> assetTypes) {
        MetricDefinitionDTO metric = new MetricDefinitionDTO();
        metric.setCode(code);
        metric.setName(name);
        metric.setCategory(category);
        metric.setDataType(dataType);
        metric.setUnit(unit);
        metric.setCollectionMethod(collectionMethod);
        metric.setParamTemplate(paramTemplate);
        metric.setStatus(1);
        
        // 设置协议映射
        List<MetricProtocolMappingDTO> protocolMappings = new ArrayList<>();
        for (String protocol : protocols) {
            MetricProtocolMappingDTO mapping = new MetricProtocolMappingDTO();
            mapping.setProtocol(protocol);
            
            // 根据协议设置不同的路径
            switch (protocol) {
                case "snmp":
                    // 从paramTemplate中提取OID作为路径
                    if (paramTemplate.contains("oid")) {
                        mapping.setPath(paramTemplate.replaceAll(".*\"oid\":\"([^\"]+)\".*", "$1"));
                    } else {
                        mapping.setPath("");
                    }
                    break;
                case "http":
                    // 从paramTemplate中提取URL作为路径
                    if (paramTemplate.contains("url")) {
                        mapping.setPath(paramTemplate.replaceAll(".*\"url\":\"([^\"]+)\".*", "$1"));
                    } else {
                        mapping.setPath("/api/metrics/" + code);
                    }
                    break;
                case "jdbc":
                    // 从paramTemplate中提取SQL作为路径
                    if (paramTemplate.contains("sql")) {
                        mapping.setPath(paramTemplate.replaceAll(".*\"sql\":\"([^\"]+)\".*", "$1"));
                    } else {
                        mapping.setPath("");
                    }
                    break;
                case "ssh":
                    mapping.setPath("script/" + code.replace(".", "_") + ".sh");
                    break;
                default:
                    mapping.setPath("");
            }
            
            mapping.setParameters(paramTemplate);
            protocolMappings.add(mapping);
        }
        metric.setProtocolMappings(protocolMappings);
        
        // 设置资产类型映射
        List<MetricAssetTypeMappingDTO> assetTypeMappings = new ArrayList<>();
        for (String assetType : assetTypes) {
            MetricAssetTypeMappingDTO mapping = new MetricAssetTypeMappingDTO();
            mapping.setAssetType(assetType);
            mapping.setDefaultEnabled(1);
            assetTypeMappings.add(mapping);
        }
        metric.setAssetTypeMappings(assetTypeMappings);
        
        return metric;
    }
} 