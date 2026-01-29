package dal.dao;

import  java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dal.DBConnection;
import dto.ReportProduct;

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
                        "       SUM(si.quantity) AS qty, " +
                        "       SUM(si.quantity * si.unit_price) AS revenue " +
                        "FROM sales_invoice_detail si " +
                        "JOIN sales_invoice s ON si.invoice_id = s.invoice_id " +
                        "JOIN product p ON si.product_id = p.product_id " +
                        "WHERE s.status = 'COMPLETED' " +
                        "  AND DATE(s.created_at) BETWEEN ? AND ? " +
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