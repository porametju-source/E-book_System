package ui.pages;

// ไกด์ไฟล์: หน้าที่: แสดงรายการโปรดของผู้ใช้ พร้อมค้นหา เรียง และเอาออกจากรายการ
// refresh โหลดและวาดรายการใหม่; updateCount แสดงจำนวน; updateGridColumns ปรับจำนวนคอลัมน์ตามจอ
// ข้อมูลจริงอยู่ใน LibraryLogic/CsvStorage; แก้หน้าตาและการโต้ตอบในไฟล์นี้

import ui.components.BookCard;
import ui.components.InputField;
import ui.components.PillButton;
import ui.components.Sidebar;
import ui.components.SlimScrollBarUI;
import logic.library.LibraryLogic;
import logic.books.BookCatalogLogic;
import books.Book;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.List;
import ui.styles.Fonts;
import ui.styles.Theme;

/**
 * หน้าแสดงรายการหนังสือโปรดของผู้ใช้ปัจจุบัน
 *
 * <p>รายการโปรดอ่านและบันทึกใน `data/favorites.csv` แยกตามอีเมลผู้ใช้</p>
 */
public class FavoritesScreen extends JFrame {

    /** ช่องค้นหาเฉพาะหนังสือในรายการโปรด */
    private final InputField search = new InputField("ค้นหาในรายการโปรด", false);

    /** ตารางการ์ดรายการโปรด */
    private final JPanel grid = new JPanel(new GridLayout(0, 2, 16, 16));

    /** ข้อความแจ้งจำนวนหนังสือที่อยู่ในรายการโปรด */
    private final JLabel sub = new JLabel();

    /** วิธีเรียงรายการปัจจุบัน */
    private String sort = "ลำดับเดิม";

    /** สร้างหน้าต่างรายการโปรด พร้อมตัวกรองและตัวเลือกเรียง */
    public FavoritesScreen() {
        Fonts.install();
        setTitle("KU Goodbook - รายการโปรด");
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // บอกแถบเมนูว่ารายการโปรดเป็นหน้าปัจจุบัน
        add(new Sidebar("รายการโปรด"), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout(0, 14));
        main.setBackground(Theme.bg());
        main.setBorder(new EmptyBorder(22, 28, 0, 20));
        add(main, BorderLayout.CENTER);

        // แสดงจำนวนปัจจุบัน พร้อมช่องค้นหาและตัวเลือกเรียง
        JLabel title = new JLabel("รายการโปรดของคุณ");
        title.setFont(Fonts.title(26));
        title.setForeground(Theme.accent());
        updateCount();
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

        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sortRow.setOpaque(false);
        JComboBox<String> sortBox = new JComboBox<>(new String[]{"ลำดับเดิม", "ชื่อเรื่อง A-Z", "คะแนนรีวิวสูงสุด"});
        sortBox.addActionListener(e -> {
            sort = (String) sortBox.getSelectedItem();
            refresh();
        });
        sortRow.add(sortBox);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(topBar, BorderLayout.NORTH);
        top.add(sortRow, BorderLayout.CENTER);
        main.add(top, BorderLayout.NORTH);

        // สร้างการ์ดจากรายการโปรดของผู้ใช้ปัจจุบัน
        grid.setOpaque(false);
        refresh();
        search.getTextComponent().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { refresh(); }
            @Override
            public void removeUpdate(DocumentEvent e) { refresh(); }
            @Override
            public void changedUpdate(DocumentEvent e) { refresh(); }
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
        // ปรับจำนวนคอลัมน์ตามพื้นที่ เพื่อให้รายการโปรดแสดงครบทุกขนาดหน้าจอ
        scroll.getViewport().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                updateGridColumns(scroll.getViewport().getWidth());
            }
        });
        main.add(scroll, BorderLayout.CENTER);

        setVisible(true);
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

    /** อัปเดตจำนวนรายการโปรดที่แสดงใต้หัวข้อ */
    private void updateCount() {
        try {
            sub.setText("คุณมีหนังสือที่บันทึกไว้ " + LibraryLogic.favorites().size() + " เรื่อง");
        } catch (IllegalStateException error) {
            showStorageError(error);
        }
    }

    /** สร้างรายการการ์ดใหม่หลังค้นหา เรียง หรือลบหนังสือ */
    private void refresh() {
        // คัดเฉพาะเล่มที่ตรงกับคำค้นหา
        List<Book> savedFavorites;
        try {
            savedFavorites = LibraryLogic.favorites();
        } catch (IllegalStateException error) {
            showStorageError(error);
            savedFavorites = new ArrayList<>();
        }

        String query = search.getTextComponent().getText().trim();
        List<Book> books = BookCatalogLogic.searchAndSort(
                savedFavorites, query, sort, false);
        grid.removeAll();
        for (Book book : books) {
            grid.add(new BookCard(book, true, () -> {
                // ปุ่มลบจะเปลี่ยนสถานะ แล้ววาดหน้าใหม่ให้รายการอัปเดตทันที
                try {
                    LibraryLogic.toggleFavorite(book);
                    updateCount();
                    refresh();
                } catch (IllegalStateException error) {
                    showStorageError(error);
                }
            }));
        }
        if (books.isEmpty()) {
            grid.add(new JLabel("ยังไม่มีหนังสือในรายการนี้"));
        }
        grid.revalidate();
        grid.repaint();
    }

    /** แสดงข้อความเมื่ออ่านหรือเขียนรายการโปรดใน CSV ไม่สำเร็จ */
    private void showStorageError(IllegalStateException error) {
        JOptionPane.showMessageDialog(this, error.getMessage(),
                "อ่าน/เขียน CSV ไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * เปิดหน้านี้โดยตรงเพื่อทดลองหน้าจอ โดยไม่ต้องเริ่มจาก Main
     *
     * @param args ค่าจากคำสั่งเปิดโปรแกรม (ยังไม่ได้ใช้)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(FavoritesScreen::new);
    }
}
