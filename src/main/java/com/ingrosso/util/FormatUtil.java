package com.ingrosso.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtil {
    private static final Locale LOCALE_IT = Locale.ITALY;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATETIME_FULL_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final DecimalFormat CURRENCY_FORMAT;
    private static final DecimalFormat QUANTITY_FORMAT;
    private static final DecimalFormat PERCENTAGE_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(LOCALE_IT);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        CURRENCY_FORMAT = new DecimalFormat("#,##0.00", symbols);
        QUANTITY_FORMAT = new DecimalFormat("#,##0.###", symbols);
        PERCENTAGE_FORMAT = new DecimalFormat("#,##0.00", symbols);
    }

    private FormatUtil() {}

    // Date formatting
    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : "";
    }

    public static String formatDateTimeFull(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FULL_FORMAT) : "";
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMAT) : "";
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    public static LocalDateTime parseDateTime(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return LocalDateTime.parse(text.trim(), DATETIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    // Number formatting
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "";
        return CURRENCY_FORMAT.format(amount) + " \u20AC";
    }

    public static String formatCurrencyNoSymbol(BigDecimal amount) {
        if (amount == null) return "";
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatQuantity(BigDecimal quantity) {
        if (quantity == null) return "";
        return QUANTITY_FORMAT.format(quantity);
    }

    public static String formatQuantity(BigDecimal quantity, int decimals) {
        if (quantity == null) return "";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(LOCALE_IT);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('.');

        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimals > 0) {
            pattern.append(".");
            pattern.append("#".repeat(decimals));
        }
        DecimalFormat format = new DecimalFormat(pattern.toString(), symbols);
        return format.format(quantity);
    }

    public static String formatPercentage(BigDecimal percentage) {
        if (percentage == null) return "";
        return PERCENTAGE_FORMAT.format(percentage) + " %";
    }

    public static String formatInteger(int value) {
        return NumberFormat.getIntegerInstance(LOCALE_IT).format(value);
    }

    // Parsing
    public static BigDecimal parseCurrency(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            String cleaned = text.replaceAll("[^0-9,.-]", "")
                    .replace(".", "")
                    .replace(",", ".");
            return new BigDecimal(cleaned).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return null;
        }
    }

    public static BigDecimal parseQuantity(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            String cleaned = text.replaceAll("[^0-9,.-]", "")
                    .replace(".", "")
                    .replace(",", ".");
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            String cleaned = text.replaceAll("[^0-9-]", "");
            return Integer.parseInt(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    // String utilities
    public static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static String padLeft(String text, int length, char padChar) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return String.valueOf(padChar).repeat(length - text.length()) + text;
    }

    public static String padRight(String text, int length, char padChar) {
        if (text == null) text = "";
        if (text.length() >= length) return text;
        return text + String.valueOf(padChar).repeat(length - text.length());
    }

    // Code generation
    public static String generateCode(String prefix, int number, int digits) {
        return prefix + padLeft(String.valueOf(number), digits, '0');
    }
}
