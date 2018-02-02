package com.zrlog.web.token;

public class AdminTokenThreadLocal {
    private static ThreadLocal<AdminToken> userThreadLocal = new ThreadLocal<>();

    public static AdminToken getUser() {
        return userThreadLocal.get();
    }

    public static void setAdminToken(AdminToken user) {
        if (userThreadLocal.get() == null)
            userThreadLocal.set(user);
    }

    public static Integer getUserId() {
        return userThreadLocal.get().getUserId();
    }

    public static void remove() {
        userThreadLocal.remove();
    }
}
