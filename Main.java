package app;

// ไกด์ไฟล์: หน้าที่: จุดเริ่มต้นของโปรแกรม Java (เมธอด main)
// ลำดับเริ่มแอปอยู่ใน main: เตรียมข้อมูลเริ่มต้น แล้วเปิดหน้าจอแรก
// ถ้าต้องเปลี่ยนหน้าที่เปิดตอนเริ่ม ให้แก้คำสั่งเปิด LoginScreen ที่นี่

import java.io.IOException;
import javax.swing.*;
import storage.CsvStorage;
import storage.account.AccountStorage;
import storage.admin.AdminStorage;
import ui.pages.LoginScreen;

/**
 * จุดเริ่มต้นของแอป KU GoodBook
 *
 * <p>เมื่อเปิดโปรแกรม คลาสนี้จะแสดงหน้าเข้าสู่ระบบก่อน
 * จากนั้นปุ่มต่าง ๆ จะพาผู้ใช้ไปหน้าอื่นผ่านคลาส Nav</p>
 */
public class Main {

    /**
     * ไม่ต้องสร้าง Main เพราะเราใช้แค่เมธอด main เพื่อเริ่มแอป
     */
    private Main() {
    }

    /**
     * เริ่มแอปโดยเปิดหน้าเข้าสู่ระบบ
     *
     * Swing มีเธรดสำหรับดูแลหน้าต่างและรับการกดปุ่มโดยเฉพาะ
     * จึงใช้ invokeLater เพื่อให้สร้างหน้าจอบนเธรดที่ถูกต้อง
     *
     * @param args ข้อความที่ส่งมาตอนเปิดโปรแกรม ปัจจุบันยังไม่ได้ใช้
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // เตรียมโฟลเดอร์และอ่านรีวิวก่อนเปิดหน้าจอแรก
                AccountStorage.initialize();
                AdminStorage.initialize();
                AdminStorage.loadBooks();
                CsvStorage.initialize();
                new LoginScreen();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "เตรียมไฟล์ CSV ไม่สำเร็จ: " + e.getMessage(),
                        "เปิดโปรแกรมไม่ได้", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
