package ui.pages;

// ไกด์ไฟล์: หน้าที่: หน้าแรกแสดงหนังสือ ค้นหา และเรียงรายการ; แอดมินเห็นการ์ดเพิ่มหนังสือด้วย
// buildTopBar/buildBookList สร้างหน้า; refreshBooks วาดผลค้นหา; makeAddBookCard เปิด AdminScreen
// แก้การค้นหา/การเรียงใน BookCatalogLogic และแก้หน้าตารายการในคลาสนี้

import ui.components.BookCard;
import ui.components.InputField;
import ui.components.PillButton;
import ui.components.Sidebar;
import ui.components.SlimScrollBarUI;
import app.UserSession;
import app.AdminSession;
import app.Nav;
import logic.books.BookCatalogLogic;
import ui.pages.AdminScreen;
import books.Book;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.List;
import ui.styles.Fonts;
import ui.styles.Theme;

/**
 * หน้าแรกที่แสดงรายการหนังสือจาก BookData
 *
 * <p>การ์ดแต่ละใบเปิดหน้ารายละเอียดของหนังสือที่เลือกได้
 * ช่องค้นหาและตัวเลือกเรียงกรองข้อมูลในรายการที่แสดงบนหน้าจอ</p>
 */
public class HomeScreen extends JFrame {
    /** ช่องค้นหาหนังสือบนแถบบน */
    private final InputField search = new InputField("ค้นหาชื่อเรื่อง ผู้แต่ง", false);

    /** ตารางการ์ดหนังสือที่ถูกกรองแล้ว */
    private final JPanel grid = new JPanel(new GridLayout(0, 2, 16, 16));

    /** วิธีเรียงรายการปัจจุบัน */
    private String sort = "ลำดับเดิม";

    /** สร้างหน้าต่างหน้าแรก พร้อมแถบเมนู แถบด้านบน และรายการหนังสือ */
    public HomeScreen() {
        Fonts.install();
        setTitle("KU Goodbook - หน้าแรก");
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // สร้างแถบเมนู และบอก Sidebar ว่าหน้าแรกเป็นหน้าปัจจุบัน
        add(new Sidebar("หน้าแรก"), BorderLayout.WEST);

        // เว้นระยะระหว่างคำทักทายด้านบนกับรายการหนังสือ
        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(Theme.bg());
        main.setBorder(new EmptyBorder(22, 28, 0, 20));
        add(main, BorderLayout.CENTER);
        main.add(buildTopBar(), BorderLayout.NORTH);
        main.add(buildBookList(), BorderLayout.CENTER);

        setVisible(true);
    }

    /*
     * สร้างแถบด้านบนที่มีคำทักทายและช่องค้นหา
     * ช่องค้นหาใช้กรองชื่อเรื่อง ผู้แต่ง และหมวดหมู่แบบทันที
     */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        String currentName = AdminSession.isLoggedIn()
                ? AdminSession.username() : UserSession.username();
        JLabel hello = new JLabel("สวัสดี, " + currentName);
        hello.setFont(Fonts.title(22));
        hello.setForeground(Theme.text());
        JLabel sub = new JLabel("พร้อมหาเรื่องใหม่อ่านหรือยัง?");
        sub.setFont(Fonts.body(14));
        sub.setForeground(Theme.muted());
        JPanel texts = new JPanel(new GridLayout(2, 1));
        texts.setOpaque(false);
        texts.add(hello);
        texts.add(sub);

        JPanel left = new JPanel(new BorderLayout(14, 0));
        left.setOpaque(false);
        left.add(texts, BorderLayout.CENTER);
        bar.add(left, BorderLayout.WEST);

