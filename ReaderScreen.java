package ui.pages;

// ไกด์ไฟล์: หน้าที่: แสดงเนื้อหาตอนที่เลือกและติดตามความคืบหน้าการอ่าน
// buildTopBar/buildPaper/buildBottomBar สร้างหน้า; changeFont ปรับขนาดตัวอักษร
// isAtEnd/saveReadStatus คุมการบันทึกว่าอ่านแล้ว; แก้ระยะขอบที่ SIDE_MARGIN

import app.Nav;
import logic.library.LibraryLogic;
import ui.components.PillButton;
import ui.components.SlimScrollBarUI;
import books.Book;
import logic.books.BookCatalogLogic;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.event.AdjustmentEvent;
import ui.styles.Fonts;
import ui.styles.Theme;

/**
 * หน้าอ่านเนื้อหาของตอนที่เลือก
 *
 * <p>ตอนในข้อมูลเริ่มนับตำแหน่งจาก 0 แต่ข้อความบนหน้าจอเริ่มนับจาก 1
 * ตัวอย่างเช่น chapterIndex 0 จะแสดงเป็น "ตอนที่ 1" ผู้ใช้ปรับขนาดตัวอักษร
 * สลับโหมดกลางคืน และทำเครื่องหมายอ่านจบได้ในระหว่างเปิดแอป</p>
 */
public class ReaderScreen extends JFrame {

    // 1) ค่าคงที่และข้อมูลของหน้าจอ
    /** ระยะขอบซ้ายและขวาของแถบเปลี่ยนตอนด้านล่าง */
    private static final int SIDE_MARGIN = 130;

    /** ขนาดตัวอักษรเริ่มต้นที่จำไว้เมื่อเปิดหน้าอ่านตอนถัดไป */
    private static int preferredFontSize = 22;

    /** หนังสือที่กำลังอ่าน */
    private final Book book;

    /** ลำดับตอนปัจจุบัน โดยเริ่มจากศูนย์ */
    private final int chapterIndex;

    /** ช่องข้อความที่แสดงเนื้อหาตอนปัจจุบัน */
    private JTextPane readerText;

    /** ขนาดตัวอักษรของหน้าที่เปิดอยู่ */
    private int readFontSize = preferredFontSize;

    // 2) สร้างหน้าต่างและวางส่วนประกอบหลัก

