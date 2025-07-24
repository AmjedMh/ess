package com.teknokote.ess.core.service.impl.transactions;

import com.teknokote.ess.dto.TransactionDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import static com.teknokote.ess.core.service.impl.transactions.TransactionPDFGenerator.*;
import static com.teknokote.ess.utils.EssUtils.formatAmount;

public class TransactionExcelGenerator {
   private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

   private TransactionExcelGenerator() {
      throw new UnsupportedOperationException("Utility class");
   }

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

   private static void setCellValue(Row row, int cellIndex, String value) {
      row.createCell(cellIndex).setCellValue(value);
   }

   private static void createTitleRow(Sheet sheet, ResourceBundle bundle, List<String> columnsToDisplay) {
      Row titleRow = sheet.createRow(0);
      Cell titleCell = titleRow.createCell(0);
      titleCell.setCellValue(bundle.getString("title"));
      sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnsToDisplay.size() - 1));

      CellStyle titleStyle = createCellStyle(sheet.getWorkbook(), true, (short) 14, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      titleCell.setCellStyle(titleStyle);
   }

   private static void createExportDateCell(Sheet sheet, ResourceBundle bundle, int lastColumnIndex) {
      Row exportDateRow = sheet.createRow(1);
      Cell exportDateCell = exportDateRow.createCell(lastColumnIndex);
      exportDateCell.setCellValue(bundle.getString("exportedOn") + ": " + LocalDateTime.now().format(formatter));

      CellStyle exportDateStyle = createCellStyle(sheet.getWorkbook(), false, (short) 10, HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
      exportDateCell.setCellStyle(exportDateStyle);
      sheet.setColumnWidth(lastColumnIndex, 5000);
   }

   public static byte[] generateExcel(List<TransactionDto> transactions, List<String> columnsToDisplay, String locale, String filterSummary) throws IOException {
      ResourceBundle bundle = getBundle(locale);
      Workbook workbook = new XSSFWorkbook();
      Sheet sheet = workbook.createSheet(bundle.getString("title"));

      createTitleRow(sheet, bundle, columnsToDisplay);
      int lastColumnIndex = columnsToDisplay.size();
      createExportDateCell(sheet, bundle, lastColumnIndex);

      int rowIndex = 2; // Starting after the title and export date rows

      if (filterSummary != null && !filterSummary.isEmpty()) {
         Row filterRow = sheet.createRow(rowIndex++);
         Cell filterCell = filterRow.createCell(0);
         filterCell.setCellValue(bundle.getString("filters") + ": " + filterSummary);
         sheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex - 1, 0, lastColumnIndex - 1));
         filterCell.setCellStyle(createCellStyle(workbook, true, (short) 10, HorizontalAlignment.CENTER, VerticalAlignment.CENTER));
      }

      // Creating header row
      Row headerRow = sheet.createRow(rowIndex++);
      int cellIndex = 0;

      for (String columnName : columnsToDisplay) {
         String headerText = bundle.getString(columnName);
         if (columnName.equals("price") || columnName.equals("amount") || columnName.equals("totalAmount")) {
            String currencySymbol = getCurrencySymbol(transactions.get(0).getDevise());
            headerText += " (" + currencySymbol + ")";
         }
         setCellValue(headerRow, cellIndex++, headerText);
      }

      // Filling in the content
      for (TransactionDto transaction : transactions) {
         Row row = sheet.createRow(rowIndex++);
         cellIndex = 0;

         for (String columnName : columnsToDisplay) {
            String cellValue = getColumnValue(transaction, columnName);
            setCellValue(row, cellIndex++, cellValue);
         }
      }

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      workbook.write(outputStream);
      workbook.close();

      return outputStream.toByteArray();
   }

   private static String getColumnValue(TransactionDto transaction, String columnName) {
      return switch (columnName) {
         case "pumpAttendantName" -> transaction.getPumpAttendantName();
         case "pump" -> transaction.getPump().toString();
         case "fuelGradeName" -> transaction.getFuelGradeName();
         case "price" -> formatAmount(transaction.getPrice(), transaction.getDevise());
         case "volume" -> transaction.getVolume().toString();
         case "totalVolume" -> transaction.getTotalVolume().toString();
         case "amount" -> formatAmount(transaction.getAmount(), transaction.getDevise());
         case "totalAmount" -> formatAmount(transaction.getTotalAmount(), transaction.getDevise());
         case "dateTimeStart" -> transaction.getDateTimeStart().format(formatter);
         default -> "";
      };
   }
}