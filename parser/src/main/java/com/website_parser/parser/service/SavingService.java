package com.website_parser.parser.service;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class SavingService {

    @SneakyThrows
    public void exportMapToExcel(Map<String, List<String>> dataMap, String filePath)  {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");
        Row headerRow = sheet.createRow(0);
        int columnIndex = 0;

        for (String columnName : dataMap.keySet()) {
            Cell cell = headerRow.createCell(columnIndex++);
            cell.setCellValue(columnName);
        }
        int maxRows = 0;
        for (List<String> rows : dataMap.values()) {
            if (rows.size() > maxRows) {
                maxRows = rows.size();
            }
        }
        for (int rowIndex = 0; rowIndex < maxRows; rowIndex++) {
            Row row = sheet.createRow(rowIndex + 1);
            columnIndex = 0;

            for (List<String> columnData : dataMap.values()) {
                Cell cell = row.createCell(columnIndex++);
                if (rowIndex < columnData.size()) {
                    cell.setCellValue(columnData.get(rowIndex));
                }
            }
        }
        FileOutputStream fileOut = new FileOutputStream(filePath);
        workbook.write(fileOut);
        workbook.close();
    }
}
