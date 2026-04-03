package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用主启动类
 * <p>
 * 技术栈：Spring Boot 3.5.x / JDK 17+ / PostgreSQL + MyBatis / MongoDB /
 *         Upstash Redis / OpenFeign / Sentry / Vercel Edge Config / Swagger3
 * </p>
 */
@SpringBootApplication
@EnableCaching                                       // 开启 Spring Cache
@EnableFeignClients(basePackages = "com.example.demo.feign")  // 开启 Feign 客户端
@MapperScan("com.example.demo.mapper")              // MyBatis Mapper 扫描
@EnableScheduling                                    // 开启定时任务（EdgeConfig 刷新）
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
