package com.teknokote.ess.core.service.impl.tank;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.teknokote.core.exceptions.ServiceValidationException;
import com.teknokote.ess.dto.TankDeliveryDto;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static com.teknokote.ess.core.service.impl.tank.TankDeliveryExcelGenerator.translateStatus;
import static com.teknokote.ess.utils.EssUtils.*;

public class TankDeliveryPDFGenerator {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static byte[] generateDeliveryPDF(List<TankDeliveryDto> tankDeliveriesDto, List<String> columnsToDisplay, String locale, String filterSummary){
        ResourceBundle bundle = getBundle(locale);
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            TankDeliveryPDFGenerator.PageNumberFooter footer = new TankDeliveryPDFGenerator.PageNumberFooter();
            writer.setPageEvent(footer);
            document.open();

            Font exportedOnFont = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.BLACK);
            String exportedOn = bundle.containsKey("exportedOn")
                    ? bundle.getString("exportedOn") + ": " + formatter.format(java.time.LocalDateTime.now())
                    : "Exported On: " + formatter.format(java.time.LocalDateTime.now());

            Paragraph exportedOnParagraph = new Paragraph(exportedOn, exportedOnFont);
            exportedOnParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(exportedOnParagraph);

            Paragraph title = new Paragraph(bundle.getString("titleDelivery"), new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            if (filterSummary != null && !filterSummary.isEmpty()) {
                String filterText = bundle.containsKey("filters") ? bundle.getString("filters") : "Filters";

                Font labelFont = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, BaseColor.BLUE);
                Font valueFont = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, BaseColor.BLACK);

                // Création d'un paragraphe
                Paragraph filtersParagraph = new Paragraph();

                filtersParagraph.add(new Chunk(filterText + ": ", valueFont));

                String[] filterParts = filterSummary.split(",");
                for (String filter : filterParts) {
                    if (filter.contains(":")) {
                        String[] parts = filter.split(":");
                        String label = parts[0].trim();
                        String value = parts.length > 1 ? parts[1].trim() : "";

                        filtersParagraph.add(new Chunk(label + ": ", labelFont));
                        filtersParagraph.add(new Chunk(value + "  ", valueFont));
                    } else {
                        filtersParagraph.add(new Chunk(filter + "  ", valueFont));
                    }
                }

                filtersParagraph.setSpacingAfter(10);
                document.add(filtersParagraph);
            }



            int totalColumns = columnsToDisplay.size() + 1;
            PdfPTable table = new PdfPTable(totalColumns);
            table.setWidthPercentage(100);

            float[] columnWidths = new float[totalColumns];
            columnWidths[0] = 1f;
            for (int i = 1; i < totalColumns; i++) {
                columnWidths[i] = 1.5f;
            }
            table.setWidths(columnWidths);

            Font headerFont = new Font(Font.FontFamily.HELVETICA, 5, Font.BOLD, BaseColor.WHITE);

            // Index Header
            PdfPCell indexHeader = new PdfPCell(new Phrase(bundle.getString("index"), headerFont));
            indexHeader.setBackgroundColor(BaseColor.BLUE);
            indexHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            indexHeader.setPadding(5);
            indexHeader.setBorder(Rectangle.NO_BORDER);
            table.addCell(indexHeader);

            // Dynamically add headers based on selected columns
            for (String column : columnsToDisplay) {
                String headerText = bundle.getString(column);

                PdfPCell headerCell = new PdfPCell(new Phrase(headerText, headerFont));
                headerCell.setBackgroundColor(BaseColor.BLUE);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                headerCell.setPadding(5);
                headerCell.setBorder(Rectangle.NO_BORDER);
                table.addCell(headerCell);
            }
            table.setHeaderRows(1);

            // Add transaction rows to the table
            Font cellFont = new Font(Font.FontFamily.HELVETICA, 6);
            int index = 1;
            boolean isAlternate = false;
            for (TankDeliveryDto tankDeliveryDto : tankDeliveriesDto) {
                BaseColor backgroundColor = isAlternate ? new BaseColor(230, 230, 230) : BaseColor.WHITE;

                PdfPCell indexCell = new PdfPCell(new Phrase(String.valueOf(index++), cellFont));
                indexCell.setNoWrap(true);
                indexCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                indexCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                indexCell.setPadding(6);
                indexCell.setBorder(Rectangle.NO_BORDER);
                indexCell.setBackgroundColor(backgroundColor);
                indexCell.setFixedHeight(20f);

                table.addCell(indexCell);

                for (String column : columnsToDisplay) {
                    PdfPCell cell = new PdfPCell(new Phrase(getColumnValue(tankDeliveryDto, column), cellFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(6);
                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setBackgroundColor(backgroundColor);
                    cell.setNoWrap(false);
                    cell.setMinimumHeight(20);

                    table.addCell(cell);
                }
                isAlternate = !isAlternate;
            }

            document.add(table);
        } catch (DocumentException e) {
            throw new ServiceValidationException("Could not generate tank delivery PDF");
        } finally {
            document.close();
        }

        return outputStream.toByteArray();
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



    public static String getColumnValue(TankDeliveryDto tankDeliveryDto, String column) {
        return switch (column) {
            case "startDateTime" -> tankDeliveryDto.getStartDateTime().format(formatter);
            case "endDateTime" -> tankDeliveryDto.getEndDateTime().format(formatter);
            case "duration" -> tankDeliveryDto.getDuration();
            case "status" -> translateStatus(tankDeliveryDto.getStatus(), getBundle(Locale.getDefault().toString()));
            case "tank" -> tankDeliveryDto.getTank().toString();
            case "fuelGradeName" -> tankDeliveryDto.getFuelGradeName();
            case "startProductVolume" -> formatVolume(tankDeliveryDto.getStartProductVolume());
            case "endProductVolume" -> formatVolume(tankDeliveryDto.getEndProductVolume());
            case "salesVolume" -> formatVolume(tankDeliveryDto.getSalesVolume());
            case "productVolume" -> formatVolume(tankDeliveryDto.getProductVolume());
            case "startProductHeight"-> formatHeight(tankDeliveryDto.getStartProductHeight().doubleValue());
            case "endProductHeight"-> formatHeight(tankDeliveryDto.getEndProductHeight().doubleValue());
            case "productHeight"-> formatHeight(tankDeliveryDto.getProductHeight().doubleValue());
            case "waterHeight" -> String.valueOf(tankDeliveryDto.getWaterHeight());
            case "temperature"-> formatTemperature(tankDeliveryDto.getTemperature().doubleValue());

            default -> "";
        };
    }
}
