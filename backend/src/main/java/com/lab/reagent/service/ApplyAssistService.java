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
        // 候选库简要信息
        List<Reagent> reagents = reagentService.list().stream().limit(300).collect(Collectors.toList());
        JSONArray candidate = new JSONArray();
        for (Reagent r : reagents) {
            JSONObject o = new JSONObject();
            o.set("id", r.getId());
            o.set("name", r.getName());
            o.set("casNo", r.getCasNo());
            o.set("specification", r.getSpecification());
            o.set("unit", r.getUnit());
            candidate.add(o);
        }

        // 优化 Prompt，修复之前的格式错误，并明确要求 AI 给出独立的用量建议
        String instruction = "你是实验室试剂申领辅助助手。\n" +
                "根据用户输入的名称/用途/数量，结合候选试剂列表，输出一个JSON对象：\n" +
                "{standardizedName, casNo, suggestedQuantity, unit, purposeTemplates[], warnings[], reasoning}\n" +
                "要求：\n" +
                "1) standardizedName命中系统常用名，若不确定选择最接近者；casNo尽量补全\n" +
                "2) suggestedQuantity: 请分析该试剂在常规实验中的典型消耗量或包装规格。如果用户输入的数量（如1）看起来像是默认值或不符合常规（例如浓硫酸通常按500ml/瓶申请，昂贵试剂按mg/g申请），请给出更合理的建议值；如果用户输入合理则保留。\n" +
                "3) purposeTemplates给出2-4条常用用途模板（简洁、专业）\n" +
                "4) warnings包含用量安全、危险性或合规性的简要提示（可空）\n" +
                "5) 严格返回JSON且字段齐全，不要输出多余文字。";

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

        ApplyOptimizeResponse resp = new ApplyOptimizeResponse();
        try {
            JSONObject obj = JSONUtil.parseObj(jsonText);
            resp.setStandardizedName(obj.getStr("standardizedName"));
            resp.setCasNo(obj.getStr("casNo"));
            resp.setSuggestedQuantity(obj.getDouble("suggestedQuantity"));
            resp.setUnit(Optional.ofNullable(obj.getStr("unit")).orElse(nvl(req.getUnit())));
            JSONArray pts = obj.getJSONArray("purposeTemplates");
            resp.setPurposeTemplates(pts == null ? Collections.emptyList() : pts.stream().map(String::valueOf).collect(Collectors.toList()));
            JSONArray warns = obj.getJSONArray("warnings");
            resp.setWarnings(warns == null ? Collections.emptyList() : warns.stream().map(String::valueOf).collect(Collectors.toList()));
            resp.setReasoning(obj.getStr("reasoning"));
        } catch (Exception e) {
            log.warn("AI申领优化解析失败，返回原文: {}", reply);
            // 兜底：直接回填输入
            resp.setStandardizedName(nvl(req.getName()));
            resp.setCasNo(nvl(req.getCasNo()));
            resp.setSuggestedQuantity(req.getQuantity());
            resp.setUnit(nvl(req.getUnit()));
            resp.setPurposeTemplates(Collections.emptyList());
            // 明确提示解析失败，方便调试
            resp.setWarnings(Collections.singletonList("系统提示：AI返回格式异常，已保留原输入。请尝试重试。"));
            resp.setReasoning("JSON parse error: " + e.getMessage());
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