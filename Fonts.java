package ui.styles;

// ไกด์ไฟล์: หน้าที่: รวมฟอนต์ของแอปไว้ให้ทุกหน้าหยิบใช้ร่วมกัน
// title/body/bold/read คืนฟอนต์ตามบทบาท; install เตรียมฟอนต์ก่อนแสดงหน้าจอ
// แก้ชื่อฟอนต์หรือค่าพื้นฐานที่นี่ แล้วตรวจผลในหน้าและปุ่มที่เรียกใช้

import java.awt.Font;
import java.util.Enumeration;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/** ใช้ฟอนต์มาตรฐานที่ Java มีให้ ไม่ต้องเพิ่มไฟล์ฟอนต์ในโปรเจ็กต์ */
public final class Fonts {
    /** ใช้กลุ่มฟอนต์ Sans Serif ของ Java ซึ่งเลือกฟอนต์ในเครื่องให้อัตโนมัติ */
    private static final String FAMILY = Font.SANS_SERIF;

    private Fonts() {
    }

    /** คืนฟอนต์ขนาดใหญ่สำหรับหัวข้อ */
    public static Font title(float size) {
        return new Font(FAMILY, Font.PLAIN, Math.round(size));
    }

    /** คืนฟอนต์ปกติสำหรับข้อความทั่วไป */
    public static Font body(float size) {
        return new Font(FAMILY, Font.PLAIN, Math.round(size));
    }

    /** คืนฟอนต์ตัวหนาสำหรับป้ายและปุ่ม */
    public static Font bold(float size) {
        return new Font(FAMILY, Font.BOLD, Math.round(size));
    }

    /** คืนฟอนต์สำหรับเนื้อหาหนังสือ */
    public static Font read(float size) {
        return new Font(FAMILY, Font.PLAIN, Math.round(size));
    }

    /** ตั้งฟอนต์มาตรฐานให้ส่วนประกอบ Swing ก่อนสร้างหน้าจอ */
    public static void install() {
        FontUIResource standardFont = new FontUIResource(body(14));
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, standardFont);
            }
        }
    }
}
