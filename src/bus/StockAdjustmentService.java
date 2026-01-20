package bus;

import dal.dao.StockAdjustmentDAO;
import dal.dao.StockAdjustmentDetailDAO;
import dto.LotOption;
import dto.ProductOption;
import dto.StockAdjustment;
import dto.StockAdjustmentDetail;
import dto.StockAdjustmentReason;
import dto.StockAdjustmentStatus;

import java.util.List;

public class StockAdjustmentService {

    private final StockAdjustmentDAO stockAdjustmentDAO = new StockAdjustmentDAO();
    private final StockAdjustmentDetailDAO stockAdjustmentDetailDAO = new StockAdjustmentDetailDAO();

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
    public StockAdjustment getById(int saId) {
        return stockAdjustmentDAO.findById(saId);
    }

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

     public void updateStatus(int saId, StockAdjustmentStatus newStatus) {

        if (saId <= 0) {
            throw new IllegalArgumentException("Phiếu kiểm kho không hợp lệ");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Trạng thái mới không hợp lệ");
        }

        // ===== Lấy trạng thái hiện tại =====
        StockAdjustment sa = stockAdjustmentDAO.findById(saId);
        if (sa == null) {
            throw new RuntimeException("Không tìm thấy phiếu kiểm kho");
        }

        StockAdjustmentStatus current = sa.getStatus();

        // ===== Rule chuyển trạng thái =====
        if (current == StockAdjustmentStatus.CONFIRMED) {
            throw new IllegalStateException("Phiếu đã xác nhận, không thể thay đổi trạng thái");
        }

        if (current == StockAdjustmentStatus.CANCELLED) {
            throw new IllegalStateException("Phiếu đã hủy, không thể thay đổi trạng thái");
        }

        if (current == newStatus) {
            return; // không cần update
        }

        // ===== Update =====
        stockAdjustmentDAO.updateStatus(saId, newStatus.name());
    }

    public void updateDraftInfo(int saId,
                            String saCode,
                            StockAdjustmentReason reason,
                            String note) {

        if (saId <= 0) {
            throw new IllegalArgumentException("Phiếu kiểm kho không hợp lệ");
        }

        StockAdjustment sa = stockAdjustmentDAO.findById(saId);
        if (sa == null) {
            throw new RuntimeException("Không tìm thấy phiếu kiểm kho");
        }

        if (sa.getStatus() != StockAdjustmentStatus.DRAFT) {
            throw new IllegalStateException(
                "Chỉ được sửa phiếu ở trạng thái DRAFT"
            );
        }

        if (reason == null) {
            throw new IllegalArgumentException("Lý do kiểm kho không được để trống");
        }

        sa.setSaCode(saCode);
        sa.setReason(reason);
        sa.setNote(note);

        stockAdjustmentDAO.updateDraftInfo(sa);
    }

    public List<StockAdjustmentDetail> getByStockAdjustment(int saId) {

        // ===== Validate =====
        if (saId <= 0) {
            throw new IllegalArgumentException("Phiếu kiểm kho không hợp lệ");
        }

        return stockAdjustmentDetailDAO.findByAdjustmentId(saId);
    }
    
     public void addDetail(StockAdjustmentDetail d) {

        if (d == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }
        if (d.getSaId() <= 0) {
            throw new IllegalArgumentException("Phiếu kiểm kho không hợp lệ");
        }
        if (d.getProductId() <= 0) {
            throw new IllegalArgumentException("Sản phẩm không hợp lệ");
        }

        // tự tính diff
        d.setDiffQty(d.getCountedQty() - d.getSystemQty());

        stockAdjustmentDetailDAO.insert(d);
    }

    /**
     * Cập nhật 1 dòng kiểm kho
     */
    public void updateDetail(StockAdjustmentDetail d) {

        if (d == null || d.getSadId() <= 0) {
            throw new IllegalArgumentException("Dòng kiểm kho không hợp lệ");
        }

        // tự tính diff
        d.setDiffQty(d.getCountedQty() - d.getSystemQty());

        stockAdjustmentDetailDAO.update(d);
    }

    /**
     * Xóa 1 dòng kiểm kho
     */
    public void deleteDetail(int sadId) {

        if (sadId <= 0) {
            throw new IllegalArgumentException("Dòng kiểm kho không hợp lệ");
        }

        stockAdjustmentDetailDAO.delete(sadId);
    }

    public List<ProductOption> getProductsForCombobox() {
        return stockAdjustmentDetailDAO.findAllForCombobox();
    }

    public List<LotOption> getLotsByProduct(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException("Sản phẩm không hợp lệ");
        }
        return stockAdjustmentDetailDAO.findLotsByProduct(productId);
    }

    public int getSystemQtyByLot(int productId, Long lotId) {

        if (productId <= 0) {
            throw new IllegalArgumentException("Sản phẩm không hợp lệ");
        }
        if (lotId == null) {
            throw new IllegalArgumentException("Lô không hợp lệ");
        }

        return stockAdjustmentDetailDAO.getSystemQtyByLot(productId, lotId);
    }
    
    public StockAdjustmentDetail getDetailById(int sadId) {
        if (sadId <= 0) {
            throw new IllegalArgumentException("Detail ID không hợp lệ");
        }
        return stockAdjustmentDetailDAO.findDetailById(sadId);
    }


}
