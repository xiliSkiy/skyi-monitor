package com.skyi.visualization.repository;

import com.skyi.visualization.model.Report;
import com.skyi.visualization.model.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 报表仓库接口
 */
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 根据名称查找报表
     */
    Optional<Report> findByName(String name);
    
    /**
     * 根据创建者ID查询报表列表
     */
    List<Report> findByCreatorId(Long creatorId);
    
    /**
     * 根据创建者ID分页查询报表
     */
    Page<Report> findByCreatorId(Long creatorId, Pageable pageable);
    
    /**
     * 根据报表类型查询报表列表
     */
    List<Report> findByType(ReportType type);
    
    /**
     * 根据报表类型分页查询报表
     */
    Page<Report> findByType(ReportType type, Pageable pageable);
    
    /**
     * 查询启用的报表列表
     */
    List<Report> findByEnabledTrue();
    
    /**
     * 查询需要生成的报表
     */
    @Query("SELECT r FROM Report r WHERE r.enabled = true AND (r.lastGeneratedTime IS NULL OR r.lastGeneratedTime < :threshold)")
    List<Report> findReportsToGenerate(@Param("threshold") LocalDateTime threshold);
    
    /**
     * 按名称或标题或描述模糊查询报表
     */
    @Query("SELECT r FROM Report r WHERE r.name LIKE %:keyword% OR r.title LIKE %:keyword% OR r.description LIKE %:keyword%")
    Page<Report> searchReports(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 查询最近生成失败的报表
     */
    @Query("SELECT r FROM Report r WHERE r.lastGeneratedStatus = 'FAILED' AND r.lastGeneratedTime > :threshold")
    List<Report> findRecentFailedReports(@Param("threshold") LocalDateTime threshold);
} 