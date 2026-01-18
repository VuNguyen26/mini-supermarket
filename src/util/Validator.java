package util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class Validator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+84|0)[0-9]{9,10}$"
    );

    private static final Pattern BARCODE_PATTERN = Pattern.compile(
        "^[0-9]{8,13}$"
    );

    // ===== String validation =====
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    public static boolean hasMinLength(String str, int minLength) {
        return str != null && str.trim().length() >= minLength;
    }

    public static boolean hasMaxLength(String str, int maxLength) {
        return str != null && str.trim().length() <= maxLength;
    }

    public static boolean isInRange(String str, int minLength, int maxLength) {
        return hasMinLength(str, minLength) && hasMaxLength(str, maxLength);
    }

    // ===== Email validation =====
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // ===== Phone validation =====
    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) return false;
        String cleaned = phone.trim().replaceAll("\\s+", "");
        return PHONE_PATTERN.matcher(cleaned).matches();
    }

    // ===== Barcode validation =====
    public static boolean isValidBarcode(String barcode) {
        if (isNullOrEmpty(barcode)) return false;
        return BARCODE_PATTERN.matcher(barcode.trim()).matches();
    }

    // ===== Number validation =====
    public static boolean isPositiveInteger(Integer value) {
        return value != null && value > 0;
    }

    public static boolean isNonNegativeInteger(Integer value) {
        return value != null && value >= 0;
    }

    public static boolean isPositiveDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isNonNegativeDecimal(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) >= 0;
    }

    // ===== Code validation (alphanumeric + underscore/dash) =====
    public static boolean isValidCode(String code) {
        if (isNullOrEmpty(code)) return false;
        return code.trim().matches("^[A-Za-z0-9_-]{2,20}$");
    }

    // ===== Tax code validation (Vietnam format: 10-13 digits) =====
    public static boolean isValidTaxCode(String taxCode) {
        if (isNullOrEmpty(taxCode)) return true; // optional field
        return taxCode.trim().matches("^[0-9]{10,13}$");
    }

    // ===== Validation error messages =====
    public static String requiredFieldMessage(String fieldName) {
        return fieldName + " không được để trống";
    }

    public static String invalidFormatMessage(String fieldName) {
        return fieldName + " không đúng định dạng";
    }

    public static String minLengthMessage(String fieldName, int minLength) {
        return fieldName + " phải có ít nhất " + minLength + " ký tự";
    }

    public static String maxLengthMessage(String fieldName, int maxLength) {
        return fieldName + " không được vượt quá " + maxLength + " ký tự";
    }

    public static String positiveNumberMessage(String fieldName) {
        return fieldName + " phải là số dương";
    }

    public static String duplicateMessage(String fieldName) {
        return fieldName + " đã tồn tại trong hệ thống";
    }
}
