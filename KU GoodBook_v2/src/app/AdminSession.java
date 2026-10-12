package app;

/** เก็บชื่อของแอดมินที่เข้าสู่ระบบอยู่ */
public final class AdminSession {
    private static String username;

    private AdminSession() {
    }

    /** ตั้งชื่อแอดมินที่ตรวจสอบผ่านให้เป็นผู้ใช้ปัจจุบัน */
    public static void startSession(String adminName) {
        username = adminName;
    }

    /** ตรวจว่ามีแอดมินเข้าสู่ระบบอยู่หรือไม่ */
    public static boolean isLoggedIn() {
        return username != null;
    }

    /** คืนชื่อแอดมินปัจจุบัน */
    public static String username() {
        return username == null ? "" : username;
    }

    /** ล้างสถานะเมื่อแอดมินออกจากระบบ */
    public static void logout() {
        username = null;
    }
}
