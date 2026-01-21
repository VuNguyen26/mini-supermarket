package dal.dao;

import dto.Payment;
import java.sql.*;

public class PaymentDAO {

    public void createPayment(Payment payment, Connection conn) throws SQLException {
        String sql = "INSERT INTO payment (inv_id, method, amount, status, note) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, payment.getInvId());
            ps.setString(2, payment.getMethod());
            ps.setDouble(3, payment.getAmount());
            ps.setString(4, payment.getStatus());
            ps.setString(5, payment.getNote());
            ps.executeUpdate();
        }
    }
}