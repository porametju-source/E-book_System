package ui.pages;

// ไกด์ไฟล์: หน้าที่: แสดงหมวดหมู่เป็นการ์ดและเปิดรายการหนังสือในหมวดที่เลือก
// refreshCategories โหลดและวาดหมวดใหม่; colorOf เลือกสี; CategoryTile วาดการ์ดหมวด
// แก้ชื่อหมวดเริ่มต้นใน NAMES; แก้ขั้นตอนคลิกเลือกหนังสือใน CategoryTile

import app.Nav;
import ui.components.InputField;
import ui.components.Sidebar;
import ui.components.SlimScrollBarUI;
import books.Book;
import logic.books.BookCatalogLogic;
import logic.books.CategoryLogic;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import ui.styles.Fonts;
import ui.styles.Theme;

/**
 * หน้าแสดงชื่อหมวดหมู่และจำนวนหนังสือในแต่ละหมวด
 *
 * <p>ค้นหาหมวด หนังสือ และผู้แต่งได้ การ์ดหมวดใช้เลือกหนังสือเพื่อเปิดหน้า
 * รายละเอียด โดยไม่แก้ข้อมูลต้นฉบับใน BookData</p>
 */
public class CategoriesScreen extends JFrame {

    /** ชื่อหมวดที่ต้องการแสดงบนหน้านี้ */
    private static final String[] NAMES = {"ทั้งหมด", "วรรณกรรม", "แฟนตาซี", "ปรัชญา", "ทั่วไป", "การศึกษา"};

    /** ช่องค้นหาหมวด หนังสือ และผู้แต่ง */
    private final InputField search = new InputField("ค้นหาหมวด หนังสือ หรือผู้แต่ง", false);

    /** พื้นที่วางการ์ดหมวดที่ตรงกับคำค้นหา */
    private final JPanel grid = new JPanel(new GridLayout(0, 3, 18, 18));

    /** สร้างหน้าหมวดหมู่ พร้อมช่องค้นหาและการ์ดหนังสือ */
    public CategoriesScreen() {
        Fonts.install();
        setTitle("KU Goodbook - หมวดหมู่");
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // บอก Sidebar ว่าหมวดหมู่เป็นหน้าที่กำลังเปิดอยู่
        add(new Sidebar("หมวดหมู่"), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(Theme.bg());
        main.setBorder(new EmptyBorder(22, 28, 0, 20));
        add(main, BorderLayout.CENTER);

        // วางชื่อและคำอธิบายไว้ซ้าย และช่องค้นหาไว้ทางขวา
        JLabel title = new JLabel("หมวดหมู่");
        title.setFont(Fonts.title(26));
        title.setForeground(Theme.accent());
        JLabel sub = new JLabel("เลือกหมวดที่อยากอ่าน");
        sub.setFont(Fonts.body(14));
        sub.setForeground(Theme.muted());
        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.setOpaque(false);
        texts.add(title);
        texts.add(sub);

        search.setPreferredSize(new Dimension(320, 44));
        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        searchWrap.setOpaque(false);
        searchWrap.add(search);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(texts, BorderLayout.WEST);
        topBar.add(searchWrap, BorderLayout.EAST);
        main.add(topBar, BorderLayout.NORTH);

        // สร้างการ์ดหมวดครั้งแรก แล้วตั้งให้สร้างใหม่ทุกครั้งที่ข้อความค้นหาเปลี่ยน
        grid.setOpaque(false);
        refreshCategories();
        search.getTextComponent().getDocument().addDocumentListener(new DocumentListener() {
            /** สร้างการ์ดใหม่เมื่อผู้ใช้พิมพ์คำค้นหาเพิ่ม */
            @Override
            public void insertUpdate(DocumentEvent e) { refreshCategories(); }
            /** สร้างการ์ดใหม่เมื่อผู้ใช้ลบคำค้นหา */
            @Override
            public void removeUpdate(DocumentEvent e) { refreshCategories(); }
            /** สร้างการ์ดใหม่เมื่อรูปแบบเอกสารคำค้นหาเปลี่ยน */
            @Override
            public void changedUpdate(DocumentEvent e) { refreshCategories(); }
        });
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 24, 10));
        wrapper.add(grid, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        main.add(scroll, BorderLayout.CENTER);

        setVisible(true);
    }

    /** ลบการ์ดเก่า แล้วแสดงเฉพาะหมวดที่มีชื่อหรือหนังสือตรงกับคำค้นหา */
    private void refreshCategories() {
        String query = search.getTextComponent().getText().trim();
        grid.removeAll();
        try {
            for (String name : CategoryLogic.allCategories(NAMES)) {
                List<Book> books = BookCatalogLogic.booksInCategory(name, query);
                List<Book> allBooks = BookCatalogLogic.booksInCategory(name, "");
                boolean categoryMatchesSearch = name.toLowerCase().contains(query.toLowerCase());
                if (!books.isEmpty() || (allBooks.isEmpty()
                        && (query.isEmpty() || categoryMatchesSearch))) {
                    grid.add(new CategoryTile(name, books, colorOf(name)));
                }
            }
        } catch (java.io.IOException error) {
            grid.add(new JLabel("โหลดหมวดหมู่ไม่สำเร็จ: " + error.getMessage()));
        }
        if (grid.getComponentCount() == 0) {
            grid.add(new JLabel("ไม่พบหมวดหรือหนังสือที่ค้นหา"));
        }
        grid.revalidate();
        grid.repaint();
    }

