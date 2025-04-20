package com.skyi.collector.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 采集状态模型
 * 用于记录采集任务的执行状态
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class CollectionStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 采集任务ID
     */
    private Long taskId;

    /**
     * 采集实例ID
     */
    private Long instanceId;

    /**
     * 资产ID
     */
    private Long assetId;

    /**
     * 采集开始时间
     */
    private Long startTime;

    /**
     * 采集结束时间
     */
    private Long endTime;

    /**
     * 采集状态
     * SUCCESS: 成功
     * FAILED: 失败
     */
    private String status;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 采集的指标数量
     */
    private Integer metricCount;

    /**
     * 采集器类型
     */
    private String collectorType;

    /**
     * 采集成功状态
     */
    public static final String STATUS_SUCCESS = "SUCCESS";

    /**
     * 采集失败状态
     */
    public static final String STATUS_FAILED = "FAILED";

    /**
     * 创建采集状态
     *
     * @param taskId 采集任务ID
     * @param instanceId 采集实例ID
     * @param assetId 资产ID
     * @return 采集状态
     */
    public static CollectionStatus of(Long taskId, Long instanceId, Long assetId) {
        return new CollectionStatus()
                .setTaskId(taskId)
                .setInstanceId(instanceId)
                .setAssetId(assetId)
                .setStartTime(System.currentTimeMillis());
    }

    /**
     * 标记采集成功
     *
     * @param metricCount 采集的指标数量
     * @return 当前对象
     */
    public CollectionStatus success(Integer metricCount) {
        this.status = STATUS_SUCCESS;
        this.metricCount = metricCount;
        this.endTime = System.currentTimeMillis();
        return this;
    }

    /**
     * 标记采集失败
     *
     * @param errorMessage 错误信息
     * @return 当前对象
     */
    public CollectionStatus failed(String errorMessage) {
        this.status = STATUS_FAILED;
        this.errorMessage = errorMessage;
        this.endTime = System.currentTimeMillis();
        return this;
    }
} 