        search.setPreferredSize(new Dimension(320, 44));
        JPanel searchWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        searchWrap.setOpaque(false);
        searchWrap.add(search);
        bar.add(searchWrap, BorderLayout.EAST);
        return bar;
    }

    /*
     * สร้างรายการหนังสือแบบสามคอลัมน์และใส่ไว้ใน JScrollPane
     * เมื่อมีหนังสือมากกว่าพื้นที่ที่เห็น ผู้ใช้จึงเลื่อนลงไปดูรายการต่อได้
     */
    private JScrollPane buildBookList() {
        JLabel heading = new JLabel("หนังสือทั้งหมด");
        heading.setFont(Fonts.title(20));
        heading.setForeground(Theme.accent());
        JPanel headingRow = new JPanel(new BorderLayout());
        headingRow.setOpaque(false);
        headingRow.add(heading, BorderLayout.WEST);
        JComboBox<String> sortBox = new JComboBox<>(new String[]{"ลำดับเดิม", "ชื่อเรื่อง A-Z", "คะแนนรีวิวสูงสุด"});
        sortBox.setFont(Fonts.body(13));
        sortBox.addActionListener(e -> {
            sort = (String) sortBox.getSelectedItem();
            refreshBooks();
        });
        headingRow.add(sortBox, BorderLayout.EAST);

        // จำนวนแถวจะเพิ่มตามจำนวนหนังสือ โดยคงไว้สามคอลัมน์
        grid.setOpaque(false);
        refreshBooks();
        search.getTextComponent().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refreshBooks(); }
            @Override
            public void removeUpdate(DocumentEvent e) { refreshBooks(); }
            @Override
            public void changedUpdate(DocumentEvent e) { refreshBooks(); }
        });

        JPanel section = new JPanel(new BorderLayout(0, 14));
        section.setOpaque(false);
        section.add(headingRow, BorderLayout.NORTH);
        section.add(grid, BorderLayout.CENTER);

        // วางเนื้อหาไว้ด้านบนของพื้นที่เลื่อน ไม่ยืดการ์ดเมื่อมีรายการน้อย
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 24, 10));
        wrapper.add(section, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapper, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        // เลือกจำนวนคอลัมน์ตามพื้นที่จริง เพื่อไม่ให้การ์ดล้ำขอบหน้าจอ
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateGridColumns(scroll.getViewport().getWidth());
            }
        });
        return scroll;
    }

    /** แสดงสามคอลัมน์บนจอกว้าง สองคอลัมน์บนจอทั่วไป และหนึ่งคอลัมน์บนจอแคบ */
    private void updateGridColumns(int availableWidth) {
        if (availableWidth <= 0) {
            return;
        }
        int columns = availableWidth >= 1100 ? 3 : availableWidth >= 620 ? 2 : 1;
        GridLayout layout = (GridLayout) grid.getLayout();
        if (layout.getColumns() != columns) {
            grid.setLayout(new GridLayout(0, columns, 16, 16));
            grid.revalidate();
        }
    }

    /** กรอง เรียง และวาดการ์ดหนังสือใหม่ตามคำค้นหากับตัวเลือกเรียง */
    private void refreshBooks() {
        String query = search.getTextComponent().getText().trim();
        List<Book> books = BookCatalogLogic.searchAndSort(
                BookCatalogLogic.allBooks(), query, sort, true);
        grid.removeAll();
        for (Book book : books) {
            grid.add(new BookCard(book, false));
        }
        // ช่องเพิ่มหนังสือจะปรากฏในตำแหน่งถัดไป เฉพาะแอดมินเท่านั้น
        if (AdminSession.isLoggedIn() && query.isEmpty()) {
            grid.add(makeAddBookCard());
        }
        if (books.isEmpty() && !AdminSession.isLoggedIn()) {
            grid.add(new JLabel("ไม่พบหนังสือที่ค้นหา"));
        }
        grid.revalidate();
        grid.repaint();
    }

    /** สร้างการ์ดเครื่องหมายบวกที่เปิดฟอร์มเพิ่มหนังสือ */
    private JComponent makeAddBookCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.card());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(14, 14, 19, 19)));
        // ให้การ์ดเพิ่มหนังสือกว้างเท่าการ์ดปกติ ไม่บังคับให้ทั้งตารางกว้างเกินจอ
        card.setPreferredSize(new Dimension(270, 400));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel plus = new JLabel("+");
        plus.setFont(Fonts.title(72));
        plus.setForeground(Theme.accent());
        plus.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel text = new JLabel("เพิ่มหนังสือ");
        text.setFont(Fonts.bold(16));
        text.setForeground(Theme.text());
        text.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(plus);
        content.add(Box.createVerticalStrut(8));
        content.add(text);
        card.add(content);
        Nav.onClick(card, () -> {
            new AdminScreen();
            dispose();
        });
        return card;
    }

    /**
     * เปิดหน้านี้โดยตรงเพื่อทดลองหน้าจอ โดยไม่ต้องเริ่มจาก Main
     *
     * @param args ค่าจากคำสั่งเปิดโปรแกรม (ยังไม่ได้ใช้)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(HomeScreen::new);
    }
}
