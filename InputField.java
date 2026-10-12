package ui.components;

// ไกด์ไฟล์: หน้าที่: ช่องกรอกข้อความแบบใช้ซ้ำ รองรับทั้งข้อความทั่วไปและรหัสผ่าน
// constructor เลือกชนิดช่องและข้อความใบ้; getTextComponent ใช้อ่านค่าจากหน้าจอ
// แก้เส้นขอบ สี และการวาดคำใบ้ใน paintComponent; ตรวจทุกหน้าที่สร้าง InputField หลังเปลี่ยน API

import ui.styles.Fonts;
import ui.styles.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;

/**
 * ช่องกรอกข้อความบรรทัดเดียวพร้อมข้อความแนะนำ
 *
 * <p>เลือกใช้ช่องรหัสผ่านเมื่อ password เป็น true เพื่อซ่อนตัวอักษร
 * ส่วนช่องทั่วไปจะแสดงข้อความตามปกติ ทั้งสองแบบใช้คำใบ้และรูปแบบเดียวกัน</p>
 */
public class InputField extends JPanel {

    /**
     * สร้างช่องกรอกและเลือกชนิดให้ตรงกับข้อมูลที่ต้องการรับ
     *
     * @param hint ข้อความที่แสดงก่อนพิมพ์
     * @param password ใส่ true เพื่อซ่อนตัวอักษรที่พิมพ์
     */
    public InputField(String hint, boolean password) {
        super(new BorderLayout());
        setBackground(Theme.tile());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()),
                new EmptyBorder(0, 0, 0, 0)));
        JTextComponent field = password ? new PasswordWithHint(hint) : new TextWithHint(hint);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(0, 18, 0, 18));
        field.setFont(Fonts.body(14));
        field.setForeground(Theme.text());
        field.setCaretColor(Theme.text());
        add(field, BorderLayout.CENTER);
        setPreferredSize(new Dimension(200, 40));
    }

    /** คืนช่องกรอกจริง เพื่อให้หน้าจออ่านข้อความไปค้นหาหรือตรวจข้อมูลได้ */
    public JTextComponent getTextComponent() {
        return (JTextComponent) getComponent(0);
    }

    /*
     * วาดข้อความแนะนำเฉพาะตอนที่ช่องยังว่าง ข้อความนี้เป็นเพียงภาพที่วาดเพิ่ม
     * ไม่ได้ถูกใส่ลงในช่อง จึงไม่ถูกรวมไปกับข้อความที่ผู้ใช้พิมพ์
     */
    static void drawHint(Graphics g, JTextComponent c, String hint) {
        if (c.getDocument().getLength() > 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(c.getFont());
        g2.setColor(Theme.muted());
        FontMetrics fm = g2.getFontMetrics();
        int y = c.getBaseline(c.getWidth(), c.getHeight());
        if (y < 0) { // ถ้าจัดแนวข้อความไม่ได้ ให้วางไว้กลางช่อง
            y = (c.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        }
        g2.drawString(hint, c.getInsets().left + 3, y);
        g2.dispose();
    }

    // ใช้ JTextField สำหรับช่องทั่วไป และวาดคำใบ้หลังวาดเนื้อหาในช่อง
    private static class TextWithHint extends JTextField {
        private final String hint;

        TextWithHint(String hint) {
            this.hint = hint;
        }

        /** วาดคำใบ้หลังจาก Swing วาดช่องกรอกข้อความแล้ว */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawHint(g, this, hint);
        }
    }

    // ใช้ JPasswordField เพื่อไม่ให้แสดงตัวอักษรจริงบนหน้าจอ
    private static class PasswordWithHint extends JPasswordField {
        private final String hint;

        PasswordWithHint(String hint) {
            this.hint = hint;
        }

        /** วาดคำใบ้ในช่องรหัสผ่านโดยไม่เพิ่มคำใบ้ลงในค่ารหัสผ่าน */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawHint(g, this, hint);
        }
    }
}
