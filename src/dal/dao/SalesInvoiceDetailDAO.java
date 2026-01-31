package dal.dao;

import dto.SalesInvoiceDetail;
import java.sql.*;

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
}