package dal.dao;

import dto.SalesInvoiceDetail;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalesInvoiceDetailDAO {
    public void createDetail(SalesInvoiceDetail detail, Connection conn) throws SQLException {
        String sql = "INSERT INTO sales_invoice_detail (inv_id, product_id, lot_id, qty, unit_price, line_total) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, detail.getInvId());
            ps.setInt(2, detail.getProductId());
            ps.setLong(3, detail.getLotId());
            ps.setInt(4, detail.getQty());
            ps.setDouble(5, detail.getUnitPrice());
            ps.setDouble(6, detail.getLineTotal());
            ps.executeUpdate();
        }
    }

    public List<SalesInvoiceDetail> getDetailsByInvoiceId(int invId, Connection conn) throws SQLException {
        List<SalesInvoiceDetail> list = new ArrayList<>();
        String sql = "SELECT d.*, p.product_name " +
                "FROM sales_invoice_detail d " +
                "JOIN product p ON d.product_id = p.product_id " +
                "WHERE d.inv_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SalesInvoiceDetail detail = new SalesInvoiceDetail();
                    detail.setinvDetailId(rs.getInt("invd_id"));
                    detail.setInvId(rs.getInt("inv_id"));
                    detail.setProductId(rs.getInt("product_id"));
                    detail.setLotId(rs.getLong("lot_id"));
                    detail.setQty(rs.getInt("qty"));
                    detail.setUnitPrice(rs.getDouble("unit_price"));
                    detail.setLineTotal(rs.getDouble("line_total"));
                    detail.setProductName(rs.getString("product_name"));

                    list.add(detail);
                }
            }
        }
        return list;
    }
}