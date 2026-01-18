package util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * MoneyUtils - Tiện ích format tiền tệ
 */
public class MoneyUtils {
    
    private static final DecimalFormat formatter = new DecimalFormat("#,###");
    
    public static String format(Double amount) {
        if (amount == null) return "0";
        return formatter.format(amount) + " đ";
    }
    
    public static String format(Integer amount) {
        if (amount == null) return "0";
        return formatter.format(amount) + " đ";
    }
    
    public static String format(Long amount) {
        if (amount == null) return "0";
        return formatter.format(amount) + " đ";
    }
    
    public static String format(BigDecimal amount) {
        if (amount == null) return "0";
        return formatter.format(amount) + " đ";
    }
}