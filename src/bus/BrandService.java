package bus;

import dal.dao.BrandDAO;
import dto.Brand;
import dto.Category;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

public class BrandService {
    
    private final BrandDAO dao = new BrandDAO();

    public List<Brand> getAll(){
        return dao.findAll();
    }

    public List<Brand> search(String keyword){
        if(Validator.isNullOrEmpty(keyword)){
            return getAll();
        }
        return dao.search(keyword);
    }

    public Brand getbyId(int BrandId){
        return dao.findById(BrandId);
    }

    public List<String> validate(Brand brand, boolean isUpdate){
        List<String> errors = new ArrayList<>();

        if(Validator.isNullOrEmpty(brand.getBrandName())){
            errors.add(Validator.requiredFieldMessage("Tên thương hiệu"));
        } else if (!Validator.hasMinLength(brand.getBrandName(), 2)) {
            errors.add(Validator.minLengthMessage("Tên thương hiệu",2));
        } else if (!Validator.hasMaxLength(brand.getBrandName(), 120)) {
            errors.add(Validator.maxLengthMessage("Tên thương hiệu", 120));
        } else {
            // Check for duplicate name
            Brand existing = dao.findByName(brand.getBrandName());
            if (existing != null) {
                if (!isUpdate || !existing.getBrandId().equals(brand.getBrandId())) {
                    errors.add(Validator.duplicateMessage("Tên thương hiệu"));
                }
            }
        }

        return errors;
    } 

    public int create(Brand brand) throws Exception {
        List<String> errors = validate(brand, false);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        int id = dao.insert(brand);
        if (id <= 0) {
            throw new Exception("Không thể tạo danh mục");
        }
        return id;
    }

    public void update(Brand brand) throws Exception {
        List<String> errors = validate(brand, true);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        if (!dao.update(brand)) {
            throw new Exception("Không thể cập nhật danh mục");
        }
    }

    public void delete(int brandId) throws Exception {
        Brand brand = dao.findById(brandId);
        if (brand == null) {
            throw new Exception("Không tìm thấy danh mục");
        }

        // Check if category is used by products
        // TODO: Add check from ProductDAO if needed

        if (!dao.delete(brandId)) {
            throw new Exception("Không thể xóa danh mục");
        }
    }
}
