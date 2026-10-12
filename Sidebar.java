package ui.components;

// ไกด์ไฟล์: หน้าที่: สร้างเมนูด้านข้าง ชื่อหน้าปัจจุบัน และกล่องข้อมูลสมาชิก/แอดมิน
// MENU กำหนดชื่อและลำดับเมนู; makeItem สร้างรายการและเรียก Nav เมื่อคลิก
// แก้ชื่อเมนูใน MENU และตรวจ Nav.openMenu ให้รองรับชื่อเดียวกัน

import app.Nav;
import app.UserSession;
import app.AdminSession;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.styles.Fonts;
import ui.styles.Theme;

/**
 * แถบเมนูด้านซ้ายที่ใช้ในหน้าแรก รายการโปรด และหมวดหมู่
 *
 * <p>เมนูของหน้าปัจจุบันแสดงพื้นหลังเด่นกว่ารายการอื่น และชื่อบัญชีด้านล่าง
 * อ่านจากผู้ใช้ที่ล็อกอินอยู่</p>
 */
public class Sidebar extends JPanel {

    private static final String[] MENU = {"หน้าแรก", "รายการโปรด", "หมวดหมู่", "ออกจากระบบ"};

    /**
     * สร้างแถบเมนูและแสดงชื่อหน้าปัจจุบันให้เห็นชัด
     *
     * @param active ชื่อเมนูของหน้าที่กำลังเปิดอยู่
     */
    public Sidebar(String active) {
        setLayout(new BorderLayout(0, 14));
        setBackground(Theme.sidebar());
        setPreferredSize(new Dimension(200, 100));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.line()), // เส้นแบ่งด้านขวา
                new EmptyBorder(24, 14, 16, 14)));

        // แสดงชื่อแอปด้านบน และเส้นคั่นเพื่อแยกจากเมนู
        JLabel logo = new JLabel("GoodBook");
        logo.setFont(Fonts.title(24));
        logo.setForeground(Theme.accent());
        logo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.line()),
                new EmptyBorder(0, 8, 12, 0)));
        add(logo, BorderLayout.NORTH);

        // วางเมนูแต่ละรายการเรียงลงมาในพื้นที่ตรงกลาง
        JPanel list = new JPanel(new GridLayout(0, 1, 0, 4));
        list.setOpaque(false);
        for (String name : MENU) {
            list.add(makeItem(name, name.equals(active)));
        }
        JPanel listWrapper = new JPanel(new BorderLayout());
        listWrapper.setOpaque(false);
        listWrapper.add(list, BorderLayout.NORTH);
        add(listWrapper, BorderLayout.CENTER);

        // ปุ่มและข้อมูลผู้ใช้จะอยู่ด้านล่าง แม้รายการเมนูจะมีความสูงต่างกัน
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);
        bottom.add(makeUserCard(), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    /*
     * สร้างเมนูหนึ่งรายการ เมนูที่เปิดอยู่แสดงเป็นข้อความเฉย ๆ
     * ส่วนเมนูอื่นคลิกได้และส่งชื่อไปให้ Nav เลือกหน้า
     */
    private JComponent makeItem(String name, boolean active) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(active);
        if (active) {
            item.setBackground(Theme.card());
            item.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(9, 14, 9, 14)));
        }
        else {
            item.setBorder(new EmptyBorder(10, 15, 10, 15));
        }
        JLabel label = new JLabel(name);
        label.setFont(active ? Fonts.bold(14) : Fonts.body(14));
        label.setForeground(active ? Theme.text() : Theme.muted());
        item.add(label);
        if (!active) {
            Nav.onClick(item, () -> Nav.openMenu(item, name));
        }
        return item;
    }

    /*
     * สร้างกล่องชื่อผู้ใช้ของบัญชีที่ล็อกอินอยู่
     */
    private JComponent makeUserCard() {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(Theme.card());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(9, 12, 9, 12)));

        JPanel avatar = new JPanel(new GridBagLayout());
        avatar.setBackground(Theme.accentFill());
        avatar.setPreferredSize(new Dimension(36, 36));
        String username = AdminSession.isLoggedIn()
                ? AdminSession.username() : UserSession.username();
        String displayName = username.isEmpty() ? "ผู้ใช้" : username;
        JLabel letter = new JLabel(displayName.substring(0, 1).toUpperCase());
        letter.setFont(Fonts.bold(16));
        letter.setForeground(Color.WHITE);
        avatar.add(letter);
        card.add(avatar, BorderLayout.WEST);

        JPanel names = new JPanel(new GridLayout(2, 1));
        names.setOpaque(false);
        JLabel name = new JLabel(displayName);
        name.setFont(Fonts.bold(14));
        name.setForeground(Theme.text());
        JLabel role = new JLabel(AdminSession.isLoggedIn() ? "ผู้ดูแลระบบ" : "บัญชีในเครื่อง");
        role.setFont(Fonts.body(12));
        role.setForeground(Theme.muted());
        names.add(name);
        names.add(role);
        card.add(names, BorderLayout.CENTER);

        return card;
    }
}
