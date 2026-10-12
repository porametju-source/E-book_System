package ui.pages;

// ไกด์ไฟล์: หน้าที่: หน้าจัดการหนังสือของแอดมิน ใช้เพิ่ม แก้ไข ลบหนังสือและเนื้อหาตอน
// buildManagerCard/buildBookCard/buildChapterCard สร้างส่วนต่าง ๆ ของฟอร์ม
// saveBook บันทึกข้อมูล; addChapter/removeChapter แก้รายการตอน; deleteCategory จัดการหมวดจากหน้านี้

import app.AdminSession;
import books.Book;
import books.BookData;
import books.Chapter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import logic.admin.AdminBookLogic;
import logic.books.CategoryLogic;
import ui.components.PillButton;
import ui.components.Sidebar;
import ui.components.SlimScrollBarUI;
import ui.styles.Fonts;
import ui.styles.Theme;

/** หน้าสำหรับแอดมินเพิ่มข้อมูลหนังสือและเนื้อหาแต่ละตอน */
public class AdminScreen extends JFrame {

    // 1. ช่องกรอกข้อมูลหลักของหนังสือ
    private final JTextField titleTh = new JTextField();
    private final JTextField titleEn = new JTextField();
    private final JTextField author = new JTextField();
    private final JTextField category = new JTextField();
    private final JLabel categoryLabel = new JLabel("ยังไม่ได้เลือกหมวดหมู่");
    private final JTextField year = new JTextField();
    private final JComboBox<String> status = new JComboBox<String>(
            new String[]{"กำลังอัปเดต", "จบแล้ว"});
    private final JTextArea blurb = new JTextArea(3, 30);

    // 2. ช่องกรอกตอนและรายการตอนที่จะบันทึก
    private final JTextField chapterTitle = new JTextField();
    private final JTextArea chapterContent = new JTextArea(6, 30);
    private final DefaultListModel<String> chapterNames = new DefaultListModel<String>();
    private final JList<String> chapterList = new JList<String>(chapterNames);
    private final ArrayList<Chapter> chapters = new ArrayList<Chapter>();
    private final JLabel chapterCountLabel = new JLabel("เพิ่มแล้ว 0 ตอน");

    // 3. ตัวเลือกหนังสือเดิมสำหรับแก้ไขหรือลบ
    private final JComboBox<String> existingBooks = new JComboBox<String>();
    private final ArrayList<Book> manageableBooks = new ArrayList<Book>();
    private Book editingBook;

    // 4. ข้อมูลรูปปกที่เลือกจากโฟลเดอร์รูป
    private final JLabel coverLabel = new JLabel("ยังไม่ได้เลือกรูปปก");
    private Path coverFile;

    // 4. สร้างหน้าจอและจัดวางส่วนต่าง ๆ
    public AdminScreen() {
        if (!AdminSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(null, "กรุณาเข้าสู่ระบบแอดมินก่อน");
            new LoginScreen();
            dispose();
            return;
        }

        Fonts.install();
        setTitle("KU Goodbook - จัดการหนังสือ");
        setSize(1280, 820);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.bg());
        add(new Sidebar("หน้าแรก"), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(Theme.bg());
        main.setBorder(new EmptyBorder(24, 30, 20, 30));
        main.add(buildHeader(), BorderLayout.NORTH);
        main.add(buildForm(), BorderLayout.CENTER);
        add(main, BorderLayout.CENTER);
        setVisible(true);
    }

    /** หัวหน้าจอวางชื่อหน้าและปุ่มกลับไว้คนละด้าน */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        JLabel title = new JLabel("จัดการหนังสือ");
        title.setFont(Fonts.title(25));
        title.setForeground(Theme.accent());
        JLabel note = new JLabel("เพิ่มหนังสือและเนื้อหาตอน · แอดมิน " + AdminSession.username());
        note.setFont(Fonts.body(14));
        note.setForeground(Theme.muted());
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(note);
        header.add(titles, BorderLayout.WEST);

