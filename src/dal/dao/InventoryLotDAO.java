package dal.dao;

import dal.DBConnection;
import dto.InventoryLot;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryLotDAO {

    /**
     * Lấy toàn bộ lô hàng tồn kho
     */
    public List<InventoryLot> getAllInventoryLots() throws SQLException {

        String sql = """
            SELECT lot_id,
                   product_id,
                   quantity,
                   remaining_quantity,
                   import_date,
                   expiry_date,
                   import_price
            FROM inventory_lot
            ORDER BY expiry_date ASC
            """;

        List<InventoryLot> lots = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lots.add(mapResultSet(rs));
            }
        }

        return lots;
    }

    /**
     * Lấy các lô hàng sắp hết hạn trong N ngày
     */
    public List<InventoryLot> getExpiringLots(int daysAhead) throws SQLException {

        String sql = """
            SELECT lot_id,
                   product_id,
                   quantity,
                   remaining_quantity,
                   import_date,
                   expiry_date,
                   import_price
            FROM inventory_lot
            WHERE expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY)
              AND remaining_quantity > 0
            ORDER BY expiry_date ASC
            """;

        List<InventoryLot> lots = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, daysAhead);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lots.add(mapResultSet(rs));
                }
            }
        }

        return lots;
    }

    /**
     * Map ResultSet → InventoryLot DTO
     */
    private InventoryLot mapResultSet(ResultSet rs) throws SQLException {

        InventoryLot lot = new InventoryLot();

        lot.setLotId(rs.getLong("lot_id"));
        lot.setProductId(rs.getLong("product_id"));
        lot.setQuantity(rs.getInt("quantity"));
        lot.setRemainingQuantity(rs.getInt("remaining_quantity"));

        Date importDate = rs.getDate("import_date");
        if (importDate != null) {
            lot.setImportDate(importDate.toLocalDate());
        }

        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) {
            lot.setExpiryDate(expiryDate.toLocalDate());
        }

        lot.setImportPrice(rs.getDouble("import_price"));

        return lot;
    }
}
