package bus;

import dal.dao.CustomerDAO;
import dal.dao.LoyaltyPointTxnDAO;
import dal.dao.SalesInvoiceDAO;
import dto.LoyaltyPointTxn;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service quản lý hệ thống điểm thưởng khách hàng
 * 
 * Quy tắc:
 * - Kiếm điểm: 1 điểm cho mỗi 10,000 VND chi tiêu
 * - Đổi điểm: 1 điểm = giảm 0.5% tổng hóa đơn
 */
public class LoyaltyService {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final LoyaltyPointTxnDAO loyaltyPointTxnDAO = new LoyaltyPointTxnDAO();
    private final SalesInvoiceDAO invoiceDAO = new SalesInvoiceDAO();
    
    // Tỷ lệ tích điểm: 1 điểm cho mỗi 10,000 VND
    private static final double EARN_RATE_MONEY = 10000.0;
    private static final int EARN_RATE_POINTS = 1;
    
    // Tỷ lệ đổi điểm: 1 điểm = 0.5% giảm giá
    private static final int REDEEM_RATE_POINTS = 1;
    private static final double REDEEM_RATE_PERCENT = 0.5;
    
    /**
     * Tính số điểm khách hàng kiếm được từ hóa đơn
     * 
     * @param subtotal Tổng tiền hóa đơn (trước giảm giá)
     * @return Số điểm kiếm được
     */
    public int calculatePointsEarned(double subtotal) {
        return (int) (subtotal / EARN_RATE_MONEY);
    }
    
    /**
     * Tính số tiền giảm khi đổi điểm
     * 
     * @param points Số điểm muốn đổi
     * @param subtotal Tổng tiền hóa đơn (để tính % giảm)
     * @return Số tiền được giảm
     */
    public double calculateDiscount(int points, double subtotal) {
        if (points <= 0 || subtotal <= 0) {
            return 0.0;
        }
        double discountPercent = points * REDEEM_RATE_PERCENT;
        return subtotal * (discountPercent / 100.0);
    }
    
    /**
     * Xử lý toàn bộ logic điểm thưởng cho một hóa đơn
     * 
     * @param customerId ID khách hàng
     * @param invoiceId ID hóa đơn
     * @param subtotal Tổng tiền hóa đơn
     * @param redeemedPoints Số điểm khách hàng muốn đổi
     * @param pointsValue Số tiền giảm tương ứng
     * @param createdBy User tạo giao dịch
     * @param conn Database connection (đang trong transaction)
     * @return true nếu thành công
     * @throws SQLException
     */
    public boolean processLoyaltyForSale(int customerId, int invoiceId, double subtotal,
                                         int redeemedPoints, double pointsValue, int createdBy,
                                         Connection conn) throws SQLException {
        if (customerId == 0) {
            // Khách lẻ không tích điểm
            return true;
        }
        
        int pointsEarned = calculatePointsEarned(subtotal);
        int normalizedRedeemedPoints = Math.max(0, redeemedPoints);
        
        // 1. Trừ điểm đã đổi (nếu có)
        if (normalizedRedeemedPoints > 0) {
            boolean consumed = customerDAO.consumeLoyaltyPointsInTransaction(customerId,
                    normalizedRedeemedPoints, conn);
            if (!consumed) {
                throw new SQLException("Điểm tích lũy không đủ để đổi khuyến mãi.");
            }
            
            // Ghi giao dịch REDEEM
            recordRedeemTransaction(customerId, invoiceId, normalizedRedeemedPoints, 
                                    pointsValue, createdBy, conn);
        }
        
        // 2. Cộng điểm kiếm được (nếu có)
        if (pointsEarned > 0) {
            boolean added = customerDAO.addLoyaltyPointsInTransaction(customerId, pointsEarned, conn);
            if (!added) {
                throw new SQLException("Không thể cộng điểm tích lũy cho khách hàng.");
            }
            
            // Ghi giao dịch EARN
            recordEarnTransaction(customerId, invoiceId, pointsEarned, subtotal, createdBy, conn);
        }
        
        // 3. Cập nhật tổng kết điểm vào sales_invoice
        invoiceDAO.updateLoyaltySummary(invoiceId, pointsEarned, normalizedRedeemedPoints,
                Math.max(0, pointsValue), conn);
        
        return true;
    }
    
    /**
     * Ghi giao dịch đổi điểm (REDEEM)
     */
    private void recordRedeemTransaction(int customerId, int invoiceId, int points,
                                         double moneyAmount, int createdBy, Connection conn) 
                                         throws SQLException {
        LoyaltyPointTxn redeemTxn = new LoyaltyPointTxn();
        redeemTxn.setCustomerId(customerId);
        redeemTxn.setInvId(invoiceId);
        redeemTxn.setCreatedBy(createdBy);
        redeemTxn.setType("REDEEM");
        redeemTxn.setPoints(-points);
        redeemTxn.setMoneyAmount(Math.max(0, moneyAmount));
        redeemTxn.setEarnRateMoney(0);
        redeemTxn.setEarnRatePoints(0);
        redeemTxn.setRedeemRatePoints(REDEEM_RATE_POINTS);
        redeemTxn.setRedeemRateMoney(REDEEM_RATE_PERCENT);
        redeemTxn.setNote("Đổi điểm giảm giá (1 điểm = 0.5%)");
        loyaltyPointTxnDAO.createTxn(redeemTxn, conn);
    }
    
    /**
     * Ghi giao dịch tích điểm (EARN)
     */
    private void recordEarnTransaction(int customerId, int invoiceId, int points,
                                       double moneyAmount, int createdBy, Connection conn) 
                                       throws SQLException {
        LoyaltyPointTxn earnTxn = new LoyaltyPointTxn();
        earnTxn.setCustomerId(customerId);
        earnTxn.setInvId(invoiceId);
        earnTxn.setCreatedBy(createdBy);
        earnTxn.setType("EARN");
        earnTxn.setPoints(points);
        earnTxn.setMoneyAmount(moneyAmount);
        earnTxn.setEarnRateMoney(EARN_RATE_MONEY);
        earnTxn.setEarnRatePoints(EARN_RATE_POINTS);
        earnTxn.setRedeemRatePoints(0);
        earnTxn.setRedeemRateMoney(0);
        earnTxn.setNote("Tích điểm theo hóa đơn");
        loyaltyPointTxnDAO.createTxn(earnTxn, conn);
    }
}
