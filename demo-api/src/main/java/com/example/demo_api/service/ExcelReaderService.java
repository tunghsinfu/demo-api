package com.example.demo_api.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ExcelReaderService {
    
    public Map<String, Object> readExcelData(byte[] excelBytes) {
        Map<String, Object> result = new HashMap<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            result.put("service", "demo-api");
            result.put("operation", "read_excel_with_poi");
            result.put("timestamp", LocalDateTime.now());
            result.put("workbook_type", workbook.getClass().getSimpleName());
            result.put("number_of_sheets", workbook.getNumberOfSheets());
            
            List<Map<String, Object>> sheetsData = new ArrayList<>();
            
            // 讀取所有工作表
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Map<String, Object> sheetInfo = readSheetData(sheet);
                sheetsData.add(sheetInfo);
            }
            
            result.put("sheets", sheetsData);
            result.put("status", "success");
            
        } catch (IOException e) {
            result.put("service", "demo-api");
            result.put("operation", "read_excel_with_poi");
            result.put("timestamp", LocalDateTime.now());
            result.put("error", "Failed to read Excel: " + e.getMessage());
            result.put("status", "error");
        }
        
        return result;
    }
    
    private Map<String, Object> readSheetData(Sheet sheet) {
        Map<String, Object> sheetData = new HashMap<>();
        
        sheetData.put("sheet_name", sheet.getSheetName());
        sheetData.put("first_row_num", sheet.getFirstRowNum());
        sheetData.put("last_row_num", sheet.getLastRowNum());
        sheetData.put("physical_number_of_rows", sheet.getPhysicalNumberOfRows());
        
        List<List<String>> rows = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        
        // 讀取所有行
        for (Row row : sheet) {
            List<String> rowData = new ArrayList<>();
            
            if (row.getRowNum() == 0) {
                // 第一行作為標題
                for (Cell cell : row) {
                    String cellValue = getCellValueAsString(cell);
                    headers.add(cellValue);
                    rowData.add(cellValue);
                }
            } else {
                // 數據行
                for (int cellIndex = 0; cellIndex < headers.size(); cellIndex++) {
                    Cell cell = row.getCell(cellIndex);
                    String cellValue = getCellValueAsString(cell);
                    rowData.add(cellValue);
                }
            }
            
            rows.add(rowData);
        }
        
        sheetData.put("headers", headers);
        sheetData.put("total_rows", rows.size());
        sheetData.put("data_rows", rows.size() - 1); // 扣除標題行
        sheetData.put("rows", rows);
        
        // 統計數據
        if (rows.size() > 1) {
            sheetData.put("sample_data", rows.subList(0, Math.min(3, rows.size()))); // 前3行作為範例
        }
        
        return sheetData;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
    
    public Map<String, Object> analyzeExcelStructure(byte[] excelBytes) {
        Map<String, Object> analysis = new HashMap<>();
        
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(excelBytes);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            analysis.put("service", "demo-api");
            analysis.put("operation", "analyze_excel_structure");
            analysis.put("timestamp", LocalDateTime.now());
            analysis.put("file_size_bytes", excelBytes.length);
            analysis.put("file_size_kb", String.format("%.2f KB", excelBytes.length / 1024.0));
            analysis.put("workbook_type", workbook.getClass().getSimpleName());
            
            List<Map<String, Object>> sheetAnalysis = new ArrayList<>();
            int totalCells = 0;
            int totalRows = 0;
            
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Map<String, Object> sheetInfo = new HashMap<>();
                
                sheetInfo.put("index", i);
                sheetInfo.put("name", sheet.getSheetName());
                sheetInfo.put("rows", sheet.getPhysicalNumberOfRows());
                
                // 計算欄位數（以第一行為準）
                Row firstRow = sheet.getRow(sheet.getFirstRowNum());
                int columns = firstRow != null ? firstRow.getPhysicalNumberOfCells() : 0;
                sheetInfo.put("columns", columns);
                
                int sheetCells = sheet.getPhysicalNumberOfRows() * columns;
                sheetInfo.put("estimated_cells", sheetCells);
                
                totalCells += sheetCells;
                totalRows += sheet.getPhysicalNumberOfRows();
                
                sheetAnalysis.add(sheetInfo);
            }
            
            analysis.put("total_sheets", workbook.getNumberOfSheets());
            analysis.put("total_rows", totalRows);
            analysis.put("estimated_total_cells", totalCells);
            analysis.put("sheets_analysis", sheetAnalysis);
            analysis.put("status", "success");
            
        } catch (IOException e) {
            analysis.put("service", "demo-api");
            analysis.put("operation", "analyze_excel_structure");
            analysis.put("timestamp", LocalDateTime.now());
            analysis.put("error", "Failed to analyze Excel: " + e.getMessage());
            analysis.put("status", "error");
        }
        
        return analysis;
    }
}