package dal.dao;

import dal.DBConnection;
import dto.InventoryLot;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryLotDAO {

    public List<InventoryLot> getAllInventoryLots() throws SQLException {
        String sql = """
            SELECT il.lot_id, il.product_id, p.product_name, il.lot_code,
                   il.quantity, il.cost_price, il.expiry_date, il.manufactured_date,
                   il.status, il.created_at
            FROM inventory_lot il
            LEFT JOIN product p ON il.product_id = p.product_id
            ORDER BY il.expiry_date ASC
            """;

        List<InventoryLot> lots = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                InventoryLot lot = new InventoryLot();
                lot.setLotId(rs.getInt("lot_id"));
                lot.setProductId(rs.getInt("product_id"));
                lot.setProductName(rs.getString("product_name"));
                lot.setLotCode(rs.getString("lot_code"));
                lot.setQuantity(rs.getInt("quantity"));
                lot.setCostPrice(rs.getBigDecimal("cost_price"));
                lot.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
                lot.setManufacturedDate(rs.getDate("manufactured_date").toLocalDate());
                lot.setStatus(rs.getString("status"));
                lot.setCreatedAt(rs.getDate("created_at").toLocalDate());
                lots.add(lot);
            }
        }
        return lots;
    }

    public List<InventoryLot> getExpiringLots(int daysAhead) throws SQLException {
        String sql = """
            SELECT il.lot_id, il.product_id, p.product_name, il.lot_code,
                   il.quantity, il.cost_price, il.expiry_date, il.manufactured_date,
                   il.status, il.created_at
            FROM inventory_lot il
            LEFT JOIN product p ON il.product_id = p.product_id
            WHERE il.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
            AND il.quantity > 0
            ORDER BY il.expiry_date ASC
            """;

        List<InventoryLot> lots = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, daysAhead);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    InventoryLot lot = new InventoryLot();
                    lot.setLotId(rs.getInt("lot_id"));
                    lot.setProductId(rs.getInt("product_id"));
                    lot.setProductName(rs.getString("product_name"));
                    lot.setLotCode(rs.getString("lot_code"));
                    lot.setQuantity(rs.getInt("quantity"));
                    lot.setCostPrice(rs.getBigDecimal("cost_price"));
                    lot.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
                    lot.setManufacturedDate(rs.getDate("manufactured_date").toLocalDate());
                    lot.setStatus(rs.getString("status"));
                    lot.setCreatedAt(rs.getDate("created_at").toLocalDate());
                    lots.add(lot);
                }
            }
        }
        return lots;
    }
}
