package com.certificate;

import com.certificate.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableAsync
public class Application {

    @Autowired(required = false)
    private LogService logService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @PostConstruct
    public void init() {
        // 记录系统启动日志
        if (logService != null) {
            try {
                logService.addLog(
                        "system",
                        "系统启动",
                        "system",
                        null,
                        "127.0.0.1",
                        "success",
                        "区块链数字证书管理系统启动成功"
                );
                System.out.println("系统启动日志记录成功");
            } catch (Exception e) {
                // 避免因为日志记录失败而影响系统启动
                System.err.println("记录系统启动日志失败: " + e.getMessage());
            }
        } else {
            System.err.println("LogService未注入，无法记录系统启动日志");
        }
    }
}