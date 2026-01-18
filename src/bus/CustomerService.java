package bus;

import dal.dao.CustomerDAO;
import dto.Customer;
import util.Validator;

import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    private final CustomerDAO dao = new CustomerDAO();

    public List<Customer> getAll() {
        return dao.findAll();
    }

    public List<Customer> search(String keyword) {
        if (Validator.isNullOrEmpty(keyword)) {
            return getAll();
        }
        return dao.search(keyword);
    }

    public Customer getById(int customerId) {
        return dao.findById(customerId);
    }

    public Customer getByPhone(String phone) {
        return dao.findByPhone(phone);
    }

    public List<String> validate(Customer customer, boolean isUpdate) {
        List<String> errors = new ArrayList<>();

        // Validate customer name
        if (Validator.isNullOrEmpty(customer.getCustomerName())) {
            errors.add("Tên khách hàng không được trống");
        } else if (!Validator.hasMinLength(customer.getCustomerName(), 2)) {
            errors.add("Tên khách hàng phải có ít nhất 2 ký tự");
        } else if (!Validator.hasMaxLength(customer.getCustomerName(), 100)) {
            errors.add("Tên khách hàng không được quá 100 ký tự");
        }

        // Validate phone
        if (Validator.isNullOrEmpty(customer.getPhone())) {
            errors.add(Validator.requiredFieldMessage("Số điện thoại"));
        } else if (!Validator.isValidPhone(customer.getPhone())) {
            errors.add(Validator.invalidFormatMessage("Số điện thoại"));
        } else {
            Customer existing = dao.findByPhone(customer.getPhone());
            if (existing != null) {
                if (!isUpdate || !existing.getCustomerId().equals(customer.getCustomerId())) {
                    errors.add(Validator.duplicateMessage("Số điện thoại"));
                }
            }
        }

        // Validate points (optional)
        if (customer.getPoints() != null && customer.getPoints() < 0) {
            errors.add("Điểm tích lũy phải là số không âm");
        }

        return errors;
    }

    public int create(Customer customer) throws Exception {
        List<String> errors = validate(customer, false);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        if (customer.getPoints() == null) {
            customer.setPoints(0);
        }

        int id = dao.insert(customer);
        if (id <= 0) {
            throw new Exception("Không thể tạo khách hàng");
        }
        return id;
    }

    public void update(Customer customer) throws Exception {
        List<String> errors = validate(customer, true);
        if (!errors.isEmpty()) {
            throw new Exception(String.join("\n", errors));
        }

        if (!dao.update(customer)) {
            throw new Exception("Không thể cập nhật khách hàng");
        }
    }

    public void delete(int customerId) throws Exception {
        Customer customer = dao.findById(customerId);
        if (customer == null) {
            throw new Exception("Không tìm thấy khách hàng");
        }

        if (!dao.delete(customerId)) {
            throw new Exception("Không thể xóa khách hàng");
        }
    }

    public void updateLoyaltyPoints(int customerId, int points) throws Exception {
        if (!Validator.isNonNegativeInteger(points)) {
            throw new Exception("Điểm tích lũy phải là số không âm");
        }

        if (!dao.updateLoyaltyPoints(customerId, points)) {
            throw new Exception("Không thể cập nhật điểm tích lũy");
        }
    }

    public void addLoyaltyPoints(int customerId, int pointsToAdd) throws Exception {
        Customer customer = dao.findById(customerId);
        if (customer == null) {
            throw new Exception("Không tìm thấy khách hàng");
        }

        int currentPoints = customer.getPoints() != null ? customer.getPoints() : 0;
        int newPoints = currentPoints + pointsToAdd;

        updateLoyaltyPoints(customerId, newPoints);
    }
}
