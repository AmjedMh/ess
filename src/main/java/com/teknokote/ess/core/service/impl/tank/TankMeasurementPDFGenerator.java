package com.teknokote.ess.core.service.impl.tank;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.dto.TankMeasurementsDto;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.teknokote.ess.core.service.impl.tank.TankMeasurementExcelGenerator.formatPorcentage;
import static com.teknokote.ess.utils.EssUtils.*;

public class TankMeasurementPDFGenerator {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font EXPORT_ON_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.BLACK);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 5, Font.BOLD, BaseColor.WHITE);
    private static final Font CELL_FONT = new Font(Font.FontFamily.HELVETICA, 6);
    private static final BaseColor HEADER_COLOR = BaseColor.BLUE;
    private static final BaseColor ALTERNATE_ROW_COLOR = new BaseColor(230, 230, 230);
    private static final BaseColor WHITE_COLOR = BaseColor.WHITE;

    public static byte[] generateMeasurementPDF(List<TankMeasurementsDto> tankMeasurements, List<String> columnsToDisplay, String locale, String filterSummary) {
        ResourceBundle bundle = getBundle(locale);
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            PageNumberFooter footer = new PageNumberFooter();
            writer.setPageEvent(footer);
            document.open();

            addExportedOnParagraph(document, bundle);
            addTitleParagraph(document, bundle);
            addFilterParagraph(document, bundle, filterSummary);

            PdfPTable table = createTable(columnsToDisplay);
            addHeaderRow(table, columnsToDisplay, bundle);
            addMeasurementRows(table, tankMeasurements, columnsToDisplay);
            document.add(table);

        } catch (DocumentException e) {
            throw new ServiceValidationException("Could not generate tank measurement PDF");
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private static void addExportedOnParagraph(Document document, ResourceBundle bundle) throws DocumentException {
        String exportedOn = bundle.containsKey("exportedOn")
                ? bundle.getString("exportedOn") + ": " + formatter.format(java.time.LocalDateTime.now())
                : "Exported On: " + formatter.format(java.time.LocalDateTime.now());

        Paragraph exportedOnParagraph = new Paragraph(exportedOn, EXPORT_ON_FONT);
        exportedOnParagraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(exportedOnParagraph);
    }

    private static void addTitleParagraph(Document document, ResourceBundle bundle) throws DocumentException {
        Paragraph title = new Paragraph(bundle.getString("titleMeasurements"), new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);
    }

    private static void addFilterParagraph(Document document, ResourceBundle bundle, String filterSummary) throws DocumentException {
        if (filterSummary != null && !filterSummary.isEmpty()) {
            // Create a new paragraph for filters
            Paragraph filtersParagraph = new Paragraph();

            // Use a distinct font style for "Filtres:"
            Chunk filtersLabel = new Chunk(bundle.getString("filters") + ": ", new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK));
            filtersParagraph.add(filtersLabel);

            String[] filterParts = filterSummary.split(",");
            for (String filter : filterParts) {
                addFilterChunk(filtersParagraph, filter);
            }
            filtersParagraph.setSpacingAfter(10);
            document.add(filtersParagraph);
        }
    }

    private static void addFilterChunk(Paragraph filtersParagraph, String filter) {
        if (filter.contains(":")) {
            String[] parts = filter.split(":");
            String label = parts[0].trim();
            String value = parts.length > 1 ? parts[1].trim() : "";

            // Use a specific color for the label
            filtersParagraph.add(new Chunk(label + ": ", new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, HEADER_COLOR)));
            filtersParagraph.add(new Chunk(value + "  ", CELL_FONT));
        } else {
            filtersParagraph.add(new Chunk(filter + "  ", CELL_FONT));
        }
    }
    private static PdfPTable createTable(List<String> columnsToDisplay) throws DocumentException {
        int totalColumns = columnsToDisplay.size() + 1;
        PdfPTable table = new PdfPTable(totalColumns);
        table.setWidthPercentage(100);
        float[] columnWidths = new float[totalColumns];
        columnWidths[0] = 1f; // Index column width
        for (int i = 1; i < totalColumns; i++) {
            columnWidths[i] = 2f; // Data columns width
        }
        table.setWidths(columnWidths);
        return table;
    }

    private static void addHeaderRow(PdfPTable table, List<String> columnsToDisplay, ResourceBundle bundle) {
        table.addCell(createCell(bundle.getString("index"), HEADER_FONT, HEADER_COLOR, Element.ALIGN_CENTER));
        for (String column : columnsToDisplay) {
            String headerText = bundle.getString(column);
            table.addCell(createCell(headerText, HEADER_FONT, HEADER_COLOR, Element.ALIGN_CENTER));
        }
        table.setHeaderRows(1);
    }

    private static void addMeasurementRows(PdfPTable table, List<TankMeasurementsDto> tankmeasurements, List<String> columnsToDisplay) {
        int index = 1;
        boolean isAlternate = false;
        for (TankMeasurementsDto tankMeasurementDto : tankmeasurements) {
            BaseColor backgroundColor = isAlternate ? ALTERNATE_ROW_COLOR : WHITE_COLOR;

            table.addCell(createCell(String.valueOf(index++), CELL_FONT, backgroundColor, Element.ALIGN_CENTER));

            for (String column : columnsToDisplay) {
                String cellValue = getColumnValue(tankMeasurementDto, column);
                table.addCell(createCell(cellValue, CELL_FONT, backgroundColor, Element.ALIGN_CENTER));
            }
            isAlternate = !isAlternate;
        }
    }

    private static PdfPCell createCell(String text, Font font, BaseColor backgroundColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(backgroundColor);
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
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
            Phrase totalPhrase = new Phrase(String.valueOf(writer.getPageNumber()), font);
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT, totalPhrase, 0, 0, 0);
        }
    }

    public static ResourceBundle getBundle(String locale) {
        return ResourceBundle.getBundle("messages", new Locale(locale));
    }

    public static String getColumnValue(TankMeasurementsDto tankMeasurementDto, String column) {
        return switch (column) {
            case "dateTime" -> tankMeasurementDto.getDateTime().format(formatter);
            case "alarms" -> tankMeasurementDto.getAlarms();
            case "tank" -> tankMeasurementDto.getTank().toString();
            case "productHeight" -> formatHeight(tankMeasurementDto.getProductHeight());
            case "productVolume" -> formatVolume(tankMeasurementDto.getProductVolume());
            case "waterHeight" -> formatHeight(tankMeasurementDto.getWaterHeight());
            case "waterVolume" -> formatVolume(tankMeasurementDto.getWaterVolume());
            case "productDensity" -> tankMeasurementDto.getProductDensity().toString();
            case "productMass" -> tankMeasurementDto.getProductMass().toString();
            case "tankFillingPercentage" -> formatPorcentage(tankMeasurementDto.getTankFillingPercentage());
            case "temperature" -> formatTemperature(tankMeasurementDto.getTemperature());
            default -> "";
        };
    }
}