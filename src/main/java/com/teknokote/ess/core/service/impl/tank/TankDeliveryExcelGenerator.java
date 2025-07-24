package com.teknokote.ess.core.service.impl.tank;

import com.teknokote.ess.core.model.EnumDeliveryStatus;
import com.teknokote.ess.dto.TankDeliveryDto;
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

public class TankDeliveryExcelGenerator {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static CellStyle getFilterSummaryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    public static byte[] generateDeliveryExcel(List<TankDeliveryDto> tankDeliveriesDto, List<String> columnsToDisplay, String locale, String filterSummary) throws IOException
    {
        ResourceBundle bundle = getBundle(locale);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(bundle.getString("titleDelivery"));

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(bundle.getString("titleDelivery"));

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnsToDisplay.size() - 1));

        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 14);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleCell.setCellStyle(titleStyle);

        Row exportDateRow = sheet.getRow(0);
        if (exportDateRow == null) {
            exportDateRow = sheet.createRow(0);
        }
        int lastColumnIndex = columnsToDisplay.size();
        Cell exportDateCell = exportDateRow.createCell(lastColumnIndex);
        exportDateCell.setCellValue(bundle.getString("exportedOn") + ": " + LocalDateTime.now().format(formatter));

        CellStyle exportDateStyle = workbook.createCellStyle();
        Font exportDateFont = workbook.createFont();
        exportDateFont.setItalic(true);
        exportDateFont.setFontHeightInPoints((short) 10);
        exportDateStyle.setFont(exportDateFont);
        exportDateStyle.setAlignment(HorizontalAlignment.RIGHT);
        exportDateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        exportDateCell.setCellStyle(exportDateStyle);

        sheet.setColumnWidth(lastColumnIndex, 5000);
        int rowIndex = 1;

        if (filterSummary != null && !filterSummary.isEmpty()) {
            // Create filter summary row at row index 0
            Row filterRow = sheet.createRow(rowIndex++);
            Cell filterCell = filterRow.createCell(0);
            filterCell.setCellValue(bundle.getString("filters") + ": "+ filterSummary);

            // Merge cells across the width of the table
            sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, columnsToDisplay.size() - 1));
            filterCell.setCellStyle(getFilterSummaryStyle(workbook));
        }

        // En-tête
        Row headerRow = sheet.createRow(rowIndex++);
        int cellIndex = 0;

        // Créer des en-têtes basés sur les colonnes à afficher
        for (String columnName : columnsToDisplay) {
            String headerText = bundle.getString(columnName);
            headerRow.createCell(cellIndex++).setCellValue(headerText);
        }

        // Contenu
        int rowNum = rowIndex;
        for (TankDeliveryDto tankDeliveryDto : tankDeliveriesDto) {
            Row row = sheet.createRow(rowNum++);
            cellIndex = 0;

            // Remplissez les cellules en fonction des colonnes à afficher
            for (String columnName : columnsToDisplay) {
                switch (columnName) {
                    case  "startDateTime":
                        row.createCell(cellIndex++).setCellValue(tankDeliveryDto.getStartDateTime().format(formatter));
                        break;
                    case "endDateTime":
                        row.createCell(cellIndex++).setCellValue(tankDeliveryDto.getEndDateTime().format(formatter));
                        break;
                    case "duration":
                        row.createCell(cellIndex++).setCellValue(tankDeliveryDto.getDuration());
                        break;
                    case "status":
                        row.createCell(cellIndex++).setCellValue(
                                tankDeliveryDto.getStatus() != null ? translateStatus(tankDeliveryDto.getStatus(), bundle) : ""
                        );
                        break;
                    case "tank":
                        row.createCell(cellIndex++).setCellValue(tankDeliveryDto.getTank());
                        break;
                    case "fuelGradeName":
                        row.createCell(cellIndex++).setCellValue(tankDeliveryDto.getFuelGradeName());
                        break;
                    case "startProductVolume":
                        row.createCell(cellIndex++).setCellValue(formatVolume(tankDeliveryDto.getStartProductVolume()));
                        break;
                    case "endProductVolume" :
                        row.createCell(cellIndex++).setCellValue(formatVolume(tankDeliveryDto.getEndProductVolume()));
                        break;
                    case "salesVolume":
                        row.createCell(cellIndex++).setCellValue(
                                tankDeliveryDto.getSalesVolume() != null ? formatVolume(tankDeliveryDto.getSalesVolume()) : "-"
                        );
                        break;
                    case "productVolume":
                        row.createCell(cellIndex++).setCellValue(formatVolume(tankDeliveryDto.getProductVolume()));
                        break;
                    case "startProductHeight":
                        row.createCell(cellIndex++).setCellValue(formatHeight(tankDeliveryDto.getStartProductHeight().doubleValue()));
                        break;
                    case "endProductHeight":
                        row.createCell(cellIndex++).setCellValue(formatHeight(tankDeliveryDto.getEndProductHeight().doubleValue()));
                        break;
                    case "productHeight":
                        row.createCell(cellIndex++).setCellValue(formatHeight(tankDeliveryDto.getProductHeight().doubleValue()));
                        break;
                    case "waterHeight":
                        row.createCell(cellIndex++).setCellValue(tankDeliveryDto.getWaterHeight());
                        break;
                    case "temperature":
                        row.createCell(cellIndex++).setCellValue(formatTemperature(tankDeliveryDto.getTemperature().doubleValue()));
                        break;
                    default:
                        break;
                }
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public static String translateStatus(EnumDeliveryStatus status, ResourceBundle bundle) {
        if (status == null) return "";
        return bundle.getString(status.name());
    }


}
