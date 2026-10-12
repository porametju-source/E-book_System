package ui.components;

// ไกด์ไฟล์: หน้าที่: วาดแผงแบรนด์ที่ใช้ในหน้าล็อกอินและสมัครสมาชิก
// constructor ตั้งขนาดและองค์ประกอบ; paintComponent วาดพื้นหลัง/ตกแต่ง
// แก้ชื่อหรือข้อความแบรนด์ใน constructor และแก้ภาพวาดใน paintComponent

import java.awt.*;
import javax.swing.*;
import ui.styles.Fonts;

/**
 * แผงชื่อแบรนด์ที่ใช้ในหน้าเข้าสู่ระบบและสมัครสมาชิก
 *
 * <p>ข้อความ ลวดลาย และรูปสันหนังสือวาดด้วย Graphics ทั้งหมด
 * จึงไม่ต้องใช้รูปภาพภายนอก และยังปรับตามขนาดพื้นที่ของแผงได้</p>
 */
public class BrandPanel extends JPanel {

    private static final Color[] SPINE_COLORS = {
        new Color(0x2F5D3A), new Color(0x6B2A8F), new Color(0x1F3F7A), new Color(0xB8893B), new Color(0x7A2B2B)
    };
    private static final int[] SPINE_HEIGHTS = {96, 126, 84, 110, 92};

    /** กำหนดขนาดเริ่มต้นให้พอสำหรับวาดชื่อแอปและลวดลายประกอบ */
    public BrandPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(290, 100));
    }

    /** วาดชื่อ GoodBook และรูปสันหนังสือด้วย Java Swing */
    @Override
    protected void paintComponent(Graphics g) {
        // ใช้สำเนา Graphics เพื่อวาดโดยไม่เปลี่ยนค่าของส่วนประกอบอื่น
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        Color cream = new Color(0xF6ECD6);
        Color gold = new Color(217, 178, 111);

        // วาดพื้นแดงไล่เฉดให้เข้ากับสีหลักของแอป
        g2.setPaint(new GradientPaint(0, 0, new Color(0xA52A32), w, h,
                new Color(0x64151D)));
        g2.fillRoundRect(0, 0, w + 40, h, 28, 28);

        // วาดกรอบสีทองบาง ๆ
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(gold.getRed(), gold.getGreen(), gold.getBlue(), 125));
        g2.drawRoundRect(12, 12, w - 26, h - 25, 18, 18);
        g2.setColor(new Color(255, 255, 255, 22));
        g2.drawRoundRect(17, 17, w - 36, h - 35, 15, 15);

        // วางชื่อแอปและคำโปรยไว้กลางแผง
        int centerX = w / 2;
        int brandTop = 42;
        g2.setColor(cream);
        g2.setFont(Fonts.bold(15));
        FontMetrics smallMetrics = g2.getFontMetrics();
        int kuY = brandTop;
        int kuWidth = smallMetrics.stringWidth("KU");
        g2.setColor(gold);
        g2.drawLine(centerX - 48, kuY - 2, centerX - kuWidth / 2 - 10, kuY - 2);
        g2.drawLine(centerX + kuWidth / 2 + 10, kuY - 2, centerX + 48, kuY - 2);
        g2.setColor(cream);
        g2.drawString("KU", centerX - kuWidth / 2, kuY);

        g2.setFont(Fonts.title(38));
        FontMetrics brandMetrics = g2.getFontMetrics();
        int goodBookY = kuY + 49;
        g2.drawString("GoodBook", centerX - brandMetrics.stringWidth("GoodBook") / 2, goodBookY);

        g2.setColor(gold);
        g2.setStroke(new BasicStroke(1.2f));
        int dividerY = goodBookY + 20;
        g2.drawLine(centerX - 54, dividerY, centerX + 54, dividerY);
        g2.fillPolygon(
                new int[]{centerX, centerX + 4, centerX, centerX - 4},
                new int[]{dividerY - 4, dividerY, dividerY + 4, dividerY}, 4);

        g2.setColor(cream);
        g2.setFont(Fonts.body(16));
        FontMetrics taglineMetrics = g2.getFontMetrics();
        String tagline = "พลิกหน้ากระดาษ";
        g2.drawString(tagline, centerX - taglineMetrics.stringWidth(tagline) / 2, dividerY + 29);
        String taglineSecondLine = "อ่านเรื่องที่ใช่";
        g2.drawString(taglineSecondLine, centerX - taglineMetrics.stringWidth(taglineSecondLine) / 2,
                dividerY + 53);

        // ปรับสันหนังสือให้เล็กลงเมื่อแผงเตี้ย เพื่อให้รูปยังอยู่ในพื้นที่
        double scale = Math.min(1.0, Math.max(0.55, (h * 0.27) / 126.0));
        int shelfY = h - 38;
        int spineWidth = (int) Math.round(30 * scale);
        int spacing = (int) Math.round(36 * scale);
        int totalWidth = SPINE_COLORS.length * spineWidth
                + (SPINE_COLORS.length - 1) * (spacing - spineWidth);
        int x = (w - totalWidth) / 2;
        for (int i = 0; i < SPINE_COLORS.length; i++) {
            int spineHeight = (int) Math.round(SPINE_HEIGHTS[i] * scale);
            int top = shelfY - spineHeight;
            g2.setColor(SPINE_COLORS[i]);
            g2.fillRoundRect(x, top, spineWidth, spineHeight, 4, 4);
            g2.setColor(gold);
            g2.fillRect(x, top + (int) (12 * scale), spineWidth, Math.max(2, (int) (3 * scale)));
            g2.fillRect(x, top + (int) (20 * scale), spineWidth, Math.max(2, (int) (3 * scale)));
            x += spacing;
        }
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(28, shelfY, w - 58, 4, 4, 4);
        g2.dispose();
    }
}
