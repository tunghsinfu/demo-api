package com.example.demo_api.controller;

import com.example.demo_api.service.ExcelReaderService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/resttemplate")
public class RestTemplateExcelController {
    
    private final RestTemplate restTemplate;
    private final ExcelReaderService excelReaderService;
    private static final String DEMO_EXCEL_URL = "http://demo-excel-dev:8080";
    
    @Autowired
    public RestTemplateExcelController(RestTemplate restTemplate, ExcelReaderService excelReaderService) {
        this.restTemplate = restTemplate;
        this.excelReaderService = excelReaderService;
    }
    
    @GetMapping("/request-and-read")
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
                result.put("http_client", "RestTemplate");
                result.put("operation", "request_and_read_excel");
                result.put("timestamp", LocalDateTime.now());
                result.put("excel_type", type);
                result.put("source_service", "demo-excel");
                result.put("source_endpoint", "/excel/generate-bytes");
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
            error.put("http_client", "RestTemplate");
            error.put("operation", "request_and_read_excel");
            error.put("timestamp", LocalDateTime.now());
            error.put("excel_type", type);
            error.put("source_service", "demo-excel");
            error.put("error", e.getMessage());
            error.put("status", "error");
            
            return error;
        }
    }
    
    @GetMapping("/request-resource-and-read")
    public Map<String, Object> requestResourceAndReadExcel(@RequestParam(defaultValue = "sample") String type) {
        try {
            // 1. 向 demo-excel 請求 Excel 檔案資源
            String downloadUrl = DEMO_EXCEL_URL + "/excel/generate-resource?type=" + type;
            
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
                result.put("http_client", "RestTemplate");
                result.put("operation", "request_resource_and_read_excel");
                result.put("timestamp", LocalDateTime.now());
                result.put("excel_type", type);
                result.put("source_service", "demo-excel");
                result.put("source_endpoint", "/excel/generate-resource");
                result.put("file_size", excelData.length);
                result.put("file_size_mb", String.format("%.2f MB", excelData.length / 1024.0 / 1024.0));
                result.put("download_status", "success");
                result.put("read_result", readResult);
                result.put("status", "completed");
                
                // 檢查回應標頭中的檔案資訊
                HttpHeaders responseHeaders = response.getHeaders();
                if (responseHeaders.getContentDisposition() != null) {
                    result.put("content_disposition", responseHeaders.getContentDisposition().toString());
                }
                if (responseHeaders.getContentLength() > 0) {
                    result.put("content_length", responseHeaders.getContentLength());
                }
                
                return result;
            } else {
                throw new RuntimeException("Failed to download Excel resource from demo-excel service");
            }
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("service", "demo-api");
            error.put("http_client", "RestTemplate");
            error.put("operation", "request_resource_and_read_excel");
            error.put("timestamp", LocalDateTime.now());
            error.put("excel_type", type);
            error.put("source_service", "demo-excel");
            error.put("source_endpoint", "/excel/generate-resource");
            error.put("error", e.getMessage());
            error.put("status", "error");
            
            return error;
        }
    }
    
    @GetMapping("/request-stream-and-read")
    public Map<String, Object> requestStreamAndReadExcel(@RequestParam(defaultValue = "sample") String type) {
        try {
            // 1. 向 demo-excel 請求 Excel 檔案串流
            String downloadUrl = DEMO_EXCEL_URL + "/excel/generate-stream?type=" + type;
            
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
                result.put("http_client", "RestTemplate");
                result.put("operation", "request_stream_and_read_excel");
                result.put("timestamp", LocalDateTime.now());
                result.put("excel_type", type);
                result.put("source_service", "demo-excel");
                result.put("source_endpoint", "/excel/generate-stream");
                result.put("optimization", "StreamingResponseBody");
                result.put("file_size", excelData.length);
                result.put("file_size_mb", String.format("%.2f MB", excelData.length / 1024.0 / 1024.0));
                result.put("download_status", "success");
                result.put("read_result", readResult);
                result.put("status", "completed");
                
                // 檢查回應標頭中的檔案資訊
                HttpHeaders responseHeaders = response.getHeaders();
                if (responseHeaders.getContentDisposition() != null) {
                    result.put("content_disposition", responseHeaders.getContentDisposition().toString());
                }
                if (responseHeaders.getContentLength() > 0) {
                    result.put("content_length", responseHeaders.getContentLength());
                }
                
                return result;
            } else {
                throw new RuntimeException("Failed to download Excel stream from demo-excel service");
            }
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("service", "demo-api");
            error.put("http_client", "RestTemplate");
            error.put("operation", "request_stream_and_read_excel");
            error.put("timestamp", LocalDateTime.now());
            error.put("excel_type", type);
            error.put("source_service", "demo-excel");
            error.put("source_endpoint", "/excel/generate-stream");
            error.put("optimization", "StreamingResponseBody");
            error.put("error", e.getMessage());
            error.put("status", "error");
            
            return error;
        }
    }
}
