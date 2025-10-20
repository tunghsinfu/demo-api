package com.example.demo_api.controller;

import com.example.demo_api.service.ExcelReaderService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {
    
    private final RestTemplate restTemplate;
    private final ExcelReaderService excelReaderService;
    private static final String DEMO_EXCEL_URL = "http://demo-excel-dev:8080";
    
    @Autowired
    public HomeController(RestTemplate restTemplate, ExcelReaderService excelReaderService) {
        this.restTemplate = restTemplate;
        this.excelReaderService = excelReaderService;
    }
    
    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Demo API is running!");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "demo-api");
        response.put("port", "8080");
        return response;
    }
    
    @GetMapping("/excel/request-and-read")
    public Map<String, Object> requestAndReadExcel(@RequestParam(defaultValue = "sample") String type) {
        try {
            // 1. 向 demo-excel 請求 Excel 檔案
            String downloadUrl = DEMO_EXCEL_URL + "/excel/generate-bytes?type=" + type;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(java.util.Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                downloadUrl, 
                HttpMethod.GET, 
                entity, 
                byte[].class
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                byte[] excelData = response.getBody();
                
                // 2. 使用 POI 讀取 Excel 檔案
                Map<String, Object> readResult = excelReaderService.readExcelData(excelData);
                
                // 3. 建立完整回應
                Map<String, Object> result = new HashMap<>();
                result.put("service", "demo-api");
                result.put("operation", "request_and_read_excel");
                result.put("timestamp", LocalDateTime.now());
                result.put("excel_type", type);
                result.put("source_service", "demo-excel");
                result.put("file_size", excelData.length);
                result.put("file_size_mb", String.format("%.2f MB", excelData.length / 1024.0 / 1024.0));
                result.put("download_status", "success");
                result.put("read_result", readResult);
                result.put("status", "completed");
                
                return result;
            } else {
                throw new RuntimeException("Failed to download Excel file from demo-excel service");
            }
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("service", "demo-api");
            error.put("operation", "request_and_read_excel");
            error.put("timestamp", LocalDateTime.now());
            error.put("excel_type", type);
            error.put("source_service", "demo-excel");
            error.put("error", e.getMessage());
            error.put("status", "error");
            
            return error;
        }
    }
}