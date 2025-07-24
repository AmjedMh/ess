package com.teknokote.ess.core.service.impl.transactions;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.dto.TransactionDto;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;

import static com.teknokote.ess.utils.EssUtils.formatAmount;

public class TransactionPDFGenerator {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static byte[] generatePDF(List<TransactionDto> transactions, List<String> columnsToDisplay, String locale, String filterSummary) {
        ResourceBundle bundle = getBundle(locale);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            writer.setPageEvent(new PageNumberFooter());
            document.open();

            addExportedOn(document, bundle);
            addTitle(document, bundle);
            addFilterSummary(document, bundle, filterSummary);

            PdfPTable table = createTable(bundle, columnsToDisplay, transactions);
            addTransactionRows(table, transactions, columnsToDisplay);

            document.add(table);
        } catch (DocumentException e) {
            throw new ServiceValidationException("Could not generate transaction PDF");
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    public static void addExportedOn(Document document, ResourceBundle bundle) throws DocumentException {
        String exportedOn = bundle.containsKey("exportedOn")
                ? bundle.getString("exportedOn") + ": " + formatter.format(java.time.LocalDateTime.now())
                : "Exported On: " + formatter.format(java.time.LocalDateTime.now());

        Paragraph paragraph = new Paragraph(exportedOn, new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
        paragraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(paragraph);
    }

    public static void addTitle(Document document, ResourceBundle bundle) throws DocumentException {
        Paragraph title = new Paragraph(bundle.getString("title"), new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);
    }

    public static void addFilterSummary(Document document, ResourceBundle bundle, String filterSummary) throws DocumentException {
        if (filterSummary == null || filterSummary.isEmpty()) return;

        String filterText = bundle.containsKey("filters") ? bundle.getString("filters") : "Filters";
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.BLUE);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 7);

        Paragraph filtersParagraph = new Paragraph(filterText + ": ", valueFont);
        for (String filter : filterSummary.split(",")) {
            String[] parts = filter.split(":", 2);
            filtersParagraph.add(new Chunk(parts[0].trim() + ": ", labelFont));
            filtersParagraph.add(new Chunk(parts.length > 1 ? parts[1].trim() + "  " : "", valueFont));
        }
        filtersParagraph.setSpacingAfter(10);
        document.add(filtersParagraph);
    }

    public static PdfPTable createTable(ResourceBundle bundle, List<String> columnsToDisplay, List<TransactionDto> transactions) throws DocumentException {
        int totalColumns = columnsToDisplay.size() + 1;
        PdfPTable table = new PdfPTable(totalColumns);
        table.setWidthPercentage(100);

        float[] columnWidths = new float[totalColumns];
        Arrays.fill(columnWidths, 1, totalColumns, 2f);
        columnWidths[0] = 1f;
        table.setWidths(columnWidths);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 5, Font.BOLD, BaseColor.WHITE);
        addHeaderCell(table, bundle.getString("index"), headerFont);

        for (String column : columnsToDisplay) {
            String headerText = bundle.getString(column);
            if (Set.of("price", "amount", "totalAmount").contains(column)) {
                headerText += " (" + getCurrencySymbol(transactions.get(0).getDevise()) + ")";
            }
            addHeaderCell(table, headerText, headerFont);
        }
        table.setHeaderRows(1);

        return table;
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell header = new PdfPCell(new Phrase(text, font));
        header.setBackgroundColor(BaseColor.BLUE);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setPadding(5);
        header.setBorder(Rectangle.NO_BORDER);
        table.addCell(header);
    }

    public static void addTransactionRows(PdfPTable table, List<TransactionDto> transactions, List<String> columnsToDisplay) {
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 6);
        boolean isAlternate = false;

        for (int i = 0; i < transactions.size(); i++) {
            BaseColor backgroundColor = isAlternate ? new BaseColor(230, 230, 230) : BaseColor.WHITE;

            addCell(table, String.valueOf(i + 1), cellFont, backgroundColor);
            for (String column : columnsToDisplay) {
                addCell(table, getColumnValue(transactions.get(i), column), cellFont, backgroundColor);
            }

            isAlternate = !isAlternate;
        }
    }

    private static void addCell(PdfPTable table, String value, Font font, BaseColor backgroundColor) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(backgroundColor);
        table.addCell(cell);
    }
    private static class PageNumberFooter extends PdfPageEventHelper {
        private PdfTemplate total;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            int currentPageNumber = writer.getPageNumber();
            String pageNumberText = currentPageNumber + " / ";
            Font font = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
            Phrase phrase = new Phrase(pageNumberText, font);

            ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER,
                    phrase, (document.left() + document.right()) / 2, document.bottom() - 20, 0);

            PdfContentByte canvas = writer.getDirectContent();
            canvas.addTemplate(total, (document.left() + document.right()) / 2 + 15, document.bottom() - 20);
        }
        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            Font font = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
            Phrase totalPhrase = new Phrase(String.valueOf(writer.getPageNumber() ), font);
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT, totalPhrase, 0, 0, 0);
        }
    }
    public static ResourceBundle getBundle(String locale) {
        return ResourceBundle.getBundle("messages", new Locale(locale));
    }

    public static String getCurrencySymbol(String currencyCode) {
        Currency currency = Currency.getInstance(currencyCode);
        return currency.getSymbol();
    }
    public static String getColumnValue(TransactionDto transaction, String column) {
        return switch (column) {
            case "pumpAttendantName" -> transaction.getPumpAttendantName();
            case "pump" -> String.valueOf(transaction.getPump());
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
