package app;

// ไกด์ไฟล์: หน้าที่: เก็บชื่อผู้ใช้และอีเมลของสมาชิกที่ล็อกอินอยู่ในหน่วยความจำ
// startSession ตั้งข้อมูล, username/accountKey อ่านข้อมูล และ logout ล้างเมื่อออกจากระบบ
// accountKey ใช้อีเมลผูกข้อมูลส่วนตัว เช่น รายการโปรดและประวัติอ่าน

/** เก็บชื่อและอีเมลของบัญชีที่เข้าสู่ระบบอยู่ */
public final class UserSession {
    /** ชื่อผู้ใช้ที่กำลังล็อกอิน */
    private static String currentUsername;

    /** อีเมลบัญชีที่กำลังล็อกอิน ใช้แยกข้อมูลของแต่ละคน */
    private static String currentEmail;

    /** คลาสนี้ใช้เมธอด static จึงไม่ต้องสร้าง object */
    private UserSession() {
    }

    /** ตั้งข้อมูลบัญชีที่ตรวจสอบผ่านให้เป็นผู้ใช้ปัจจุบัน */
    public static void startSession(String username, String email) {
        currentUsername = username;
        currentEmail = email;
    }

    /** คืนชื่อผู้ใช้ที่กำลังล็อกอิน */
    public static String username() {
        if (currentUsername == null) {
            return "";
        }
        return currentUsername;
    }

    /** คืนอีเมลของผู้ใช้ปัจจุบัน เพื่อใช้ผูกข้อมูลส่วนตัวใน CSV */
    public static String accountKey() {
        if (currentEmail == null) {
            return "";
        }
        return currentEmail;
    }

    /** ล้างชื่อและอีเมลของผู้ใช้ปัจจุบันเมื่อออกจากระบบ */
    public static void logout() {
        currentUsername = null;
        currentEmail = null;
    }
}
