package bus;

import dal.dao.CategoryDAO;
import dal.dao.ProductDAO;
import dto.Category;
import dto.Product;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductService - Business logic for product management
 * Updated to match new Product DTO structure
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    public List<Product> getAll() {
        return productDAO.findAll();
    }

    public List<Product> search(String keyword) {
        if (Validator.isNullOrEmpty(keyword)) {
            return getAll();
        }
        return productDAO.search(keyword);
    }

    public List<Product> filter(Integer categoryId, Integer brandId, String status) {
        return productDAO.filter(categoryId, brandId, status);
    }

    public List<Product> getLowStockProducts() {
        return productDAO.findLowStockProducts();
    }

    public Product getById(int productId) {
        return productDAO.findById(productId);
    }

    public Product getByBarcode(String barcode) {
        return productDAO.findByBarcode(barcode);
    }

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public int getProductCountByCategory(int categoryId) {
        return productDAO.countByCategory(categoryId);
    }

    /**
     * Validate product data
     */
    public List<String> validate(Product product, boolean isUpdate) {
        List<String> errors = new ArrayList<>();

        // Validate barcode
        if (Validator.isNullOrEmpty(product.getBarcode())) {
            errors.add("Mã sản phẩm không được trống");
        } else {
            Product existing = productDAO.findByBarcode(product.getBarcode());
            if (existing != null) {
                if (!isUpdate || !existing.getProductId().equals(product.getProductId())) {
                    errors.add("Mã sản phẩm đã tồn tại");
                }
            }
        }

        // Validate product name
        if (Validator.isNullOrEmpty(product.getProductName())) {
            errors.add("Tên sản phẩm không được trống");
        } else if (!Validator.hasMinLength(product.getProductName(), 2)) {
            errors.add("Tên sản phẩm phải có ít nhất 2 ký tự");
        } else if (!Validator.hasMaxLength(product.getProductName(), 200)) {
            errors.add("Tên sản phẩm không được quá 200 ký tự");
        }

        // Validate unit
        if (Validator.isNullOrEmpty(product.getUnit())) {
            errors.add("Đơn vị không được trống");
        }

        // Validate category
        if (product.getCategoryId() == null || product.getCategoryId() <= 0) {
            errors.add("Vui lòng chọn danh mục");
        }

        // Validate import price
        if (product.getImportPrice() == null || product.getImportPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            errors.add("Giá nhập phải lớn hơn 0");
        }

        // Validate sale price
        if (product.getSalePrice() == null || product.getSalePrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            errors.add("Giá bán phải lớn hơn 0");
        }

        // Validate stock
        if (product.getStockQty() == null || product.getStockQty() < 0) {
            errors.add("Tồn kho không được âm");
        }

        if (product.getMinStock() == null || product.getMinStock() < 0) {
            errors.add("Tồn tối thiểu không được âm");
        }

        return errors;
    }

    /**
     * Create new product
     */
    public int create(Product product) throws Exception {
        List<String> errors = validate(product, false);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        // Set defaults
        if (product.getStatus() == null) {
            product.setStatus("ACTIVE");
        }
        if (product.getStockQty() == null) {
            product.setStockQty(0);
        }
        if (product.getMinStock() == null) {
            product.setMinStock(0);
        }

        int id = productDAO.insert(product);
        if (id <= 0) {
            throw new Exception("Không thể tạo sản phẩm");
        }
        return id;
    }

    /**
     * Update existing product
     */
    public void update(Product product) throws Exception {
        List<String> errors = validate(product, true);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        productDAO.update(product);
    }

    /**
     * Delete product
     */
    public void delete(int productId) throws Exception {
        Product product = productDAO.findById(productId);
        if (product == null) {
            throw new Exception("Không tìm thấy sản phẩm");
        }

        productDAO.delete(productId);
    }

    /**
     * Update stock quantity
     */
    public void updateStock(int productId, int newQty) throws Exception {
        if (newQty < 0) {
            throw new Exception("Số lượng tồn kho không được âm");
        }
        productDAO.updateStock(productId, newQty);
    }
}
