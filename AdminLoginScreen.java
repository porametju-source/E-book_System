package ui.pages;

// ไกด์ไฟล์: หน้าที่: หน้าล็อกอินแอดมินแยกแบบเดิม ซึ่งไม่ใช่เส้นทางล็อกอินหลักแล้ว
// ระบบปัจจุบันใช้ LoginScreen หน้าเดียวและตรวจ AdminLogic ก่อนบัญชีสมาชิก
// ถ้าจะเปลี่ยนหน้าล็อกอินหลัก ให้แก้ LoginScreen; หน้านี้คงไว้เพื่อไม่ให้ลบโค้ดเดิมโดยไม่จำเป็น

import logic.admin.AdminLogic;
import app.Nav;
import ui.components.BrandPanel;
import ui.components.InputField;
import ui.components.PillButton;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import ui.styles.Fonts;
import ui.styles.Theme;

/** หน้าเข้าสู่ระบบแอดมิน แยกจากหน้าสมาชิก */
public class AdminLoginScreen extends JFrame {
    public AdminLoginScreen() {
        Fonts.install();
        setTitle("KU Goodbook - เข้าสู่ระบบแอดมิน");
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.bg());
        setContentPane(body);
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.card());
        card.setPreferredSize(new Dimension(820, 480));
        card.setBorder(new EmptyBorder(0, 0, 5, 5));
        body.add(card);
        card.add(new BrandPanel(), BorderLayout.WEST);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(42, 44, 30, 44));
        card.add(form, BorderLayout.CENTER);

        JLabel title = new JLabel("เข้าสู่ระบบแอดมิน");
        title.setFont(Fonts.title(26));
        title.setForeground(Theme.accent());
        addRow(form, title, 22);

        addRow(form, label("อีเมลแอดมิน"), 6);
        InputField email = new InputField("กรอกอีเมลแอดมิน", false);
        email.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addRow(form, email, 16);
        addRow(form, label("รหัสผ่าน"), 6);
        InputField password = new InputField("รหัสผ่าน", true);
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        addRow(form, password, 22);

        PillButton login = new PillButton("เข้าสู่ระบบแอดมิน", PillButton.PRIMARY);
        login.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        login.addActionListener(e -> login(email.getTextComponent().getText(),
                password.getTextComponent().getText()));
        addRow(form, login, 16);

        JLabel back = new JLabel("กลับไปหน้าเข้าสู่ระบบสมาชิก");
        back.setFont(Fonts.bold(13));
        back.setForeground(Theme.accent());
        Nav.onClick(back, () -> Nav.openLogin(this));
        JPanel link = new JPanel(new FlowLayout(FlowLayout.CENTER));
        link.setOpaque(false);
        link.add(back);
        addRow(form, link, 0);
        setVisible(true);
    }

    /** ตรวจบัญชีแอดมิน แล้วเปิดหน้าหลักเมื่อข้อมูลเข้าสู่ระบบถูกต้อง */
    private void login(String email, String password) {
        try {
            if (!AdminLogic.login(email, password)) {
                JOptionPane.showMessageDialog(this, "อีเมลหรือรหัสผ่านแอดมินไม่ถูกต้อง");
                return;
            }
            new ui.pages.HomeScreen();
            dispose();
        } catch (java.io.IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "เข้าสู่ระบบไม่ได้",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** สร้างป้ายชื่อช่องกรอกด้วยฟอนต์และสีมาตรฐานของโปรเจกต์ */
    private static JLabel label(String text) {
        JLabel result = new JLabel(text);
        result.setFont(Fonts.bold(13));
        result.setForeground(Theme.text());
        return result;
    }

    /** เพิ่มส่วนประกอบหนึ่งแถวลงในฟอร์มและเว้นระยะก่อนแถวถัดไป */
    private static void addRow(JPanel panel, JComponent component, int gap) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(component);
        panel.add(Box.createVerticalStrut(gap));
    }

    /** เปิดหน้าเข้าสู่ระบบแอดมินโดยตรงบนเธรด UI ของ Swing */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminLoginScreen::new);
    }
}
