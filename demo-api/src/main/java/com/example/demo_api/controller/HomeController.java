package com.example.demo_api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {
    
    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Demo API is running!");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "demo-api");
        response.put("port", "8080");
        response.put("endpoints", Map.of(
            "resttemplate", Map.of(
                "request-and-read", "/resttemplate/request-and-read?type=sample",
                "request-resource-and-read", "/resttemplate/request-resource-and-read?type=sample",
                "request-stream-and-read", "/resttemplate/request-stream-and-read?type=sample"
            ),
            "urlconnection", Map.of(
                "request-stream-and-read", "/urlconnection/request-stream-and-read?type=sample",
                "generate-and-read-sample", "/urlconnection/generate-and-read-sample"
            )
        ));
        return response;
    }
}
