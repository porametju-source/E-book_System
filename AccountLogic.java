package logic.account;

// ไกด์ไฟล์: หน้าที่: รวมกฎสมัครสมาชิกและเข้าสู่ระบบ โดยเชื่อมหน้าจอกับ AccountStorage
// login ตรวจบัญชีและเริ่ม UserSession; register บันทึกบัญชีและเริ่ม session เมื่อสำเร็จ
// validateRegistration เป็นจุดแก้ข้อกำหนด เช่น รูปแบบอีเมล ความยาวรหัสผ่าน และยอมรับเงื่อนไข

import app.UserSession;
import storage.account.AccountStorage;
import java.io.IOException;
import java.nio.file.Path;

/** จัดการกฎสมัครสมาชิกและเข้าสู่ระบบของผู้ใช้ */
public final class AccountLogic {

    private AccountLogic() {
    }

    /** ตรวจบัญชีจาก CSV และตั้งผู้ใช้ปัจจุบันเมื่อเข้าสู่ระบบสำเร็จ */
    public static boolean login(String email, String password) throws IOException {
        // ให้ส่วน Storage ตรวจอีเมลและรหัสผ่านกับไฟล์ CSV
        String[] account = AccountStorage.login(email, password);
        // ถ้าไม่พบข้อมูลบัญชี ให้แจ้งหน้าจอว่าล็อกอินไม่สำเร็จ
        if (account == null) {
            return false;
        }
        // เก็บชื่อและอีเมลไว้ใน session เพื่อใช้ระบุตัวผู้ใช้ระหว่างเปิดแอป
        UserSession.startSession(account[0], account[1]);
        return true;
    }

    /** บันทึกบัญชีใหม่และตั้งผู้สมัครเป็นผู้ใช้ปัจจุบัน */
    public static boolean register(String username, String email, String password)
            throws IOException {
        // ขอให้ Storage ตรวจอีเมลซ้ำและเขียนบัญชีใหม่ลง CSV
        boolean wasCreated = AccountStorage.register(username, email, password);
        // เริ่ม session ต่อเมื่อบันทึกบัญชีสำเร็จ
        if (wasCreated) {
            UserSession.startSession(username.trim(), email.trim().toLowerCase());
        }
        return wasCreated;
    }

    /** คืนตำแหน่งไฟล์ CSV บัญชีสำหรับแสดงในข้อความสมัครสำเร็จ */
    public static Path userFilePath() {
        return AccountStorage.userFilePath();
    }

    /** คืนข้อความแจ้งเตือนถ้าข้อมูลไม่ผ่าน หรือคืนข้อความว่างถ้าพร้อมสมัคร */
    public static String validateRegistration(String username, String email,
                                              String password, String confirmation,
                                              boolean acceptedTerms) {
        // ตรวจช่องว่างก่อน เพื่อบอกผู้ใช้ได้ว่าต้องกรอกข้อมูลส่วนใด
        if (username.trim().isEmpty() || email.trim().isEmpty() || password.isEmpty()) {
            return "กรุณากรอกชื่อผู้ใช้ อีเมล และรหัสผ่านให้ครบ";
        }
        // ตรวจรูปแบบพื้นฐานของอีเมลก่อนบันทึกลง CSV
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return "กรุณากรอกอีเมลให้ถูกต้อง";
        }
        // กำหนดความยาวขั้นต่ำให้รหัสผ่าน
        if (password.length() < 6) {
            return "รหัสผ่านต้องมีอย่างน้อย 6 ตัวอักษร";
        }
        // เทียบรหัสผ่านกับช่องยืนยันว่าตรงกัน
        if (!password.equals(confirmation)) {
            return "รหัสผ่านและการยืนยันไม่ตรงกัน";
        }
        // ต้องติ๊กยอมรับก่อนจึงจะสมัครได้
        if (!acceptedTerms) {
            return "กรุณายอมรับเงื่อนไขการใช้งาน";
        }
        return "";
    }
}
