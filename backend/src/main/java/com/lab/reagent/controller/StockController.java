package com.lab.reagent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.StockInRecord;
import com.lab.reagent.entity.StockOutRecord;
import com.lab.reagent.mapper.StockInRecordMapper;
import com.lab.reagent.mapper.StockOutRecordMapper;
import com.lab.reagent.service.StockService;
import com.lab.reagent.vo.StockInVO;
import com.lab.reagent.vo.StockOutVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 出入库Controller
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private StockInRecordMapper stockInRecordMapper;
    
    @Autowired
    private StockOutRecordMapper stockOutRecordMapper;
    
    /**
     * 入库
     */
    @PostMapping("/in")
    public Result<String> stockIn(@RequestBody StockInRecord record,
                                   @RequestHeader("userId") Long userId,
                                   @RequestHeader("realName") String realName) {
        try {
            // URL解码中文名称
            String decodedName = URLDecoder.decode(realName, StandardCharsets.UTF_8.name());
            record.setOperatorId(userId);
            record.setOperatorName(decodedName);
            stockService.stockIn(record);
            return Result.success("入库成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 出库
     */
    @PostMapping("/out")
    public Result<String> stockOut(@RequestBody StockOutRecord record,
                                    @RequestHeader("userId") Long userId,
                                    @RequestHeader("realName") String realName) {
        try {
            // URL解码中文名称
            String decodedName = URLDecoder.decode(realName, StandardCharsets.UTF_8.name());
            record.setOperatorId(userId);
            record.setOperatorName(decodedName);
            stockService.stockOut(record);
            return Result.success("出库成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 入库记录查询
     */
    @GetMapping("/in/list")
    public Result<Page<StockInVO>> inList(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<StockInVO> pageInfo = new Page<>(page, size);
            Page<StockInVO> result = stockInRecordMapper.selectPageWithVO(pageInfo);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 出库记录查询
     */
    @GetMapping("/out/list")
    public Result<Page<StockOutVO>> outList(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        try {
            Page<StockOutVO> pageInfo = new Page<>(page, size);
            Page<StockOutVO> result = stockOutRecordMapper.selectPageWithVO(pageInfo);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}






