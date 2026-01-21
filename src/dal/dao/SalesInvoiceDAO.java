package dal.dao;

import dto.SalesInvoice;
import java.sql.*;

public class SalesInvoiceDAO {
    public int createInvoice(SalesInvoice invoice, Connection conn) throws SQLException {
        String sql = "INSERT INTO sales_invoice (customer_id, created_by, sub_total, discount, grand_total, payment_method) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (invoice.getCustomerId() == 0) {
                ps.setNull(1, Types.INTEGER);
            } else {
                ps.setInt(1, invoice.getCustomerId());
            }

            ps.setInt(2, invoice.getCreatedBy());
            ps.setDouble(3, invoice.getSubTotal());
            ps.setDouble(4, invoice.getDiscount());
            ps.setDouble(5, invoice.getGrandTotal());
            ps.setString(6, invoice.getPaymentMethod());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public SalesInvoice getInvoiceById(int invId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM sales_invoice WHERE inv_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SalesInvoice invoice = new SalesInvoice();
                    invoice.setInvId(rs.getInt("inv_id"));
                    invoice.setCustomerId(rs.getInt("customer_id"));
                    invoice.setCreatedBy(rs.getInt("created_by"));
                    invoice.setSubTotal(rs.getDouble("sub_total"));
                    invoice.setDiscount(rs.getDouble("discount"));
                    invoice.setGrandTotal(rs.getDouble("grand_total"));
                    invoice.setPaymentMethod(rs.getString("payment_method"));
                    invoice.setCreatedAt(rs.getTimestamp("created_at"));
                    return invoice;
                }
            }
        }

        return null;
    }
}