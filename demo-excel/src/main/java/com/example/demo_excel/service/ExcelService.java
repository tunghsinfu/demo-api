package com.example.demo_excel.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {
    // 集中測試資料與標題
    private static final String[] SAMPLE_HEADERS = {"ID", "姓名", "部門", "薪資", "入職日期", "狀態"};
    private static final String[][] SAMPLE_DATA = {
        {"1", "張三", "資訊部", "50000", "2023-01-15", "在職"},
        {"2", "李四", "財務部", "48000", "2022-12-01", "在職"},
        {"3", "王五", "人資部", "51000", "2021-11-20", "在職"},
        {"4", "趙六", "行銷部", "47000", "2020-10-10", "離職"},
        {"5", "陳七", "資訊部", "53000", "2023-03-05", "在職"},
        {"6", "林八", "財務部", "49500", "2022-08-18", "在職"},
        {"7", "周九", "人資部", "52000", "2021-07-30", "在職"},
        {"8", "吳十", "行銷部", "46000", "2020-06-25", "離職"},
        {"9", "鄭十一", "資訊部", "54000", "2023-04-12", "在職"},
        {"10", "謝十二", "財務部", "50000", "2022-05-15", "在職"}
    };

    // 共用產生 sample excel 的主邏輯
    private Workbook createSampleWorkbook() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sample Data");

        // 標題樣式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 標題行
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < SAMPLE_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(SAMPLE_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }

        // 資料樣式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);

        // 寫入資料
        for (int i = 0; i < SAMPLE_DATA.length; i++) {
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < SAMPLE_DATA[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(SAMPLE_DATA[i][j]);
                cell.setCellStyle(dataStyle);
            }
        }

        // 自動調整欄寬
        for (int i = 0; i < SAMPLE_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
        }
        return workbook;
    }

    public byte[] generateSampleExcel() throws IOException {
        try (Workbook workbook = createSampleWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    // 統一假資料workbook產生
    private Workbook getFakeWorkbook() {
        return createSampleWorkbook();
    }

    public byte[] generateReportExcel(String reportType) throws IOException {
        try (Workbook workbook = getFakeWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    // 真正優化的方法：使用 InputStreamResource 避免額外記憶體分配
    public InputStream generateSampleExcelAsInputStream() throws IOException {
        try (Workbook workbook = getFakeWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    // 為 ResponseEntity<Resource> 優化的方法
    public ByteArrayOutputStream generateSampleExcelAsStream() throws IOException {
        try (Workbook workbook = getFakeWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream;
        }
    }

    // 最佳實踐：直接寫入到 OutputStream，完全避免記憶體中介
    public void generateSampleExcelToStream(OutputStream outputStream) throws IOException {
        try (Workbook workbook = getFakeWorkbook()) {
            workbook.write(outputStream);
        }
    }

    public void generateReportExcelToStream(String reportType, OutputStream outputStream) throws IOException {
        try (Workbook workbook = getFakeWorkbook()) {
            workbook.write(outputStream);
        }
    }

    public ByteArrayOutputStream generateReportExcelAsStream(String reportType) throws IOException {
        try (Workbook workbook = getFakeWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream;
        }
    }
    
    // 接收 Excel 並寫入 SAMPLE_DATA
    public byte[] fillExcelWithData(byte[] excelData) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(excelData);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            // 取得第一個工作表
            Sheet sheet = workbook.getSheetAt(0);
            
            // 檢查是否有標題行
            if (sheet.getPhysicalNumberOfRows() == 0) {
                throw new IOException("Excel file is empty");
            }
            
            // 資料樣式
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            
            // 寫入 SAMPLE_DATA（從第二行開始）
            for (int i = 0; i < SAMPLE_DATA.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < SAMPLE_DATA[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(SAMPLE_DATA[i][j]);
                    cell.setCellStyle(dataStyle);
                }
            }
            
            // 自動調整欄寬
            for (int i = 0; i < SAMPLE_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 輸出為 byte array
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }
    
}