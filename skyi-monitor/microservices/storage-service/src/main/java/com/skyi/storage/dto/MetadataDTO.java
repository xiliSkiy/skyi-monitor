package com.skyi.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 元数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetadataDTO {

    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 元数据类型
     */
    @NotBlank(message = "元数据类型不能为空")
    private String type;
    
    /**
     * 关联ID
     */
    @NotBlank(message = "关联ID不能为空")
    private String refId;
    
    /**
     * 元数据键
     */
    @NotBlank(message = "元数据键不能为空")
    private String metaKey;
    
    /**
     * 元数据值
     */
    private String metaValue;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 