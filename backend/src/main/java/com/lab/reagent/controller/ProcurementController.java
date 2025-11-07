package com.lab.reagent.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lab.reagent.common.Result;
import com.lab.reagent.entity.ProcurementRequest;
import com.lab.reagent.service.ProcurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 采购申请 Controller
 */
@RestController
@RequestMapping("/procurement")
@CrossOrigin
public class ProcurementController {
    
    @Autowired
    private ProcurementService procurementService;
    
    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") Integer current,
                      @RequestParam(defaultValue = "10") Integer size,
                      @RequestParam(required = false) String keyword,
                      @RequestParam(required = false) String status,
                      @RequestParam(required = false) Long groupId) {
        Page<ProcurementRequest> page = new Page<>(current, size);
        page = procurementService.page(page, keyword, status, groupId);
        return Result.success(page);
    }
    
    /**
     * 创建采购申请
     */
    @PostMapping("/create")
    public Result create(@RequestBody ProcurementRequest request) {
        int result = procurementService.createRequest(request);
        return result > 0 ? Result.success("申请创建成功") : Result.error("创建失败");
    }
    
    /**
     * PI审批
     */
    @PostMapping("/approve/pi")
    public Result piApprove(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long reviewerId = Long.valueOf(params.get("reviewerId").toString());
        String reviewerName = params.get("reviewerName").toString();
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";
        boolean approved = Boolean.parseBoolean(params.get("approved").toString());
        
        boolean result = procurementService.piApprove(id, reviewerId, reviewerName, remark, approved);
        return result ? Result.success(approved ? "审批通过" : "已拒绝") : Result.error("操作失败");
    }
    
    /**
     * 管理员审批（下单）
     */
    @PostMapping("/approve/admin")
    public Result adminApprove(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Long reviewerId = Long.valueOf(params.get("reviewerId").toString());
        String reviewerName = params.get("reviewerName").toString();
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";
        BigDecimal actualPrice = new BigDecimal(params.get("actualPrice").toString());
        String supplier = params.get("supplier").toString();
        
        boolean result = procurementService.adminApprove(id, reviewerId, reviewerName, remark, actualPrice, supplier);
        return result ? Result.success("下单成功") : Result.error("操作失败");
    }
    
    /**
     * 根据ID获取
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        ProcurementRequest request = procurementService.getById(id);
        return Result.success(request);
    }
}

