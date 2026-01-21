package dal.dao;

import dal.DBConnection;
import dto.BankConfig;
import java.sql.*;

public class BankConfigDAO {

    public BankConfig getFirstConfig() {
        String sql = "SELECT * FROM bankconfig LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            if (rs.next()) {
                BankConfig config = new BankConfig();
                config.setId(rs.getInt("id")); 
                config.setBankId(rs.getString("bank_id"));
                config.setaccountNumber(rs.getString("account_number"));
                config.setAccountName(rs.getString("account_name"));
                config.setTemplate(rs.getString("template"));
                return config;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}