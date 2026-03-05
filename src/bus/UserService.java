package bus;

import dal.dao.UserDAO;
import dto.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

public class UserService {
    private final UserDAO dao = new UserDAO();

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

        return dao.insert(u);
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

        if (rawPasswordOrEmpty != null && !rawPasswordOrEmpty.isBlank()) {
            if (rawPasswordOrEmpty.trim().length() < 4) throw new IllegalArgumentException("Mật khẩu tối thiểu 4 ký tự");
            u.setPasswordHash(BCrypt.hashpw(rawPasswordOrEmpty, BCrypt.gensalt(10)));
            return dao.updateWithPassword(u);
        }
        return dao.updateInfo(u);
    }

    public boolean lock(int userId) throws Exception {
        return dao.setStatus(userId, "INACTIVE");
    }

    public boolean unlock(int userId) throws Exception {
        return dao.setStatus(userId, "ACTIVE");
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}