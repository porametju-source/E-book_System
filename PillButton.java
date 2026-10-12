package ui.components;

// ไกด์ไฟล์: หน้าที่: ปุ่ม Swing รูปทรงมนที่ใช้ร่วมกันในหลายหน้า
// constructor รับข้อความและชนิดปุ่ม เช่น PRIMARY หรือ OUTLINE; paintComponent วาดหน้าตาปุ่ม
// แก้สี/ขนาด/การวาดที่นี่ แล้วตรวจผลทุกหน้าที่สร้าง PillButton

import ui.styles.Fonts;
import ui.styles.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * ปุ่มทรงมนที่ใช้รูปแบบสีหลัก สีรอง หรือสีเตือน
 *
 * <p>คลาสนี้ดูแลเฉพาะหน้าตาของปุ่ม การทำงานหลังคลิกต้องกำหนดเพิ่ม
 * ด้วย ActionListener หรือคำสั่งคลิกจากหน้าจอที่นำปุ่มไปใช้</p>
 */
public class PillButton extends JButton {

    /** ปุ่มหลัก พื้นสีแดงและข้อความสีขาว */
    public static final int PRIMARY = 0;

    /** ปุ่มรอง พื้นสีอ่อนและมีเส้นขอบ */
    public static final int OUTLINE = 1;

    /** ปุ่มลบ ใช้ข้อความสีแดง */
    public static final int DANGER = 2;

    /** เก็บรูปแบบสีไว้ใช้ตอนวาดปุ่ม */
    private final int style;

    /**
     * สร้างปุ่มพร้อมข้อความและรูปแบบสีที่เลือก
     *
     * @param text ข้อความบนปุ่ม
     * @param style เลือก PRIMARY, OUTLINE หรือ DANGER
     */
    public PillButton(String text, int style) {
        super(text);
        this.style = style;
        setFont(Fonts.bold(13));
        setForeground(style == PRIMARY ? Color.WHITE : (style == DANGER ? Theme.accent() : Theme.text()));
        // ปิดพื้นและเส้นขอบมาตรฐาน เพราะคลาสนี้วาดพื้นปุ่มเอง
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setMargin(new Insets(6, 18, 6, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** วาดพื้นปุ่มทรงมน แล้วให้ Swing วาดข้อความและสถานะปุ่ม */
    @Override
    protected void paintComponent(Graphics g) {
        // วาดพื้นก่อน แล้วใช้ JButton วาดข้อความและสถานะของปุ่มตามปกติ
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 1;
        int h = getHeight() - 1;

        if (style == PRIMARY) {
            g2.setColor(Theme.accentFill());
            g2.fillRoundRect(0, 0, w, h, h, h);
        } else {
            g2.setColor(Theme.card());
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(style == DANGER ? Theme.accent() : Theme.line());
            g2.drawRoundRect(0, 0, w, h, h, h);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
