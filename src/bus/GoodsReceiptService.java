package bus;

import java.time.LocalDateTime;
import java.util.List;

import dal.dao.GoodsReceiptDAO;
import dto.GoodsReceipt;
import dto.Supplier;

public class GoodsReceiptService {

	private final GoodsReceiptDAO goodsReceiptDAO = new GoodsReceiptDAO();

	public String getSupplierName(int id) { return goodsReceiptDAO.getSupplierName(id) + " ("+ id + ")"; }
	public String getUserName(int id) { return goodsReceiptDAO.getUserName(id) + " ("+ id + ")"; }
	// Temp
	public List<Supplier> getAllSuppilers() {
		return goodsReceiptDAO.getAllSupplier();
	}

	public List<GoodsReceipt> getList(
		Integer supplierId,
		LocalDateTime from,
		LocalDateTime to,
		String sortBy,
		boolean isAscending
	) {
		return goodsReceiptDAO.getList(supplierId, from, to, sortBy, isAscending);
	}
}
