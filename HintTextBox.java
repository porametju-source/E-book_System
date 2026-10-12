package ui.components;

// ไกด์ไฟล์: หน้าที่: ห่อ JTextArea เพื่อแสดงข้อความแนะนำเมื่อยังไม่มีเนื้อหา
// constructor รับจำนวนแถว/คอลัมน์และคำใบ้; getTextArea คืนช่องจริงให้หน้า UI อ่านค่า
// แก้รูปแบบช่องและคำใบ้ในคลาสนี้; ขนาดและสีของแต่ละฟอร์มกำหนดจากหน้าที่เรียกใช้

import ui.styles.Fonts;
import ui.styles.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * กล่องพิมพ์ข้อความหลายบรรทัดพร้อมข้อความแนะนำ
 *
 * <p>ข้อความแนะนำจะวาดทับบนช่องเฉพาะตอนที่ยังไม่มีข้อความ
 * จึงไม่ถูกรวมเป็นข้อมูลที่ผู้ใช้พิมพ์ และหายไปทันทีเมื่อเริ่มเขียน</p>
 */
public class HintTextBox extends JPanel {
    private final JTextArea area;

    /**
     * สร้างกล่องข้อความที่ตัดบรรทัดให้อัตโนมัติและแสดงคำใบ้ก่อนพิมพ์
     *
     * @param rows จำนวนบรรทัดเริ่มต้น
     * @param columns จำนวนคอลัมน์เริ่มต้น
     * @param hint ข้อความที่แสดงก่อนพิมพ์
     */
    public HintTextBox(int rows, int columns, String hint) {
        super(new BorderLayout());
        setBackground(Theme.tile());
        setBorder(BorderFactory.createLineBorder(Theme.line()));
        area = new JTextArea(rows, columns) {
            /** วาดข้อความแนะนำเมื่อช่องหลายบรรทัดยังว่าง */
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // วาดคำใบ้หลังจากวาดพื้นและข้อความจริงแล้ว
                InputField.drawHint(g, this, hint);
            }
        };
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Fonts.body(14));
        area.setForeground(Theme.text());
        area.setCaretColor(Theme.text());
        area.setBorder(new EmptyBorder(12, 16, 12, 16));
        add(area, BorderLayout.CENTER);
    }

    /** คืนช่องข้อความจริง เพื่อให้หน้า UI อ่านหรือแก้ข้อความที่กรอกได้ */
    public JTextArea getTextArea() { return area; }
}
