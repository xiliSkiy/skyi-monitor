package com.skyi.asset.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产数据传输对象
 */
@Data
public class AssetDTO {
    /**
     * 资产ID
     */
    private Long id;
    
    /**
     * 资产名称
     */
    @NotBlank(message = "资产名称不能为空")
    @Size(max = 100, message = "资产名称长度不能超过100")
    private String name;
    
    /**
     * 资产编码
     */
    @NotBlank(message = "资产编码不能为空")
    @Size(max = 50, message = "资产编码长度不能超过50")
    private String code;
    
    /**
     * 资产类型
     */
    @NotBlank(message = "资产类型不能为空")
    @Pattern(regexp = "server|database|middleware|application", message = "资产类型只能是server/database/middleware/application")
    private String type;
    
    /**
     * IP地址
     */
    @Pattern(regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$", message = "IP地址格式不正确")
    private String ipAddress;
    
    /**
     * 端口
     */
    private Integer port;
    
    /**
     * 所属部门
     */
    @Size(max = 50, message = "所属部门长度不能超过50")
    private String department;
    
    /**
     * 负责人
     */
    @Size(max = 50, message = "负责人长度不能超过50")
    private String owner;
    
    /**
     * 状态
     */
    @NotNull(message = "状态不能为空")
    private Integer status;
    
    /**
     * 描述
     */
    @Size(max = 500, message = "描述长度不能超过500")
    private String description;
    
    /**
     * 标签列表
     */
    private List<TagDTO> tags;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 标签数据传输对象
     */
    @Data
    public static class TagDTO {
        /**
         * 标签ID
         */
        private Long id;
        
        /**
         * 标签键
         */
        @NotBlank(message = "标签键不能为空")
        @Size(max = 50, message = "标签键长度不能超过50")
        private String tagKey;
        
        /**
         * 标签值
         */
        @NotBlank(message = "标签值不能为空")
        @Size(max = 100, message = "标签值长度不能超过100")
        private String tagValue;
    }
} 