package ui.pages;

// ไกด์ไฟล์: หน้าที่: ฟอร์มเข้าสู่ระบบเดียวสำหรับสมาชิกและแอดมิน
// login ตรวจ AdminLogic ก่อน แล้วตรวจ AccountLogic; แอดมินไปหน้าแรกในสถานะแอดมิน ส่วนสมาชิกไปหน้าแรกในสถานะสมาชิก
// แก้ช่องและปุ่มใน constructor; แก้เส้นทางหลังล็อกอินใน login

import app.Nav;
import app.AdminSession;
import app.UserSession;
import logic.account.AccountLogic;
import logic.admin.AdminLogic;
import ui.components.BrandPanel;
import ui.components.InputField;
import ui.components.PillButton;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.styles.Fonts;
import ui.styles.Theme;

/**
 * หน้าเข้าสู่ระบบ
 *
 * <p>ตรวจอีเมลและรหัสผ่านจาก `data/users.csv`
 * แล้วโหลดชื่อผู้ใช้และอีเมลของบัญชีที่เข้าสู่ระบบ</p>
 */
public class LoginScreen extends JFrame {

    /**
     * สร้างหน้าต่าง ฟอร์มเข้าสู่ระบบ และลิงก์ไปหน้าสมัครสมาชิก
     * หลังจัดวางส่วนต่าง ๆ แล้วจึงแสดงหน้าต่างให้ผู้ใช้เห็น
     */
    public LoginScreen() {
        Fonts.install();
        setTitle("KU Goodbook - เข้าสู่ระบบ");
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ใช้ GridBagLayout เพื่อวางการ์ดให้อยู่กลางพื้นที่หน้าต่าง
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.bg());
        setContentPane(body);

        // การ์ดหลักแบ่งเป็นแผงแบรนด์ด้านซ้ายและฟอร์มด้านขวา
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.card());
        card.setBorder(BorderFactory.createLineBorder(Theme.line()));
        card.setBorder(new EmptyBorder(0, 0, 5, 5)); // เว้นที่ให้เงา
        card.setPreferredSize(new Dimension(820, 480));
        body.add(card);

        card.add(new BrandPanel(), BorderLayout.WEST);

        // เรียงหัวข้อ คำอธิบาย ช่องกรอก และปุ่มจากบนลงล่าง
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(40, 44, 30, 44));
        card.add(form, BorderLayout.CENTER);

        JLabel title = new JLabel("เข้าสู่ระบบ");
        title.setFont(Fonts.title(28));
        title.setForeground(Theme.accent());
        addRow(form, title, 4);

        JLabel welcome = new JLabel("เข้าสู่ระบบด้วยอีเมลและรหัสผ่านที่สมัครไว้");
        welcome.setFont(Fonts.body(14));
        welcome.setForeground(Theme.muted());
        addRow(form, welcome, 22);

        addRow(form, makeLabel("อีเมล"), 6);
        InputField emailField = makeField("name@example.com", false);
        addRow(form, emailField, 16);
        addRow(form, makeLabel("รหัสผ่าน"), 6);
        InputField passwordField = makeField("••••••••", true);
        addRow(form, passwordField, 26);

        PillButton loginButton = new PillButton("เข้าสู่ระบบ", PillButton.PRIMARY);
        loginButton.setFont(Fonts.bold(15));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        // อ่านข้อมูลจากช่อง แล้วส่งให้เมธอดตรวจบัญชี
        loginButton.addActionListener(e -> {
            login(emailField.getTextComponent().getText(), passwordField.getTextComponent().getText());
        });
        addRow(form, loginButton, 18);

        // แสดงคำถามและข้อความที่คลิกได้เพื่อไปหน้าสมัครสมาชิก
        JPanel link = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        link.setOpaque(false);
        JLabel ask = new JLabel("ยังไม่มีบัญชี?");
        ask.setFont(Fonts.body(13));
        ask.setForeground(Theme.muted());
        JLabel go = new JLabel("สมัครสมาชิก");
        go.setFont(Fonts.bold(13));
        go.setForeground(Theme.accent());
        Nav.onClick(go, () -> Nav.openRegister(this));
        link.add(ask);
        link.add(go);
        addRow(form, link, 0);

        setVisible(true);
    }

    /** ตรวจบัญชี แล้วเปิดหน้าแรกเมื่อเข้าสู่ระบบสำเร็จ */
    private void login(String email, String password) {
        try {
            // ตรวจบัญชีแอดมินก่อน เพื่อให้บัญชีเดียวกันใช้หน้าเข้าสู่ระบบนี้ได้
            if (AdminLogic.login(email, password)) {
                UserSession.logout();
                Nav.openHome(this);
                return;
            }

            // ถ้าไม่ใช่แอดมิน ให้ตรวจต่อว่าเป็นบัญชีสมาชิกหรือไม่
            if (AccountLogic.login(email, password)) {
                AdminSession.logout();
                Nav.openHome(this);
                return;
            }
        } catch (java.io.IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "อ่านไฟล์บัญชีไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "อีเมลหรือรหัสผ่านไม่ถูกต้อง หรือยังไม่มีบัญชีนี้",
                "เข้าสู่ระบบไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * สร้างข้อความกำกับช่องกรอกให้ใช้รูปแบบเดียวกัน
     *
     * @param text ข้อความที่ต้องการแสดง
     * @return ป้ายข้อความที่ตั้งฟอนต์และสีไว้แล้ว
     */
    private static JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.bold(13));
        label.setForeground(Theme.text());
        return label;
    }

    /**
     * สร้างช่องกรอกและกำหนดความกว้างให้ขยายตามฟอร์ม
     *
     * @param hint ข้อความแนะนำก่อนเริ่มพิมพ์
     * @param password ระบุว่าเป็นช่องรหัสผ่านหรือไม่
     * @return ช่องกรอกที่พร้อมนำไปวางในฟอร์ม
     */
    private static InputField makeField(String hint, boolean password) {
        InputField field = new InputField(hint, password);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return field;
    }

    /*
     * เพิ่มส่วนประกอบหนึ่งชิ้นในฟอร์ม และเว้นช่องว่างด้านล่างตามที่กำหนด
     * การจัดชิดซ้ายช่วยให้หัวข้อ ช่องกรอก และปุ่มเริ่มตรงแนวเดียวกัน
     */
    private static void addRow(JPanel form, JComponent c, int gap) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(c);
        form.add(Box.createVerticalStrut(gap));
    }

    /**
     * เปิดหน้านี้โดยตรงเพื่อทดลองหน้าจอ โดยไม่ต้องเริ่มจาก Main
     *
     * @param args ค่าจากคำสั่งเปิดโปรแกรม (ยังไม่ได้ใช้)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
