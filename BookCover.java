package ui.components;

// ไกด์ไฟล์: หน้าที่: วาดหน้าปกหนังสือจากข้อมูลและสีที่กำหนด
// constructor รับหนังสือและขนาด; wrap จัดข้อความหลายบรรทัด; paintComponent วาดปก
// แก้แบบปกและตำแหน่งข้อความใน paintComponent โดยระวังพิกัดและขนาดที่รับเข้ามา

import app.ProjectPaths;
import books.Book;
import ui.styles.Fonts;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * วาดปกหนังสือจากข้อมูลใน Book โดยไม่ต้องเตรียมไฟล์รูปแยก
 *
 * <p>ปกประกอบด้วยพื้นไล่สี เงา สันหนังสือ กรอบ และชื่อเรื่อง
 * การวาดจะเกิดขึ้นใหม่เมื่อ Swing ต้องแสดงส่วนประกอบนี้</p>
 */
public class BookCover extends JComponent {

    /** ข้อมูลที่ใช้เลือกสีและชื่อบนปก */
    private final Book book;
    private final ImageIcon coverImage;

    /**
     * กำหนดหนังสือและขนาดของปก
     *
     * @param book หนังสือที่จะแสดง
     * @param width ความกว้างปก
     * @param height ความสูงปก
     */
    public BookCover(Book book, int width, int height) {
        this.book = book;
        ImageIcon image = null;
        if (!book.coverPath.isEmpty()) {
            Path imagePath = ProjectPaths.dataFolder().resolve(book.coverPath);
            if (Files.exists(imagePath)) {
                image = new ImageIcon(imagePath.toString());
            }
        }
        coverImage = image;
        setPreferredSize(new Dimension(width, height));
    }

    /*
     * แบ่งชื่อเรื่องเป็นหลายบรรทัดตามความกว้างที่ปกมี
     * ปกติจะแบ่งตรงช่องว่างก่อน หากคำยาวจนเกินพื้นที่ จะค่อยแบ่งคำนั้นเป็นตัวอักษร
     */
    private ArrayList<String> wrap(String text, FontMetrics fm, int maxWidth) {
        ArrayList<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String attempt = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(attempt) <= maxWidth) {
                line = new StringBuilder(attempt);
                continue;
            }
            if (line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder();
            }
            for (char c : word.toCharArray()) {
                if (fm.stringWidth(line.toString() + c) > maxWidth) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
                line.append(c);
            }
        }
        lines.add(line.toString());
        return lines;
    }

    // ลดค่าสีแดง เขียว และน้ำเงินลง เพื่อสร้างสีเข้มจากสีปกเดิม
    private Color darken(Color c, double amount) {
        double k = 1 - amount;
        return new Color((int) (c.getRed() * k), (int) (c.getGreen() * k), (int) (c.getBlue() * k));
    }

    @Override
    protected void paintComponent(Graphics g) {
        // วาดบนสำเนา Graphics เพื่อไม่ให้การตั้งค่ากระทบส่วนประกอบอื่น
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // เว้นที่ด้านขวาและด้านล่างไว้ให้เงาไม่ถูกตัดขอบ
        int w = getWidth() - 8;
        int h = getHeight() - 8;
        Shape cover = new RoundRectangle2D.Float(0, 0, w, h, 10, 10);
        Shape oldClip = g2.getClip();

        // ใช้รูปปกจากไฟล์เมื่อแอดมินเพิ่มรูปไว้
        if (coverImage != null && coverImage.getIconWidth() > 0) {
            // ตัดพื้นที่วาดให้พอดีกับปก โดยยังรักษาขอบเขตที่ Swing กำหนดไว้
            g2.clip(cover);
            g2.drawImage(coverImage.getImage(), 0, 0, w, h, this);
            g2.setClip(oldClip);
            g2.setColor(new Color(217, 178, 111, 190));
            g2.draw(cover);
            g2.dispose();
            return;
        }

        // วาดเงาของปก
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(7, 8, w, h, 10, 10);

        // วาดพื้นปกไล่สี
        g2.setPaint(new GradientPaint(0, 0, book.color, 0, h, darken(book.color, 0.65)));
        g2.fill(cover);

        // จำกัดการวาดสันให้อยู่ในรูปทรงปก เพื่อไม่ให้สีล้ำออกนอกขอบมน
        // clip() จะตัดพื้นที่เพิ่มจากขอบเขตเดิม ป้องกันลายปกวาดทับส่วนอื่นของหน้าจอ
        g2.clip(cover);
        g2.setPaint(new GradientPaint(0, 0, new Color(0, 0, 0, 110), 16, 0, new Color(255, 255, 255, 30)));
        g2.fillRect(0, 0, 16, h);
        g2.setClip(oldClip);

        // วาดกรอบสีทอง
        g2.setColor(new Color(217, 178, 111, 190));
        g2.drawRoundRect(22, 10, w - 32, h - 21, 6, 6);

        // วัดความยาวแต่ละบรรทัดก่อนวาด เพื่อจัดชื่อให้อยู่กึ่งกลางแนวนอน
        g2.setFont(Fonts.title(Math.max(12, w / 12)));
        g2.setColor(new Color(0xF6ECD6));
        FontMetrics fm = g2.getFontMetrics();
        ArrayList<String> lines = wrap(book.titleTh, fm, w - 56);
        int y = (h - lines.size() * fm.getHeight()) / 2 + fm.getAscent();
        for (String line : lines) {
            g2.drawString(line, 16 + (w - 16 - fm.stringWidth(line)) / 2, y);
            y += fm.getHeight();
        }
        g2.dispose();
    }
}
