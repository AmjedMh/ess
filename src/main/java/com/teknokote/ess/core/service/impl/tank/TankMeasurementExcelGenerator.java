package com.teknokote.ess.core.service.impl.tank;

import com.teknokote.ess.dto.TankMeasurementsDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import static com.teknokote.ess.core.service.impl.transactions.TransactionPDFGenerator.getBundle;
import static com.teknokote.ess.utils.EssUtils.*;

public class TankMeasurementExcelGenerator {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Create CellStyle for headers
    private static CellStyle createCellStyle(Workbook workbook, boolean bold, short fontSize, HorizontalAlignment hAlign, VerticalAlignment vAlign) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(bold);
        font.setFontHeightInPoints(fontSize);
        style.setFont(font);
        style.setAlignment(hAlign);
        style.setVerticalAlignment(vAlign);
        return style;
    }

    // Method to set a value to a cell
    private static void setCellValue(Row row, int cellIndex, String value) {
        row.createCell(cellIndex).setCellValue(value);
    }

    // Centralizing logic to retrieve cell value based on the column name
    private static String getColumnValue(TankMeasurementsDto tankMeasurementDto, String columnName) {
        return switch (columnName) {
            case "dateTime" -> tankMeasurementDto.getDateTime().format(formatter);
            case "alarms" -> tankMeasurementDto.getAlarms() != null ? tankMeasurementDto.getAlarms() : "-";
            case "tank" -> tankMeasurementDto.getTank().toString();
            case "productHeight" -> formatHeight(tankMeasurementDto.getProductHeight());
            case "productVolume" -> formatVolume(tankMeasurementDto.getProductVolume());
            case "waterHeight" -> formatHeight(tankMeasurementDto.getWaterHeight());
            case "waterVolume" -> formatHeight(tankMeasurementDto.getWaterVolume());
            case "productDensity" -> tankMeasurementDto.getProductDensity().toString();
            case "productMass" -> tankMeasurementDto.getProductMass().toString();
            case "tankFillingPercentage" -> formatPorcentage(tankMeasurementDto.getTankFillingPercentage());
            case "temperature" -> formatTemperature(tankMeasurementDto.getTemperature());
            default -> "";
        };
    }

    public static byte[] generateMeasurementExcel(List<TankMeasurementsDto> tankMeasurements, List<String> columnsToDisplay, String locale, String filterSummary) throws IOException {
        ResourceBundle bundle = getBundle(locale);

        // Use try-with-resources to handle Workbook and ByteArrayOutputStream
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(bundle.getString("titleMeasurements"));

            createTitleRow(sheet, bundle, columnsToDisplay);
            createExportDateCell(sheet, bundle, columnsToDisplay);

            int rowIndex = initializeRows(sheet, columnsToDisplay, filterSummary);

            for (TankMeasurementsDto tankMeasurementDto : tankMeasurements) {
                Row row = sheet.createRow(rowIndex++);
                int cellIndex = 0;

                for (String columnName : columnsToDisplay) {
                    String cellValue = getColumnValue(tankMeasurementDto, columnName);
                    setCellValue(row, cellIndex++, cellValue);
                }
            }

            // Write the workbook to the output stream
            workbook.write(outputStream);

            // Return the byte array from the output stream
            return outputStream.toByteArray();
        }
    }
    private static void createTitleRow(Sheet sheet, ResourceBundle bundle, List<String> columnsToDisplay) {
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(bundle.getString("titleMeasurements"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnsToDisplay.size() - 1));

        CellStyle titleStyle = createCellStyle(sheet.getWorkbook(), true, (short) 14, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);
    }

    private static void createExportDateCell(Sheet sheet, ResourceBundle bundle, List<String> columnsToDisplay) {
        int lastColumnIndex = columnsToDisplay.size();
        Row exportDateRow = sheet.createRow(1);
        Cell exportDateCell = exportDateRow.createCell(lastColumnIndex);
        exportDateCell.setCellValue(bundle.getString("exportedOn") + ": " + LocalDateTime.now().format(formatter));

        CellStyle exportDateStyle = createCellStyle(sheet.getWorkbook(), false, (short) 10, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
        exportDateCell.setCellStyle(exportDateStyle);
        sheet.setColumnWidth(lastColumnIndex, 5000);
    }

    private static int initializeRows(Sheet sheet, List<String> columnsToDisplay, String filterSummary) {
        int rowIndex = 2; // Start after title and export date row

        if (filterSummary != null && !filterSummary.isEmpty()) {
            Row filterRow = sheet.createRow(rowIndex++);
            Cell filterCell = filterRow.createCell(0);
            filterCell.setCellValue(filterSummary);
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, columnsToDisplay.size() - 1));

            CellStyle filterStyle = createCellStyle(sheet.getWorkbook(), true, (short) 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
            filterCell.setCellStyle(filterStyle);
        }

        // Create header row
        Row headerRow = sheet.createRow(rowIndex++);
        int cellIndex = 0;
        for (String columnName : columnsToDisplay) {
            String headerText = sheet.getWorkbook().getSheetAt(0).getSheetName() + " - " + columnName; // Add your header text logic as needed
            setCellValue(headerRow, cellIndex++, headerText);
        }

        return rowIndex;
    }

    public static String formatPorcentage(double tankFillingPercentage) {
        return String.format("%,.0f %%", tankFillingPercentage).replace(".", ",");
    }
}