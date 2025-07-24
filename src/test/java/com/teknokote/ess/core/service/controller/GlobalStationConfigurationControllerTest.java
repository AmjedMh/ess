package com.teknokote.ess.core.service.controller;

import com.teknokote.ess.controller.front.GlobalStationConfigurationController;
import com.teknokote.ess.core.service.impl.TankDeliveryService;
import com.teknokote.ess.core.service.impl.TankMeasurementServices;
import com.teknokote.ess.core.service.impl.transactions.TransactionService;
import com.teknokote.ess.dto.TankFilterDto;
import com.teknokote.ess.dto.TransactionFilterDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;
import static org.springframework.http.MediaType.APPLICATION_PDF;

@ExtendWith(MockitoExtension.class)
 class GlobalStationConfigurationControllerTest {

    @InjectMocks
    private GlobalStationConfigurationController controller;
    @Mock
    private TransactionService transactionService;
    @Mock
    private TankDeliveryService tankDeliveryService;
    @Mock
    private TankMeasurementServices tankMeasurementServices;

    @Test
    void testGenerateExcelOfTransactions() throws IOException {
        Long idCtr = 1L;
        TransactionFilterDto filterDto = new TransactionFilterDto();
        String filterSummary = "summary";
        List<String> columnsToDisplay = Arrays.asList("col1", "col2");
        String locale = "en";

        // Mocking the transaction service to return a dummy byte array
        when(transactionService.generateExcelTransactionsByFilter(idCtr, filterDto, 0, 50, columnsToDisplay, locale, filterSummary)).thenReturn(new byte[10]);

        // Call the method under test
        ResponseEntity<byte[]> response = controller.generateEXCELOfTransactions(idCtr, filterDto, filterSummary, 0, 50, columnsToDisplay, locale);

        // Asserting the response headers
        assertEquals("attachment; filename=transactions.xlsx", response.getHeaders().getFirst(CONTENT_DISPOSITION));
        assertEquals(APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());

        // Verify the correct length of the returned byte array
        assertEquals(10, response.getBody().length);
    }
    @Test
    void testGeneratePDFOfTransactions() throws IOException {
        Long idCtr = 1L;
        TransactionFilterDto filterDto = new TransactionFilterDto();
        String filterSummary = "summary";
        List<String> columnsToDisplay = Arrays.asList("col1", "col2");
        String locale = "en";
        byte[] pdfContent = new byte[]{1, 2, 3, 4, 5};

        when(transactionService.generatePDFTransactionsByFilter(idCtr, filterDto, 0, 50, columnsToDisplay, locale, filterSummary))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> response = controller.generatePDFOfTransactions(idCtr, filterDto, filterSummary, 0, 50, columnsToDisplay, locale);

        assertEquals(APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("attachment; filename=transactions.pdf", response.getHeaders().getFirst(CONTENT_DISPOSITION));
        assertArrayEquals(pdfContent, response.getBody());
    }

    @Test
    void testGenerateEXCELOfDelivery() throws IOException {
        Long idCtr = 1L;
        TankFilterDto filterDto = new TankFilterDto();
        String filterSummary = "summary";
        List<String> columnsToDisplay = Arrays.asList("col1", "col2");
        String locale = "en";
        byte[] excelContent = new byte[]{1, 2, 3, 4, 5};

        when(tankDeliveryService.generateExcelDeliveryByFilter(idCtr, filterDto, 0, 50, columnsToDisplay, locale, filterSummary))
                .thenReturn(excelContent);

        ResponseEntity<byte[]> response = controller.generateEXCELOfDelivery(idCtr, filterDto, filterSummary, 0, 50, columnsToDisplay, locale);

        assertEquals(APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertEquals("attachment; filename=tankDelivery.xlsx", response.getHeaders().getFirst(CONTENT_DISPOSITION));
        assertArrayEquals(excelContent, response.getBody());
    }

    @Test
    void testGeneratePDFOfTankDelivery() throws IOException {
        Long idCtr = 1L;
        TankFilterDto filterDto = new TankFilterDto();
        String filterSummary = "summary";
        List<String> columnsToDisplay = Arrays.asList("col1", "col2");
        String locale = "en";
        byte[] pdfContent = new byte[]{1, 2, 3, 4, 5};

        when(tankDeliveryService.generatePDFTankDeliveryByFilter(idCtr, filterDto, 0, 50, columnsToDisplay, locale, filterSummary))
                .thenReturn(pdfContent);

        ResponseEntity<byte[]> response = controller.generatePDFOfTankDelivery(idCtr, filterDto, filterSummary, 0, 50, columnsToDisplay, locale);

        assertEquals(APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("attachment; filename=transactions.pdf", response.getHeaders().getFirst(CONTENT_DISPOSITION));
        assertArrayEquals(pdfContent, response.getBody());
    }
    @Test
    void testGenerateEXCELOfMeasurement() throws IOException {
        Long idCtr = 1L;
        TankFilterDto filterDto = new TankFilterDto();
        String filterSummary = "summary";
        List<String> columnsToDisplay = Arrays.asList("col1", "col2");
        String locale = "en";
        byte[] excelContent = new byte[]{1, 2, 3, 4, 5};

        // Mock the service call
        when(tankMeasurementServices.generateExcelMeasurementByFilter(
                idCtr, filterDto, 0, 50, columnsToDisplay, locale, filterSummary))
                .thenReturn(excelContent);

        // Call the method under test
        ResponseEntity<byte[]> response = controller.generateEXCELOfMeasurement(idCtr, filterDto, filterSummary, 0, 50, columnsToDisplay, locale);

        // Assertions
        assertEquals(APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertEquals("attachment; filename=tankMeasurement.xlsx", response.getHeaders().getFirst(CONTENT_DISPOSITION));
        assertArrayEquals(excelContent, response.getBody());
    }

    @Test
    void testGeneratePDFOfTankMeasurement() throws IOException {
        Long idCtr = 1L;
        TankFilterDto filterDto = new TankFilterDto();
        String filterSummary = "summary";
        List<String> columnsToDisplay = Arrays.asList("col1", "col2");
        String locale = "en";
        byte[] pdfContent = new byte[]{6, 7, 8, 9, 10};

        // Mock the service call
        when(tankMeasurementServices.generatePDFTankMeasurementByFilter(
                idCtr, filterDto, 0, 50, columnsToDisplay, locale, filterSummary))
                .thenReturn(pdfContent);

        // Call the method under test
        ResponseEntity<byte[]> response = controller.generatePDFOfTankMeasurement(idCtr, filterDto, filterSummary, 0, 50, columnsToDisplay, locale);

        // Assertions
        assertEquals(APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("attachment; filename=tankMeasurement.pdf", response.getHeaders().getFirst(CONTENT_DISPOSITION));
        assertArrayEquals(pdfContent, response.getBody());
    }
}