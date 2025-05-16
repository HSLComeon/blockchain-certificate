package com.certificate.controller;

import com.certificate.service.LogService;
import com.certificate.util.IpUtil;
import com.certificate.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/test")
public class TestController {

    @Autowired
    private LogService logService;

    /**
     * 测试日志生成
     */
    @PostMapping("/log")
    public ResponseVO<Map<String, Object>> createTestLog(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String message = params.getOrDefault("message", "测试日志记录");
        String type = params.getOrDefault("type", "test");

        System.out.println("收到测试日志请求: " + message);

        // 添加测试日志
        boolean success = logService.addLog(
                type,
                "测试日志记录",
                "测试用户",
                null,
                IpUtil.getIpAddress(request),
                "success",
                message
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("time", System.currentTimeMillis());

        return ResponseVO.success("测试日志创建" + (success ? "成功" : "失败"), result);
    }

    /**
     * 简单测试接口
     */
    @GetMapping("/hello")
    public ResponseVO<String> hello() {
        return ResponseVO.success("Hello from Blockchain Certificate System!");
    }
}