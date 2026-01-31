package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    // ========== (HEAD) DEFAULT PATTERNS ==========
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT_ISO = "yyyy-MM-dd HH:mm:ss";

    // ========== (DUY) FORMATTERS (java.time) ==========
    // Format: 25/12/2023
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    // Format: 25/12/2023 14:30:05
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    // ==================================================
    // ========== FORMAT java.util.Date ==========
    // ==================================================

    public static String formatDate(Date date, String pattern) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static String formatDateToString(Date date) {
        return formatDate(date, DATE_FORMAT);
    }

    public static String formatDateTimeToString(Date date) {
        return formatDate(date, DATETIME_FORMAT);
    }

    public static String formatDateToISO(Date date) {
        return formatDate(date, DATE_FORMAT_ISO);
    }

    // ==================================================
    // ========== PARSE String -> java.util.Date =========
    // ==================================================

    public static Date parseStringToDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseStringToDate(String dateStr) {
        return parseStringToDate(dateStr, DATE_FORMAT);
    }

    public static Date parseStringToDateTime(String dateStr) {
        return parseStringToDate(dateStr, DATETIME_FORMAT);
    }

    public static Date parseISOToDate(String dateStr) {
        return parseStringToDate(dateStr, DATE_FORMAT_ISO);
    }

    // ==================================================
    // ========== NOW ==========
    // ==================================================

    public static Date getCurrentDate() {
        return new Date();
    }

    public static Date getCurrentDateTime() {
        return new Date();
    }

    // ==================================================
    // ========== DATE ARITHMETIC ==========
    // ==================================================

    public static Date addDays(Date date, int days) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }

    public static Date addMonths(Date date, int months) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }

    public static Date addYears(Date date, int years) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, years);
        return cal.getTime();
    }

    public static Date addHours(Date date, int hours) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }

    // ==================================================
    // ========== COMPARE / BETWEEN ==========
    // ==================================================

    public static boolean isBetween(Date date, Date start, Date end) {
        if (date == null || start == null || end == null) return false;
        return !date.before(start) && !date.after(end);
    }

    public static boolean isExpired(Date expiryDate) {
        if (expiryDate == null) return false;
        return expiryDate.before(getCurrentDate());
    }

    public static long daysBetween(Date start, Date end) {
        if (start == null || end == null) return 0;
        long diffInMillis = end.getTime() - start.getTime();
        return diffInMillis / (1000L * 60 * 60 * 24);
    }

    // ==================================================
    // ========== START/END OF DAY/MONTH/YEAR ==========
    // ==================================================

    public static Date getStartOfDay(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getEndOfDay(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static Date getStartOfMonth(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getEndOfMonth(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    public static Date getStartOfYear(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getEndOfYear(Date date) {
        if (date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    // ==================================================
    // ========== VALIDATION / EXTRACT ==========
    // ==================================================

    public static boolean isValidDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isValidDate(String dateStr) {
        return isValidDate(dateStr, DATE_FORMAT);
    }

    public static boolean isDateInPast(Date date) {
        if (date == null) return false;
        return date.before(getCurrentDate());
    }

    public static boolean isDateInFuture(Date date) {
        if (date == null) return false;
        return date.after(getCurrentDate());
    }

    public static boolean isToday(Date date) {
        if (date == null) return false;
        Date today = getStartOfDay(getCurrentDate());
        Date tomorrow = addDays(today, 1);
        return isBetween(date, today, tomorrow);
    }

    public static int getYear(Date date) {
        if (date == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        if (date == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    public static int getDayOfMonth(Date date) {
        if (date == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int compareDatesOnly(Date date1, Date date2) {
        if (date1 == null && date2 == null) return 0;
        if (date1 == null) return -1;
        if (date2 == null) return 1;
        Date d1 = getStartOfDay(date1);
        Date d2 = getStartOfDay(date2);
        return d1.compareTo(d2);
    }

    // ==================================================
    // ========== SQL CONVERSION ==========
    // ==================================================

    public static java.sql.Date toSqlDate(Date date) {
        if (date == null) return null;
        return new java.sql.Date(date.getTime());
    }

    public static Date fromSqlDate(java.sql.Date sqlDate) {
        if (sqlDate == null) return null;
        return new Date(sqlDate.getTime());
    }

    public static java.sql.Timestamp toSqlTimestamp(Date date) {
        if (date == null) return null;
        return new java.sql.Timestamp(date.getTime());
    }

    public static Date fromSqlTimestamp(java.sql.Timestamp timestamp) {
        if (timestamp == null) return null;
        return new Date(timestamp.getTime());
    }

    // ==================================================
    // ========== EXPIRY HELPERS ==========
    // ==================================================

    public static boolean isExpiringSoon(Date expiryDate, int days) {
        if (expiryDate == null) return false;
        Date now = getCurrentDate();
        if (expiryDate.before(now)) return false;
        long daysLeft = daysBetween(now, expiryDate);
        return daysLeft <= days;
    }

    public static long getDaysUntilExpiry(Date expiryDate) {
        if (expiryDate == null) return 0;
        return daysBetween(getCurrentDate(), expiryDate);
    }

    public static ExpiryStatus getExpiryStatus(Date expiryDate) {
        if (expiryDate == null) return ExpiryStatus.UNKNOWN;
        long daysLeft = getDaysUntilExpiry(expiryDate);
        if (daysLeft < 0) return ExpiryStatus.EXPIRED;
        if (daysLeft <= 7) return ExpiryStatus.EXPIRING_SOON;
        return ExpiryStatus.FRESH;
    }

    public enum ExpiryStatus {
        EXPIRED,        // Da het han
        EXPIRING_SOON,  // Sap het han (trong 7 ngay)
        FRESH,          // Con moi
        UNKNOWN         // Khong xac dinh
    }

    // ==================================================
    // ========== (DUY) java.time helpers ==========
    // ==================================================

    /** Format LocalDate to dd/MM/yyyy */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    /** Format LocalDateTime to dd/MM/yyyy HH:mm:ss */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMATTER);
    }

    /** Format LocalDateTime to dd/MM/yyyy (date only) */
    public static String formatDateOnly(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMATTER);
    }

    /** Convert java.util.Date -> LocalDateTime using system default zone */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /** Convert LocalDateTime -> java.util.Date using system default zone */
    public static Date fromLocalDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
