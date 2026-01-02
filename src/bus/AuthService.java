package bus;

import dal.dao.UserDAO;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    // POJO dùng cho login
    public static class AuthUser {
        public int userId;
        public String username;
        public String passwordHash;
        public String fullName;
        public String phone;
        public int roleId;
        public String roleName;     // ADMIN / CASHIER / WAREHOUSE
        public String status;       // ACTIVE / INACTIVE
    }

    /**
     * Login đơn giản:
     * - DB seed đang lưu password_hash = plaintext -> so sánh trực tiếp
     * - Sau này nâng cấp: lưu BCrypt và verify bằng PasswordUtils
     */
    public AuthUser login(String username, String password) {
        AuthUser user = userDAO.findByUsername(username);
        if (user == null) return null;

        if (user.status == null || !"ACTIVE".equalsIgnoreCase(user.status)) return null;

        if (user.passwordHash == null) return null;
        if (!user.passwordHash.equals(password)) return null;

        return user;
    }
}
