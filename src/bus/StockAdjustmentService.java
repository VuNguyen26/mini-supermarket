package bus;

import dal.dao.StockAdjustmentDAO;
import dto.StockAdjustment;
import dto.StockAdjustmentReason;
import dto.StockAdjustmentStatus;

import java.util.List;

public class StockAdjustmentService {

    private final StockAdjustmentDAO stockAdjustmentDAO = new StockAdjustmentDAO();

    /**
     * Load danh sách phiếu kiểm kho
     * Dùng cho màn hình sidebar "Kiểm kho"
     */
    public List<StockAdjustment> getAll() {
        return stockAdjustmentDAO.findAll();
    }

    /**
     * Load 1 phiếu theo id
     * Dùng khi click View / Double click
     */
    // public StockAdjustment getStockAdjustmentById(int saId) {
    //     return stockAdjustmentDAO.findById(saId);
    // }

    public int createDraft(int createdBy,
                            StockAdjustmentReason reason,
                            String saCode,
                            String note) {

        // ===== Validate =====
        if (createdBy <= 0) {
            throw new IllegalArgumentException("Người tạo không hợp lệ");
        }
        if (reason == null) {
            throw new IllegalArgumentException("Lý do kiểm kho không được để trống");
        }

        // ===== Build StockAdjustment =====
        StockAdjustment sa = new StockAdjustment();
        sa.setSaCode(saCode);
        sa.setCreatedBy(createdBy);
        sa.setReason(reason);
        sa.setStatus(StockAdjustmentStatus.DRAFT);
        sa.setNote(note);

        // ===== Insert =====
        int saId = stockAdjustmentDAO.insert(sa);

        if (saId <= 0) {
            throw new RuntimeException("Không tạo được phiếu kiểm kho");
        }

        return saId;
    }
}
