package com.skyi.collector.service;

import com.skyi.collector.dto.CollectorAgentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 采集Agent服务接口
 */
public interface CollectorAgentService {
    
    /**
     * 注册Agent
     *
     * @param agentDTO Agent信息
     * @return 注册后的Agent信息
     */
    CollectorAgentDTO registerAgent(CollectorAgentDTO agentDTO);
    
    /**
     * 更新Agent
     *
     * @param id Agent ID
     * @param agentDTO Agent信息
     * @return 更新后的Agent信息
     */
    CollectorAgentDTO updateAgent(Long id, CollectorAgentDTO agentDTO);
    
    /**
     * 删除Agent
     *
     * @param id Agent ID
     */
    void deleteAgent(Long id);
    
    /**
     * 根据ID查询Agent
     *
     * @param id Agent ID
     * @return Agent信息
     */
    CollectorAgentDTO getAgentById(Long id);
    
    /**
     * 根据编码查询Agent
     *
     * @param code Agent编码
     * @return Agent信息
     */
    CollectorAgentDTO getAgentByCode(String code);
    
    /**
     * 分页查询Agent列表
     *
     * @param name Agent名称
     * @param status 状态
     * @param pageable 分页参数
     * @return Agent分页列表
     */
    Page<CollectorAgentDTO> listAgents(String name, Integer status, Pageable pageable);
    
    /**
     * 根据状态查询Agent列表
     *
     * @param status 状态
     * @return Agent列表
     */
    List<CollectorAgentDTO> listAgentsByStatus(Integer status);
    
    /**
     * 根据协议查询支持的Agent列表
     *
     * @param protocol 协议类型
     * @return Agent列表
     */
    List<CollectorAgentDTO> listAgentsByProtocol(String protocol);
    
    /**
     * Agent心跳
     *
     * @param code Agent编码
     * @return 心跳结果
     */
    boolean heartbeat(String code);
    
    /**
     * 检查Agent是否在线
     *
     * @param id Agent ID
     * @return 是否在线
     */
    boolean isAgentOnline(Long id);
    
    /**
     * 向Agent下发配置
     *
     * @param id Agent ID
     * @param config 配置信息
     * @return 是否成功
     */
    boolean deployConfig(Long id, String config);
}