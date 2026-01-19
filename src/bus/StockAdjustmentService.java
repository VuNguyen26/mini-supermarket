package bus;

import dal.dao.StockAdjustmentDAO;
import dto.StockAdjustment;

import java.util.List;

public class StockAdjustmentService {

    private final StockAdjustmentDAO stockAdjustmentDAO = new StockAdjustmentDAO();

    /**
     * Load danh sách phiếu kiểm kho
     * Dùng cho màn hình sidebar "Kiểm kho"
     */
    public List<StockAdjustment> getAllStockAdjustments() {
        return stockAdjustmentDAO.findAll();
    }

    /**
     * Load 1 phiếu theo id
     * Dùng khi click View / Double click
     */
    // public StockAdjustment getStockAdjustmentById(int saId) {
    //     return stockAdjustmentDAO.findById(saId);
    // }
}
