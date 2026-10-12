package app;

// ไกด์ไฟล์: หน้าที่: เก็บชื่อแอดมินที่กำลังเข้าสู่ระบบไว้ในหน่วยความจำระหว่างเปิดแอป
// เมธอด startSession ตั้งชื่อ, isLoggedIn ตรวจสถานะ, username อ่านชื่อ และ logout ล้างสถานะ
// แก้ไฟล์นี้เมื่อเพิ่มข้อมูลที่ต้องจำของแอดมิน; อย่าเก็บรหัสผ่านไว้ใน session

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
