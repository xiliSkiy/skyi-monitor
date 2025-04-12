package com.skyi.collector.repository;

import com.skyi.collector.model.CollectorAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 采集Agent数据访问接口
 */
public interface CollectorAgentRepository extends JpaRepository<CollectorAgent, Long>, JpaSpecificationExecutor<CollectorAgent> {
    
    /**
     * 根据Agent编码查询
     *
     * @param code Agent编码
     * @return Agent信息
     */
    Optional<CollectorAgent> findByCode(String code);
    
    /**
     * 根据IP地址查询
     *
     * @param ipAddress IP地址
     * @return Agent信息
     */
    Optional<CollectorAgent> findByIpAddress(String ipAddress);
    
    /**
     * 根据状态查询列表
     *
     * @param status 状态
     * @return Agent列表
     */
    List<CollectorAgent> findByStatus(Integer status);
    
    /**
     * 查询超过指定时间未心跳的Agent列表
     *
     * @param time 指定时间
     * @return Agent列表
     */
    List<CollectorAgent> findByLastHeartbeatTimeBefore(LocalDateTime time);
    
    /**
     * 根据支持的协议类型查询Agent列表
     *
     * @param protocol 协议类型
     * @return Agent列表
     */
    List<CollectorAgent> findBySupportedProtocolsContaining(String protocol);
} 