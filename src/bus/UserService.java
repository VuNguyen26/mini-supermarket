package bus;

import dal.dao.UserDAO;
import dto.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {
    private final UserDAO dao = new UserDAO();
    private final AuditLogService auditLogService = new AuditLogService();

    public List<User> getAll() throws Exception {
        return dao.findAll();
    }

    public List<User> search(String keyword) throws Exception {
        return dao.search(keyword);
    }

    public int add(String username, String rawPassword, String fullName, String phone, int roleId, String status) throws Exception {
        username = safe(username);
        fullName = safe(fullName);
        phone = safe(phone);

        if (username.isEmpty()) throw new IllegalArgumentException("Username không được rỗng");
        if (rawPassword == null || rawPassword.trim().length() < 4) throw new IllegalArgumentException("Mật khẩu tối thiểu 4 ký tự");
        if (dao.existsUsername(username, null)) throw new IllegalArgumentException("Username đã tồn tại");

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt(10)));
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setRoleId(roleId);
        u.setStatus(status == null || status.isBlank() ? "ACTIVE" : status);

        int newUserId = dao.insert(u);

        auditLogService.log(
                null,
                "CREATE",
                "user",
                (long) newUserId,
                "Tạo nhân viên: " + username
        );

        return newUserId;
    }

    public boolean update(int userId, String fullName, String phone, int roleId, String status, String rawPasswordOrEmpty) throws Exception {
        fullName = safe(fullName);
        phone = safe(phone);

        if (userId <= 0) throw new IllegalArgumentException("Chưa chọn nhân viên để sửa");

        User u = new User();
        u.setUserId(userId);
        u.setFullName(fullName);
        u.setPhone(phone);
        u.setRoleId(roleId);
        u.setStatus(status);

        boolean updated;
        boolean changedPassword = false;

        if (rawPasswordOrEmpty != null && !rawPasswordOrEmpty.isBlank()) {
            if (rawPasswordOrEmpty.trim().length() < 4) throw new IllegalArgumentException("Mật khẩu tối thiểu 4 ký tự");
            u.setPasswordHash(BCrypt.hashpw(rawPasswordOrEmpty, BCrypt.gensalt(10)));
            updated = dao.updateWithPassword(u);
            changedPassword = updated;
        } else {
            updated = dao.updateInfo(u);
        }

        if (updated) {
            auditLogService.log(
                    null,
                    "UPDATE",
                    "user",
                    (long) userId,
                    changedPassword
                            ? "Cập nhật nhân viên ID " + userId + " và đổi mật khẩu"
                            : "Cập nhật nhân viên ID " + userId
            );
        }

        return updated;
    }

    public boolean lock(int userId) throws Exception {
        boolean ok = dao.setStatus(userId, "INACTIVE");
        if (ok) {
            auditLogService.log(
                    null,
                    "LOCK",
                    "user",
                    (long) userId,
                    "Khóa tài khoản nhân viên ID " + userId
            );
        }
        return ok;
    }

    public boolean unlock(int userId) throws Exception {
        boolean ok = dao.setStatus(userId, "ACTIVE");
        if (ok) {
            auditLogService.log(
                    null,
                    "UNLOCK",
                    "user",
                    (long) userId,
                    "Mở khóa tài khoản nhân viên ID " + userId
            );
        }
        return ok;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}