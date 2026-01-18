package bus;

import dal.dao.SupplierDAO;
import dto.Supplier;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

public class SupplierService {

    private final SupplierDAO dao = new SupplierDAO();

    public List<Supplier> getAll() {
        return dao.findAll();
    }

    public List<Supplier> search(String keyword) {
        if (Validator.isNullOrEmpty(keyword)) {
            return getAll();
        }
        return dao.search(keyword);
    }

    public Supplier getById(int supplierId) {
        return dao.findById(supplierId);
    }

    public List<String> validate(Supplier supplier, boolean isUpdate) {
        List<String> errors = new ArrayList<>();

        // Validate supplier name
        if (Validator.isNullOrEmpty(supplier.getSupplierName())) {
            errors.add(Validator.requiredFieldMessage("Tên nhà cung cấp"));
        } else if (!Validator.hasMinLength(supplier.getSupplierName(), 2)) {
            errors.add(Validator.minLengthMessage("Tên nhà cung cấp", 2));
        } else if (!Validator.hasMaxLength(supplier.getSupplierName(), 150)) {
            errors.add(Validator.maxLengthMessage("Tên nhà cung cấp", 150));
        } else if (!isUpdate) {
            // Check duplicate name only when creating new supplier
            Supplier existing = dao.findByName(supplier.getSupplierName());
            if (existing != null) {
                errors.add(Validator.duplicateMessage("Tên nhà cung cấp"));
            }
        }

        // Validate phone
        if (Validator.isNullOrEmpty(supplier.getPhone())) {
            errors.add(Validator.requiredFieldMessage("Số điện thoại"));
        } else if (!Validator.isValidPhone(supplier.getPhone())) {
            errors.add(Validator.invalidFormatMessage("Số điện thoại"));
        }

        // Validate email (optional)
        if (Validator.isNotEmpty(supplier.getEmail()) && !Validator.isValidEmail(supplier.getEmail())) {
            errors.add(Validator.invalidFormatMessage("Email"));
        }

        return errors;
    }

    public int create(Supplier supplier) throws Exception {
        List<String> errors = validate(supplier, false);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        if (supplier.getStatus() == null) {
            supplier.setStatus("ACTIVE");
        }

        int id = dao.insert(supplier);
        if (id <= 0) {
            throw new Exception("Không thể tạo nhà cung cấp");
        }
        return id;
    }

    public void update(Supplier supplier) throws Exception {
        List<String> errors = validate(supplier, true);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        if (!dao.update(supplier)) {
            throw new Exception("Không thể cập nhật nhà cung cấp");
        }
    }

    public void delete(int supplierId) throws Exception {
        Supplier supplier = dao.findById(supplierId);
        if (supplier == null) {
            throw new Exception("Không tìm thấy nhà cung cấp");
        }

        if (!dao.delete(supplierId)) {
            throw new Exception("Không thể xóa nhà cung cấp");
        }
    }
}
