package com.skyi.collector.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyi.collector.service.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产服务HTTP实现
 * 通过调用资产服务API获取资产信息
 */
@Slf4j
@Service
public class HttpAssetServiceImpl implements AssetService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private DiscoveryClient discoveryClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${service.asset.name:asset-service}")
    private String assetServiceName;
    
    @Value("${service.asset.fallback.url:http://localhost:8081}")
    private String fallbackUrl;
    
    /**
     * 获取资产服务URL
     * 
     * @return 资产服务URL
     */
    private String getServiceUrl() {
        List<ServiceInstance> instances = discoveryClient.getInstances(assetServiceName);
        if (instances != null && !instances.isEmpty()) {
            return instances.get(0).getUri().toString();
        }
        log.warn("无法从服务发现获取资产服务地址，使用回退地址: {}", fallbackUrl);
        return fallbackUrl;
    }
    
    @Override
    public Map<String, Object> getAssetBasicInfo(Long assetId) {
        if (assetId == null) {
            log.warn("资产ID为空");
            return new HashMap<>();
        }
        
        try {
            String url = getServiceUrl() + "/assets/" + assetId + "/basic";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseAssetResponse(response.getBody());
            } else {
                log.warn("获取资产基本信息失败，状态码: {}", response.getStatusCode());
                return new HashMap<>();
            }
        } catch (RestClientException e) {
            log.error("调用资产服务获取基本信息出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> getAssetDetailInfo(Long assetId) {
        if (assetId == null) {
            log.warn("资产ID为空");
            return new HashMap<>();
        }
        
        try {
            String url = getServiceUrl() + "/assets/" + assetId;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseAssetResponse(response.getBody());
            } else {
                log.warn("获取资产详细信息失败，状态码: {}", response.getStatusCode());
                return new HashMap<>();
            }
        } catch (RestClientException e) {
            log.error("调用资产服务获取详细信息出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> getAssetsByType(String assetType) {
        if (assetType == null || assetType.isBlank()) {
            log.warn("资产类型为空");
            return new ArrayList<>();
        }
        
        try {
            String url = getServiceUrl() + "/assets/type/" + assetType;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseAssetListResponse(response.getBody());
            } else {
                log.warn("获取资产列表失败，状态码: {}", response.getStatusCode());
                return new ArrayList<>();
            }
        } catch (RestClientException e) {
            log.error("调用资产服务获取资产列表出错: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean existsAsset(Long assetId) {
        if (assetId == null) {
            return false;
        }
        
        try {
            String url = getServiceUrl() + "/assets/" + assetId + "/exists";
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> result = parseGenericResponse(response.getBody());
                if (result.containsKey("exists")) {
                    return Boolean.TRUE.equals(result.get("exists"));
                }
            }
            
            // 如果没有专门的exists接口，则尝试获取资产信息判断
            url = getServiceUrl() + "/assets/" + assetId;
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            
            return response.getStatusCode().is2xxSuccessful() && response.getBody() != null && 
                   !response.getBody().isBlank();
            
        } catch (RestClientException e) {
            log.error("调用资产服务检查资产是否存在出错: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getAssetConnectionParams(Long assetId, String protocol) {
        if (assetId == null || protocol == null || protocol.isBlank()) {
            log.warn("资产ID或协议为空");
            return new HashMap<>();
        }
        
        try {
            String url = getServiceUrl() + "/assets/" + assetId + "/connection/" + protocol;
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseConnectionParamsResponse(response.getBody());
            } else {
                log.warn("获取资产连接参数失败，状态码: {}", response.getStatusCode());
                
                // 如果没有专门的连接参数接口，则尝试从资产详情中提取
                Map<String, Object> assetInfo = getAssetDetailInfo(assetId);
                if (assetInfo.containsKey("connectionInfo")) {
                    Object connectionInfo = assetInfo.get("connectionInfo");
                    if (connectionInfo instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> connInfoMap = (Map<String, Object>) connectionInfo;
                        if (connInfoMap.containsKey(protocol)) {
                            return (Map<String, Object>) connInfoMap.get(protocol);
                        }
                    }
                }
                
                // 提取常见连接信息
                Map<String, Object> result = new HashMap<>();
                if (assetInfo.containsKey("ip")) {
                    result.put("ipAddress", assetInfo.get("ip"));
                }
                if (assetInfo.containsKey("port")) {
                    result.put("port", assetInfo.get("port"));
                }
                if (assetInfo.containsKey("username")) {
                    result.put("username", assetInfo.get("username"));
                }
                if (assetInfo.containsKey("password")) {
                    result.put("password", assetInfo.get("password"));
                }
                
                // 对于SNMP协议，添加默认的community
                if ("snmp".equalsIgnoreCase(protocol)) {
                    result.putIfAbsent("community", "public");
                    result.putIfAbsent("version", 2);
                }
                
                return result;
            }
        } catch (RestClientException e) {
            log.error("调用资产服务获取连接参数出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 解析资产响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseAssetResponse(String responseBody) {
        try {
            // 解析响应体
            Map<String, Object> response = objectMapper.readValue(responseBody, 
                    new TypeReference<Map<String, Object>>() {});
            
            // 检查响应格式
            if (response.containsKey("data")) {
                Object data = response.get("data");
                if (data instanceof Map) {
                    return (Map<String, Object>) data;
                }
            }
            
            // 如果响应中没有data字段，则尝试直接使用整个响应
            if (!response.isEmpty()) {
                return response;
            }
            
            return new HashMap<>();
        } catch (JsonProcessingException e) {
            log.error("解析资产响应出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 解析资产列表响应
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseAssetListResponse(String responseBody) {
        try {
            // 解析响应体
            Map<String, Object> response = objectMapper.readValue(responseBody, 
                    new TypeReference<Map<String, Object>>() {});
            
            // 检查响应格式
            if (response.containsKey("data")) {
                Object data = response.get("data");
                if (data instanceof List) {
                    return (List<Map<String, Object>>) data;
                }
            }
            
            return new ArrayList<>();
        } catch (JsonProcessingException e) {
            log.error("解析资产列表响应出错: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 解析连接参数响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConnectionParamsResponse(String responseBody) {
        try {
            // 解析响应体
            Map<String, Object> response = objectMapper.readValue(responseBody, 
                    new TypeReference<Map<String, Object>>() {});
            
            // 检查响应格式
            if (response.containsKey("data")) {
                Object data = response.get("data");
                if (data instanceof Map) {
                    return (Map<String, Object>) data;
                }
            }
            
            return new HashMap<>();
        } catch (JsonProcessingException e) {
            log.error("解析连接参数响应出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 解析通用响应
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseGenericResponse(String responseBody) {
        try {
            // 解析响应体
            Map<String, Object> response = objectMapper.readValue(responseBody, 
                    new TypeReference<Map<String, Object>>() {});
            
            // 检查响应格式
            if (response.containsKey("data")) {
                Object data = response.get("data");
                if (data instanceof Map) {
                    return (Map<String, Object>) data;
                } else if (data instanceof Boolean || data instanceof Number || data instanceof String) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("value", data);
                    return result;
                }
            }
            
            return response;
        } catch (JsonProcessingException e) {
            log.error("解析通用响应出错: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
} 