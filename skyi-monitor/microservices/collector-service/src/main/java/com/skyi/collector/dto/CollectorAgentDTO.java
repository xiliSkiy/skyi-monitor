package com.skyi.collector.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集Agent数据传输对象
 */
@Data
public class CollectorAgentDTO {
    /**
     * Agent ID
     */
    private Long id;
    
    /**
     * Agent名称
     */
    @NotBlank(message = "Agent名称不能为空")
    private String name;
    
    /**
     * Agent编码
     */
    @NotBlank(message = "Agent编码不能为空")
    private String code;
    
    /**
     * IP地址
     */
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", message = "IP地址格式不正确")
    private String ipAddress;
    
    /**
     * 版本
     */
    private String version;
    
    /**
     * 状态（1-在线，0-离线）
     */
    private Integer status;
    
    /**
     * 支持的协议类型
     */
    private List<String> supportedProtocols;
    
    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 