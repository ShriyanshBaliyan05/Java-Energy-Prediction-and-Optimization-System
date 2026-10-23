package com.energy.utils;
public class ValidationUtils {

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidDouble(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            Double.parseDouble(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPositiveDouble(String value) {
        return isValidDouble(value) && Double.parseDouble(value.trim()) > 0;
    }

    public static boolean isValidNonNegativeDouble(String value) {
        return isValidDouble(value) && Double.parseDouble(value.trim()) >= 0;
    }

    public static boolean isValidInteger(String value) {
        if (!isNotEmpty(value)) return false;
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPositiveInteger(String value) {
        return isValidInteger(value) && Integer.parseInt(value.trim()) > 0;
    }

    public static double parseDoubleOrZero(String value) {
        if (!isValidDouble(value)) return 0.0;
        return Double.parseDouble(value.trim());
    }

    public static Double parseDoubleOrNull(String value) {
        if (!isValidDouble(value)) return null;
        return Double.parseDouble(value.trim());
    }

    public static int parseIntOrZero(String value) {
        if (!isValidInteger(value)) return 0;
        return Integer.parseInt(value.trim());
    }

    private ValidationUtils() {
    }
}