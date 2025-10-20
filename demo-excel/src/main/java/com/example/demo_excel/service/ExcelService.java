package com.example.demo_excel.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {
    
    public byte[] generateSampleExcel() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sample Data");
        
        // 建立標題樣式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 建立標題行
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "姓名", "部門", "薪資", "入職日期", "狀態"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 建立資料樣式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        
        // 新增範例資料
        String[][] sampleData = {
            {"1", "張三", "資訊部", "50000", "2023-01-15", "在職"},
            {"2", "李四", "業務部", "45000", "2023-02-20", "在職"},
            {"3", "王五", "人資部", "48000", "2023-03-10", "在職"},
            {"4", "趙六", "財務部", "52000", "2023-04-05", "在職"},
            {"5", "錢七", "研發部", "55000", "2023-05-12", "在職"},
            {"6", "孫八", "行銷部", "47000", "2023-06-18", "在職"},
            {"7", "周九", "客服部", "42000", "2023-07-22", "在職"},
            {"8", "吳十", "採購部", "49000", "2023-08-30", "在職"},
            {"9", "鄭一", "品管部", "46000", "2023-09-15", "在職"},
            {"10", "馮二", "生產部", "44000", "2023-10-01", "在職"}
        };
        
        for (int i = 0; i < sampleData.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < sampleData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(sampleData[i][j]);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // 自動調整欄寬
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 轉換為 byte array
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
    public byte[] generateReportExcel(String reportType) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(reportType + " Report");
        
        // 建立標題
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(reportType + " 報表 - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleCell.setCellStyle(titleStyle);
        
        // 建立表頭
        Row headerRow = sheet.createRow(2);
        String[] headers = {"項目", "數量", "金額", "百分比", "備註"};
        
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 新增統計資料
        String[][] reportData = {
            {"收入", "100", "1000000", "65%", "主要營收來源"},
            {"支出", "50", "350000", "23%", "營運成本"},
            {"利潤", "50", "650000", "42%", "淨利潤"},
            {"稅務", "15", "97500", "6.5%", "營業稅等"},
            {"其他", "10", "52500", "3.5%", "雜項費用"}
        };
        
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        
        for (int i = 0; i < reportData.length; i++) {
            Row row = sheet.createRow(i + 3);
            for (int j = 0; j < reportData[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(reportData[i][j]);
                cell.setCellStyle(dataStyle);
            }
        }
        
        // 自動調整欄寬
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        
        return outputStream.toByteArray();
    }
    
}