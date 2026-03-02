package dal.dao;

import dto.LoyaltyPointTxn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class LoyaltyPointTxnDAO {

	public boolean createTxn(LoyaltyPointTxn txn, Connection conn) throws SQLException {
		String sql = "INSERT INTO loyalty_point_txn "
				+ "(customer_id, inv_id, created_by, type, points, money_amount, earn_rate_money, "
				+ "earn_rate_points, redeem_rate_points, redeem_rate_money, note) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, txn.getCustomerId());
			if (txn.getInvId() == null) {
				ps.setNull(2, Types.INTEGER);
			} else {
				ps.setInt(2, txn.getInvId());
			}
			ps.setInt(3, txn.getCreatedBy());
			ps.setString(4, txn.getType());
			ps.setInt(5, txn.getPoints());
			ps.setDouble(6, txn.getMoneyAmount());
			ps.setDouble(7, txn.getEarnRateMoney());
			ps.setInt(8, txn.getEarnRatePoints());
			ps.setInt(9, txn.getRedeemRatePoints());
			ps.setDouble(10, txn.getRedeemRateMoney());
			ps.setString(11, txn.getNote());
			return ps.executeUpdate() > 0;
		}
	}
}
