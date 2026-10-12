package ui.pages;

// ไกด์ไฟล์: หน้าที่: ฟอร์มสมัครสมาชิกและแจ้งผลการสมัคร
// attemptRegister ตรวจข้อมูลและแสดงผล; กฎขั้นต่ำอยู่ใน AccountLogic; ตรวจชื่อ/อีเมลซ้ำจริงอยู่ใน AccountStorage
// แก้ช่องและข้อความหน้าแบบฟอร์มใน constructor; การแจ้งข้อผิดพลาดรวมอยู่ใน showRegistrationError

import app.Nav;
import logic.account.AccountLogic;
import ui.components.BrandPanel;
import ui.components.InputField;
import ui.components.PillButton;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.styles.Fonts;
import ui.styles.Theme;

/**
 * หน้าสมัครสมาชิก
 *
 * <p>ตรวจข้อมูลในฟอร์มและบันทึกบัญชีลง `data/users.csv`</p>
 */
public class RegisterScreen extends JFrame {

    /**
     * สร้างฟอร์มสมัครสมาชิกและจัดวางไว้ข้างแผงแบรนด์
     * เพิ่มความสูงของการ์ดให้พอกับช่องกรอกและปุ่มทั้งหมด
     */
    public RegisterScreen() {
        Fonts.install();
        setTitle("KU Goodbook - สมัครสมาชิก");
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Theme.bg());
        setContentPane(body);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Theme.card());
        card.setBorder(BorderFactory.createLineBorder(Theme.line()));
        card.setBorder(new EmptyBorder(0, 0, 5, 5));
        // ฟอร์มสมัครมีช่องมากกว่าหน้าเข้าสู่ระบบ จึงใช้การ์ดที่สูงกว่า
        card.setPreferredSize(new Dimension(820, 640));
        body.add(card);

        card.add(new BrandPanel(), BorderLayout.WEST);

        // เรียงข้อมูลสมัครสมาชิกจากชื่อหน้าไปจนถึงลิงก์กลับหน้าเข้าสู่ระบบ
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(34, 44, 26, 44));
        card.add(form, BorderLayout.CENTER);

        JLabel title = new JLabel("สมัครสมาชิก");
        title.setFont(Fonts.title(28));
        title.setForeground(Theme.accent());
        addRow(form, title, 4);

        JLabel welcome = new JLabel("สร้างบัญชีเพื่อเริ่มใช้งาน KU GoodBook");
        welcome.setFont(Fonts.body(14));
        welcome.setForeground(Theme.muted());
        addRow(form, welcome, 18);

        addRow(form, makeLabel("ชื่อผู้ใช้"), 6);
        InputField usernameField = makeField("username", false);
        addRow(form, usernameField, 12);
        addRow(form, makeLabel("อีเมล"), 6);
        InputField emailField = makeField("name@example.com", false);
        addRow(form, emailField, 12);
        addRow(form, makeLabel("รหัสผ่าน"), 6);
        InputField passwordField = makeField("••••••••", true);
        addRow(form, passwordField, 12);
        addRow(form, makeLabel("ยืนยันรหัสผ่าน"), 6);
        InputField confirmField = makeField("••••••••", true);
        addRow(form, confirmField, 14);

        JCheckBox accept = new JCheckBox("ฉันยอมรับเงื่อนไขการใช้งาน");
        accept.setOpaque(false);
        accept.setFont(Fonts.body(13));
        accept.setForeground(Theme.muted());
        accept.setFocusPainted(false);
        addRow(form, accept, 16);

        PillButton registerButton = new PillButton("สมัครสมาชิก", PillButton.PRIMARY);
        registerButton.setFont(Fonts.bold(15));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        // รับค่าจากฟอร์ม แล้วส่งไปตรวจในเมธอดด้านล่าง
        registerButton.addActionListener(e -> {
            attemptRegister(
                    usernameField.getTextComponent().getText(),
                    emailField.getTextComponent().getText(),
                    passwordField.getTextComponent().getText(),
                    confirmField.getTextComponent().getText(),
                    accept.isSelected());
        });
        addRow(form, registerButton, 16);

        JPanel link = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        link.setOpaque(false);
        JLabel ask = new JLabel("มีบัญชีอยู่แล้ว?");
        ask.setFont(Fonts.body(13));
        ask.setForeground(Theme.muted());
        JLabel go = new JLabel("เข้าสู่ระบบ");
        go.setFont(Fonts.bold(13));
        go.setForeground(Theme.accent());
        Nav.onClick(go, () -> Nav.openLogin(this));
        link.add(ask);
        link.add(go);
        addRow(form, link, 0);

        setVisible(true);
    }

    /** ตรวจข้อมูลทีละข้อ แล้วบันทึกบัญชีลง users.csv เมื่อข้อมูลครบ */
    private void attemptRegister(String username, String email, String password,
                                 String confirmation, boolean acceptedTerms) {
        // ตัดช่องว่างหัวท้ายของชื่อก่อนตรวจและบันทึก
        username = username.trim();
        // ตัดช่องว่างหัวท้ายของอีเมลก่อนตรวจและบันทึก
        email = email.trim();

        // ส่งข้อมูลทั้งหมดไปตรวจ เช่น ช่องว่าง อีเมล รหัสผ่าน และการยอมรับเงื่อนไข
        String validationMessage = AccountLogic.validateRegistration(
                username, email, password, confirmation, acceptedTerms);
        // ถ้ามีข้อความผิดพลาด แปลว่าข้อมูลไม่ผ่านการตรวจ
        if (!validationMessage.isEmpty()) {
            // แสดงสาเหตุให้ผู้ใช้แก้ แล้วหยุดการสมัครในรอบนี้
            showRegistrationError(validationMessage, "ข้อมูลสมัครสมาชิกไม่ถูกต้อง");
            return;
        }

        // ประกาศตัวแปรรับผล: true คือสมัครได้, false คืออีเมลหรือชื่อซ้ำ
        boolean wasRegistered;
        try {
            // ให้ Logic และ Storage ตรวจข้อมูลซ้ำ แล้วบันทึกบัญชีถ้าผ่าน
            wasRegistered = AccountLogic.register(username, email, password);
        } catch (java.io.IOException error) {
            // ถ้าอ่านหรือเขียนไฟล์ไม่ได้ ให้แสดงข้อผิดพลาดและหยุดทำงาน
            showRegistrationError(error.getMessage(), "บันทึก users.csv ไม่สำเร็จ");
            return;
        }

        // ถ้าบันทึกไม่สำเร็จ แสดงว่าเจออีเมลหรือชื่อผู้ใช้ที่มีคนใช้แล้ว
        if (!wasRegistered) {
            // บอกให้เลือกอีเมลหรือชื่อใหม่ แล้วหยุดก่อนแสดงว่าสมัครสำเร็จ
            showRegistrationError("อีเมลหรือชื่อผู้ใช้นี้มีคนใช้แล้ว กรุณาเลือกข้อมูลใหม่",
                    "ข้อมูลซ้ำ");
            return;
        }

        // สมัครสำเร็จแล้ว แสดงตำแหน่งไฟล์ที่บันทึกบัญชีไว้
        JOptionPane.showMessageDialog(this,
                "สมัครสมาชิกสำเร็จแล้ว\nบันทึกบัญชีไว้ที่:\n"
                        + AccountLogic.userFilePath(),
                "สมัครสมาชิกสำเร็จ", JOptionPane.INFORMATION_MESSAGE);
        // เปิดหน้ารายการหนังสือและปิดหน้าสมัครสมาชิก
        Nav.openHome(this);
    }

    /** แสดงข้อความผิดพลาดในรูปแบบเดียวกันทุกกรณี */
    private void showRegistrationError(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * สร้างป้ายชื่อช่องกรอกด้วยรูปแบบเดียวกันทั้งฟอร์ม
     *
     * @param text ข้อความกำกับช่อง
     * @return ป้ายข้อความที่ตั้งฟอนต์และสีไว้แล้ว
     */
    private static JLabel makeLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Fonts.bold(13));
        label.setForeground(Theme.text());
        return label;
    }

    /**
     * สร้างช่องกรอกพร้อมคำแนะนำและกำหนดความสูงให้คงที่
     *
     * @param hint ข้อความแนะนำก่อนเริ่มพิมพ์
     * @param password ระบุว่าให้ซ่อนตัวอักษรหรือไม่
     * @return ช่องกรอกที่พร้อมเพิ่มลงในฟอร์ม
     */
    private static InputField makeField(String hint, boolean password) {
        InputField field = new InputField(hint, password);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return field;
    }

    /**
     * เพิ่มส่วนประกอบลงในฟอร์ม แล้วเว้นระยะก่อนแถวถัดไป
     *
     * @param form ฟอร์มที่ใช้เรียงส่วนประกอบ
     * @param c ส่วนประกอบที่จะเพิ่ม เช่น ป้ายหรือปุ่ม
     * @param gap ระยะห่างด้านล่าง หน่วยเป็นพิกเซล
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
        SwingUtilities.invokeLater(RegisterScreen::new);
    }
}
