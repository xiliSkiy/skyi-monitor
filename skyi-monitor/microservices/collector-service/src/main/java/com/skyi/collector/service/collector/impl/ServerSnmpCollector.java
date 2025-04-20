package com.skyi.collector.service.collector.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.skyi.collector.dto.CollectorTaskDTO;
import com.skyi.collector.dto.MetricDataDTO;
import com.skyi.collector.model.CollectorTask;
import com.skyi.collector.service.collector.AbstractCollector;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器SNMP采集器
 */
@Slf4j
@Component
public class ServerSnmpCollector extends AbstractCollector {
    
    private static final String TYPE = "server";
    private static final String PROTOCOL = "snmp";
    
    // OID前缀
    private static final String CPU_LOAD_OID = ".1.3.6.1.4.1.2021.10.1.3.1";
    private static final String MEM_TOTAL_OID = ".1.3.6.1.4.1.2021.4.5.0";
    private static final String MEM_FREE_OID = ".1.3.6.1.4.1.2021.4.6.0";
    private static final String DISK_TOTAL_OID_PREFIX = ".1.3.6.1.4.1.2021.9.1.6";
    private static final String DISK_USED_OID_PREFIX = ".1.3.6.1.4.1.2021.9.1.8";
    
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
            String ipAddress = connectionParams.get("ipAddress");
            String port = connectionParams.getOrDefault("port", "161");
            String community = connectionParams.getOrDefault("community", "public");
            int version = Integer.parseInt(connectionParams.getOrDefault("version", "1"));
            
            // 创建SNMP实例并测试连接
            Address targetAddress = new UdpAddress(ipAddress + "/" + port);
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();
            
            // 创建目标对象
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setRetries(2);
            target.setTimeout(1000);
            target.setVersion(version == 2 ? SnmpConstants.version2c : SnmpConstants.version1);
            
            // 创建PDU
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(CPU_LOAD_OID)));
            pdu.setType(PDU.GET);
            
            // 发送GET请求
            ResponseEvent response = snmp.send(pdu, target);
            
            // 关闭连接
            snmp.close();
            
            // 检查响应
            if (response != null && response.getResponse() != null) {
                return response.getResponse().getErrorStatus() == PDU.noError;
            }
            
            return false;
        } catch (Exception e) {
            log.error("SNMP连接测试失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<MetricDataDTO> collect(CollectorTask task, Long instanceId) {
        List<MetricDataDTO> results = new ArrayList<>();
        try {
            // 解析连接参数和指标
            Map<String, String> connectionParams = parseConnectionParams(task);
            List<CollectorTaskDTO.MetricDTO> metrics = parseMetrics(task);
            
            if (metrics.isEmpty() || connectionParams.isEmpty()) {
                log.warn("采集任务配置不完整, taskId={}", task.getId());
                return results;
            }
            
            String ipAddress = connectionParams.get("ipAddress");
            String port = connectionParams.getOrDefault("port", "161");
            String community = connectionParams.getOrDefault("community", "public");
            int version = Integer.parseInt(connectionParams.getOrDefault("version", "1"));
            
            // 创建SNMP实例
            Address targetAddress = new UdpAddress(ipAddress + "/" + port);
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();
            
            // 创建目标对象
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setAddress(targetAddress);
            target.setRetries(2);
            target.setTimeout(1000);
            target.setVersion(version == 2 ? SnmpConstants.version2c : SnmpConstants.version1);
            
            // 处理每个指标
            for (CollectorTaskDTO.MetricDTO metric : metrics) {
                if (!metric.getEnabled()) {
                    continue;
                }
                
                // 获取指标OID
                String oid = metric.getPath();
                
                try {
                    // 创建PDU
                    PDU pdu = new PDU();
                    pdu.add(new VariableBinding(new OID(oid)));
                    pdu.setType(PDU.GET);
                    
                    // 发送GET请求
                    ResponseEvent response = snmp.send(pdu, target);
                    
                    // 处理响应
                    if (response != null && response.getResponse() != null) {
                        PDU responsePDU = response.getResponse();
                        if (responsePDU.getErrorStatus() == PDU.noError) {
                            VariableBinding vb = responsePDU.get(0);
                            if (vb != null) {
                                Variable variable = vb.getVariable();
                                Double value = parseSnmpValue(vb);
                                
                                if (value != null) {
                                    // 创建标签
                                    Map<String, String> labels = new HashMap<>();
                                    labels.put("host", ipAddress);
                                    labels.put("metric", metric.getName());
                                    
                                    // 创建指标数据
                                    MetricDataDTO metricData = createMetricData(
                                            task, instanceId, metric.getName(), value, labels);
                                    results.add(metricData);
                                }
                            }
                        } else {
                            log.warn("SNMP响应错误: {}, OID={}", 
                                    responsePDU.getErrorStatusText(), oid);
                        }
                    } else {
                        log.warn("SNMP响应为空, OID={}", oid);
                    }
                } catch (Exception e) {
                    log.error("采集指标出错: {}, OID={}", e.getMessage(), oid, e);
                }
            }
            
            // 关闭连接
            snmp.close();
            
        } catch (IOException e) {
            log.error("SNMP采集出错: {}", e.getMessage(), e);
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
                    return "指标路径(OID)不能为空";
                }
                if (!metric.getPath().startsWith(".")) {
                    return "指标路径必须是有效的OID格式";
                }
            }
            
            return null;
        } catch (Exception e) {
            return "指标配置格式不正确: " + e.getMessage();
        }
    }
    
    /**
     * 将SNMP变量值解析为Double
     * @param variableBinding 变量绑定对象
     * @return Double类型的值
     */
    private Double parseSnmpValue(VariableBinding variableBinding) {
        if (variableBinding == null) {
            log.warn("SNMP变量绑定对象为空");
            return 0.0;
        }

        Variable variable = variableBinding.getVariable();
        try {
            if (variable instanceof Integer32) {
                return (double) ((Integer32) variable).getValue();
            } else if (variable instanceof UnsignedInteger32) {
                return (double) ((UnsignedInteger32) variable).getValue();
            } else if (variable instanceof Counter32) {
                return (double) ((Counter32) variable).getValue();
            } else if (variable instanceof Counter64) {
                return (double) ((Counter64) variable).getValue();
            } else if (variable instanceof Gauge32) {
                return (double) ((Gauge32) variable).getValue();
            } else if (variable instanceof TimeTicks) {
                return (double) ((TimeTicks) variable).getValue();
            } else {
                try {
                    return Double.parseDouble(variable.toString());
                } catch (NumberFormatException e) {
                    log.warn("无法将SNMP值[{}]转换为Double: {}", variable, e.getMessage());
                    return 0.0;
                }
            }
        } catch (Exception e) {
            log.warn("解析SNMP值时发生错误: {}", e.getMessage());
            return 0.0;
        }
    }
} 