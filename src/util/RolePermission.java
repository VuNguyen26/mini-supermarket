package util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class RolePermission {
    private static Set<String> PERMS = new HashSet<>();
    private RolePermission() {}

    public static void set(Set<String> perms) {
        PERMS = (perms == null) ? new HashSet<>() : new HashSet<>(perms);
    }

    public static boolean has(String code) {
        return PERMS.contains(code);
    }

    public static Set<String> all() {
        return Collections.unmodifiableSet(PERMS);
    }

    public static void clear() {
        PERMS.clear();
    }
}
