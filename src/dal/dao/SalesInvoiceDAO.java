package dal.dao;

import dto.SalesInvoice;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesInvoiceDAO {
    public List<SalesInvoice> searchInvoices(Integer invId, String customerName,
                                            Timestamp fromCreated, Timestamp toCreated,
                                            Connection conn) throws SQLException {
        List<SalesInvoice> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT i.*, c.customer_name, u.full_name as created_by_name ")
                .append("FROM sales_invoice i ")
                .append("LEFT JOIN customer c ON i.customer_id = c.customer_id ")
                .append("JOIN user u ON i.created_by = u.user_id ")
                .append("WHERE 1=1 ");

        // Dynamic filters
        List<Object> params = new ArrayList<>();
        if (invId != null) {
            sql.append(" AND i.inv_id = ? ");
            params.add(invId);
        }
        if (fromCreated != null) {
            sql.append(" AND i.created_at >= ? ");
            params.add(fromCreated);
        }
        if (toCreated != null) {
            sql.append(" AND i.created_at <= ? ");
            params.add(toCreated);
        }
        if (customerName != null && !customerName.trim().isEmpty()) {
            // Use COALESCE so "Khách lẻ" (null customer) can still be searched.
            sql.append(" AND COALESCE(c.customer_name, 'Khách lẻ') LIKE ? ");
            params.add("%" + customerName.trim() + "%");
        }

        sql.append(" ORDER BY i.created_at DESC");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                int idx = i + 1;
                if (p instanceof Integer) {
                    ps.setInt(idx, (Integer) p);
                } else if (p instanceof Timestamp) {
                    ps.setTimestamp(idx, (Timestamp) p);
                } else if (p instanceof String) {
                    ps.setString(idx, (String) p);
                } else {
                    ps.setObject(idx, p);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SalesInvoice invoice = new SalesInvoice();
                    invoice.setInvId(rs.getInt("inv_id"));
                    invoice.setCustomerId(rs.getInt("customer_id"));
                    invoice.setCreatedBy(rs.getInt("created_by"));
                    invoice.setSubTotal(rs.getDouble("sub_total"));
                    invoice.setDiscount(rs.getDouble("discount"));
                    invoice.setGrandTotal(rs.getDouble("grand_total"));
                    invoice.setPaymentMethod(rs.getString("payment_method"));
                    invoice.setCreatedAt(rs.getTimestamp("created_at"));

                    invoice.setCustomerName(
                            rs.getString("customer_name") != null ? rs.getString("customer_name") : "Khách lẻ");
                    invoice.setCreatedByName(rs.getString("created_by_name"));

                    list.add(invoice);
                }
            }
        }

        return list;
    }

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

    public List<SalesInvoice> getAllInvoices(Connection conn) throws SQLException {
        List<SalesInvoice> list = new ArrayList<>();
        String sql = "SELECT i.*, c.customer_name, u.full_name as created_by_name " +
                "FROM sales_invoice i " +
                "LEFT JOIN customer c ON i.customer_id = c.customer_id " +
                "JOIN user u ON i.created_by = u.user_id " +
                "ORDER BY i.created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SalesInvoice invoice = new SalesInvoice();
                invoice.setInvId(rs.getInt("inv_id"));
                invoice.setCustomerId(rs.getInt("customer_id"));
                invoice.setCreatedBy(rs.getInt("created_by"));
                invoice.setSubTotal(rs.getDouble("sub_total"));
                invoice.setDiscount(rs.getDouble("discount"));
                invoice.setGrandTotal(rs.getDouble("grand_total"));
                invoice.setPaymentMethod(rs.getString("payment_method"));
                invoice.setCreatedAt(rs.getTimestamp("created_at"));

                invoice.setCustomerName(
                        rs.getString("customer_name") != null ? rs.getString("customer_name") : "Khách lẻ");
                invoice.setCreatedByName(rs.getString("created_by_name"));

                list.add(invoice);
            }
        }
        return list;
    }
}