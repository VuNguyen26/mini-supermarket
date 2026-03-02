package bus;

import dal.dao.PermissionDAO;
import dal.dao.UserDAO;
import dto.User;
import org.mindrot.jbcrypt.BCrypt;
import util.RolePermission;

import java.util.Collections;
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

        User u;
        try {
            u = userDAO.findByUsername(username.trim()); // UserDAO trả về dto.User
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        if (u == null) return null;

        // map dto.User -> AuthUser
        AuthUser user = new AuthUser();
        user.userId = u.getUserId();
        user.username = u.getUsername();
        user.passwordHash = u.getPasswordHash();
        user.fullName = u.getFullName();
        user.phone = u.getPhone();
        user.roleId = u.getRoleId();
        user.status = u.getStatus();

        // roleName (tạm map theo roleId nếu chưa join bảng role)
        user.roleName = switch (user.roleId) {
            case 1 -> "ADMIN";
            case 2 -> "MANAGER";
            case 3 -> "CASHIER";
            case 4 -> "WAREHOUSE";
            case 5 -> "ACCOUNTANT";
            default -> "STAFF";
        };

        if (user.status == null || !"ACTIVE".equalsIgnoreCase(user.status)) return null;
        if (user.passwordHash == null || user.passwordHash.isBlank()) return null;

        if (!verifyPassword(password, user.passwordHash)) return null;

        // ====== load permissions -> set session ======
        try {
            Set<String> perms = permissionDAO.findPermCodesByUserId(user.userId);
            if (perms == null) perms = Collections.emptySet();
            RolePermission.set(perms);
        } catch (Exception ex) {
            ex.printStackTrace();
            RolePermission.clear(); // fail thì coi như chưa có quyền
        }
        // ============================================

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