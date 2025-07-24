package com.teknokote.ess.core.service.export;

import com.teknokote.ess.core.service.impl.transactions.TransactionExcelGenerator;
import com.teknokote.ess.dto.TransactionDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class TransactionExcelGeneratorTest {

    @Test
    void testGenerateExcel() throws IOException {
        // Arrange: Create sample TransactionDto objects
        List<TransactionDto> transactions = Arrays.asList(
                createTransactionDto("John Doe", 1L, "Diesel", 1.5, 10.0, 15.0, 20.0, "USD", LocalDateTime.now()),
                createTransactionDto("Jane Smith", 2L, "Petrol", 2.0, 20.0, 30.0, 40.0, "USD", LocalDateTime.now().plusHours(1))
        );
        List<String> columnsToDisplay = Arrays.asList("pumpAttendantName", "pump", "fuelGradeName", "price", "volume", "totalVolume", "amount", "totalAmount", "dateTimeStart");
        String locale = Locale.ENGLISH.toString();
        String filterSummary = "Test Summary";

        // Act: Generate the Excel file
        byte[] excelData = TransactionExcelGenerator.generateExcel(transactions, columnsToDisplay, locale, filterSummary);

        // Assert: Ensure the generated Excel data is not null and has a positive size
        Assertions.assertNotNull(excelData);
        Assertions.assertTrue(excelData.length > 0);
    }

    private TransactionDto createTransactionDto(String pumpAttendantName, Long pump, String fuelGradeName,
                                                double price, double volume, double totalVolume,
                                                double amount, String devise, LocalDateTime dateTimeStart) {
        TransactionDto dto = new TransactionDto();
        dto.setPumpAttendantName(pumpAttendantName);
        dto.setPump(pump);
        dto.setFuelGradeName(fuelGradeName);
        dto.setPrice((float) price);
        dto.setVolume(BigDecimal.valueOf(volume));
        dto.setTotalVolume(BigDecimal.valueOf(totalVolume));
        dto.setAmount(BigDecimal.valueOf(amount));
        dto.setTotalAmount(BigDecimal.valueOf(amount * price));
        dto.setDevise(devise);
        dto.setDateTimeStart(dateTimeStart);
        return dto;
    }
}