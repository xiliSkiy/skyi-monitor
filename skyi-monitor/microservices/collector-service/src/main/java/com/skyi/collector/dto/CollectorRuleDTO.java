package com.skyi.collector.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 调度规则数据传输对象
 */
@Data
public class CollectorRuleDTO {
    /**
     * 规则ID
     */
    private Long id;
    
    /**
     * 规则名称
     */
    @NotBlank(message = "规则名称不能为空")
    private String name;
    
    /**
     * 规则类型（cron, interval, fixedTime）
     */
    @NotBlank(message = "规则类型不能为空")
    private String type;
    
    /**
     * 表达式
     * cron类型: cron表达式，如 0 0 12 * * ?
     * interval类型: 间隔秒数，如 60
     * fixedTime类型: 固定时间，如 12:00:00
     */
    @NotBlank(message = "表达式不能为空")
    private String expression;
    
    /**
     * 状态（1-启用，0-禁用）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 关联的任务ID列表
     */
    @NotEmpty(message = "任务ID列表不能为空")
    private List<Long> taskIds;
    
    /**
     * 执行超时时间(秒)
     */
    @Min(value = 5, message = "超时时间不能小于5秒")
    private Integer timeout;
    
    /**
     * 失败重试次数
     */
    @Min(value = 0, message = "重试次数不能小于0")
    private Integer retryCount;
    
    /**
     * 重试间隔(秒)
     */
    @Min(value = 5, message = "重试间隔不能小于5秒")
    private Integer retryInterval;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 