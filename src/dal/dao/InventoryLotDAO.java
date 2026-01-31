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
    // ========== FOR POS/Payment (use existing Connection) ==========
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
                + "ORDER BY expiry_date ASC"
                + (forUpdate ? " FOR UPDATE" : "");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InventoryLot lot = new InventoryLot();

                    // lot_id can be int/long depending on DTO -> use long then DTO can cast/overload
                    long lotId = rs.getLong("lot_id");
                    lot.setLotId(lotId);

                    lot.setProductId(rs.getInt("product_id"));
                    lot.setQtyRemaining(rs.getInt("qty_remaining"));

                    // expiry_date -> LocalDate preferred, but keep compatibility if DTO has setExpiryDate(Date)
                    Date exp = rs.getDate("expiry_date");
                    if (exp != null) {
                        // if DTO supports LocalDate:
                        try {
                            lot.setExpiry(exp.toLocalDate());
                        } catch (Exception ignore) {
                            // fallback if DTO supports java.sql.Date:
                            lot.setExpiryDate(exp);
                        }
                    }

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
    // ========== FOR Inventory/GoodsReceipt (DUY style) ==========
    // ==================================================

    /**
     * Retrieves inventory lots with optional filters and sorting.
     *
     * @param productId   filter by product_id, or null for all
     * @param from        filter expiry_date >= from, or null
     * @param to          filter expiry_date < to, or null
     * @param status      filter status, or null
     * @param sortBy      column for sorting (whitelisted)
     * @param isAscending true ASC, false DESC
     */
    public List<InventoryLot> findFiltered(
            Integer productId,
            LocalDate from,
            LocalDate to,
            String status,
            String sortBy,
            boolean isAscending
    ) {
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

            int idx = 1;
            if (productId != null) ps.setInt(idx++, productId);
            if (from != null) ps.setObject(idx++, from);
            if (to != null) ps.setObject(idx++, to);
            if (status != null) ps.setString(idx++, status);

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
     * Inserts a new inventory lot into DB.
     */
    public boolean insert(Connection con, InventoryLot lot) {
        String sql = "INSERT INTO inventory_lot "
                + "(product_id, grd_id, lot_code, received_date, expiry_date, qty_in, qty_out, qty_remaining, out_of_stock_at, status, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, lot.getProductId());
            ps.setInt(2, lot.getGrdId());
            ps.setString(3, lot.getLotCode());
            ps.setObject(4, lot.getReceivedDate());
            ps.setObject(5, lot.getExpiry());
            ps.setInt(6, lot.getQtyIn());
            ps.setInt(7, lot.getQtyOut());
            ps.setInt(8, lot.getQtyRemaining());
            ps.setObject(9, lot.getOutOfStockAt());

            InventoryLot.Status st = lot.getStatusEnum();
            ps.setString(10, st != null ? st.name() : "AVAILABLE");

            ps.setObject(11, lot.getCreatedAt());
            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert new inventory lot: " + e.getMessage(), e);
        }
    }

    // ==================================================
    // ========== EXTRA: report-like APIs (adapted to DUY schema) ==========
    // ==================================================

    /** Lấy toàn bộ lô hàng (theo schema qty_remaining/received_date/expiry_date). */
    public List<InventoryLot> getAllInventoryLots() {
        String sql = "SELECT il.* FROM inventory_lot il ORDER BY il.expiry_date ASC";
        List<InventoryLot> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all inventory lots: " + e.getMessage(), e);
        }

        return list;
    }

    /** Lấy các lô sắp hết hạn trong N ngày (qty_remaining > 0). */
    public List<InventoryLot> getExpiringLots(int daysAhead) {
        String sql = "SELECT il.* FROM inventory_lot il "
                + "WHERE il.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) "
                + "AND il.qty_remaining > 0 "
                + "ORDER BY il.expiry_date ASC";

        List<InventoryLot> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, daysAhead);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve expiring lots: " + e.getMessage(), e);
        }

        return list;
    }

    // ==================================================
    // ========== Mapper / Utils ==========
    // ==================================================

    private InventoryLot map(ResultSet rs) throws SQLException {
        InventoryLot lot = new InventoryLot();

        lot.setLotId(rs.getInt("lot_id"));
        lot.setProductId(rs.getInt("product_id"));

        // Some schemas may not have grd_id / lot_code etc -> guard by try/catch
        try { lot.setGrdId(rs.getInt("grd_id")); } catch (SQLException ignored) {}
        try { lot.setLotCode(rs.getString("lot_code")); } catch (SQLException ignored) {}

        try { lot.setReceivedDate(rs.getObject("received_date", LocalDate.class)); } catch (SQLException ignored) {}
        try { lot.setExpiry(rs.getObject("expiry_date", LocalDate.class)); } catch (SQLException ignored) {}

        try { lot.setQtyIn(rs.getInt("qty_in")); } catch (SQLException ignored) {}
        try { lot.setQtyOut(rs.getInt("qty_out")); } catch (SQLException ignored) {}
        try { lot.setQtyRemaining(rs.getInt("qty_remaining")); } catch (SQLException ignored) {}

        try { lot.setOutOfStockAt(rs.getObject("out_of_stock_at", LocalDateTime.class)); } catch (SQLException ignored) {}

        String st = null;
        try { st = rs.getString("status"); } catch (SQLException ignored) {}
        if (st != null) {
            try { lot.setStatusEnum(InventoryLot.Status.valueOf(st)); } catch (Exception ignored) {}
        }

        try { lot.setCreatedAt(rs.getObject("created_at", LocalDateTime.class)); } catch (SQLException ignored) {}

        return lot;
    }

    /** Avoid SQL injection by whitelisting ORDER BY columns. */
    private String sanitizeSortBy(String sortBy) {
        if (sortBy == null) return "il.expiry_date";

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
