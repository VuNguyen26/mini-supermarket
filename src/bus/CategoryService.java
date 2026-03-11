package bus;

import dal.dao.CategoryDAO;
import dto.Category;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * CategoryService - Business logic for Category
 * Simplified validation: only category_name is required and must be unique
 */
public class CategoryService {

    private final CategoryDAO dao = new CategoryDAO();
    private final AuditLogService auditLogService = new AuditLogService();

    public List<Category> getAll() {
        return dao.findAll();
    }

    public List<Category> search(String keyword) {
        if (Validator.isNullOrEmpty(keyword)) {
            return getAll();
        }
        return dao.search(keyword);
    }

    public Category getById(int categoryId) {
        return dao.findById(categoryId);
    }

    /**
     * Validate category - chỉ validate category_name (required, unique)
     */
    public List<String> validate(Category category, boolean isUpdate) {
        List<String> errors = new ArrayList<>();

        if (Validator.isNullOrEmpty(category.getCategoryName())) {
            errors.add(Validator.requiredFieldMessage("Tên danh mục"));
        } else if (!Validator.hasMinLength(category.getCategoryName(), 2)) {
            errors.add(Validator.minLengthMessage("Tên danh mục", 2));
        } else if (!Validator.hasMaxLength(category.getCategoryName(), 120)) {
            errors.add(Validator.maxLengthMessage("Tên danh mục", 120));
        } else {
            Category existing = dao.findByName(category.getCategoryName());
            if (existing != null) {
                if (!isUpdate || !existing.getCategoryId().equals(category.getCategoryId())) {
                    errors.add(Validator.duplicateMessage("Tên danh mục"));
                }
            }
        }

        return errors;
    }

    public int create(Category category) throws Exception {
        List<String> errors = validate(category, false);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        int id = dao.insert(category);
        if (id <= 0) {
            throw new Exception("Không thể tạo danh mục");
        }

        auditLogService.log(
                null,
                "CREATE",
                "category",
                (long) id,
                "Tạo danh mục: " + category.getCategoryName()
        );

        return id;
    }

    public void update(Category category) throws Exception {
        List<String> errors = validate(category, true);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        if (!dao.update(category)) {
            throw new Exception("Không thể cập nhật danh mục");
        }

        auditLogService.log(
                null,
                "UPDATE",
                "category",
                category.getCategoryId() != null ? category.getCategoryId().longValue() : null,
                "Cập nhật danh mục: " + category.getCategoryName()
        );
    }

    public void delete(int categoryId) throws Exception {
        Category category = dao.findById(categoryId);
        if (category == null) {
            throw new Exception("Không tìm thấy danh mục");
        }

        if (!dao.delete(categoryId)) {
            throw new Exception("Không thể xóa danh mục");
        }

        auditLogService.log(
                null,
                "DELETE",
                "category",
                (long) categoryId,
                "Xóa danh mục: " + category.getCategoryName()
        );
    }
}