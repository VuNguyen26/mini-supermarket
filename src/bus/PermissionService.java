package bus;

import dal.dao.PermissionDAO;
import dal.dao.RoleDAO;
import dal.dao.RolePermissionDAO;
import dto.Permission;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PermissionService {

    private final RoleDAO roleDAO = new RoleDAO();
    private final PermissionDAO permissionDAO = new PermissionDAO();
    private final RolePermissionDAO rolePermissionDAO = new RolePermissionDAO();

    public Map<Integer, String> getAllRoles() {
        return roleDAO.findAllRoles();
    } // Phan quyen

    public List<Permission> getAllPermissions() {
        return permissionDAO.findAll();
    }

    public Set<Integer> getPermIdsOfRole(int roleId) {
        return permissionDAO.findPermIdsByRoleId(roleId);
    }

    public void saveRolePermissions(int roleId, Set<Integer> permIds) {
        rolePermissionDAO.replaceRolePermissions(roleId, permIds);
    }

    public void addPermission(Permission permission) {
        permissionDAO.insert(permission);
    }

    public void deletePermission(int permId) {
        permissionDAO.delete(permId);
    }

    /* ==== tạo UI cho login*/
    public Set<String> getPermCodesByUserId(int userId) {
        return permissionDAO.findPermCodesByUserId(userId);
    }

}
