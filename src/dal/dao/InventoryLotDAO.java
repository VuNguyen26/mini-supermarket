package dal.dao;

import dal.DBConnection;
import dto.InventoryLot;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InventoryLotDAO {

    // ==================================================
    // ========== (HEAD) FOR POS/Payment ==========
    // ==================================================

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
                    lot.setLotId(rs.getLong("lot_id")); // compat method exists
                    lot.setProductId(rs.getInt("product_id"));
                    lot.setQtyRemaining(rs.getInt("qty_remaining"));
                    lot.setExpiryDate(rs.getDate("expiry_date")); // compat method exists
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

    // ==================================================
    // ========== (DUY) FOR Inventory/GoodsReceipt ==========
    // ==================================================

    /**
     * Retrieves all inventory lot with optional filters and sorting.
     *
     * @param productId   the product ID to filter by, or null for all products
     * @param from        filter lots which expired at or after this date, or null
     * @param to          filter lots which expired before this date, or null
     * @param status      filter lots which has this status, or null
     * @param sortBy      the database column used for sorting
     * @param isAscending true for ascending order, false for descending
     * @return a filtered list of inventory lot
     */
    public List<InventoryLot> findFiltered(
            Integer productId,
            LocalDate from,
            LocalDate to,
            String status,
            String sortBy,
            boolean isAscending
    ) {
        // Whitelist sort columns to avoid SQL injection
        String sortColumn = sanitizeSortBy(sortBy);

        StringBuilder sql = new StringBuilder(
                "SELECT il.* " +
                        "FROM inventory_lot il " +
                        "JOIN product p ON il.product_id = p.product_id " +
                        "WHERE 1 = 1 "
        );

        if (productId != null) sql.append("AND il.product_id = ? ");
        if (from != null) sql.append("AND il.expiry_date >= ? ");
        if (to != null) sql.append("AND il.expiry_date < ? ");
        if (status != null) sql.append("AND il.status = ? ");

        sql.append("ORDER BY ").append(sortColumn).append(isAscending ? " ASC " : " DESC ");

        List<InventoryLot> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (productId != null) ps.setInt(paramIndex++, productId);
            if (from != null) ps.setObject(paramIndex++, from);
            if (to != null) ps.setObject(paramIndex++, to);
            if (status != null) ps.setString(paramIndex++, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve inventory lot list: " + e.getMessage(), e);
        }

        return list;
    }

    public boolean updateStatus(Connection con, int lotId, InventoryLot.Status status) {
        String sql = "UPDATE inventory_lot SET status = ? WHERE lot_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, lotId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update status of the inventory lot: " + e.getMessage(), e);
        }
    }

    /**
     * Inserts a new inventory_lot into the database
     *
     * @param con the Connection to use
     * @param lot the InventoryLot to be saved
     * @return true if the insert was successful, false otherwise
     */
    public boolean insert(Connection con, InventoryLot lot) {
        String sql = "INSERT INTO inventory_lot " +
                "(product_id, grd_id, lot_code, received_date, expiry_date, qty_in, qty_out, qty_remaining, out_of_stock_at, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, lot.getProductId());
            ps.setInt(2, lot.getGrdId());
            ps.setString(3, lot.getLotCode());

            ps.setObject(4, lot.getReceivedDate()); // LocalDate
            ps.setObject(5, lot.getExpiry());        // LocalDate

            ps.setInt(6, lot.getQtyIn());
            ps.setInt(7, lot.getQtyOut());
            ps.setInt(8, lot.getQtyRemaining());

            ps.setObject(9, lot.getOutOfStockAt());  // LocalDateTime nullable

            InventoryLot.Status st = lot.getStatusEnum();
            ps.setString(10, st != null ? st.name() : "AVAILABLE");

            ps.setObject(11, lot.getCreatedAt());    // LocalDateTime nullable

            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert new inventory lot: " + e.getMessage(), e);
        }
    }

    private InventoryLot map(ResultSet rs) throws SQLException {
        InventoryLot lot = new InventoryLot();
        lot.setLotId(rs.getInt("lot_id"));
        lot.setProductId(rs.getInt("product_id"));
        lot.setGrdId(rs.getInt("grd_id"));
        lot.setLotCode(rs.getString("lot_code"));

        lot.setReceivedDate(rs.getObject("received_date", LocalDate.class));
        lot.setExpiry(rs.getObject("expiry_date", LocalDate.class));

        lot.setQtyIn(rs.getInt("qty_in"));
        lot.setQtyOut(rs.getInt("qty_out"));
        lot.setQtyRemaining(rs.getInt("qty_remaining"));

        lot.setOutOfStockAt(rs.getObject("out_of_stock_at", LocalDateTime.class));

        String st = rs.getString("status");
        if (st != null) {
            try {
                lot.setStatusEnum(InventoryLot.Status.valueOf(st));
            } catch (Exception ignored) {
                // keep null if unknown
            }
        }

        // created_at might exist
        try {
            lot.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        } catch (SQLException ignored) {
        }

        return lot;
    }

    /**
     * Avoid SQL injection by whitelisting columns for ORDER BY.
     */
    private String sanitizeSortBy(String sortBy) {
        if (sortBy == null) return "il.expiry_date";

        // allow only known columns
        switch (sortBy) {
            case "il.lot_id":
            case "lot_id":
                return "il.lot_id";
            case "il.product_id":
            case "product_id":
                return "il.product_id";
            case "il.received_date":
            case "received_date":
                return "il.received_date";
            case "il.expiry_date":
            case "expiry_date":
                return "il.expiry_date";
            case "il.qty_remaining":
            case "qty_remaining":
                return "il.qty_remaining";
            case "il.status":
            case "status":
                return "il.status";
            case "il.created_at":
            case "created_at":
                return "il.created_at";
            default:
                return "il.expiry_date";
        }
    }
}
