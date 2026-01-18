package com.lab.reagent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 时间服务
 * 提供项目统一的时间获取接口
 */
@Slf4j
@Service
public class TimeService {
    
    /**
     * 获取当前系统时间（北京时间）
     * 作为项目统一的时间获取接口
     * @return 当前系统的LocalDateTime
     */
    public LocalDateTime getBeijingTime() {
        LocalDateTime systemTime = LocalDateTime.now();
        log.debug("获取系统时间: {}", systemTime);
        return systemTime;
    }
    
    /**
     * 获取格式化的时间描述
     */
    public String getFormattedTimeDescription() {
        LocalDateTime time = getBeijingTime();
        return String.format("当前系统时间（北京时间）：%s（%d年%d月%d日 %d:%02d:%02d）",
                time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                time.getYear(),
                time.getMonthValue(),
                time.getDayOfMonth(),
                time.getHour(),
                time.getMinute(),
                time.getSecond()
        );
    }
}
