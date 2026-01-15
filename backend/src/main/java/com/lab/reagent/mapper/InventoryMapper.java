package com.lab.reagent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.entity.Inventory;
import com.lab.reagent.vo.InventoryVO;
import com.lab.reagent.vo.LocationStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 库存Mapper
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {
    
    /**
     * 查询库存列表（包含试剂和位置信息）
     */
    @Select("SELECT i.id, i.reagent_id, r.name AS reagent_name, r.cas_no, c.name AS category_name, " +
            "r.specification, r.unit, i.batch_no, i.quantity, i.warning_threshold, i.expiry_date, " +
            "l.full_location AS location_name, i.supplier, r.danger_level, " +
            "CASE " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < CURDATE() THEN 'EXPIRED' " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'EXPIRING' " +
            "  WHEN i.quantity <= i.warning_threshold THEN 'LOW' " +
            "  ELSE 'NORMAL' " +
            "END AS status " +
            "FROM inventory i " +
            "LEFT JOIN reagent r ON i.reagent_id = r.id " +
            "LEFT JOIN reagent_category c ON r.category_id = c.id " +
            "LEFT JOIN storage_location l ON i.location_id = l.id " +
            "WHERE 1=1 " +
            "AND (#{name} IS NULL OR r.name LIKE CONCAT('%', #{name}, '%') OR r.cas_no LIKE CONCAT('%', #{name}, '%')) " +
            "AND (#{status} IS NULL OR (CASE " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < CURDATE() THEN 'EXPIRED' " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'EXPIRING' " +
            "  WHEN i.quantity <= i.warning_threshold THEN 'LOW' " +
            "  ELSE 'NORMAL' " +
            "END) = #{status}) " +
            "ORDER BY i.update_time DESC")
    List<InventoryVO> selectInventoryList(@Param("name") String name, @Param("status") String status);
    
    /**
     * 分页查询库存列表（包含试剂和位置信息）
     */
    @Select("SELECT i.id, i.reagent_id, r.name AS reagent_name, r.cas_no, c.name AS category_name, " +
            "r.specification, r.unit, i.batch_no, i.quantity, i.warning_threshold, i.expiry_date, " +
            "l.full_location AS location_name, i.supplier, r.danger_level, " +
            "CASE " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < CURDATE() THEN 'EXPIRED' " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'EXPIRING' " +
            "  WHEN i.quantity <= i.warning_threshold THEN 'LOW' " +
            "  ELSE 'NORMAL' " +
            "END AS status " +
            "FROM inventory i " +
            "LEFT JOIN reagent r ON i.reagent_id = r.id " +
            "LEFT JOIN reagent_category c ON r.category_id = c.id " +
            "LEFT JOIN storage_location l ON i.location_id = l.id " +
            "WHERE 1=1 " +
            "AND (#{name} IS NULL OR r.name LIKE CONCAT('%', #{name}, '%') OR r.cas_no LIKE CONCAT('%', #{name}, '%')) " +
            "AND (#{status} IS NULL OR (CASE " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < CURDATE() THEN 'EXPIRED' " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'EXPIRING' " +
            "  WHEN i.quantity <= i.warning_threshold THEN 'LOW' " +
            "  ELSE 'NORMAL' " +
            "END) = #{status}) " +
            "ORDER BY i.update_time DESC")
    Page<InventoryVO> selectInventoryPage(Page<InventoryVO> page, @Param("name") String name, @Param("status") String status);
    
    /**
     * 动态排序查询库存列表（包含试剂和位置信息）
     */
    List<InventoryVO> selectInventoryListWithSort(@Param("name") String name, 
                                                    @Param("status") String status,
                                                    @Param("sortField") String sortField,
                                                    @Param("sortOrder") String sortOrder);
    
    /**
     * 分页+动态排序查询库存列表（包含试剂和位置信息）
     */
    Page<InventoryVO> selectInventoryPageWithSort(Page<InventoryVO> page,
                                                   @Param("name") String name, 
                                                   @Param("status") String status,
                                                   @Param("sortField") String sortField,
                                                   @Param("sortOrder") String sortOrder);
    
    /**
     * 查询预警列表
     */
    @Select("SELECT i.id, i.reagent_id, r.name AS reagent_name, r.cas_no, c.name AS category_name, " +
            "r.specification, r.unit, i.batch_no, i.quantity, i.warning_threshold, i.expiry_date, " +
            "l.full_location AS location_name, i.supplier, r.danger_level, " +
            "CASE " +
            "  WHEN i.status = 'DISCARDED' THEN 'DISCARDED' " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < CURDATE() THEN 'EXPIRED' " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'EXPIRING' " +
            "  WHEN i.quantity <= i.warning_threshold THEN 'LOW' " +
            "  ELSE 'NORMAL' " +
            "END AS status " +
            "FROM inventory i " +
            "LEFT JOIN reagent r ON i.reagent_id = r.id " +
            "LEFT JOIN reagent_category c ON r.category_id = c.id " +
            "LEFT JOIN storage_location l ON i.location_id = l.id " +
            "WHERE (CASE " +
            "  WHEN i.status = 'DISCARDED' THEN 'DISCARDED' " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < CURDATE() THEN 'EXPIRED' " +
            "  WHEN i.expiry_date IS NOT NULL AND i.expiry_date < DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'EXPIRING' " +
            "  WHEN i.quantity <= i.warning_threshold THEN 'LOW' " +
            "  ELSE 'NORMAL' " +
            "END) IN ('LOW', 'EXPIRING', 'EXPIRED', 'DISCARDED') " +
            "ORDER BY status, i.update_time DESC")
    List<InventoryVO> selectWarningList();
    
    /**
     * 按位置统计库存信息
     */
    @Select("SELECT l.id AS locationId, l.room_name AS roomName, l.cabinet_no AS cabinetNo, " +
            "l.shelf_no AS shelfNo, l.full_location AS fullLocation, l.description, " +
            "CAST(COUNT(DISTINCT i.reagent_id) AS SIGNED) AS reagentCount, " +
            "COALESCE(SUM(i.quantity), 0) AS totalQuantity " +
            "FROM storage_location l " +
            "LEFT JOIN inventory i ON l.id = i.location_id " +
            "GROUP BY l.id, l.room_name, l.cabinet_no, l.shelf_no, l.full_location, l.description " +
            "ORDER BY l.room_name, l.cabinet_no, l.shelf_no")
    List<LocationStatisticsVO> selectLocationStatistics();
    

}







