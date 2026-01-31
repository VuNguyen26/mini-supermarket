package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    
    // Cac dinh dang ngay thang mac dinh
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATE_FORMAT_ISO = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT_ISO = "yyyy-MM-dd HH:mm:ss";
    
    // DINH DANG NGAY THANG
    
    // Dinh dang ngay theo pattern tu chon
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }
    
    // Dinh dang ngay sang chuoi (dd/MM/yyyy)
    public static String formatDateToString(Date date) {
        return formatDate(date, DATE_FORMAT);
    }
    
    // Dinh dang ngay gio sang chuoi (dd/MM/yyyy HH:mm:ss)
    public static String formatDateTimeToString(Date date) {
        return formatDate(date, DATETIME_FORMAT);
    }
    
    // Dinh dang ngay sang ISO (yyyy-MM-dd)
    public static String formatDateToISO(Date date) {
        return formatDate(date, DATE_FORMAT_ISO);
    }
    
    // CHUYEN DOI CHUOI SANG NGAY
    
    // Chuyen chuoi sang ngay theo pattern tu chon
    public static Date parseStringToDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
    
    // Chuyen chuoi sang ngay (dd/MM/yyyy)
    public static Date parseStringToDate(String dateStr) {
        return parseStringToDate(dateStr, DATE_FORMAT);
    }
    
    // Chuyen chuoi sang ngay gio (dd/MM/yyyy HH:mm:ss)
    public static Date parseStringToDateTime(String dateStr) {
        return parseStringToDate(dateStr, DATETIME_FORMAT);
    }
    
    // Chuyen chuoi ISO sang ngay (yyyy-MM-dd)
    public static Date parseISOToDate(String dateStr) {
        return parseStringToDate(dateStr, DATE_FORMAT_ISO);
    }
    
    // LAY NGAY HIEN TAI
    
    // Lay ngay hien tai
    public static Date getCurrentDate() {
        return new Date();
    }
    
    // Lay ngay gio hien tai
    public static Date getCurrentDateTime() {
        return new Date();
    }
    
    // THAO TAC VOI NGAY THANG
    
    // Them hoac bot ngay
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_MONTH, days);
        return cal.getTime();
    }
    
    // Them hoac bot thang
    public static Date addMonths(Date date, int months) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, months);
        return cal.getTime();
    }
    
    // Them hoac bot nam
    public static Date addYears(Date date, int years) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, years);
        return cal.getTime();
    }
    
    // Them hoac bot gio
    public static Date addHours(Date date, int hours) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR_OF_DAY, hours);
        return cal.getTime();
    }
    
    //SO SANH NGAY THANG
    
    // Kiem tra ngay co nam trong khoang start va end khong
    public static boolean isBetween(Date date, Date start, Date end) {
        if (date == null || start == null || end == null) {
            return false;
        }
        return !date.before(start) && !date.after(end);
    }
    
    // Kiem tra da het han chua
    public static boolean isExpired(Date expiryDate) {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.before(getCurrentDate());
    }
    
    // Tinh so ngay giua 2 ngay
    public static long daysBetween(Date start, Date end) {
        if (start == null || end == null) {
            return 0;
        }
        long diffInMillis = end.getTime() - start.getTime();
        return diffInMillis / (1000 * 60 * 60 * 24);
    }
    
    //LAY DAU/CUOI NGAY, THANG, NAM
    
    // Lay dau ngay (00:00:00)
    public static Date getStartOfDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    // Lay cuoi ngay (23:59:59)
    public static Date getEndOfDay(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    
    // Lay dau thang
    public static Date getStartOfMonth(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
    // Lay cuoi thang
    public static Date getEndOfMonth(Date date) {
        if (date == null) {
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }
    
    // Lay dau nam
    public static Date getStartOfYear(Date date) {
        if (date == null) {
            return null;
        }
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
    
    // Lay cuoi nam
    public static Date getEndOfYear(Date date) {
        if (date == null) {
            return null;
        }
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
    
    //KIEM TRA TINH HOP LE
    
    // Kiem tra chuoi co phai ngay hop le khong (theo pattern)
    public static boolean isValidDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            sdf.setLenient(false);
            sdf.parse(dateStr);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
    
    // Kiem tra chuoi co phai ngay hop le khong (dd/MM/yyyy)
    public static boolean isValidDate(String dateStr) {
        return isValidDate(dateStr, DATE_FORMAT);
    }
    
    // Kiem tra ngay da qua chua
    public static boolean isDateInPast(Date date) {
        if (date == null) {
            return false;
        }
        return date.before(getCurrentDate());
    }
    
    // Kiem tra ngay con trong tuong lai khong
    public static boolean isDateInFuture(Date date) {
        if (date == null) {
            return false;
        }
        return date.after(getCurrentDate());
    }
    
    // Kiem tra co phai hom nay khong
    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }
        Date today = getStartOfDay(getCurrentDate());
        Date tomorrow = addDays(today, 1);
        return isBetween(date, today, tomorrow);
    }
    
    //TRICH XUAT THONG TIN NGAY THANG
    
    // Lay nam tu ngay
    public static int getYear(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
    
    // Lay thang tu ngay (1-12)
    public static int getMonth(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }
    
    // Lay ngay trong thang
    public static int getDayOfMonth(Date date) {
        if (date == null) {
            return 0;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }
    
    // So sanh 2 ngay (bo qua gio phut giay)
    public static int compareDatesOnly(Date date1, Date date2) {
        if (date1 == null && date2 == null) {
            return 0;
        }
        if (date1 == null) {
            return -1;
        }
        if (date2 == null) {
            return 1;
        }
        Date d1 = getStartOfDay(date1);
        Date d2 = getStartOfDay(date2);
        return d1.compareTo(d2);
    }
    
    //CHUYEN DOI SQL DATE
    
    // Chuyen java.util.Date sang java.sql.Date
    public static java.sql.Date toSqlDate(Date date) {
        if (date == null) {
            return null;
        }
        return new java.sql.Date(date.getTime());
    }
    
    // Chuyen java.sql.Date sang java.util.Date
    public static Date fromSqlDate(java.sql.Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        return new Date(sqlDate.getTime());
    }
    
    // Chuyen java.util.Date sang java.sql.Timestamp
    public static java.sql.Timestamp toSqlTimestamp(Date date) {
        if (date == null) {
            return null;
        }
        return new java.sql.Timestamp(date.getTime());
    }
    
    // Chuyen java.sql.Timestamp sang java.util.Date
    public static Date fromSqlTimestamp(java.sql.Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Date(timestamp.getTime());
    }
    
    // KIEM TRA HAN SU DUNG SAN PHAM
    
    // Kiem tra san pham sap het han trong X ngay
    public static boolean isExpiringSoon(Date expiryDate, int days) {
        if (expiryDate == null) {
            return false;
        }
        Date now = getCurrentDate();
        if (expiryDate.before(now)) {
            return false;
        }
        long daysLeft = daysBetween(now, expiryDate);
        return daysLeft <= days;
    }
    
    // Lay so ngay con lai den han (so am neu da het han)
    public static long getDaysUntilExpiry(Date expiryDate) {
        if (expiryDate == null) {
            return 0;
        }
        return daysBetween(getCurrentDate(), expiryDate);
    }
    
    // Trang thai han su dung
    public static ExpiryStatus getExpiryStatus(Date expiryDate) {
        if (expiryDate == null) {
            return ExpiryStatus.UNKNOWN;
        }
        long daysLeft = getDaysUntilExpiry(expiryDate);
        if (daysLeft < 0) {
            return ExpiryStatus.EXPIRED;
        } else if (daysLeft <= 7) {
            return ExpiryStatus.EXPIRING_SOON;
        } else {
            return ExpiryStatus.FRESH;
        }
    }
    
    // Enum trang thai han su dung
    public enum ExpiryStatus {
        EXPIRED,        // Da het han
        EXPIRING_SOON,  // Sap het han (trong 7 ngay)
        FRESH,          // Con moi
        UNKNOWN         // Khong xac dinh
    }
}
