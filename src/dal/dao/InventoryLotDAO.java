package dal.dao;

import dto.InventoryLot;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryLotDAO {
    public List<InventoryLot> getAvailableLotsByProduct(int productId, Connection conn) throws SQLException {
        return getAvailableLotsByProduct(productId, conn, false);
    }

    public List<InventoryLot> getAvailableLotsByProductForUpdate(int productId, Connection conn) throws SQLException {
        return getAvailableLotsByProduct(productId, conn, true);
    }

    private List<InventoryLot> getAvailableLotsByProduct(int productId, Connection conn, boolean forUpdate) throws SQLException {
        List<InventoryLot> list = new ArrayList<>();

        String sql = "SELECT lot_id, product_id, qty_remaining, expiry_date "
                   + "FROM inventory_lot "
                   + "WHERE product_id = ? AND status = 'AVAILABLE' AND qty_remaining > 0 "
                   + "ORDER BY expiry_date ASC" + (forUpdate ? " FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryLot lot = new InventoryLot();
                    lot.setLotId(rs.getLong("lot_id"));
                    lot.setProductId(rs.getInt("product_id"));
                    lot.setQtyRemaining(rs.getInt("qty_remaining"));
                    lot.setExpiryDate(rs.getDate("expiry_date"));
                    list.add(lot);
                }
            }
        }
        return list;
    }

    public void updateQtyRemaining(long lotId, int newQty, Connection conn) throws SQLException {
        String sql = "UPDATE inventory_lot SET qty_remaining = ? WHERE lot_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQty);
            ps.setLong(2, lotId);
            ps.executeUpdate();
        }
    }

    public boolean decreaseQtyRemaining(long lotId, int qtyToDecrease, Connection conn) throws SQLException {
        if (qtyToDecrease <= 0) return true;
        String sql = "UPDATE inventory_lot "
                   + "SET qty_remaining = qty_remaining - ? "
                   + "WHERE lot_id = ? AND qty_remaining >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qtyToDecrease);
            ps.setLong(2, lotId);
            ps.setInt(3, qtyToDecrease);
            return ps.executeUpdate() > 0;
        }
    }
}