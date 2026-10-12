package logic.admin;

// ไกด์ไฟล์: หน้าที่: ตรวจข้อมูลล็อกอินของแอดมินผ่าน AdminStorage
// login จะเริ่ม AdminSession เฉพาะเมื่อพบอีเมลและรหัสผ่านที่ถูกต้อง
// การเลือกว่าจะเปิดหน้าใดหลังล็อกอินอยู่ใน LoginScreen ไม่ใช่ในคลาสนี้

import app.AdminSession;
import storage.admin.AdminStorage;
import java.io.IOException;

/** ตรวจบัญชีแอดมินและเริ่ม session เมื่อเข้าสู่ระบบสำเร็จ */
public final class AdminLogic {

    private AdminLogic() {
    }

    /** ตรวจอีเมลและรหัสผ่านจาก admins.csv */
    public static boolean login(String email, String password) throws IOException {
        // ตรวจข้อมูลแอดมินกับ admins.csv
        String username = AdminStorage.login(email, password);
        // ไม่เริ่ม session ถ้าไม่พบอีเมลและรหัสผ่านที่ตรงกัน
        if (username == null) {
            return false;
        }
        // เก็บชื่อแอดมินไว้เพื่อให้หน้าจอรู้ว่ากำลังใช้สิทธิ์ผู้ดูแล
        AdminSession.startSession(username);
        return true;
    }
}
