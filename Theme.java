package ui.styles;

// ไกด์ไฟล์: หน้าที่: รวมค่าสีของพื้นหลัง แผง การ์ด ตัวอักษร และสีเน้น
// เมธอด bg/sidebar/card/text/accent คืนสีที่ส่วน UI เรียกใช้
// เปลี่ยนค่าสีในคลาสนี้เพื่อปรับธีมทั้งแอป โดยไม่ต้องไล่แก้ทุกหน้าทีละไฟล์

import java.awt.Color;

/**
 * รวมสีที่ใช้ในแอปไว้ที่เดียว เพื่อให้หน้าจอต่าง ๆ ใช้สีชุดเดียวกัน
 *
 * <p>เมื่อเปลี่ยนค่า dark สีที่เมธอดของคลาสนี้คืนให้จะเปลี่ยนตาม
 * ค่าของ dark ไม่ได้เปลี่ยนหน้าจอที่วาดไปแล้วให้อัตโนมัติ</p>
 */
public class Theme {

    /** false ใช้สีสว่าง ส่วน true เลือกชุดสีสำหรับโหมดกลางคืน */
    public static boolean dark = false;

    /** คลาสนี้เก็บค่าสีและเมธอด static จึงไม่ต้องสร้าง Theme */
    private Theme() {
    }

    // คืนค่าสีสำหรับโหมดปัจจุบัน โดยแปลงตัวเลขสีเป็นชนิด Color ของ Java
    private static Color pick(int light, int night) {
        return new Color(dark ? night : light);
    }

    /**
     * เลือกสีพื้นหลังตามโหมด
     *
     * @return สีพื้นหลัง
     */
    public static Color bg() {
        return pick(0xEEE3CD, 0x1C1711);
    }

    /**
     * เลือกสีพื้นแถบเมนูตามโหมด
     *
     * @return สีแถบเมนู
     */
    public static Color sidebar() {
        return pick(0xE5D6BA, 0x241D15);
    }

    /**
     * เลือกสีพื้นการ์ดตามโหมด
     *
     * @return สีพื้นการ์ด
     */
    public static Color card() {
        return pick(0xFBF6EA, 0x2C241B);
    }

    /**
     * เลือกสีพื้นช่องกรอกและช่องรายการตามโหมด
     *
     * @return สีพื้นช่อง
     */
    public static Color tile() {
        return pick(0xF2E8D2, 0x372E23);
    }

    /**
     * เลือกสีเส้นขอบตามโหมด
     *
     * @return สีเส้นขอบ
     */
    public static Color line() {
        return pick(0xDFD0B2, 0x41372A);
    }

    /**
     * เลือกสีข้อความหลักตามโหมด
     *
     * @return สีข้อความ
     */
    public static Color text() {
        return pick(0x2B1D14, 0xEFE4D0);
    }

    /**
     * เลือกสีข้อความรองตามโหมด
     *
     * @return สีข้อความรอง
     */
    public static Color muted() {
        return pick(0x8A7A66, 0xA89A86);
    }

    /**
     * เลือกสีเน้นสำหรับข้อความและเส้นตามโหมด
     *
     * @return สีเน้น
     */
    public static Color accent() {
        return pick(0x8B1E24, 0xD2676C);
    }

    /**
     * เลือกสีพื้นปุ่มตามโหมด
     *
     * @return สีปุ่ม
     */
    public static Color accentFill() {
        return pick(0x8B1E24, 0xA8343A);
    }

    /**
     * เลือกสีทองสำหรับลวดลายตามโหมด
     *
     * @return สีทอง
     */
    public static Color gold() {
        return pick(0xB8893B, 0xD9B26F);
    }

    /**
     * เลือกสีพื้นของตอนที่อ่านแล้วตามโหมด
     *
     * @return สีพื้นตอนที่อ่านแล้ว
     */
    public static Color readBg() {
        return pick(0xF0D9D3, 0x4A2A2C);
    }
}
