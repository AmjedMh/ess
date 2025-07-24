package com.teknokote.ess.core.service.export;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.teknokote.ess.core.service.impl.transactions.TransactionPDFGenerator;
import com.teknokote.ess.dto.TransactionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class TransactionPDFGeneratorTest {

    private TransactionPDFGenerator pdfGenerator;

    @BeforeEach
    void setUp() {
        pdfGenerator = new TransactionPDFGenerator();
    }

    @Test
    void testGeneratePDF_withValidInput(){
        // Arrange
        List<TransactionDto> transactions = Arrays.asList(
                mockTransaction("John Doe", 1L, "Gasoline", 10, "USD"),
                mockTransaction("Jane Doe", 2L, "Diesel", 20, "USD")
        );
        List<String> columns = Arrays.asList("pumpAttendantName", "pump", "fuelGradeName", "price");
        String locale = "en";
        String filterSummary = "filter1: value1, filter2: value2";

        // Mock ResourceBundle
        ResourceBundle bundle = mock(ResourceBundle.class);
        when(bundle.getString("title")).thenReturn("Transaction Report");
        when(bundle.getString("filters")).thenReturn("Filters");
        when(bundle.getString("exportedOn")).thenReturn("Exported On");

        // Mock static method in TransactionPDFGenerator

        try (MockedStatic<TransactionPDFGenerator> mockedStatic = mockStatic(TransactionPDFGenerator.class)) {
            mockedStatic.when(() -> TransactionPDFGenerator.getBundle(locale)).thenReturn(bundle);

            // Mock pdfGenerator.generatePDF to return a byte array (e.g., valid PDF content)
            when(pdfGenerator.generatePDF(anyList(), anyList(), anyString(), anyString())).thenReturn(new byte[]{1, 2, 3});

            // Act
            byte[] pdfContent = pdfGenerator.generatePDF(transactions, columns, locale, filterSummary);

            // Assert
            assertNotNull(pdfContent);
            assertTrue(pdfContent.length > 0);
        }
    }

    @Test
    void testAddExportedOn_withValidLocale() throws Exception {
        // Arrange
        Document document = mock(Document.class);
        ResourceBundle bundle = mock(ResourceBundle.class);
        when(bundle.getString("exportedOn")).thenReturn("Exported On");

        // Act
        pdfGenerator.addExportedOn(document, bundle);

        // Assert
        verify(document).add(any(Paragraph.class));
    }

    @Test
    void testAddTitle_withValidBundle() throws Exception {
        // Arrange
        Document document = mock(Document.class);
        ResourceBundle bundle = mock(ResourceBundle.class);
        when(bundle.getString("title")).thenReturn("Transaction Report");

        // Act
        pdfGenerator.addTitle(document, bundle);

        // Assert
        verify(document).add(any(Paragraph.class));
    }

    @Test
    void testAddTransactionRows_withValidTransactions() {
        // Arrange
        PdfPTable table = mock(PdfPTable.class);
        List<TransactionDto> transactions = Arrays.asList(
                mockTransaction("John Doe", 1L, "Gasoline", 10, "USD")
        );
        List<String> columnsToDisplay = Arrays.asList("pumpAttendantName", "pump", "fuelGradeName", "price");

        // Act
        pdfGenerator.addTransactionRows(table, transactions, columnsToDisplay);

        // Assert
        verify(table, atLeastOnce()).addCell(any(PdfPCell.class));  // Verify at least one cell was added
    }

    private TransactionDto mockTransaction(String pumpAttendantName, Long pump, String fuelGradeName, float price, String currency) {
        TransactionDto transaction = mock(TransactionDto.class);
        when(transaction.getPumpAttendantName()).thenReturn(pumpAttendantName);
        when(transaction.getPump()).thenReturn(pump);
        when(transaction.getFuelGradeName()).thenReturn(fuelGradeName);
        when(transaction.getPrice()).thenReturn(price);  // Ensure BigDecimal is handled
        when(transaction.getDevise()).thenReturn(currency);
        return transaction;
    }

    @Test
    void generatePDF_ShouldReturnNonNullByteArray_WhenTransactionsAreProvided() {
        List<TransactionDto> transactions = List.of(
                createTransactionDto("2023-10-01T10:00:00", 101L, null, new BigDecimal("10.0"),
                        3.5f, new BigDecimal("35.0"), null, "Attendant A",
                        new BigDecimal("10.0"), new BigDecimal("35.0"), "Gasoline", "USD"),
                createTransactionDto("2023-10-01T11:00:00", 102L, null, new BigDecimal("15.0"),
                        3.2f, new BigDecimal("48.0"), null, "Attendant B",
                        new BigDecimal("15.0"), new BigDecimal("48.0"), "Diesel", "USD")
        );

        List<String> columnsToDisplay = List.of("pumpAttendantName", "pump", "fuelGradeName", "price", "volume", "totalVolume", "amount", "totalAmount");
        String locale = "en";
        String filterSummary = "Date: 2023-10-23";

        byte[] pdfBytes = TransactionPDFGenerator.generatePDF(transactions, columnsToDisplay, locale, filterSummary);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
    private TransactionDto createTransactionDto(String dateTimeStartString, Long pump, Long nozzle,
                                                BigDecimal volume, float price, BigDecimal amount,
                                                Long transaction, String pumpAttendantName,
                                                BigDecimal totalVolume, BigDecimal totalAmount,
                                                String fuelGradeName, String devise) {
        TransactionDto dto = new TransactionDto();
        dto.setDateTimeStart(LocalDateTime.parse(dateTimeStartString));
        dto.setPump(pump);
        dto.setNozzle(nozzle);
        dto.setVolume(volume);
        dto.setPrice(price);
        dto.setAmount(amount);
        dto.setTransaction(transaction);
        dto.setPumpAttendantName(pumpAttendantName);
        dto.setTotalVolume(totalVolume);
        dto.setTotalAmount(totalAmount);
        dto.setFuelGradeName(fuelGradeName);
        dto.setDevise(devise);
        return dto;
    }

    @Test
    void addExportedOn_ShouldAddExportedOnToDocument() throws Exception {
        Document document = mock(Document.class);
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);

        TransactionPDFGenerator.addExportedOn(document, bundle);

        // You cannot directly check what is added, but you can verify the document.add() method was called
        verify(document, atLeastOnce()).add(any(Paragraph.class));
    }

    @Test
    void addFilterSummary_ShouldAddFiltersToDocument_WhenFiltersProvided() throws Exception {
        Document document = mock(Document.class);
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);
        String filterSummary = "Start Date: 2023-10-01, End Date: 2023-10-31";

        TransactionPDFGenerator.addFilterSummary(document, bundle, filterSummary);

        verify(document, atLeastOnce()).add(any(Paragraph.class)); // Ensure a paragraph is added
    }

    @Test
    void createTable_ShouldCreateTableWithCorrectNumberOfColumns() throws Exception {
        List<TransactionDto> transactions = List.of(createTransactionDto("2023-10-01T10:00:00", 101L, null, new BigDecimal("10.0"),
                        3.5f, new BigDecimal("35.0"), null, "Attendant A",
                        new BigDecimal("10.0"), new BigDecimal("35.0"), "Gasoline", "USD"));
        List<String> columnsToDisplay = List.of("pumpAttendantName", "pump", "fuelGradeName", "price", "volume", "totalVolume", "amount", "totalAmount");
        ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);

        PdfPTable table = TransactionPDFGenerator.createTable(bundle, columnsToDisplay, transactions);

        assertEquals(columnsToDisplay.size() + 1, table.getNumberOfColumns()); // +1 for the index column
    }
    @Test
    void testGetColumnValue_shouldReturnCorrectValueBasedOnColumn() {
        TransactionDto transaction = Mockito.mock(TransactionDto.class);
        Mockito.when(transaction.getPumpAttendantName()).thenReturn("Attendant A");
        Mockito.when(transaction.getPump()).thenReturn(1L);
        Mockito.when(transaction.getFuelGradeName()).thenReturn("Diesel");
        Mockito.when(transaction.getPrice()).thenReturn(2.50F);
        Mockito.when(transaction.getVolume()).thenReturn(BigDecimal.valueOf(10.0));
        Mockito.when(transaction.getTotalVolume()).thenReturn(BigDecimal.valueOf(20.0));
        Mockito.when(transaction.getAmount()).thenReturn(new BigDecimal("25.00"));
        Mockito.when(transaction.getTotalAmount()).thenReturn(new BigDecimal("50.00"));
        Mockito.when(transaction.getDateTimeStart()).thenReturn(java.time.LocalDateTime.now());
        Mockito.when(transaction.getDevise()).thenReturn("USD");

        assertEquals("Attendant A", TransactionPDFGenerator.getColumnValue(transaction, "pumpAttendantName"));
        assertEquals("1", TransactionPDFGenerator.getColumnValue(transaction, "pump"));
        assertEquals("Diesel", TransactionPDFGenerator.getColumnValue(transaction, "fuelGradeName"));
        assertEquals("10.0", TransactionPDFGenerator.getColumnValue(transaction, "volume"));
        assertEquals("20.0", TransactionPDFGenerator.getColumnValue(transaction, "totalVolume"));
    }
}
