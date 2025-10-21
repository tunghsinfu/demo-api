package com.example.demo_api.controller;

import com.example.demo_api.service.ExcelReaderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/urlconnection")
public class HttpURLConnectionExcelController {
    
    private final ExcelReaderService excelReaderService;
    private static final String DEMO_EXCEL_URL = "http://demo-excel-dev:8080";
    
    @Autowired
    public HttpURLConnectionExcelController(ExcelReaderService excelReaderService) {
        this.excelReaderService = excelReaderService;
    }
    
    @GetMapping("/request-stream-and-read")
    public Map<String, Object> requestStreamAndReadExcel(@RequestParam(defaultValue = "sample") String type) {
        HttpURLConnection connection = null;
        try {
            // 1. 建立 HttpURLConnection 連接到 demo-excel
            String downloadUrl = DEMO_EXCEL_URL + "/excel/generate-stream?type=" + type;
            URL url = new URL(downloadUrl);
            connection = (HttpURLConnection) url.openConnection();
            
            // 2. 設定請求參數
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/octet-stream");
            connection.setConnectTimeout(30000); // 30 秒連接超時
            connection.setReadTimeout(60000);    // 60 秒讀取超時
            
            // 3. 建立連接
            connection.connect();
            
            // 4. 檢查回應狀態
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                
                // 5. 讀取回應資料 (使用串流方式)
                byte[] excelData = readInputStreamToBytes(connection.getInputStream());
                
                // 6. 使用 POI 讀取 Excel 檔案
                Map<String, Object> readResult = excelReaderService.readExcelData(excelData);
                
                // 7. 建立完整回應
                Map<String, Object> result = new HashMap<>();
                result.put("service", "demo-api");
                result.put("operation", "request_stream_and_read_excel");
                result.put("timestamp", LocalDateTime.now());
                result.put("excel_type", type);
                result.put("source_service", "demo-excel");
                result.put("source_endpoint", "/excel/generate-stream");
                result.put("http_client", "java.net.HttpURLConnection");
                result.put("optimization", "StreamingResponseBody + URLConnection");
                result.put("file_size", excelData.length);
                result.put("file_size_mb", String.format("%.2f MB", excelData.length / 1024.0 / 1024.0));
                result.put("download_status", "success");
                result.put("response_code", responseCode);
                result.put("read_result", readResult);
                result.put("status", "completed");
                
                // 8. 檢查回應標頭資訊
                String contentLength = connection.getHeaderField("Content-Length");
                String contentType = connection.getHeaderField("Content-Type");
                String contentDisposition = connection.getHeaderField("Content-Disposition");
                
                if (contentLength != null) {
                    result.put("content_length", contentLength);
                }
                if (contentType != null) {
                    result.put("content_type", contentType);
                }
                if (contentDisposition != null) {
                    result.put("content_disposition", contentDisposition);
                }
                
                return result;
                
            } else {
                throw new RuntimeException("HTTP request failed with response code: " + responseCode + 
                                         " - " + connection.getResponseMessage());
            }
            
        } catch (IOException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("service", "demo-api");
            error.put("operation", "request_stream_and_read_excel");
            error.put("timestamp", LocalDateTime.now());
            error.put("excel_type", type);
            error.put("source_service", "demo-excel");
            error.put("source_endpoint", "/excel/generate-stream");
            error.put("http_client", "java.net.HttpURLConnection");
            error.put("optimization", "StreamingResponseBody + URLConnection");
            error.put("error_type", "IOException");
            error.put("error", e.getMessage());
            error.put("status", "error");
            
            return error;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("service", "demo-api");
            error.put("operation", "request_stream_and_read_excel");
            error.put("timestamp", LocalDateTime.now());
            error.put("excel_type", type);
            error.put("source_service", "demo-excel");
            error.put("source_endpoint", "/excel/generate-stream");
            error.put("http_client", "java.net.HttpURLConnection");
            error.put("optimization", "StreamingResponseBody + URLConnection");
            error.put("error_type", "Exception");
            error.put("error", e.getMessage());
            error.put("status", "error");
            
            return error;
        } finally {
            // 9. 確保連接被關閉
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    @GetMapping("/generate-and-read-sample")
    public Map<String, Object> generateAndReadSampleExcel() {
        try {
            // 步驟 1: 在 demo-api 本地建立一個只有標題列的 Excel
            byte[] emptyExcelData = createEmptyExcelWithHeaders();
            
            // 步驟 2: 使用 HttpURLConnection 將 Excel 傳送到 demo-excel 填充資料
            String uploadUrl = DEMO_EXCEL_URL + "/excel/fill-data";
            byte[] filledExcelData = uploadExcelAndGetResponse(uploadUrl, emptyExcelData, "template.xlsx");
            
            // 步驟 3: 讀取並解析填充後的 Excel
            Map<String, Object> readResult = excelReaderService.readExcelData(filledExcelData);
            
            // 步驟 4: 驗證標題列
            String[] expectedHeaders = {"ID", "姓名", "部門", "薪資", "入職日期", "狀態"};
            Map<String, Object> validationResult = validateHeaders(readResult, expectedHeaders);
            
            // 步驟 5: 建立完整回應
            Map<String, Object> result = new HashMap<>();
            result.put("service", "demo-api");
            result.put("operation", "generate_and_read_sample_excel");
            result.put("timestamp", LocalDateTime.now());
            result.put("workflow", "demo-api creates template → demo-excel fills data → demo-api reads result");
            result.put("step_1", "Create empty Excel with headers in demo-api");
            result.put("step_2", "Upload to demo-excel and get filled Excel back");
            result.put("step_3", "Read and parse filled Excel in demo-api");
            result.put("source_service", "demo-excel");
            result.put("target_endpoint", "/excel/fill-data");
            result.put("http_client", "java.net.HttpURLConnection");
            result.put("expected_headers", expectedHeaders);
            result.put("headers_validation", validationResult.get("valid"));
            result.put("headers_validation_message", validationResult.get("message"));
            result.put("empty_excel_size", emptyExcelData.length);
            result.put("filled_excel_size", filledExcelData.length);
            result.put("filled_excel_size_kb", String.format("%.2f KB", filledExcelData.length / 1024.0));
            result.put("excel_content", readResult);
            result.put("status", "completed");
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("service", "demo-api");
            error.put("operation", "generate_and_read_sample_excel");
            error.put("timestamp", LocalDateTime.now());
            error.put("error_type", e.getClass().getSimpleName());
            error.put("error", e.getMessage());
            error.put("status", "error");
            
            e.printStackTrace();
            return error;
        }
    }
    
    // 建立只有標題列的空 Excel
    private byte[] createEmptyExcelWithHeaders() throws IOException {
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Sample Data");
        
        // 建立標題行
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "姓名", "部門", "薪資", "入職日期", "狀態"};
        
        // 標題樣式
        org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        }
    }
    
    // 使用 HttpURLConnection 上傳 Excel 並接收 StreamingResponseBody 回應
    private byte[] uploadExcelAndGetResponse(String uploadUrl, byte[] excelData, String filename) throws IOException {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
        
        HttpURLConnection connection = null;
        try {
            URL url = new URL(uploadUrl);
            connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Accept", "application/octet-stream");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            
            // 建立 multipart/form-data 請求體
            try (java.io.OutputStream outputStream = connection.getOutputStream()) {
                // 寫入檔案部分
                outputStream.write(("--" + boundary + "\r\n").getBytes());
                outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n").getBytes());
                outputStream.write(("Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet\r\n\r\n").getBytes());
                outputStream.write(excelData);
                outputStream.write(("\r\n--" + boundary + "--\r\n").getBytes());
                outputStream.flush();
            }
            
            // 讀取 StreamingResponseBody 回應
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 使用串流方式讀取回應，適合處理 StreamingResponseBody
                return readInputStreamToBytes(connection.getInputStream());
            } else {
                // 讀取錯誤訊息
                String errorMessage = connection.getResponseMessage();
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    String errorBody = new String(readInputStreamToBytes(errorStream), "UTF-8");
                    errorMessage += " - " + errorBody;
                }
                throw new IOException("Upload failed with response code: " + responseCode + " - " + errorMessage);
            }
            
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    // 驗證標題列
    private Map<String, Object> validateHeaders(Map<String, Object> readResult, String[] expectedHeaders) {
        Map<String, Object> validation = new HashMap<>();
        boolean headersValid = false;
        String message = "";
        
        if (readResult.containsKey("sheets") && readResult.get("sheets") instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> sheets = (java.util.List<Map<String, Object>>) readResult.get("sheets");
            if (!sheets.isEmpty()) {
                Map<String, Object> firstSheet = sheets.get(0);
                if (firstSheet.containsKey("data") && firstSheet.get("data") instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<java.util.List<String>> data = (java.util.List<java.util.List<String>>) firstSheet.get("data");
                    if (!data.isEmpty()) {
                        java.util.List<String> actualHeaders = data.get(0);
                        headersValid = actualHeaders.size() == expectedHeaders.length;
                        if (headersValid) {
                            for (int i = 0; i < expectedHeaders.length; i++) {
                                if (!expectedHeaders[i].equals(actualHeaders.get(i))) {
                                    headersValid = false;
                                    break;
                                }
                            }
                        }
                        
                        if (headersValid) {
                            message = "標題列驗證成功";
                        } else {
                            message = "標題列不符合預期。預期: " + 
                                java.util.Arrays.toString(expectedHeaders) + 
                                ", 實際: " + actualHeaders.toString();
                        }
                    }
                }
            }
        }
        
        validation.put("valid", headersValid ? "PASSED" : "FAILED");
        validation.put("message", message);
        return validation;
    }
    
    // 共用方法：使用串流方式讀取 InputStream 到 byte array
    private byte[] readInputStreamToBytes(InputStream inputStream) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(inputStream);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[8192]; // 8KB 緩衝區
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            
            return baos.toByteArray();
        }
    }
}
