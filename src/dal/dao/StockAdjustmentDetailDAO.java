package dal.dao;

import dal.DBConnection;
import dto.LotOption;
import dto.ProductOption;
import dto.StockAdjustmentDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class StockAdjustmentDetailDAO {

    // ====== load danh sách chi tiết theo phiếu ======
    public List<StockAdjustmentDetail> findByAdjustmentId(int saId) {
        String sql =
                "SELECT sad_id, sa_id, s.product_id, product_name, s.lot_id, il.lot_code, " +
                "system_qty, counted_qty, diff_qty, note " +
                "FROM stock_adjustment_detail s JOIN product p ON s.product_id = p.product_id " +
                "JOIN inventory_lot il on il.lot_id = s.lot_id " +
                "WHERE sa_id = ?";

        List<StockAdjustmentDetail> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, saId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Load stock adjustment details failed: " + e.getMessage(), e);
        }
        return list;
    }

    // ====== thêm 1 dòng kiểm kho ======
    public void insert(StockAdjustmentDetail d) {
        String sql =
                "INSERT INTO stock_adjustment_detail " +
                "(sa_id, product_id, lot_id, system_qty, counted_qty, diff_qty, note) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getSaId());
            ps.setInt(2, d.getProductId());
            ps.setObject(3, d.getLotId());
            ps.setInt(4, d.getSystemQty());
            ps.setInt(5, d.getCountedQty());
            ps.setInt(6, d.getDiffQty());
            ps.setString(7, d.getNote());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Insert stock adjustment detail failed: " + e.getMessage(), e);
        }
    }

    // ====== cập nhật 1 dòng kiểm kho ======
    public void update(StockAdjustmentDetail d) {
        String sql =
                "UPDATE stock_adjustment_detail " +
                "SET counted_qty = ?, diff_qty = ?, note = ? " +
                "WHERE sad_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getCountedQty());
            ps.setInt(2, d.getDiffQty());
            ps.setString(3, d.getNote());
            ps.setInt(4, d.getSadId());

            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Update stock adjustment detail failed: " + e.getMessage(), e);
        }
    }

    // ====== xóa 1 dòng kiểm kho ======
    public void delete(int sadId) {
        String sql = "DELETE FROM stock_adjustment_detail WHERE sad_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sadId);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Delete stock adjustment detail failed: " + e.getMessage(), e);
        }
    }

    public List<ProductOption> findAllForCombobox() {
        String sql = "SELECT product_id, product_name FROM product";

        List<ProductOption> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ProductOption(
                        rs.getInt("product_id"),
                        rs.getString("product_name")
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    public List<LotOption> findLotsByProduct(int productId) {

        String sql = """
            SELECT lot_id, lot_code, qty_remaining
            FROM inventory_lot
            WHERE product_id = ? AND status = 'AVAILABLE'
        """;

        List<LotOption> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LotOption(
                            rs.getLong("lot_id"),
                            rs.getString("lot_code"),
                            rs.getInt("qty_remaining")
                    ));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Load lots failed", e);
        }
        return list;
    }


    public int getSystemQtyByLot(int productId, Long lotId) {
        String sql =
            "SELECT qty_remaining " +
            "FROM inventory_lot " +
            "WHERE product_id = ? AND lot_id = ? AND status = 'AVAILABE'";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setLong(2, lotId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("qty_remaining");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Get system qty by lot failed", e);
        }
        return 0;
    }

    public StockAdjustmentDetail findDetailById(int sadId) {
        String sql = """
            SELECT sad_id, sa_id, s.product_id, p.product_name,
                s.lot_id, il.lot_code,
                system_qty, counted_qty, diff_qty, note
            FROM stock_adjustment_detail s
            JOIN product p ON s.product_id = p.product_id
            JOIN inventory_lot il ON il.lot_id = s.lot_id
            WHERE sad_id = ?
        """;

        try (Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, sadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    // ====== map ResultSet -> DTO ======
    private StockAdjustmentDetail map(ResultSet rs) throws Exception {
        StockAdjustmentDetail d = new StockAdjustmentDetail();
        d.setSadId(rs.getInt("sad_id"));
        d.setSaId(rs.getInt("sa_id"));
        d.setProductId(rs.getInt("product_id"));
        d.setProductName(rs.getString("product_name"));
        d.setLotId(rs.getObject("lot_id", Long.class));
        d.setLotCode(rs.getString("lot_code"));
        d.setSystemQty(rs.getInt("system_qty"));
        d.setCountedQty(rs.getInt("counted_qty"));
        d.setDiffQty(rs.getInt("diff_qty"));
        d.setNote(rs.getString("note"));
        return d;
    }
}
