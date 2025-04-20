package com.skyi.collector.service.collector.impl;

import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.model.MetricDefinition;
import com.skyi.collector.service.collector.AbstractMetricCollector;
import com.skyi.collector.service.collector.CollectionContext;
import com.skyi.collector.service.collector.CollectionResult;
import com.skyi.collector.service.collector.ConnectionTestResult;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

/**
 * SNMP协议指标采集器
 * 支持服务器、网络设备等资产类型的SNMP指标采集
 */
@Slf4j
@Component
public class SnmpMetricCollector extends AbstractMetricCollector {
    
    private static final String PROTOCOL = "snmp";
    private static final List<String> SUPPORTED_ASSET_TYPES = Arrays.asList("server", "network", "storage");
    
    @Override
    public String getProtocol() {
        return PROTOCOL;
    }
    
    @Override
    public List<String> getSupportedAssetTypes() {
        return SUPPORTED_ASSET_TYPES;
    }
    
    @Override
    public ConnectionTestResult testConnection(String assetType, Map<String, Object> connectionParams) {
        LocalDateTime startTime = LocalDateTime.now();
        try {
            // 解析连接参数
            String ipAddress = getStringParam(connectionParams, "ipAddress");
            int port = getIntParam(connectionParams, "port", 161);
            String community = getStringParam(connectionParams, "community", "public");
            int version = getIntParam(connectionParams, "version", 2);
            int timeout = getIntParam(connectionParams, "timeout", 1000);
            int retries = getIntParam(connectionParams, "retries", 2);
            
            if (ipAddress == null || ipAddress.isBlank()) {
                return ConnectionTestResult.failure("连接测试失败", "IP地址不能为空");
            }
            
            // 创建SNMP连接
            Address targetAddress = new UdpAddress(ipAddress + "/" + port);
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();
            
            // 创建目标对象
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setRetries(retries);
            target.setTimeout(timeout);
            target.setVersion(version == 1 ? SnmpConstants.version1 : 
                             (version == 3 ? SnmpConstants.version3 : SnmpConstants.version2c));
            
            // 创建测试OID (sysDescr.0)
            OID testOid = new OID(".1.3.6.1.2.1.1.1.0");
            
            // 创建PDU
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(testOid));
            pdu.setType(PDU.GET);
            
            // 发送GET请求并记录时间
            long requestTime = System.currentTimeMillis();
            ResponseEvent response = snmp.send(pdu, target);
            long responseTime = System.currentTimeMillis();
            long latency = responseTime - requestTime;
            
            // 关闭连接
            snmp.close();
            
            // 检查响应
            if (response == null || response.getResponse() == null) {
                return ConnectionTestResult.failure("连接测试失败", "未收到响应");
            }
            
            PDU responsePDU = response.getResponse();
            if (responsePDU.getErrorStatus() != PDU.noError) {
                return ConnectionTestResult.failure("连接测试失败", 
                        "错误码: " + responsePDU.getErrorStatus() + 
                        ", 错误信息: " + responsePDU.getErrorStatusText());
            }
            
            // 提取系统描述信息
            VariableBinding vb = responsePDU.get(0);
            String sysDescr = vb != null ? vb.getVariable().toString() : "未知";
            
            // 返回成功结果
            ConnectionTestResult result = ConnectionTestResult.success("连接测试成功", latency);
            result.addDetail("sysDescr", sysDescr);
            result.addDetail("ipAddress", ipAddress);
            result.addDetail("port", port);
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            log.error("SNMP连接测试失败: {}", e.getMessage(), e);
            return ConnectionTestResult.failure("连接测试失败", e.getMessage())
                    .addDetail("duration", duration);
        }
    }
    
    @Override
    public CollectionResult collect(CollectionContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        List<MetricDataDTO> results = new ArrayList<>();
        
        try {
            // 预处理上下文
            preProcess(context);
            
            // 解析连接参数
            Map<String, Object> connectionParams = context.getConnectionParams();
            String ipAddress = getStringParam(connectionParams, "ipAddress");
            int port = getIntParam(connectionParams, "port", 161);
            String community = getStringParam(connectionParams, "community", "public");
            int version = getIntParam(connectionParams, "version", 2);
            int timeout = getIntParam(connectionParams, "timeout", 1000);
            int retries = getIntParam(connectionParams, "retries", 2);
            
            if (ipAddress == null || ipAddress.isBlank()) {
                return CollectionResult.failure(startTime, LocalDateTime.now(), "IP地址不能为空");
            }
            
            // 获取要采集的指标
            List<MetricDefinition> metrics = context.getMetrics();
            if (metrics == null || metrics.isEmpty()) {
                return CollectionResult.failure(startTime, LocalDateTime.now(), "没有配置采集指标");
            }
            
            // 创建SNMP连接
            Address targetAddress = new UdpAddress(ipAddress + "/" + port);
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();
            
            // 创建目标对象
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setRetries(retries);
            target.setTimeout(timeout);
            target.setVersion(version == 1 ? SnmpConstants.version1 : 
                             (version == 3 ? SnmpConstants.version3 : SnmpConstants.version2c));
            
            // 创建结果对象
            CollectionResult result = CollectionResult.builder()
                    .success(true)
                    .startTime(startTime)
                    .metricData(new ArrayList<>())
                    .build();
            
            // 使用批量采集模式，将多个OID一次性查询
            if (supportsBatchCollection()) {
                result = collectBatch(context, snmp, target, metrics, result);
            } else {
                // 单独采集每个指标
                for (MetricDefinition metric : metrics) {
                    collectSingleMetric(context, snmp, target, metric, result);
                }
            }
            
            // 关闭连接
            snmp.close();
            
            // 设置结束时间
            result.setEndTime(LocalDateTime.now());
            
            // 后处理结果
            return postProcess(context, result);
            
        } catch (Exception e) {
            return handleCollectionException(e, startTime, "SNMP采集出错");
        }
    }
    
    /**
     * 批量采集多个指标
     */
    private CollectionResult collectBatch(
            CollectionContext context,
            Snmp snmp, 
            CommunityTarget target,
            List<MetricDefinition> metrics,
            CollectionResult result) throws Exception {
        
        // 创建PDU
        PDU pdu = new PDU();
        pdu.setType(PDU.GET);
        
        // 记录指标ID和OID的映射关系，用于结果处理
        Map<String, MetricDefinition> oidToMetricMap = new HashMap<>();
        
        // 添加所有指标的OID到PDU
        for (MetricDefinition metric : metrics) {
            String path = getMetricPath(context, metric);
            if (path == null || path.isBlank()) {
                result.addMetricError(metric.getId(), "指标路径为空");
                continue;
            }
            
            try {
                OID oid = new OID(path);
                pdu.add(new VariableBinding(oid));
                oidToMetricMap.put(oid.toString(), metric);
            } catch (Exception e) {
                log.warn("无效的OID[{}]: {}", path, e.getMessage());
                result.addMetricError(metric.getId(), "无效的OID: " + e.getMessage());
            }
        }
        
        // 如果PDU为空，则返回结果
        if (pdu.size() == 0) {
            result.setErrorMessage("没有有效的OID可供查询");
            result.setSuccess(false);
            return result;
        }
        
        // 发送请求
        ResponseEvent response = snmp.send(pdu, target);
        
        // 处理响应
        if (response == null || response.getResponse() == null) {
            result.setErrorMessage("未收到SNMP响应");
            result.setSuccess(false);
            return result;
        }
        
        PDU responsePDU = response.getResponse();
        if (responsePDU.getErrorStatus() != PDU.noError) {
            result.setErrorMessage("SNMP错误: " + responsePDU.getErrorStatusText());
            result.setSuccess(false);
            return result;
        }
        
        // 处理每个变量绑定
        for (int i = 0; i < responsePDU.size(); i++) {
            VariableBinding vb = responsePDU.get(i);
            OID oid = vb.getOid();
            Variable var = vb.getVariable();
            
            // 查找对应的指标
            MetricDefinition metric = oidToMetricMap.get(oid.toString());
            if (metric == null) {
                log.warn("收到未知OID的响应: {}", oid);
                continue;
            }
            
            try {
                // 解析值
                Double value = parseSnmpValue(vb);
                
                // 应用表达式转换
                if (value != null) {
                    value = parseExpression(context, metric, value);
                }
                
                // 创建指标数据
                if (value != null) {
                    Map<String, String> labels = new HashMap<>();
                    labels.put("host", context.getAssetIp());
                    labels.put("oid", oid.toString());
                    
                    MetricDataDTO metricData = createMetricData(context, metric, value, labels);
                    result.addMetricData(metricData);
                } else {
                    result.addMetricError(metric.getId(), "无法解析指标值");
                }
            } catch (Exception e) {
                log.warn("处理指标[{}]响应出错: {}", metric.getCode(), e.getMessage());
                result.addMetricError(metric.getId(), "处理响应出错: " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * 采集单个指标
     */
    private void collectSingleMetric(
            CollectionContext context,
            Snmp snmp, 
            CommunityTarget target,
            MetricDefinition metric,
            CollectionResult result) {
        
        String path = getMetricPath(context, metric);
        if (path == null || path.isBlank()) {
            result.addMetricError(metric.getId(), "指标路径为空");
            return;
        }
        
        try {
            // 创建PDU
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(path)));
            pdu.setType(PDU.GET);
            
            // 发送请求
            ResponseEvent response = snmp.send(pdu, target);
            
            // 处理响应
            if (response == null || response.getResponse() == null) {
                result.addMetricError(metric.getId(), "未收到SNMP响应");
                return;
            }
            
            PDU responsePDU = response.getResponse();
            if (responsePDU.getErrorStatus() != PDU.noError) {
                result.addMetricError(metric.getId(), 
                        "SNMP错误: " + responsePDU.getErrorStatusText());
                return;
            }
            
            // 获取变量绑定
            VariableBinding vb = responsePDU.get(0);
            if (vb == null) {
                result.addMetricError(metric.getId(), "响应中没有变量绑定");
                return;
            }
            
            // 解析值
            Double value = parseSnmpValue(vb);
            
            // 应用表达式转换
            if (value != null) {
                value = parseExpression(context, metric, value);
            }
            
            // 创建指标数据
            if (value != null) {
                Map<String, String> labels = new HashMap<>();
                labels.put("host", context.getAssetIp());
                labels.put("oid", path);
                
                MetricDataDTO metricData = createMetricData(context, metric, value, labels);
                result.addMetricData(metricData);
            } else {
                result.addMetricError(metric.getId(), "无法解析指标值");
            }
            
        } catch (Exception e) {
            log.warn("采集指标[{}]出错: {}", metric.getCode(), e.getMessage());
            result.addMetricError(metric.getId(), "采集出错: " + e.getMessage());
        }
    }
    
    /**
     * 解析SNMP变量值为Double
     */
    private Double parseSnmpValue(VariableBinding vb) {
        if (vb == null) {
            return null;
        }
        
        Variable var = vb.getVariable();
        if (var == null) {
            return null;
        }
        
        try {
            if (var instanceof Integer32) {
                return (double) ((Integer32) var).getValue();
            } else if (var instanceof UnsignedInteger32) {
                return (double) ((UnsignedInteger32) var).getValue();
            } else if (var instanceof Counter32) {
                return (double) ((Counter32) var).getValue();
            } else if (var instanceof Counter64) {
                return (double) ((Counter64) var).getValue();
            } else if (var instanceof Gauge32) {
                return (double) ((Gauge32) var).getValue();
            } else if (var instanceof TimeTicks) {
                return (double) ((TimeTicks) var).getValue();
            } else {
                try {
                    return Double.parseDouble(var.toString());
                } catch (NumberFormatException e) {
                    log.warn("无法将SNMP值[{}]转换为Double: {}", var, e.getMessage());
                    return null;
                }
            }
        } catch (Exception e) {
            log.warn("解析SNMP值时发生错误: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取字符串类型的连接参数
     */
    private String getStringParam(Map<String, Object> params, String key) {
        return getStringParam(params, key, null);
    }
    
    /**
     * 获取字符串类型的连接参数，带默认值
     */
    private String getStringParam(Map<String, Object> params, String key, String defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        
        Object value = params.get(key);
        return value != null ? value.toString() : defaultValue;
    }
    
    /**
     * 获取整数类型的连接参数，带默认值
     */
    private int getIntParam(Map<String, Object> params, String key, int defaultValue) {
        if (params == null || !params.containsKey(key)) {
            return defaultValue;
        }
        
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
} 