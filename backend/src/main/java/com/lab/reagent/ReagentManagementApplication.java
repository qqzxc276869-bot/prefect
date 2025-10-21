package com.lab.reagent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 实验室化学试剂与耗材库存管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.lab.reagent.mapper")
public class ReagentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReagentManagementApplication.class, args);
        System.out.println("========================================");
        System.out.println("实验室化学试剂与耗材库存管理系统启动成功！");
        System.out.println("访问地址：http://localhost:8081");
        System.out.println("========================================");
    }
}






