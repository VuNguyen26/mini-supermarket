package bus;

import dal.dao.PermissionDAO;
import dal.dao.UserDAO;
import org.mindrot.jbcrypt.BCrypt;
import util.RolePermission;

import java.util.Set;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();

    // POJO dùng cho login
    public static class AuthUser {
        public int userId;
        public String username;
        public String passwordHash;
        public String fullName;
        public String phone;
        public int roleId;
        public String roleName;     // ADMIN / CASHIER / WAREHOUSE / ...
        public String status;       // ACTIVE / INACTIVE
    }

    public AuthUser login(String username, String password) {
        // basic validate
        if (username == null || username.isBlank()) return null;
        if (password == null) return null;

        AuthUser user = userDAO.findByUsername(username.trim());
        if (user == null) return null;

        if (user.status == null || !"ACTIVE".equalsIgnoreCase(user.status)) return null;
        if (user.passwordHash == null || user.passwordHash.isBlank()) return null;

        if (!verifyPassword(password, user.passwordHash)) return null;

        // ====== NEW: load permissions -> set session ======
        Set<String> perms = permissionDAO.findPermCodesByUserId(user.userId);
        RolePermission.set(perms);
        // ================================================

        return user;
    }

    public void logout() {
        RolePermission.clear();
    }

    private boolean verifyPassword(String rawPassword, String storedHashOrPlaintext) {
        // BCrypt thường bắt đầu bằng $2a$, $2b$, $2y$
        boolean looksLikeBCrypt =
                storedHashOrPlaintext.startsWith("$2a$") ||
                        storedHashOrPlaintext.startsWith("$2b$") ||
                        storedHashOrPlaintext.startsWith("$2y$");

        if (looksLikeBCrypt) {
            try {
                return BCrypt.checkpw(rawPassword, storedHashOrPlaintext);
            } catch (Exception ex) {
                // hash lỗi format
                return false;
            }
        }

        // fallback plaintext (chỉ để tương thích dữ liệu cũ)
        return rawPassword.equals(storedHashOrPlaintext);
    }
}