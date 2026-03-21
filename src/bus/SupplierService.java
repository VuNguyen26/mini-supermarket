package bus;

import dal.dao.SupplierDAO;
import dto.Supplier;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

public class SupplierService {

    private final SupplierDAO dao = new SupplierDAO();
    private final AuditLogService auditLogService = new AuditLogService();

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

    public Supplier getByCode(String supplierCode) {
        if (Validator.isNullOrEmpty(supplierCode)) {
            return null;
        }
        return dao.findByCode(supplierCode.trim());
    }

    public List<String> validate(Supplier supplier, boolean isUpdate) {
        List<String> errors = new ArrayList<>();

        if (supplier == null) {
            errors.add("Dữ liệu nhà cung cấp không hợp lệ");
            return errors;
        }

        // Mã NCC
        if (Validator.isNullOrEmpty(supplier.getSupplierCode())) {
            errors.add(Validator.requiredFieldMessage("Mã nhà cung cấp"));
        } else if (!Validator.hasMinLength(supplier.getSupplierCode(), 2)) {
            errors.add(Validator.minLengthMessage("Mã nhà cung cấp", 2));
        } else if (!Validator.hasMaxLength(supplier.getSupplierCode(), 50)) {
            errors.add(Validator.maxLengthMessage("Mã nhà cung cấp", 50));
        } else {
            String code = supplier.getSupplierCode().trim();

            if (!isUpdate) {
                if (dao.existsByCode(code)) {
                    errors.add(Validator.duplicateMessage("Mã nhà cung cấp"));
                }
            } else {
                Integer supplierId = supplier.getSupplierId();
                if (supplierId == null) {
                    errors.add("Thiếu ID nhà cung cấp khi cập nhật");
                } else if (dao.existsByCodeExceptId(code, supplierId)) {
                    errors.add(Validator.duplicateMessage("Mã nhà cung cấp"));
                }
            }
        }

        // Tên NCC
        if (Validator.isNullOrEmpty(supplier.getSupplierName())) {
            errors.add(Validator.requiredFieldMessage("Tên nhà cung cấp"));
        } else if (!Validator.hasMinLength(supplier.getSupplierName(), 2)) {
            errors.add(Validator.minLengthMessage("Tên nhà cung cấp", 2));
        } else if (!Validator.hasMaxLength(supplier.getSupplierName(), 150)) {
            errors.add(Validator.maxLengthMessage("Tên nhà cung cấp", 150));
        } else if (!isUpdate) {
            Supplier existing = dao.findByName(supplier.getSupplierName().trim());
            if (existing != null) {
                errors.add(Validator.duplicateMessage("Tên nhà cung cấp"));
            }
        }

        // Số điện thoại
        if (Validator.isNullOrEmpty(supplier.getPhone())) {
            errors.add(Validator.requiredFieldMessage("Số điện thoại"));
        } else if (!Validator.isValidPhone(supplier.getPhone().trim())) {
            errors.add(Validator.invalidFormatMessage("Số điện thoại"));
        }

        // Email
        if (Validator.isNotEmpty(supplier.getEmail()) && !Validator.isValidEmail(supplier.getEmail().trim())) {
            errors.add(Validator.invalidFormatMessage("Email"));
        }

        return errors;
    }

    public int create(Supplier supplier) throws Exception {
        normalizeSupplier(supplier);

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

        auditLogService.log(
                null,
                "CREATE",
                "supplier",
                (long) id,
                "Tạo nhà cung cấp: [" + safe(supplier.getSupplierCode()) + "] " + safe(supplier.getSupplierName())
                        + (supplier.getPhone() != null ? " - " + supplier.getPhone() : "")
        );

        return id;
    }

    public void update(Supplier supplier) throws Exception {
        normalizeSupplier(supplier);

        List<String> errors = validate(supplier, true);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        if (!dao.update(supplier)) {
            throw new Exception("Không thể cập nhật nhà cung cấp");
        }

        auditLogService.log(
                null,
                "UPDATE",
                "supplier",
                supplier.getSupplierId() != null ? supplier.getSupplierId().longValue() : null,
                "Cập nhật nhà cung cấp: [" + safe(supplier.getSupplierCode()) + "] " + safe(supplier.getSupplierName())
                        + (supplier.getPhone() != null ? " - " + supplier.getPhone() : "")
        );
    }

    public void delete(int supplierId) throws Exception {
        Supplier supplier = dao.findById(supplierId);
        if (supplier == null) {
            throw new Exception("Không tìm thấy nhà cung cấp");
        }

        if (!dao.delete(supplierId)) {
            throw new Exception("Không thể xóa nhà cung cấp");
        }

        auditLogService.log(
                null,
                "DELETE",
                "supplier",
                (long) supplierId,
                "Xóa nhà cung cấp: [" + safe(supplier.getSupplierCode()) + "] " + safe(supplier.getSupplierName())
                        + (supplier.getPhone() != null ? " - " + supplier.getPhone() : "")
        );
    }

    private void normalizeSupplier(Supplier supplier) {
        if (supplier == null) return;

        if (supplier.getSupplierCode() != null) {
            supplier.setSupplierCode(supplier.getSupplierCode().trim());
        }

        if (supplier.getSupplierName() != null) {
            supplier.setSupplierName(supplier.getSupplierName().trim());
        }

        if (supplier.getPhone() != null) {
            supplier.setPhone(supplier.getPhone().trim());
        }

        if (supplier.getEmail() != null) {
            supplier.setEmail(supplier.getEmail().trim());
        }

        if (supplier.getAddress() != null) {
            supplier.setAddress(supplier.getAddress().trim());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}