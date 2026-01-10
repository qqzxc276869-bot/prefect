package com.lab.reagent.controller;

import com.lab.reagent.common.Result;
import com.lab.reagent.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 数据导出Controller
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {
    
    @Autowired
    private ExportService exportService;
    
    /**
     * 导出库存清单
     */
    @GetMapping("/inventory")
    public ResponseEntity<ByteArrayResource> exportInventory() {
        try {
            byte[] data = exportService.exportInventory();
            ByteArrayResource resource = new ByteArrayResource(data);
            
            String filename = URLEncoder.encode("库存清单.xlsx", StandardCharsets.UTF_8.name());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(data.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出入库记录
     */
    @GetMapping("/stock-in")
    public ResponseEntity<ByteArrayResource> exportStockIn(@RequestParam(required = false) String startDate,
                                                             @RequestParam(required = false) String endDate) {
        try {
            byte[] data = exportService.exportStockIn(startDate, endDate);
            ByteArrayResource resource = new ByteArrayResource(data);
            
            String filename = URLEncoder.encode("入库记录.xlsx", StandardCharsets.UTF_8.name());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(data.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 导出出库记录
     */
    @GetMapping("/stock-out")
    public ResponseEntity<ByteArrayResource> exportStockOut(@RequestParam(required = false) String startDate,
                                                              @RequestParam(required = false) String endDate) {
        try {
            byte[] data = exportService.exportStockOut(startDate, endDate);
            ByteArrayResource resource = new ByteArrayResource(data);
            
            String filename = URLEncoder.encode("出库记录.xlsx", StandardCharsets.UTF_8.name());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(data.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}




