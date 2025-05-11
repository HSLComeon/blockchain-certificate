package com.certificate.controller;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, API is working!";
    }

    @PostMapping("/echo")
    public String echo(@RequestBody Map<String, Object> data) {
        return "Received: " + data.toString();
    }
}