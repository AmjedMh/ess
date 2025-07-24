package com.teknokote.ess.core.service.Entities;

import com.teknokote.ess.utils.EssUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class EssUtilsTest {

    private WebSocketSession mockSession;
    private HttpHeaders mockHeaders;

    @BeforeEach
    void setUp() {
        // Mock WebSocketSession
        mockSession = mock(WebSocketSession.class);

        // Create and set up HttpHeaders if needed for later tests
        mockHeaders = new HttpHeaders();
        mockHeaders.put("X-Pts-Id", Collections.singletonList("12345"));

    }

    @Test
    void testGetKeyFromWebSocketSession() {

        when(mockSession.getHandshakeHeaders()).thenReturn(mockHeaders);

        // Call the method
        String result = EssUtils.getKeyFromWebSocketSession(mockSession, EssUtils.EnumHeaderAttributes.PTS_ID);

        // Verify result
        assertNotNull(result);
        assertEquals("12345", result);
    }

    @Test
    void testExtractPtsId() {
        String jsonText = "{\"PtsId\": \"ABC123\",}";
        String extractedId = EssUtils.extractPtsId(jsonText);
        assertEquals("ABC123", extractedId);
    }

    @Test
    void testFormattedDuration() {
        assertEquals("01h30mn00s", EssUtils.formattedDuration(Duration.ofMinutes(90)));
        assertEquals("00h10mn05s", EssUtils.formattedDuration(Duration.ofSeconds(605)));
    }

    @Test
    void testFormatAmount_shouldReturnFormattedString() {
        String formattedBigDecimal = EssUtils.formatAmount(new BigDecimal("1234.56"), "USD");
        String formattedFloat = EssUtils.formatAmount(1234.56f, "EUR");

        assertEquals("1 235", formattedBigDecimal);
        assertEquals("1 235", formattedFloat);
    }

    @Test
    void testFormatBigDecimalAmountWithCurrency_shouldReturnProperlyFormattedString() {
        String formattedAmount = EssUtils.formatBigDecimalAmountWithCurrency(new BigDecimal("12345.67"), "USD");
        assertEquals("12 346", formattedAmount);
    }

    @Test
    void testFormatFloatAmountWithCurrency_shouldReturnProperlyFormattedString() {
        String formattedAmount = EssUtils.formatFloatAmountWithCurrency(12345.67f, "EUR");
        assertEquals("12 346", formattedAmount);
    }

    @Test
    void testGetLocaleForCurrency() {
        // Test for known currency codes
        assertEquals(Locale.US, EssUtils.getLocaleForCurrency("USD"));
        assertEquals(Locale.FRANCE, EssUtils.getLocaleForCurrency("EUR"));
        assertEquals(Locale.UK, EssUtils.getLocaleForCurrency("GBP"));
        assertEquals(Locale.JAPAN, EssUtils.getLocaleForCurrency("JPY"));
        assertEquals(Locale.GERMANY, EssUtils.getLocaleForCurrency("CHF"));
        assertEquals(Locale.CANADA, EssUtils.getLocaleForCurrency("CAD"));
        assertEquals(Locale.CANADA_FRENCH, EssUtils.getLocaleForCurrency("AUD"));
        assertEquals(new Locale("en", "IN"), EssUtils.getLocaleForCurrency("INR"));
        assertEquals(Locale.CHINA, EssUtils.getLocaleForCurrency("CNY"));
        assertEquals(new Locale("es", "MX"), EssUtils.getLocaleForCurrency("MXN"));
        assertEquals(new Locale("pt", "BR"), EssUtils.getLocaleForCurrency("BRL"));
        assertEquals(new Locale("en", "ZA"), EssUtils.getLocaleForCurrency("ZAR"));
        assertEquals(new Locale("en", "KE"), EssUtils.getLocaleForCurrency("KES"));
        assertEquals(new Locale("ar", "MA"), EssUtils.getLocaleForCurrency("MAD"));
        assertEquals(new Locale("ar", "TN"), EssUtils.getLocaleForCurrency("TND"));
        assertEquals(new Locale("fr", "CI"), EssUtils.getLocaleForCurrency("XOF"));
        assertEquals(new Locale("fr", "CD"), EssUtils.getLocaleForCurrency("CDF"));
        assertEquals(new Locale("en", "GH"), EssUtils.getLocaleForCurrency("GHS"));
        assertEquals(new Locale("fr", "CM"), EssUtils.getLocaleForCurrency("XAF"));
        assertEquals(new Locale("sv", "SE"), EssUtils.getLocaleForCurrency("SEK"));
        assertEquals(new Locale("no", "NO"), EssUtils.getLocaleForCurrency("NOK"));
        assertEquals(new Locale("da", "DK"), EssUtils.getLocaleForCurrency("DKK"));
        assertEquals(new Locale("en", "SG"), EssUtils.getLocaleForCurrency("SGD"));
        assertEquals(new Locale("ms", "MY"), EssUtils.getLocaleForCurrency("MYR"));
        assertEquals(new Locale("th", "TH"), EssUtils.getLocaleForCurrency("THB"));
        assertEquals(new Locale("en", "PH"), EssUtils.getLocaleForCurrency("PHP"));
        assertEquals(Locale.KOREA, EssUtils.getLocaleForCurrency("KRW"));
        assertEquals(new Locale("ar", "AE"), EssUtils.getLocaleForCurrency("AED"));
        assertEquals(new Locale("pl", "PL"), EssUtils.getLocaleForCurrency("PLN"));
        assertEquals(new Locale("cs", "CZ"), EssUtils.getLocaleForCurrency("CZK"));
        assertEquals(new Locale("hu", "HU"), EssUtils.getLocaleForCurrency("HUF"));
        assertEquals(new Locale("ro", "RO"), EssUtils.getLocaleForCurrency("RON"));
        assertEquals(new Locale("tr", "TR"), EssUtils.getLocaleForCurrency("TRY"));
        assertEquals(new Locale("vi", "VN"), EssUtils.getLocaleForCurrency("VND"));
        assertEquals(new Locale("ar", "EG"), EssUtils.getLocaleForCurrency("EGP"));
        assertEquals(new Locale("si", "LK"), EssUtils.getLocaleForCurrency("LKR"));
        assertEquals(new Locale("is", "IS"), EssUtils.getLocaleForCurrency("ISK"));
        assertEquals(new Locale("hr", "HR"), EssUtils.getLocaleForCurrency("HRK"));
        assertEquals(new Locale("ar", "BH"), EssUtils.getLocaleForCurrency("BHD"));
        assertEquals(new Locale("ar", "OM"), EssUtils.getLocaleForCurrency("OMR"));
        assertEquals(new Locale("ar", "JO"), EssUtils.getLocaleForCurrency("JOD"));
        assertEquals(new Locale("ar", "LB"), EssUtils.getLocaleForCurrency("LBP"));
        assertEquals(new Locale("ar", "IQ"), EssUtils.getLocaleForCurrency("IQD"));
        assertEquals(new Locale("ar", "SY"), EssUtils.getLocaleForCurrency("SYP"));
        assertEquals(new Locale("ar", "YE"), EssUtils.getLocaleForCurrency("YER"));
        assertEquals(new Locale("ne", "NP"), EssUtils.getLocaleForCurrency("NPR"));
        assertEquals(new Locale("zh", "TW"), EssUtils.getLocaleForCurrency("TWD"));

        // Test for unknown currency code
        assertEquals(Locale.getDefault(), EssUtils.getLocaleForCurrency("XYZ")); // Assuming "XYZ" is not in your switch case
    }
}