    /**
     * สร้างหน้าต่างอ่านและแสดงชื่อเรื่อง เนื้อหา และปุ่มเปลี่ยนตอน
     *
     * @param book หนังสือที่ต้องการอ่าน
     * @param chapterIndex ลำดับตอน โดยเริ่มจากศูนย์
     */
    public ReaderScreen(Book book, int chapterIndex) {
        Fonts.install();
        this.book = book;
        this.chapterIndex = chapterIndex;
        setTitle("KU Goodbook - " + book.titleTh + " ตอนที่ " + (chapterIndex + 1));
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 14));
        root.setBackground(Theme.bg());
        root.setBorder(new EmptyBorder(18, 40, 18, 40));
        setContentPane(root);

        // แถบชื่อเรื่องและเครื่องมืออยู่ด้านบน
        root.add(buildTopBar(), BorderLayout.NORTH);
        // หน้ากระดาษอยู่กลางหน้าต่าง ส่วนปุ่มเปลี่ยนตอนอยู่ด้านล่าง
        root.add(buildPaper(), BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // 3) สร้างส่วนต่าง ๆ ของหน้าจออ่าน
    /*
     * สร้างแถบด้านบนที่มีปุ่มกลับ ชื่อหนังสือ ตอน และเครื่องมืออ่าน
     */
    private JPanel buildTopBar() {
        JLabel title = new JLabel(book.titleTh + " · ตอนที่ " + (chapterIndex + 1) + ": "
                + book.chapterTitles.get(chapterIndex), SwingConstants.CENTER);
        title.setFont(Fonts.bold(15));
        title.setForeground(Theme.text());

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        tools.setOpaque(false);
        PillButton smaller = new PillButton("A-", PillButton.OUTLINE);
        PillButton larger = new PillButton("A+", PillButton.OUTLINE);
        PillButton night = new PillButton(Theme.dark ? "โหมดกลางวัน" : "โหมดกลางคืน", PillButton.OUTLINE);
        // ปรับตัวอักษรครั้งละสองพอยต์
        smaller.addActionListener(e -> changeFont(-2));
        larger.addActionListener(e -> changeFont(2));
        // เปลี่ยนชุดสี แล้วสร้างหน้าผู้อ่านใหม่เพื่อใช้สีตามโหมด
        night.addActionListener(e -> {
            Theme.dark = !Theme.dark;
            new ReaderScreen(book, chapterIndex);
            dispose();
        });
        tools.add(smaller);
        tools.add(larger);
        tools.add(night);

        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);
        PillButton back = new PillButton("กลับ", PillButton.OUTLINE);
        back.addActionListener(e -> Nav.openDetail(this, book));
        bar.add(back, BorderLayout.WEST);
        bar.add(title, BorderLayout.CENTER);
        bar.add(tools, BorderLayout.EAST);
        return bar;
    }

    /*
     * สร้างหน้ากระดาษสำหรับอ่านเนื้อหาของตอนที่เลือก
     * ข้อความอยู่ในช่องที่เลื่อนได้ เผื่อเนื้อหายาวกว่าพื้นที่บนหน้าจอ
     */
    private JComponent buildPaper() {
        JPanel paper = new JPanel(new BorderLayout(0, 18)) {
            {
                setPreferredSize(new Dimension(900, 600));
            }
            @Override
            protected void paintChildren(Graphics g) {
                super.paintChildren(g);
                // วาดริบบิ้นหลังวาดส่วนประกอบภายใน เพื่อให้ลายอยู่มุมบนของกระดาษ
                paintRibbon((Graphics2D) g, getWidth() - 5 - 90);
            }
        };
        paper.setBackground(Theme.card());
        // เว้นขอบด้านในให้ข้อความไม่ชิดกรอบกระดาษ
        paper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(28, 48, 34, 48)));

        // จัดเลขตอน ชื่อตอน และลายเส้นไว้เหนือเนื้อหา
        JLabel eyebrow = new JLabel("ตอนที่ " + (chapterIndex + 1), SwingConstants.CENTER);
        eyebrow.setFont(Fonts.bold(13));
        eyebrow.setForeground(Theme.gold());
        JLabel chapterTitle = new JLabel(book.chapterTitles.get(chapterIndex), SwingConstants.CENTER);
        chapterTitle.setFont(Fonts.title(30));
        chapterTitle.setForeground(Theme.accent());
        JPanel head = new JPanel(new BorderLayout(0, 4));
        head.setOpaque(false);
        head.add(eyebrow, BorderLayout.NORTH);
        head.add(chapterTitle, BorderLayout.CENTER);
        head.add(makeOrnament(), BorderLayout.SOUTH);
        paper.add(head, BorderLayout.NORTH);

        // แสดงเนื้อหาจากข้อมูลหนังสือ พร้อมรูปแบบตัวอักษรและย่อหน้า
        JTextPane text = new JTextPane();
        readerText = text;
        text.setEditable(false);
        text.setOpaque(false);
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setFontFamily(style, Fonts.read(20).getFamily());
        StyleConstants.setFontSize(style, readFontSize);
        StyleConstants.setForeground(style, Theme.text());
        StyleConstants.setLineSpacing(style, 0.8f);
        StyleConstants.setFirstLineIndent(style, 32);
        StyleConstants.setLeftIndent(style, 0);
        StyleConstants.setRightIndent(style, 0);
        try {
            StyledDocument doc = text.getStyledDocument();
            doc.insertString(0, book.chapterTexts.get(chapterIndex), style);
            // ใช้รูปแบบย่อหน้ากับข้อความทั้งหมดที่เพิ่งใส่ลงในเอกสาร
            doc.setParagraphAttributes(0, doc.getLength(), style, false);
        } catch (BadLocationException e) {
            throw new IllegalStateException("ไม่สามารถแสดงเนื้อหาตอนได้", e);
        }
        JScrollPane scroll = new JScrollPane(text);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        paper.add(scroll, BorderLayout.CENTER);

        // แสดงสถานะอ่านจบแทนปุ่ม เพราะระบบจะบันทึกให้อัตโนมัติเมื่อถึงท้ายตอน
        JPanel buttonWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonWrap.setOpaque(false);
        JLabel readStatus = new JLabel();
        readStatus.setFont(Fonts.body(14));
        readStatus.setForeground(Theme.muted());
        buttonWrap.add(readStatus);
        final boolean[] alreadyRead = {false};
        try {
            alreadyRead[0] = LibraryLogic.isChapterRead(book, chapterIndex);
            if (alreadyRead[0]) {
                readStatus.setText("อ่านจบแล้ว ✓");
                readStatus.setForeground(Theme.accent());
            } else {
                readStatus.setText("เลื่อนอ่านถึงท้ายตอน ระบบจะบันทึกว่าอ่านแล้วอัตโนมัติ");
            }
        } catch (IllegalStateException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "อ่าน CSV ไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
            readStatus.setText("อ่านสถานะไม่ได้");
        }

        // เมื่อเลื่อนถึงท้ายเนื้อหา ให้บันทึกตอนนี้ลง read_chapters.csv
        scroll.getVerticalScrollBar().addAdjustmentListener(event -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            boolean canScroll = bar.getMaximum() > bar.getVisibleAmount();
            if (!alreadyRead[0] && canScroll && isAtEnd(event)) {
                saveReadStatus(readStatus, alreadyRead);
            }
        });
        // ถ้าเนื้อหาสั้นและแสดงได้ครบโดยไม่ต้องเลื่อน ให้บันทึกหลังจัดวางหน้าเสร็จ
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            if (!alreadyRead[0] && bar.getMaximum() <= bar.getVisibleAmount()) {
                saveReadStatus(readStatus, alreadyRead);
            }
        });

        JPanel paperCenter = new JPanel(new GridBagLayout());
        paperCenter.setOpaque(false);
        paperCenter.add(paper);

        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 24, 0, 24));
        wrapper.add(paperCenter, BorderLayout.CENTER);
        wrapper.add(buttonWrap, BorderLayout.SOUTH);
        return wrapper;
    }

    // 4) จัดการสถานะการอ่านและขนาดตัวอักษร
    /** ตรวจว่าตำแหน่งเลื่อนลงมาถึงท้ายเนื้อหาแล้วหรือยัง */
    private boolean isAtEnd(AdjustmentEvent event) {
        JScrollBar bar = (JScrollBar) event.getAdjustable();
        return bar.getValue() + bar.getVisibleAmount() >= bar.getMaximum() - 2;
    }

    /** บันทึกสถานะอ่านจบ และอัปเดตข้อความใต้หน้ากระดาษ */
    private void saveReadStatus(JLabel status, boolean[] alreadyRead) {
        try {
            LibraryLogic.markChapterRead(book, chapterIndex);
            alreadyRead[0] = true;
            status.setText("อ่านจบแล้ว ✓");
            status.setForeground(Theme.accent());
        } catch (IllegalStateException error) {
            status.setText("บันทึกสถานะไม่สำเร็จ");
            JOptionPane.showMessageDialog(this, error.getMessage(),
                    "บันทึก CSV ไม่สำเร็จ", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** เพิ่มหรือลดขนาดตัวอักษร และจำค่าไว้สำหรับตอนถัดไป */
    private void changeFont(int delta) {
        readFontSize = Math.max(16, Math.min(32, readFontSize + delta));
        preferredFontSize = readFontSize;
        if (readerText != null) {
            SimpleAttributeSet size = new SimpleAttributeSet();
            StyleConstants.setFontSize(size, readFontSize);
            readerText.getStyledDocument().setCharacterAttributes(0, readerText.getDocument().getLength(), size, false);
        }
    }

    // 5) วาดลวดลายและองค์ประกอบตกแต่ง
    /*
     * วาดริบบิ้นสีแดงที่มุมบนของหน้ากระดาษ
     * วาดเงาก่อน แล้ววาดริบบิ้นจริงทับเพื่อให้ดูนูนขึ้น
     */
    private void paintRibbon(Graphics2D g, int x) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = 26;
        int h = 70;
        int[] xs = {x, x + w, x + w, x + w / 2, x};
        int[] ys = {1, 1, h, h - 14, h};
        g2.setColor(new Color(0, 0, 0, 50));
        g2.translate(2, 3);
        g2.fillPolygon(xs, ys, 5);
        g2.translate(-2, -3);
        g2.setColor(Theme.accentFill());
        g2.fillPolygon(xs, ys, 5);
        g2.dispose();
    }

    // สร้างเส้นสีทองกับรูปข้าวหลามตัดเล็ก ๆ คั่นหัวตอนกับเนื้อหา
    private JComponent makeOrnament() {
        return new JComponent() {
            {
                setPreferredSize(new Dimension(300, 26));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                Color gold = Theme.gold();
                Color clear = new Color(gold.getRed(), gold.getGreen(), gold.getBlue(), 0);
                g2.setPaint(new GradientPaint(cx - 120, 0, clear, cx - 14, 0, gold));
                g2.drawLine(cx - 120, cy, cx - 14, cy);
                g2.setPaint(new GradientPaint(cx + 14, 0, gold, cx + 120, 0, clear));
                g2.drawLine(cx + 14, cy, cx + 120, cy);
                g2.setColor(gold);
                g2.fillPolygon(new int[]{cx, cx + 6, cx, cx - 6}, new int[]{cy - 6, cy, cy + 6, cy}, 4);
                g2.dispose();
            }
        };
    }

    /*
     * สร้างปุ่มไปตอนก่อนหน้าและตอนถัดไป พร้อมเลขบอกตำแหน่งตอน
     * ปิดปุ่มก่อนหน้าเมื่ออยู่ตอนแรก และปิดปุ่มถัดไปเมื่ออยู่ตอนสุดท้าย
     */
    private JPanel buildBottomBar() {
        PillButton prev = new PillButton("ตอนก่อนหน้า", PillButton.OUTLINE);
        // ตอนแรกไม่มีตอนก่อนหน้า จึงปิดปุ่มนั้นไว้
        prev.setEnabled(chapterIndex > 0);
        PillButton next = new PillButton("ตอนถัดไป", PillButton.OUTLINE);
        next.setEnabled(chapterIndex < book.chapterTitles.size() - 1);
        prev.addActionListener(e -> Nav.openReader(this, book, chapterIndex - 1));
        next.addActionListener(e -> Nav.openReader(this, book, chapterIndex + 1));

        JComponent dots = makeDots();
        JLabel number = new JLabel("ตอนที่ " + (chapterIndex + 1) + " / " + book.chapterTitles.size(), SwingConstants.CENTER);
        number.setFont(Fonts.body(13));
        number.setForeground(Theme.muted());
        JPanel middle = new JPanel(new BorderLayout(0, 4));
        middle.setOpaque(false);
        middle.add(dots, BorderLayout.NORTH);
        middle.add(number, BorderLayout.CENTER);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(0, SIDE_MARGIN - 40, 0, SIDE_MARGIN - 40));
        bar.add(prev, BorderLayout.WEST);
        bar.add(middle, BorderLayout.CENTER);
        bar.add(next, BorderLayout.EAST);
        return bar;
    }

    /*
     * วาดจุดแทนทุกตอน จุดทึบคือตอนก่อนหน้า วงกลมเด่นคือตอนปัจจุบัน
     * และวงกลมสีอ่อนคือตอนที่ยังอยู่ถัดไป
     */
    private JComponent makeDots() {
        final int total = book.chapterTitles.size();
        return new JComponent() {
            {
                setPreferredSize(new Dimension(total * 18, 14));
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int startX = (getWidth() - total * 18) / 2 + 4;
                for (int i = 0; i < total; i++) {
                    int x = startX + i * 18;
                    if (i < chapterIndex) {
                        g2.setColor(Theme.accent());
                        g2.fillOval(x, 2, 10, 10);
                    } else if (i == chapterIndex) {
                        g2.setColor(Theme.accent());
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawOval(x, 2, 10, 10);
                    } else {
                        g2.setColor(Theme.line());
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawOval(x, 2, 10, 10);
                    }
                }
                g2.dispose();
            }
        };
    }

    /**
     * เปิดหน้าอ่านโดยระบุเลขหนังสือและเลขตอนจากคำสั่ง
     * ค่าที่รับเริ่มนับจาก 1 เพื่อให้ง่ายต่อการพิมพ์ในหน้าต่างคำสั่ง
     * ก่อนส่งไปยัง ReaderScreen จะเปลี่ยนเป็นตำแหน่งเริ่มจาก 0
     *
     * @param args เลขหนังสือและเลขตอน ถ้าไม่ระบุจะเปิดเรื่องแรกตอนแรก
     */
    public static void main(String[] args) {
        java.util.List<Book> books = BookCatalogLogic.allBooks();
        if (books.isEmpty()) {
            return;
        }
        int bookNumber = args.length > 0 ? Integer.parseInt(args[0]) : 1;
        int chapterNumber = args.length > 1 ? Integer.parseInt(args[1]) : 1;
        Book book = books.get(Math.max(1, Math.min(books.size(), bookNumber)) - 1);
        int index = Math.max(1, Math.min(book.chapterTitles.size(), chapterNumber)) - 1;
        SwingUtilities.invokeLater(() -> new ReaderScreen(book, index));
    }
}
