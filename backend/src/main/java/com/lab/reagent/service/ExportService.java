package com.lab.reagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lab.reagent.entity.*;
import com.lab.reagent.mapper.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据导出Service
 */
@Service
public class ExportService {
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private StockInRecordMapper stockInRecordMapper;
    
    @Autowired
    private StockOutRecordMapper stockOutRecordMapper;
    
    @Autowired
    private ReagentMapper reagentMapper;
    
    @Autowired
    private StorageLocationMapper storageLocationMapper;
    
    /**
     * 导出库存清单
     */
    public byte[] exportInventory() throws Exception {
        List<Inventory> inventoryList = inventoryService.list();
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("库存清单");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"试剂名称", "规格型号", "批次号", "库存数量", "单位", "预警阈值", 
                           "存放位置", "有效期", "供应商", "采购价格", "状态", "备注"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        int rowNum = 1;
        for (Inventory inventory : inventoryList) {
            Row row = sheet.createRow(rowNum++);
            
            Reagent reagent = reagentMapper.selectById(inventory.getReagentId());
            StorageLocation location = storageLocationMapper.selectById(inventory.getLocationId());
            
            row.createCell(0).setCellValue(reagent != null ? reagent.getName() : "");
            row.createCell(1).setCellValue(reagent != null ? reagent.getSpecification() : "");
            row.createCell(2).setCellValue(inventory.getBatchNo());
            row.createCell(3).setCellValue(inventory.getQuantity() != null ? inventory.getQuantity().doubleValue() : 0);
            row.createCell(4).setCellValue(reagent != null ? reagent.getUnit() : "");
            row.createCell(5).setCellValue(inventory.getWarningThreshold() != null ? inventory.getWarningThreshold().doubleValue() : 0);
            row.createCell(6).setCellValue(location != null ? location.getFullLocation() : "");
            row.createCell(7).setCellValue(inventory.getExpiryDate() != null ? inventory.getExpiryDate().toString() : "");
            row.createCell(8).setCellValue(inventory.getSupplier());
            row.createCell(9).setCellValue(inventory.getPurchasePrice() != null ? inventory.getPurchasePrice().doubleValue() : 0);
            row.createCell(10).setCellValue(getStatusText(inventory.getStatus()));
            row.createCell(11).setCellValue(inventory.getRemark());
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 导出入库记录
     */
    public byte[] exportStockIn(String startDate, String endDate) throws Exception {
        LambdaQueryWrapper<StockInRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            wrapper.ge(StockInRecord::getCreateTime, start);
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            wrapper.le(StockInRecord::getCreateTime, end);
        }
        
        wrapper.orderByDesc(StockInRecord::getCreateTime);
        List<StockInRecord> records = stockInRecordMapper.selectList(wrapper);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("入库记录");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"试剂名称", "批次号", "入库数量", "单位", "有效期", 
                           "供应商", "采购价格", "存放位置", "操作人", "入库时间", "备注"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (StockInRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            
            Reagent reagent = reagentMapper.selectById(record.getReagentId());
            StorageLocation location = storageLocationMapper.selectById(record.getLocationId());
            
            row.createCell(0).setCellValue(reagent != null ? reagent.getName() : "");
            row.createCell(1).setCellValue(record.getBatchNo());
            row.createCell(2).setCellValue(record.getQuantity() != null ? record.getQuantity().doubleValue() : 0);
            row.createCell(3).setCellValue(reagent != null ? reagent.getUnit() : "");
            row.createCell(4).setCellValue(record.getExpiryDate() != null ? record.getExpiryDate().toString() : "");
            row.createCell(5).setCellValue(record.getSupplier());
            row.createCell(6).setCellValue(record.getPurchasePrice() != null ? record.getPurchasePrice().doubleValue() : 0);
            row.createCell(7).setCellValue(location != null ? location.getFullLocation() : "");
            row.createCell(8).setCellValue(record.getOperatorName());
            row.createCell(9).setCellValue(record.getCreateTime() != null ? record.getCreateTime().format(formatter) : "");
            row.createCell(10).setCellValue(record.getRemark());
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    /**
     * 导出出库记录
     */
    public byte[] exportStockOut(String startDate, String endDate) throws Exception {
        LambdaQueryWrapper<StockOutRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            wrapper.ge(StockOutRecord::getCreateTime, start);
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);
            wrapper.le(StockOutRecord::getCreateTime, end);
        }
        
        wrapper.orderByDesc(StockOutRecord::getCreateTime);
        List<StockOutRecord> records = stockOutRecordMapper.selectList(wrapper);
        
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("出库记录");
        
        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"试剂名称", "出库数量", "单位", "领用人", 
                           "用途", "操作人", "出库时间", "备注"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (StockOutRecord record : records) {
            Row row = sheet.createRow(rowNum++);
            
            Reagent reagent = reagentMapper.selectById(record.getReagentId());
            
            row.createCell(0).setCellValue(reagent != null ? reagent.getName() : "");
            row.createCell(1).setCellValue(record.getQuantity() != null ? record.getQuantity().doubleValue() : 0);
            row.createCell(2).setCellValue(reagent != null ? reagent.getUnit() : "");
            row.createCell(3).setCellValue(record.getRecipientName());
            row.createCell(4).setCellValue(record.getPurpose());
            row.createCell(5).setCellValue(record.getOperatorName());
            row.createCell(6).setCellValue(record.getCreateTime() != null ? record.getCreateTime().format(formatter) : "");
            row.createCell(7).setCellValue(record.getRemark());
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    private String getStatusText(String status) {
        if (status == null) return "";
        switch (status) {
            case "NORMAL": return "正常";
            case "LOW": return "库存不足";
            case "EXPIRING": return "即将过期";
            case "EXPIRED": return "已过期";
            default: return status;
        }
    }
}




