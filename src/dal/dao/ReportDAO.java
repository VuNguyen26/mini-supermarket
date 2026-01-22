package dal.dao;

import  dto.ReportProduct;
import dal.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public List<ReportProduct> getProductReport(
            LocalDate fromDate,
            LocalDate toDate,
            String type,
            int limit) {

        List<ReportProduct> list = new ArrayList<>();

        // chỉ cho phép 2 kiểu sort → an toàn
        String orderBy = "qty";
        if ("revenue".equalsIgnoreCase(type)) {
            orderBy = "revenue";
        }

        String sql =
                "SELECT p.product_id, p.product_name, " +
                        "       SUM(oi.quantity) AS qty, " +
                        "       SUM(oi.quantity * oi.sale_price) AS revenue " +
                        "FROM order_items oi " +
                        "JOIN orders o ON oi.order_id = o.order_id " +
                        "JOIN product p ON oi.product_id = p.product_id " +
                        "WHERE o.status = 'COMPLETED' " +
                        "  AND o.order_date BETWEEN ? AND ? " +
                        "GROUP BY p.product_id, p.product_name " +
                        "ORDER BY " + orderBy + " DESC " +
                        "LIMIT ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(fromDate));
            ps.setDate(2, java.sql.Date.valueOf(toDate));
            ps.setInt(3, limit);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ReportProduct(
                        rs.getInt("product_id"),
                        rs.getString("product_name"),
                        rs.getLong("qty"),
                        rs.getBigDecimal("revenue")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}