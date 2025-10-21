package com.example.demo_excel.controller;

import com.example.demo_excel.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
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

    
    // 針對 ResponseEntity<Resource> 的優化實踐：
    // 1. 避免多次記憶體分配 - 不要先轉為 byte[] 再包裝成 Resource
    // 2. 使用 ByteArrayOutputStream 直接建立 ByteArrayResource
    // 3. 設定正確的 Content-Length 和 Content-Disposition 標頭
    // 4. 適用於中小型檔案（建議 < 50MB）
    @GetMapping("/excel/generate-resource")
    public ResponseEntity<Resource> generateExcelResource(@RequestParam(defaultValue = "sample") String type) {
        try {
            ByteArrayResource resource;
            
            if ("report".equals(type)) {
                // 優化版本：直接使用 ByteArrayOutputStream
                ByteArrayOutputStream outputStream = excelService.generateReportExcelAsStream("月度");
                resource = new ByteArrayResource(outputStream.toByteArray());
            } else {
                // 優化版本：直接使用 ByteArrayOutputStream
                ByteArrayOutputStream outputStream = excelService.generateSampleExcelAsStream();
                resource = new ByteArrayResource(outputStream.toByteArray());
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.xlsx\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 真正的最佳實踐：使用 StreamingResponseBody 直接串流，避免記憶體中介
    @GetMapping("/excel/generate-stream")
    public ResponseEntity<StreamingResponseBody> generateExcelStream(@RequestParam(defaultValue = "sample") String type) {
        try {
            StreamingResponseBody stream = outputStream -> {
                if ("report".equals(type)) {
                    excelService.generateReportExcelToStream("月度", outputStream);
                } else {
                    excelService.generateSampleExcelToStream(outputStream);
                }
            };
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "report.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stream);
                    
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 接收 Excel 檔案，寫入 SAMPLE_DATA 後回傳 (使用 StreamingResponseBody 優化)
    @PostMapping("/excel/fill-data")
    public ResponseEntity<StreamingResponseBody> fillExcelWithData(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            
            // 讀取上傳的 Excel 檔案
            byte[] inputExcelData = file.getBytes();
            
            // 建立 StreamingResponseBody 來串流回傳填充後的 Excel
            StreamingResponseBody stream = outputStream -> {
                // 使用 ExcelService 填充資料並直接寫入輸出串流
                byte[] filledExcelData = excelService.fillExcelWithData(inputExcelData);
                outputStream.write(filledExcelData);
                outputStream.flush();
            };
            
            // 設定回應標頭
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "filled_data.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(stream);
            
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}