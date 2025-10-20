package com.example.demo_excel.controller;

import com.example.demo_excel.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {
    
    @Autowired
    private ExcelService excelService;

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "demo-excel");
        return response;
    }
    
    @GetMapping("/excel/generate-bytes")
    public ResponseEntity<byte[]> generateExcelBytes(@RequestParam(defaultValue = "sample") String type) {
        try {
            byte[] excelData;
            
            if ("report".equals(type)) {
                excelData = excelService.generateReportExcel("月度");
            } else {
                excelData = excelService.generateSampleExcel();
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            
            return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}