        PillButton back = new PillButton("กลับหน้าหลัก", PillButton.OUTLINE);
        back.setPreferredSize(new Dimension(150, 42));
        back.addActionListener(e -> {
            new HomeScreen();
            dispose();
        });
        header.add(back, BorderLayout.EAST);
        return header;
    }

    /** วางฟอร์มที่เลื่อนได้ไว้ตรงกลาง และตรึงปุ่มบันทึกไว้ด้านล่าง */
    private JPanel buildForm() {
        JPanel page = new JPanel(new BorderLayout(0, 12));
        page.setOpaque(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Theme.bg());
        content.add(buildManagerCard());
        content.add(Box.createVerticalStrut(16));
        content.add(buildBookCard());
        content.add(Box.createVerticalStrut(16));
        content.add(buildChapterCard());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getViewport().setBackground(Theme.bg());
        // ขยับหน้าฟอร์มมากขึ้นต่อการหมุนล้อเมาส์หนึ่งครั้ง
        scroll.getVerticalScrollBar().setUnitIncrement(32);
        scroll.getVerticalScrollBar().setBlockIncrement(180);
        scroll.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        // เพิ่มความกว้างให้จับแถบเลื่อนได้ง่ายขึ้น
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(14, 0));
        page.add(scroll, BorderLayout.CENTER);
        page.add(buildSaveBar(), BorderLayout.SOUTH);
        return page;
    }

    /** เลือกหนังสือเดิมเพื่อโหลดมาแก้ หรือสั่งลบออกจากระบบ */
    private JPanel buildManagerCard() {
        JPanel card = createCard("จัดการหนังสือที่มีอยู่", "เลือกหนังสือก่อนกดแก้ไข หรือลบหนังสือออกจากระบบ");
        refreshBookChoices();
        existingBooks.setFont(Fonts.body(14));
        existingBooks.setBackground(Theme.tile());
        existingBooks.setForeground(Theme.text());
        existingBooks.setPreferredSize(new Dimension(100, 42));

        JPanel actions = new JPanel(new BorderLayout(12, 0));
        actions.setOpaque(false);
        actions.add(existingBooks, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);

        PillButton edit = new PillButton("แก้ไขหนังสือที่เลือก", PillButton.OUTLINE);
        edit.setPreferredSize(new Dimension(180, 40));
        edit.addActionListener(e -> loadSelectedBook());
        PillButton delete = new PillButton("ลบหนังสือ", PillButton.DANGER);
        delete.setPreferredSize(new Dimension(130, 40));
        delete.addActionListener(e -> deleteSelectedBook());
        buttons.add(edit);
        buttons.add(delete);
        actions.add(buttons, BorderLayout.EAST);
        card.add(actions);
        return card;
    }

    /** โหลดหนังสือปัจจุบันลงในตัวเลือกสำหรับแก้ไขและลบ */
    private void refreshBookChoices() {
        manageableBooks.clear();
        existingBooks.removeAllItems();
        for (Book book : BookData.ALL) {
            manageableBooks.add(book);
            existingBooks.addItem(book.id + " - " + book.titleTh);
        }
    }

    /** คืนหนังสือที่ตรงกับรายการที่แอดมินเลือก */
    private Book getSelectedBook() {
        int index = existingBooks.getSelectedIndex();
        return index < 0 || index >= manageableBooks.size() ? null : manageableBooks.get(index);
    }

    /** นำข้อมูลจากหนังสือที่เลือกมาใส่ในฟอร์มแก้ไข */
    private void loadSelectedBook() {
        Book book = getSelectedBook();
        if (book == null) {
            JOptionPane.showMessageDialog(this, "ยังไม่มีหนังสือให้เลือก");
            return;
        }
        editingBook = book;
        titleTh.setText(book.titleTh);
        titleEn.setText(book.titleEn);
        author.setText(book.author);
        setCategory(book.category);
        year.setText(String.valueOf(book.year));
        status.setSelectedItem(book.status);
        blurb.setText(book.blurb);
        coverFile = null;
        coverLabel.setText(book.coverPath.isEmpty() ? "เล่มนี้ยังไม่มีรูปปก" : "ใช้รูปปกเดิม (เลือกรูปใหม่ได้)");
        chapters.clear();
        chapterNames.clear();
        for (int i = 0; i < book.chapterTitles.size(); i++) {
            chapters.add(new Chapter(book.chapterTitles.get(i), book.chapterTexts.get(i)));
            chapterNames.addElement("ตอนที่ " + (i + 1) + ": " + book.chapterTitles.get(i));
        }
        updateChapterCount();
        chapterTitle.setText("");
        chapterContent.setText("");
        chapterList.clearSelection();
        JOptionPane.showMessageDialog(this, "โหลดข้อมูล “" + book.titleTh + "” แล้ว แก้ไขได้จากฟอร์มด้านล่าง");
    }

    /** ถามยืนยันก่อนลบหนังสือและข้อมูลผู้ใช้ที่เกี่ยวข้อง */
    private void deleteSelectedBook() {
        Book book = getSelectedBook();
        if (book == null) {
            JOptionPane.showMessageDialog(this, "ยังไม่มีหนังสือให้เลือก");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this,
                "ลบ “" + book.titleTh + "” พร้อมตอน รีวิว รายการโปรด และประวัติอ่านหรือไม่?",
                "ยืนยันการลบหนังสือ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        try {
            AdminBookLogic.deleteBook(book);
            if (editingBook == book) clearForm();
            refreshBookChoices();
            JOptionPane.showMessageDialog(this, "ลบหนังสือเรียบร้อยแล้ว");
        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "ลบหนังสือไม่สำเร็จ",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /** ล้างฟอร์มหลังลบหนังสือที่กำลังแก้ไข */
    private void clearForm() {
        editingBook = null;
        titleTh.setText(""); titleEn.setText(""); author.setText(""); setCategory("");
        year.setText(""); blurb.setText(""); coverFile = null;
        coverLabel.setText("ยังไม่ได้เลือกรูปปก");
        chapters.clear(); chapterNames.clear(); chapterTitle.setText(""); chapterContent.setText("");
        updateChapterCount();
    }

    /** กล่องข้อมูลหลักที่อิงจากรายละเอียดของหนังสือที่มีอยู่ในระบบ */
    private JPanel buildBookCard() {
        JPanel card = createCard("ข้อมูลหนังสือ", "กรอกช่องที่มี * ชื่อภาษาอังกฤษและรูปปกเป็นข้อมูลเสริม");

        JPanel fields = new JPanel(new GridLayout(0, 2, 18, 14));
        fields.setOpaque(false);
        addField(fields, "ชื่อหนังสือภาษาไทย *", titleTh);
        addField(fields, "ชื่อภาษาอังกฤษ", titleEn);
        addField(fields, "ผู้แต่ง *", author);
        addCategoryField(fields);
        addField(fields, "ปีที่พิมพ์ *", year);
        addField(fields, "สถานะหนังสือ *", status);
        card.add(fields);

        year.setToolTipText("กรอกปีที่พิมพ์เป็นตัวเลข เช่น 2025");
        card.add(Box.createVerticalStrut(14));
        card.add(textAreaField("เรื่องย่อ *", blurb, 82));

        JPanel coverRow = new JPanel(new BorderLayout(14, 0));
        coverRow.setOpaque(false);
        coverRow.setBorder(new EmptyBorder(14, 0, 0, 0));
        PillButton chooseCover = new PillButton("เลือกรูปปก", PillButton.OUTLINE);
        chooseCover.setPreferredSize(new Dimension(145, 40));
        chooseCover.addActionListener(e -> chooseCover());
        coverRow.add(chooseCover, BorderLayout.WEST);
        coverLabel.setFont(Fonts.body(13));
        coverLabel.setForeground(Theme.muted());
        coverLabel.setToolTipText("ไม่เลือกรูปก็ได้ ระบบจะแสดงปกสีพร้อมชื่อหนังสือแทน");
        coverRow.add(coverLabel, BorderLayout.CENTER);
        card.add(coverRow);
        return card;
    }

    /** กล่องเพิ่มชื่อตอนและเนื้อหา แล้วแสดงรายการตอนที่เตรียมบันทึก */
    private JPanel buildChapterCard() {
        JPanel card = createCard("ตอนและเนื้อหา", "เพิ่มทีละตอน รายการตอนจะแสดงตามลำดับที่เพิ่ม");
        addField(card, "ชื่อตอน *", chapterTitle);
        card.add(Box.createVerticalStrut(12));
        card.add(textAreaField("เนื้อหาตอน *", chapterContent, 150));
        card.add(Box.createVerticalStrut(12));

        JPanel chapterActions = new JPanel(new BorderLayout(12, 0));
        chapterActions.setOpaque(false);
        PillButton addChapter = new PillButton("+ เพิ่มตอนในรายการ", PillButton.OUTLINE);
        addChapter.setPreferredSize(new Dimension(190, 40));
        addChapter.addActionListener(e -> addChapter());
        PillButton updateChapter = new PillButton("อัปเดตตอนที่เลือก", PillButton.OUTLINE);
        updateChapter.setPreferredSize(new Dimension(175, 40));
        updateChapter.addActionListener(e -> updateSelectedChapter());
        chapterCountLabel.setFont(Fonts.body(13));
        chapterCountLabel.setForeground(Theme.muted());
        JPanel chapterButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chapterButtons.setOpaque(false);
        chapterButtons.add(addChapter);
        chapterButtons.add(updateChapter);
        chapterActions.add(chapterButtons, BorderLayout.WEST);
        chapterActions.add(chapterCountLabel, BorderLayout.EAST);
        card.add(chapterActions);
        card.add(Box.createVerticalStrut(10));

        chapterList.setVisibleRowCount(3);
        chapterList.setFixedCellHeight(36);
        chapterList.setFont(Fonts.body(14));
        chapterList.setBackground(Theme.tile());
        chapterList.setForeground(Theme.text());
        chapterList.setSelectionBackground(Theme.readBg());
        chapterList.setSelectionForeground(Theme.text());
        chapterList.setBorder(new EmptyBorder(4, 8, 4, 8));
        chapterList.addListSelectionListener(e -> {
            int selectedIndex = chapterList.getSelectedIndex();
            if (!e.getValueIsAdjusting() && selectedIndex >= 0 && selectedIndex < chapters.size()) {
                chapterTitle.setText(chapters.get(selectedIndex).title);
                chapterContent.setText(chapters.get(selectedIndex).content);
            }
        });
        JScrollPane listScroll = styledScroll(chapterList, 116);
        card.add(listScroll);

        PillButton removeChapter = new PillButton("ลบตอนที่เลือก", PillButton.DANGER);
        removeChapter.setAlignmentX(LEFT_ALIGNMENT);
        removeChapter.setPreferredSize(new Dimension(150, 38));
        removeChapter.addActionListener(e -> removeChapter());
        JPanel removeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
        removeRow.setOpaque(false);
        removeRow.add(removeChapter);
        card.add(removeRow);
        return card;
    }

    /** แถบด้านล่างช่วยให้ปุ่มบันทึกหาเจอได้ตลอด แม้เลื่อนอ่านฟอร์มอยู่ */
    private JPanel buildSaveBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.line()),
                new EmptyBorder(12, 0, 0, 0)));

        JLabel note = new JLabel("ต้องเพิ่มอย่างน้อยหนึ่งตอนก่อนบันทึก");
        note.setFont(Fonts.body(13));
        note.setForeground(Theme.muted());
        bar.add(note, BorderLayout.WEST);

        PillButton save = new PillButton("บันทึกหนังสือ", PillButton.PRIMARY);
        save.setPreferredSize(new Dimension(190, 46));
        save.addActionListener(e -> saveBook());
        bar.add(save, BorderLayout.EAST);
        return bar;
    }

    /** สร้างกล่องสีอ่อนสำหรับแยกข้อมูลแต่ละส่วน */
    private JPanel createCard(String titleText, String noteText) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.card());
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()),
                new EmptyBorder(18, 20, 18, 20)));

        JLabel title = new JLabel(titleText);
        title.setFont(Fonts.title(18));
        title.setForeground(Theme.accent());
        title.setAlignmentX(LEFT_ALIGNMENT);
        JLabel note = new JLabel(noteText);
        note.setFont(Fonts.body(13));
        note.setForeground(Theme.muted());
        note.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(3));
        card.add(note);
        card.add(Box.createVerticalStrut(16));
        return card;
    }

    /** วางชื่อช่องไว้เหนือช่องกรอกและกำหนดสีให้เข้ากับธีม */
    private JPanel addField(JPanel parent, String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(Fonts.bold(13));
        label.setForeground(Theme.text());
        styleField(field);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        parent.add(panel);
        return panel;
    }

    /** แสดงหมวดหมู่ที่เลือกและเปิดหน้าต่างเลือกหรือเพิ่มหมวดใหม่ */
    private void addCategoryField(JPanel parent) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel title = new JLabel("หมวดหมู่ *");
        title.setFont(Fonts.bold(13));
        title.setForeground(Theme.text());

        JPanel chooser = new JPanel(new BorderLayout(10, 0));
        chooser.setOpaque(true);
        chooser.setBackground(Theme.tile());
        chooser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(4, 10, 4, 8)));
        categoryLabel.setFont(Fonts.body(14));
        categoryLabel.setForeground(Theme.text());
        categoryLabel.setToolTipText("เลือกหมวดหมู่ที่มีอยู่ หรือเพิ่มหมวดหมู่ใหม่");
        chooser.add(categoryLabel, BorderLayout.CENTER);

        JPanel categoryButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        categoryButtons.setOpaque(false);
        PillButton choose = new PillButton("เลือกหมวดหมู่", PillButton.OUTLINE);
        choose.setPreferredSize(new Dimension(140, 36));
        choose.addActionListener(e -> chooseCategory());
        PillButton delete = new PillButton("ลบหมวด", PillButton.DANGER);
        delete.setPreferredSize(new Dimension(90, 36));
        delete.addActionListener(e -> deleteCategory());
        categoryButtons.add(choose);
        categoryButtons.add(delete);
        chooser.add(categoryButtons, BorderLayout.EAST);

        panel.add(title, BorderLayout.NORTH);
        panel.add(chooser, BorderLayout.CENTER);
        parent.add(panel);
    }

    /** เปิดรายการหมวดที่มีอยู่ หรือบันทึกชื่อหมวดใหม่ */
    private void chooseCategory() {
        ArrayList<String> options = new ArrayList<String>();
        try {
            options.addAll(CategoryLogic.allCategories(new String[0]));
        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "โหลดหมวดหมู่ไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String addNewOption = "+ เพิ่มหมวดหมู่ใหม่";
        options.add(addNewOption);

        Object selected = JOptionPane.showInputDialog(this,
                "เลือกหมวดหมู่สำหรับหนังสือ", "เลือกหมวดหมู่",
                JOptionPane.PLAIN_MESSAGE, null, options.toArray(),
                category.getText().isEmpty() ? options.get(0) : category.getText());
        if (selected == null) return;

        String value = selected.toString();
        if (addNewOption.equals(value)) {
            value = JOptionPane.showInputDialog(this, "พิมพ์ชื่อหมวดหมู่ใหม่", "เพิ่มหมวดหมู่",
                    JOptionPane.PLAIN_MESSAGE);
            if (value == null) return;
            value = value.trim();
            if (value.isEmpty()) {
                JOptionPane.showMessageDialog(this, "กรุณาพิมพ์ชื่อหมวดหมู่");
                return;
            }
            try {
                CategoryLogic.addCategory(value);
            } catch (IOException error) {
                JOptionPane.showMessageDialog(this, error.getMessage(),
                        "เพิ่มหมวดหมู่ไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        setCategory(value);
    }

    /** ตั้งค่าหมวดในข้อมูลฟอร์มและปรับข้อความที่แสดงบนปุ่ม */
    private void setCategory(String value) {
        category.setText(value == null ? "" : value.trim());
        categoryLabel.setText(category.getText().isEmpty()
                ? "ยังไม่ได้เลือกหมวดหมู่" : category.getText());
    }

    /** เลือกและลบหมวดว่างที่แอดมินสร้างไว้ */
    private void deleteCategory() {
        ArrayList<String> options = new ArrayList<String>();
        try {
            options.addAll(CategoryLogic.allCategories(new String[0]));
        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "โหลดหมวดหมู่ไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (options.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ไม่มีหมวดหมู่ให้ลบ");
            return;
        }

        Object selected = JOptionPane.showInputDialog(this,
                "เลือกหมวดหมู่ที่ต้องการลบ (ลบได้เมื่อไม่มีหนังสือใช้หมวดนี้)",
                "ลบหมวดหมู่", JOptionPane.WARNING_MESSAGE, null,
                options.toArray(), options.get(0));
        if (selected == null) return;

        String name = selected.toString();
        int answer = JOptionPane.showConfirmDialog(this,
                "ยืนยันการลบหมวดหมู่ “" + name + "” หรือไม่?",
                "ยืนยันการลบ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer != JOptionPane.YES_OPTION) return;

        try {
            CategoryLogic.deleteCategory(name);
            if (category.getText().equalsIgnoreCase(name)) {
                setCategory("");
            }
            JOptionPane.showMessageDialog(this, "ลบหมวดหมู่เรียบร้อยแล้ว");
        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "ลบหมวดหมู่ไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** สร้างช่องข้อความที่มีป้ายชื่อและแถบเลื่อนที่เข้ากับธีม */
    private JPanel textAreaField(String labelText, JTextArea area, int height) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(Fonts.bold(13));
        label.setForeground(Theme.text());
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(Fonts.body(14));
        area.setForeground(Theme.text());
        area.setBackground(Theme.tile());
        area.setCaretColor(Theme.text());
        area.setBorder(new EmptyBorder(10, 11, 10, 11));
        JScrollPane scroll = styledScroll(area, height);
        panel.add(label, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    /** ตั้งสี ขนาด และขอบช่องกรอกให้เหมือนกันทั้งหน้า */
    private void styleField(JComponent field) {
        field.setFont(Fonts.body(14));
        field.setForeground(Theme.text());
        field.setBackground(Theme.tile());
        field.setPreferredSize(new Dimension(100, 42));
        if (field instanceof JTextField) {
            JTextField textField = (JTextField) field;
            textField.setCaretColor(Theme.text());
            textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.line()),
                    new EmptyBorder(0, 11, 0, 11)));
        } else if (field instanceof JComboBox) {
            field.setBorder(BorderFactory.createLineBorder(Theme.line()));
        }
    }

    /** ตั้งขนาดและเส้นขอบของพื้นที่เลื่อน */
    private JScrollPane styledScroll(JComponent content, int height) {
        JScrollPane scroll = new JScrollPane(content);
        scroll.setPreferredSize(new Dimension(100, height));
        scroll.setBorder(BorderFactory.createLineBorder(Theme.line()));
        scroll.getViewport().setBackground(Theme.tile());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(9, 0));
        return scroll;
    }

    // 5. เลือกรูป เพิ่มตอน และบันทึกหนังสือ
    private void chooseCover() {
        // เก็บรูปต้นฉบับไว้ในโฟลเดอร์ image ของโปรเจกต์
        Path imageFolder = app.ProjectPaths.dataFolder().getParent().resolve("image");
        JFileChooser chooser = new JFileChooser(imageFolder.toFile());
        chooser.setDialogTitle("เลือกรูปปกหนังสือ");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter(
                "ไฟล์รูปภาพ (PNG, JPG, JPEG, GIF)", "png", "jpg", "jpeg", "gif"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            coverFile = chooser.getSelectedFile().toPath();
            coverLabel.setText(coverFile.getFileName().toString());
            coverLabel.setToolTipText(coverFile.toString());
        }
    }

    /** ตรวจข้อมูลตอนแล้วเพิ่มลงในรายการที่เตรียมบันทึก */
    private void addChapter() {
        String name = chapterTitle.getText().trim();
        String text = chapterContent.getText().trim();
        if (name.isEmpty() || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "กรอกชื่อตอนและเนื้อหาก่อน");
            return;
        }

        chapters.add(new Chapter(name, text));
        chapterNames.addElement("ตอนที่ " + chapters.size() + ": " + name);
        chapterTitle.setText("");
        chapterContent.setText("");
        updateChapterCount();
    }

    /** ลบตอนที่เลือกและจัดเลขลำดับตอนที่เหลือใหม่ */
    private void removeChapter() {
        int selectedIndex = chapterList.getSelectedIndex();
        if (selectedIndex < 0) {
            JOptionPane.showMessageDialog(this, "เลือกตอนที่ต้องการลบก่อน");
            return;
        }

        chapters.remove(selectedIndex);
        chapterNames.remove(selectedIndex);
        for (int i = 0; i < chapters.size(); i++) {
            chapterNames.set(i, "ตอนที่ " + (i + 1) + ": " + chapters.get(i).title);
        }
        updateChapterCount();
    }

    /** บันทึกชื่อและเนื้อหาที่แก้ลงในตอนที่เลือก */
    private void updateSelectedChapter() {
        int selectedIndex = chapterList.getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= chapters.size()) {
            JOptionPane.showMessageDialog(this, "เลือกตอนที่ต้องการแก้ก่อน");
            return;
        }
        String name = chapterTitle.getText().trim();
        String text = chapterContent.getText().trim();
        if (name.isEmpty() || text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "กรอกชื่อตอนและเนื้อหาให้ครบก่อน");
            return;
        }
        chapters.set(selectedIndex, new Chapter(name, text));
        chapterNames.set(selectedIndex, "ตอนที่ " + (selectedIndex + 1) + ": " + name);
        JOptionPane.showMessageDialog(this, "อัปเดตตอนในรายการแล้ว อย่าลืมกดบันทึกหนังสือ");
    }

    /** อัปเดตจำนวนตอนบนแถบเพิ่มตอน */
    private void updateChapterCount() {
        chapterCountLabel.setText("เพิ่มแล้ว " + chapters.size() + " ตอน");
    }

    /** ตรวจข้อมูลและบันทึกหนังสือใหม่หรือบันทึกทับเล่มที่กำลังแก้ */
    private void saveBook() {
        try {
            Book book;
            if (editingBook == null) {
                book = AdminBookLogic.addBook(titleTh.getText(), titleEn.getText(), author.getText(),
                        category.getText(), year.getText(), (String) status.getSelectedItem(),
                        blurb.getText(), coverFile, chapters);
            } else {
                book = AdminBookLogic.updateBook(editingBook, titleTh.getText(), titleEn.getText(),
                        author.getText(), category.getText(), year.getText(),
                        (String) status.getSelectedItem(), blurb.getText(), coverFile, chapters);
            }
            JOptionPane.showMessageDialog(this, "บันทึก “" + book.titleTh + "” เรียบร้อย");
            new HomeScreen();
            dispose();
        } catch (IOException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "บันทึกหนังสือไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** เปิดหน้าแอดมินบนเธรด UI ของ Swing เพื่อทดลองหน้าจอนี้โดยตรง */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminScreen::new);
    }
}
