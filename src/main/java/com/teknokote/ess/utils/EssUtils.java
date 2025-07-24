package com.teknokote.ess.utils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class EssUtils
{

   public static final String PTS_ID_REGEX = "\"PtsId\"\\s*:\\s*\"([^\"]+)\"";
   private static final String PACKET_ID_REGEX = "\"Id\"\\s*:\\s*(\\d+)";
   private static final String TYPE_REGEX = "\"Type\"\\s*:\\s*\"([^\"]+)\"";
   public enum  EnumHeaderAttributes{
      PTS_ID("X-Pts-Id"),
      FIRMWARE_VERSION_DATE_TIME("X-Pts-Firmware-Version-DateTime"),
      CONFIGURATION_ID("X-Pts-Configuration-Identifier");

      @Getter
      private String attributeName;

      EnumHeaderAttributes(String attribute){
         attributeName = attribute;
      }
   }

   /**
    * Remonte la valeur de l'attribut selon son nom {@param attribute} dans ka session websocket.
    */
   public static String getKeyFromWebSocketSession(WebSocketSession session, EnumHeaderAttributes attribute) {
      final HttpHeaders handshakeHeaders = session.getHandshakeHeaders();
      for (Map.Entry<String, List<String>> entry : handshakeHeaders.entrySet()) {
         String key = entry.getKey();
         List<String> values = entry.getValue();
         log.info(key + ": " + String.join(", ", values));
      }
      log.info("*************************************************");
      List<String> attributeValuesList = handshakeHeaders.get(attribute.getAttributeName());
      if (attributeValuesList != null && !attributeValuesList.isEmpty()) {
         return attributeValuesList.get(0);
      } else {
         return null;
      }
   }


   /**
    * Extrait le PtsId du contrôleur du text
    */
   public static String extractPtsId(String text){
      final Pattern pattern = Pattern.compile(PTS_ID_REGEX);
      final Matcher matcher = pattern.matcher(text);
      if(matcher.find()){
         return matcher.group(1);
      }
      return null;
   }
   public static String extractPacketId(String text){
      final Pattern pattern = Pattern.compile(PACKET_ID_REGEX);
      final Matcher matcher = pattern.matcher(text);
      if (matcher.find()) {
         // Le groupe 1 correspond au numéro d'Id
         return matcher.group(1);
      }
      return null;
   }
   public static String extractType(String text){
      final Pattern pattern = Pattern.compile(TYPE_REGEX);
      final Matcher matcher = pattern.matcher(text);
      if (matcher.find()) {
         // Le groupe 1 correspond au numéro d'Id
         return matcher.group(1);
      }
      return null;
   }

   public static String formattedDuration(Duration duration) {
      long hours = duration.toHours();
      long minutes = duration.toMinutes() % 60;
      long seconds = duration.getSeconds() % 60;
      return String.format("%02dh%02dmn%02ds", hours, minutes, seconds);
   }
   public static String formatVolume(Double volume) {
      if (volume == null) {
         return "-";
      }

      String formattedVolume = String.format("%,.3f", volume).replace(".", ",");

      if (formattedVolume.endsWith(",000")) {
         formattedVolume = formattedVolume.substring(0, formattedVolume.length() - 4);
      }

      formattedVolume = formattedVolume.replaceAll("\\s+", " ")
              .trim() // Trim leading/trailing spaces
              .replaceAll("(\\d{1,3}(?:\\s\\d{1,3})?)", "$1");

      return formattedVolume;
   }



   public static String formatHeight(Double height) {
      if (height == null) {
         return "-";
      }

      String formattedHeight = String.format("%,.1f", height).replace(".", ",");

      if (formattedHeight.endsWith(",0")) {
         formattedHeight = formattedHeight.substring(0, formattedHeight.length() - 2);
      }
      formattedHeight = formattedHeight.replace(",", " ");

      formattedHeight = formattedHeight.replaceAll("\\s+", " ")
              .trim() // Trim leading/trailing spaces
              .replaceAll("(\\d{1,3}(?:\\s\\d{1,3})?)", "$1");

      return formattedHeight;
   }

   public static String formatTemperature(Double temperature) {
      if (temperature == null) {
         return "-";
      }

     return String.format("%,.1f °C", temperature).replace(".", ",");
   }

   public static String formatAmount(Number amount, String currencyCode) {
      if (amount instanceof BigDecimal) {
         return formatBigDecimalAmountWithCurrency((BigDecimal) amount, currencyCode);
      } else if (amount instanceof Float || amount instanceof Double) {
         return formatFloatAmountWithCurrency((float) amount, currencyCode);
      }
      return "";
   }

   public static String formatBigDecimalAmountWithCurrency(BigDecimal amount, String currencyCode) {
      Locale locale = getLocaleForCurrency(currencyCode);
      // Use DecimalFormat for better control over the formatting
      java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(locale);

      // Manually set the thousands separator to a space
      symbols.setGroupingSeparator(' ');

      java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###", symbols);
      return formatter.format(amount);
   }

   public static String formatFloatAmountWithCurrency(float amount, String currencyCode) {
      Locale locale = getLocaleForCurrency(currencyCode);
      // Use DecimalFormat for better control over the formatting
      java.text.DecimalFormatSymbols symbols = new java.text.DecimalFormatSymbols(locale);

      // Manually set the thousands separator to a space
      symbols.setGroupingSeparator(' ');

      java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,###", symbols);
      return formatter.format(amount);
   }
   public static Locale getLocaleForCurrency(String currencyCode) {
      switch (currencyCode) {
         case "USD":
            return Locale.US;
         case "EUR":
            return Locale.FRANCE;
         case "GBP":
            return Locale.UK;
         case "JPY":
            return Locale.JAPAN;
         case "CHF":
            return Locale.GERMANY;
         case "CAD":
            return Locale.CANADA;
         case "AUD":
            return Locale.CANADA_FRENCH;
         case "INR":
            return new Locale("en", "IN");
         case "CNY":
            return Locale.CHINA;
         case "MXN":
            return new Locale("es", "MX");
         case "BRL":
            return new Locale("pt", "BR");
         case "ZAR":
            return new Locale("en", "ZA");
         case "KES":
            return new Locale("en", "KE");
         case "NGN":
            return new Locale("en", "NG");
         case "MAD":
            return new Locale("ar", "MA");
         case "TND":
            return new Locale("ar", "TN");
         case "XOF":
            return new Locale("fr", "CI");
         case "CDF":
            return new Locale("fr", "CD");
         case "GHS":
            return new Locale("en", "GH");
         case "XAF":
            return new Locale("fr", "CM");
         case "SEK":
            return new Locale("sv", "SE");
         case "NOK":
            return new Locale("no", "NO");
         case "DKK":
            return new Locale("da", "DK");
         case "SGD":
            return new Locale("en", "SG");
         case "MYR":
            return new Locale("ms", "MY");
         case "THB":
            return new Locale("th", "TH");
         case "PHP":
            return new Locale("en", "PH");
         case "KRW":
            return Locale.KOREA;
         case "AED":
            return new Locale("ar", "AE");
         case "PLN":
            return new Locale("pl", "PL");
         case "CZK":
            return new Locale("cs", "CZ");
         case "HUF":
            return new Locale("hu", "HU");
         case "RON":
            return new Locale("ro", "RO");
         case "TRY":
            return new Locale("tr", "TR");
         case "VND":
            return new Locale("vi", "VN");
         case "EGP":
            return new Locale("ar", "EG");
         case "LKR":
            return new Locale("si", "LK");
         case "ISK":
            return new Locale("is", "IS");
         case "HRK":
            return new Locale("hr", "HR");
         case "BHD":
            return new Locale("ar", "BH");
         case "OMR":
            return new Locale("ar", "OM");
         case "JOD":
            return new Locale("ar", "JO");
         case "LBP":
            return new Locale("ar", "LB");
         case "IQD":
            return new Locale("ar", "IQ");
         case "SYP":
            return new Locale("ar", "SY");
         case "YER":
            return new Locale("ar", "YE");
         case "NPR":
            return new Locale("ne", "NP");
         case "TWD":
            return new Locale("zh", "TW");
         default:
            return Locale.getDefault();
      }
   }
}