    /*
     * ใช้สีปกของหนังสือเล่มแรกในหมวดเพื่อให้การ์ดสื่อถึงเรื่องในหมวดนั้น
     * หมวด "ทั้งหมด" หรือหมวดที่ยังไม่มีหนังสือจะใช้สีแดงหลักของแอป
     */
    /** เลือกสีจากปกเล่มแรกในหมวด หรือใช้สีหลักถ้าไม่มีหนังสือ */
    private static Color colorOf(String category) {
        Book book = BookCatalogLogic.firstBookInCategory(category);
        if (category.equals("ทั้งหมด") || book == null) {
            return new Color(0x8B1E24);
        }
        return book.color;
    }

    /**
     * วาดการ์ดหนึ่งใบที่แสดงชื่อหมวดและจำนวนหนังสือ
     * ใช้สีที่ส่งเข้ามาเป็นพื้นหลังและวาดลวดลายสันหนังสือประกอบ
     */
    private static class CategoryTile extends JComponent {
        private final String name;
        private final List<Book> books;
        private final Color color;

        /** สร้างการ์ดและผูกการคลิกเพื่อเลือกหนังสือในหมวด */
        CategoryTile(String name, List<Book> books, Color color) {
            this.name = name;
            this.books = books;
            this.color = color;
            setPreferredSize(new Dimension(300, 150));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                /** เปิดรายการหนังสือในหมวดเมื่อผู้ใช้คลิกการ์ด */
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    chooseBook();
                }
            });
        }

        /** เปิดกล่องเลือกชื่อหนังสือ แล้วไปหน้ารายละเอียดของเล่มที่เลือก */
        private void chooseBook() {
            if (books.isEmpty()) {
                JOptionPane.showMessageDialog(this, "หมวด “" + name + "” ยังไม่มีหนังสือ");
                return;
            }
            String[] titles = new String[books.size()];
            for (int i = 0; i < books.size(); i++) {
                titles[i] = books.get(i).titleTh;
            }

            String selectedTitle = (String) JOptionPane.showInputDialog(
                    this,
                    "เลือกหนังสือเพื่อเปิดรายละเอียด",
                    "หมวด " + name,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    titles,
                    titles[0]);

            if (selectedTitle == null) {
                return;
            }

            for (Book book : books) {
                if (book.titleTh.equals(selectedTitle)) {
                    Nav.openDetail(this, book);
                    return;
                }
            }
        }

        /** วาดพื้น การ์ด ชื่อหมวด และจำนวนหนังสือ */
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            // ลดพื้นที่วาดเล็กน้อยเพื่อเหลือที่ให้เงาโดยไม่ชนขอบ
            int w = getWidth() - 6;
            int h = getHeight() - 6;

            // เพิ่มความเข้มของเงาในโหมดกลางคืนเพื่อให้เห็นชัด
            g2.setColor(new Color(60, 35, 15, Theme.dark ? 90 : 45));
            g2.fillRoundRect(5, 5, w, h, 26, 26);

            Color dark = new Color((int) (color.getRed() * 0.4), (int) (color.getGreen() * 0.4), (int) (color.getBlue() * 0.4));
            // ใช้สีหมวดเป็นสีเริ่มต้น แล้วไล่ไปยังเฉดที่เข้มกว่า
            g2.setPaint(new GradientPaint(0, 0, color, w, h, dark));
            g2.fillRoundRect(0, 0, w, h, 26, 26);

            // กรอบสีทองและสันหนังสือเป็นลวดลายตกแต่งของการ์ด
            g2.setColor(new Color(217, 178, 111, 140));
            g2.drawRoundRect(9, 9, w - 18, h - 18, 18, 18);

            int[] spineHeights = {64, 92, 54};
            for (int i = 0; i < 3; i++) {
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRoundRect(w - 110 + i * 26, h - 22 - spineHeights[i], 20, spineHeights[i], 4, 4);
            }

            // ใช้สีอ่อนเพื่อให้อ่านชื่อหมวดบนพื้นเข้มได้ชัด
            g2.setColor(new Color(0xF6ECD6));
            g2.setFont(Fonts.title(24));
            g2.drawString(name, 28, h - 52);
            g2.setFont(Fonts.body(14));
            g2.drawString(books.size() + " เรื่อง · กดเพื่อเลือกหนังสือ", 28, h - 28);
            g2.dispose();
        }
    }

    /**
     * เปิดหน้านี้โดยตรงเพื่อทดลองหน้าจอ โดยไม่ต้องเริ่มจาก Main
     *
     * @param args ค่าจากคำสั่งเปิดโปรแกรม (ยังไม่ได้ใช้)
     */
    /** เปิดหน้าหมวดหมู่บนเธรด UI ของ Swing เพื่อทดลองหน้านี้โดยตรง */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(CategoriesScreen::new);
    }
}
