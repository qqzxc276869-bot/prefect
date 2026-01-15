package com.lab.reagent.service;

import com.lab.reagent.entity.Reagent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApplyAssistService {

    @Autowired
    private ReagentService reagentService;

    @Autowired
    private AiService aiService;

    @Data
    public static class ApplyOptimizeRequest {
        private String name;
        private String casNo;
        private Double quantity;
        private String unit;
        private String purpose;
        private String model; // 可选覆盖模型
    }

    @Data
    public static class ApplyOptimizeResponse {
        private String standardizedName;
        private String casNo;
        private Double suggestedQuantity;
        private String unit;
        private List<String> purposeTemplates;
        private List<String> warnings;
        private String reasoning;
    }

    public ApplyOptimizeResponse optimize(ApplyOptimizeRequest req) {
        // 优化候选库大小：只传递最相关的20条数据，减少AI处理量
        List<Reagent> nearby = reagentService.list().stream()
                .filter(r -> r.getName() != null && req.getName() != null && r.getName().toLowerCase().contains(req.getName().toLowerCase()))
                .limit(20)  // 从300减少到20
                .collect(Collectors.toList());
        
        JSONArray candidate = new JSONArray();
        for (Reagent r : nearby) {
            JSONObject o = new JSONObject();
            o.set("id", r.getId());
            o.set("name", r.getName());
            o.set("casNo", r.getCasNo());
            o.set("specification", r.getSpecification());
            o.set("unit", r.getUnit());
            candidate.add(o);
        }
        // 增强 Prompt：引导 AI 生成多样化（实验用途、辅助用品、清洗维护等）的申领模板
        String instruction = "你是实验室试剂申领助手。根据用户输入和候选试剂列表，返回严格的JSON格式。\n" +
                "【必须输出格式】：{\"standardizedName\":\"试剂标准名称\",\"casNo\":\"CAS号\",\"suggestedQuantity\":数字,\"unit\":\"单位\",\"purposeTemplates\":[\"用途1\",\"用途2\",\"用途3\",\"用途4\"],\"warnings\":[\"安全提示\"],\"reasoning\":\"简短理由\"}\n" +
                "【重要规则】：\n" +
                "1) purposeTemplates 必须包含 3-4 个不同类别的用途，例如：[实验核心反应, 溶剂/稀释/提取, 仪器清洗/日常维护, 储备液配制]。\n" +
                "2) 每个模板应具体、符合科研场景（如 '用于XXX的酯化反应核心试剂' 而不仅仅是 '化学实验'）。\n" +
                "3) 优先匹配候选列表中的试剂。若候选列表中没有相关试剂，尝试通过常见化学知识补全CAS号。\n" +
                "4) 只输出JSON，严禁包含任何Markdown语法或解释性文字。";

        JSONObject payload = new JSONObject();
        payload.set("input", new JSONObject()
                .set("name", nvl(req.getName()))
                .set("casNo", nvl(req.getCasNo()))
                .set("quantity", req.getQuantity())
                .set("unit", nvl(req.getUnit()))
                .set("purpose", nvl(req.getPurpose())));
        payload.set("candidates", candidate);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sys = new HashMap<>();
        sys.put("role", "system");
        sys.put("content", instruction);
        messages.add(sys);
        Map<String, String> usr = new HashMap<>();
        usr.put("role", "user");
        usr.put("content", payload.toString());
        messages.add(usr);

        String reply = aiService.chat(req.getModel(), messages);
        // 增强 JSON 提取逻辑
        String jsonText = stripFence(reply);
        log.info("AI原始响应: {}", reply);
        log.info("提取的JSON: {}", jsonText);

        ApplyOptimizeResponse resp = new ApplyOptimizeResponse();
        try {
            JSONObject obj = JSONUtil.parseObj(jsonText);
            
            // 安全提取每个字段，提供默认值
            resp.setStandardizedName(obj.getStr("standardizedName", nvl(req.getName())));
            resp.setCasNo(obj.getStr("casNo", nvl(req.getCasNo())));
            
            Double suggestedQty = obj.getDouble("suggestedQuantity");
            resp.setSuggestedQuantity(suggestedQty != null ? suggestedQty : req.getQuantity());
            
            resp.setUnit(obj.getStr("unit", nvl(req.getUnit())));
            
            JSONArray pts = obj.getJSONArray("purposeTemplates");
            resp.setPurposeTemplates(pts == null ? Collections.emptyList() : 
                pts.stream().map(String::valueOf).collect(Collectors.toList()));
            
            JSONArray warns = obj.getJSONArray("warnings");
            resp.setWarnings(warns == null ? Collections.emptyList() : 
                warns.stream().map(String::valueOf).collect(Collectors.toList()));
            
            resp.setReasoning(obj.getStr("reasoning", "AI推荐"));
            
            log.info("JSON解析成功");
        } catch (Exception e) {
            log.error("AI申领优化解析失败，原始响应: {}, 错误: {}", reply, e.getMessage());
            // 兜底：直接回填输入
            resp.setStandardizedName(nvl(req.getName()));
            resp.setCasNo(nvl(req.getCasNo()));
            resp.setSuggestedQuantity(req.getQuantity());
            resp.setUnit(nvl(req.getUnit()));
            resp.setPurposeTemplates(Collections.emptyList());
            // 明确提示解析失败，方便调试
            resp.setWarnings(Collections.singletonList("系统提示：AI返回格式异常，已保留原输入。请尝试重试。"));
            resp.setReasoning("JSON解析错误: " + e.getMessage());
        }
        
        // 如果 AI 仍然返回了超过 10 瓶/个的大额建议，进行最后的常识性兜底（除非确实是微量单位如mg）
        if (resp.getUnit() != null && (resp.getUnit().contains("瓶") || resp.getUnit().contains("个"))) {
            if (resp.getSuggestedQuantity() != null && resp.getSuggestedQuantity() > 10) {
                resp.setSuggestedQuantity(1.0);
                if (resp.getWarnings() == null) resp.setWarnings(new ArrayList<>());
                resp.getWarnings().add("建议：单次申请量过大，系统已自动调整为 1.0 " + resp.getUnit());
            }
        }
        
        return resp;
    }

    /**
     * 增强版字符串清洗，提取 JSON 部分
     */
    private String stripFence(String text) {
        if (text == null) return "";
        String t = text.trim();

        // 1. 处理 markdown 代码块
        if (t.startsWith("```")) {
            int idx = t.indexOf('\n');
            if (idx > 0) t = t.substring(idx + 1);
            int end = t.lastIndexOf("```");
            if (end > 0) t = t.substring(0, end);
        }

        // 2. 尝试寻找最外层的 { 和 }，处理非代码块包裹的 JSON
        t = t.trim();
        int start = t.indexOf('{');
        int end = t.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return t.substring(start, end + 1);
        }

        return t;
    }

    private String nvl(String s) { return s == null ? "" : s; }
}