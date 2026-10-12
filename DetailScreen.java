package ui.pages;

// ไกด์ไฟล์: หน้าที่: แสดงข้อมูลหนังสือ ตอน รีวิว และฟอร์มเขียนรีวิว
// buildHeroCard/buildChapterCard/buildReviewCard สร้างส่วนแสดงผล; makeChapterTile เปิดตอนอ่าน
// buildWriteReviewCard คุมการส่งรีวิว; เงื่อนไขสิทธิ์อยู่ใน ReviewLogic และการบันทึกอยู่ใน CsvStorage

import app.AdminSession;
import app.Nav;
import app.UserSession;
import ui.components.BookCover;
import ui.components.HintTextBox;
import ui.components.PillButton;
import ui.components.Sidebar;
import ui.components.SlimScrollBarUI;
import books.Book;
import logic.library.LibraryLogic;
import logic.reviews.ReviewLogic;
import logic.books.BookCatalogLogic;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import ui.styles.Fonts;
import ui.styles.Theme;

/**
 * หน้าแสดงข้อมูลหนังสือ สารบัญตอน และรีวิว
 *
 * <p>หน้านี้ใช้ข้อมูลจาก Book ที่ส่งเข้ามา ผู้ใช้เลือกตอน ทำรีวิว
 * และจัดการรายการโปรดได้ ข้อมูลรีวิวเก็บใน `reviews.csv`
 * ส่วนรายการโปรดและสถานะอ่านเก็บแยกตามอีเมลใน CSV</p>
 */
public class DetailScreen extends JFrame {

    /** หนังสือที่กำลังแสดง */
    private final Book book;

    // ===== 1. สร้างหน้ารายละเอียดและข้อมูลหนังสือ =====
    /**
     * สร้างหน้าต่างรายละเอียด และจัดการ์ดข้อมูลให้เลื่อนดูได้
     *
     * @param book หนังสือที่ต้องการแสดง ทั้งชื่อ ปก ตอน และรีวิว
     */
    public DetailScreen(Book book) {
        Fonts.install();
        this.book = book;
        setTitle("KU Goodbook - " + book.titleTh);
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // เมนูนี้ยังเน้นหน้าแรกเหมือนหน้ารายการหนังสือ
        add(new Sidebar("หน้าแรก"), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.bg());
        main.setBorder(new EmptyBorder(18, 28, 0, 20));
        add(main, BorderLayout.CENTER);

        // เรียงปุ่มกลับ ข้อมูลหลัก สารบัญ รีวิว และฟอร์มรีวิวจากบนลงล่าง
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setOpaque(false);
        addToColumn(column, buildBackRow(), 14);
        addToColumn(column, buildHeroCard(), 18);
        addToColumn(column, buildChapterCard(), 18);
        addToColumn(column, buildReviewCard(), 18);
        addToColumn(column, buildWriteReviewCard(), 28);

        // วางเนื้อหาเริ่มจากด้านบน แม้หน้าจอจะมีพื้นที่ว่างเหลืออยู่
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(0, 0, 0, 10));
        wrapper.add(column, BorderLayout.NORTH);

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

    // สร้างปุ่มที่พากลับไปยังรายการหนังสือหน้าแรก
    private JComponent buildBackRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        PillButton back = new PillButton("กลับหน้าแรก", PillButton.OUTLINE);
        back.addActionListener(e -> Nav.openHome(this));
        row.add(back);
        return row;
    }

    /*
     * แสดงปกไว้ทางซ้าย ส่วนชื่อ ผู้แต่ง สถานะ คะแนน และเรื่องย่ออยู่ทางขวา
     * ปุ่มรายการโปรดจะบันทึกสถานะหนังสือใน `favorites.csv`
     */
    private JComponent buildHeroCard() {
        JPanel card = new JPanel(new BorderLayout(26, 0));
        card.setBackground(Theme.card());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(22, 22, 27, 27)));
        card.add(new BookCover(book, 190, 260), BorderLayout.WEST);

        // รวมข้อมูลสั้น ๆ ที่ใช้ระบุหนังสือไว้เหนือเรื่องย่อ
        JPanel head = new JPanel(new GridLayout(0, 1, 0, 6));
        head.setOpaque(false);

        JLabel title = new JLabel(book.titleTh);
        title.setFont(Fonts.title(26));
        title.setForeground(Theme.accent());
        head.add(title);

        JLabel authorLine = new JLabel(book.author + "  ·  " + book.category + "  ·  ปี " + book.year);
        authorLine.setFont(Fonts.body(14));
        authorLine.setForeground(Theme.muted());
        // ทำป้ายสถานะให้แยกจากชื่อผู้แต่งและหมวดหมู่
        JPanel statusChip = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusChip.setOpaque(false);
        statusChip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.gold()), new EmptyBorder(1, 12, 1, 12)));
        JLabel statusText = new JLabel(book.status);
        statusText.setFont(Fonts.bold(12));
        statusText.setForeground(Theme.gold());
        statusChip.add(statusText);
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        metaRow.setOpaque(false);
        metaRow.add(authorLine);
        metaRow.add(statusChip);
        head.add(metaRow);

        JLabel star = new JLabel(book.reviews.isEmpty() ? "" : "★");
        star.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        star.setForeground(Theme.accent());
        JLabel rating = new JLabel(book.reviews.isEmpty() ? "ยังไม่มีคะแนน"
                : String.format(java.util.Locale.ROOT, "%.1f", book.averageReviewRating()));
        rating.setFont(Fonts.bold(14));
        rating.setForeground(Theme.accent());
        JPanel ratingRow = new JPanel();
        ratingRow.setLayout(new BoxLayout(ratingRow, BoxLayout.X_AXIS));
        ratingRow.setOpaque(false);
        ratingRow.add(star);
        ratingRow.add(Box.createHorizontalStrut(4));
        ratingRow.add(rating);
        ratingRow.add(Box.createHorizontalStrut(16));
        PillButton favorite = new PillButton(LibraryLogic.isFavorite(book) ? "นำออกจากรายการโปรด" : "เพิ่มในรายการโปรด", PillButton.OUTLINE);
        favorite.addActionListener(e -> {
            try {
                LibraryLogic.toggleFavorite(book);
                favorite.setText(LibraryLogic.isFavorite(book) ? "นำออกจากรายการโปรด" : "เพิ่มในรายการโปรด");
            } catch (IllegalStateException error) {
                showStorageError(error);
            }
        });
        ratingRow.add(favorite);
        head.add(ratingRow);

        // กำหนดความกว้างของเรื่องย่อเพื่อให้ข้อความตัดเป็นบรรทัดอ่านง่าย
        JLabel synopsisTitle = new JLabel("เรื่องย่อ");
        synopsisTitle.setFont(Fonts.bold(14));
        synopsisTitle.setForeground(Theme.gold());
        JTextArea blurb = makeWrappedText(book.blurb, Fonts.body(14), Theme.muted(), 60);
        JPanel synopsis = new JPanel(new BorderLayout(0, 4));
        synopsis.setOpaque(false);
        synopsis.add(synopsisTitle, BorderLayout.NORTH);
        synopsis.add(blurb, BorderLayout.CENTER);

        JPanel info = new JPanel(new BorderLayout(0, 14));
        info.setOpaque(false);
        info.add(head, BorderLayout.NORTH);
        info.add(synopsis, BorderLayout.CENTER);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    /** สร้างสารบัญ พร้อมอ่านสถานะของแต่ละตอนจาก read_chapters.csv */
    private JComponent buildChapterCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.card());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(18, 22, 23, 27)));

        JLabel header = new JLabel("สารบัญตอน");
        header.setFont(Fonts.title(18));
        header.setForeground(Theme.accent());
        card.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 10, 10));
        grid.setOpaque(false);
        for (int i = 0; i < book.chapterTitles.size(); i++) {
            grid.add(makeChapterTile(i));
        }
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    /**
     * สร้างแถวสารบัญหนึ่งตอน พร้อมสีที่บอกสถานะการอ่าน
     *
     * @param index ตำแหน่งตอนในรายการ โดยตอนแรกอยู่ตำแหน่ง 0
     * @return แถวสารบัญที่คลิกเพื่อเปิดหน้าอ่านตอนนี้ได้
     */
    private JComponent makeChapterTile(int index) {
        boolean read = false;
        boolean current = false;
        try {
            read = LibraryLogic.isChapterRead(book, index);
            current = !read && index == LibraryLogic.nextUnreadChapter(book);
        } catch (IllegalStateException error) {
            showStorageError(error);
        }

        Color fill = read ? Theme.readBg() : Theme.tile();
        Color border = current ? Theme.accent() : (read ? null : Theme.line());
        JPanel tile = new JPanel(new BorderLayout(10, 0));
        tile.setBackground(fill);
        if (border != null) {
            tile.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(border), new EmptyBorder(10, 14, 10, 14)));
        } else {
            tile.setBorder(new EmptyBorder(11, 15, 11, 15));
        }

        JLabel name = new JLabel("ตอนที่ " + (index + 1) + ": " + book.chapterTitles.get(index));
        name.setFont(Fonts.body(14));
        name.setForeground(read ? Theme.accent() : Theme.text());
        tile.add(name, BorderLayout.CENTER);

        String tagText = read ? "อ่านแล้ว" : (current ? "ต่อจากนี้" : "");
        JLabel tag = new JLabel(tagText);
        tag.setFont(Fonts.bold(12));
        tag.setForeground(read || current ? Theme.accent() : Theme.muted());
        tile.add(tag, BorderLayout.EAST);
        // ส่งหนังสือและตำแหน่งตอนที่เลือกไปยังหน้าอ่าน
        Nav.onClick(tile, () -> Nav.openReader(this, book, index));
        return tile;
    }

    // ===== 2. แสดงรีวิวที่มีอยู่และฟอร์มเขียนรีวิว =====
    /** แสดงค่าเฉลี่ยคะแนนจากรีวิวที่โหลดมาจาก `reviews.csv` */
    private JComponent buildReviewCard() {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Theme.card());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(18, 22, 23, 27)));

        // แสดงชื่อส่วนและจำนวนรีวิว
        JLabel header = new JLabel("คะแนนและรีวิว");
        header.setFont(Fonts.title(18));
        header.setForeground(Theme.accent());
        JLabel count = new JLabel("รีวิวทั้งหมด " + book.reviews.size() + " รายการ");
        count.setFont(Fonts.body(13));
        count.setForeground(Theme.muted());
        JPanel headRow = new JPanel(new BorderLayout());
        headRow.setOpaque(false);
        headRow.add(header, BorderLayout.WEST);
        headRow.add(count, BorderLayout.EAST);

        // แยกสัญลักษณ์ดาว คะแนนตัวเลข และคำอธิบายออกจากกัน
        int ratedCount = book.ratedReviewCount();
        JLabel bigStars = new JLabel(ratedCount == 0 ? ""
                : stars((int) Math.round(book.averageReviewRating())));
        bigStars.setFont(new Font(Font.DIALOG, Font.PLAIN, 28));
        bigStars.setForeground(Theme.gold());
        JLabel score = new JLabel(ratedCount == 0 ? "—"
                : String.format(java.util.Locale.ROOT, "%.1f", book.averageReviewRating()));
        score.setFont(Fonts.bold(26));
        score.setForeground(Theme.text());
        JLabel avg = new JLabel(ratedCount == 0 ? "ยังไม่มีคะแนน" : "คะแนนเฉลี่ย");
        avg.setFont(Fonts.body(13));
        avg.setForeground(Theme.muted());
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        summary.setOpaque(false);
        summary.add(bigStars);
        summary.add(score);
        summary.add(avg);

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(headRow, BorderLayout.NORTH);
        top.add(summary, BorderLayout.CENTER);
        card.add(top, BorderLayout.NORTH);

        // สร้างหนึ่งแถวต่อหนึ่งรีวิวที่ผู้ใช้ส่งไว้
        JPanel list = new JPanel(new GridLayout(0, 1, 0, 0));
        list.setOpaque(false);
        for (String[] review : book.reviews) {
            list.add(makeReviewRow(review[0], Integer.parseInt(review[1]), review[2]));
        }
        if (book.reviews.isEmpty()) {
            JLabel empty = new JLabel("ยังไม่มีรีวิว เป็นคนแรกที่เขียนรีวิวได้เลย");
            empty.setFont(Fonts.body(14));
            empty.setForeground(Theme.muted());
            empty.setBorder(new EmptyBorder(12, 0, 6, 0));
            list.add(empty);
        }
        card.add(list, BorderLayout.CENTER);
        return card;
    }

    /** จัดชื่อ คะแนน และข้อความของรีวิวหนึ่งรายการให้อ่านได้ในแถวเดียว */
    private JComponent makeReviewRow(String name, int starCount, String text) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.line()),
                new EmptyBorder(12, 0, 12, 0)));

        JPanel avatar = new JPanel(new GridBagLayout());
        avatar.setBackground(Theme.accentFill());
        avatar.setPreferredSize(new Dimension(38, 38));
        JLabel letter = new JLabel(name.substring(0, 1));
        letter.setFont(Fonts.bold(16));
        letter.setForeground(Color.WHITE);
        avatar.add(letter);
        JPanel avatarWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        avatarWrap.setOpaque(false);
        avatarWrap.add(avatar);
        row.add(avatarWrap, BorderLayout.WEST);

        JLabel who = new JLabel(name);
        who.setFont(Fonts.bold(14));
        who.setForeground(Theme.text());
        JLabel starLabel = new JLabel(starCount == 0 ? "ความคิดเห็น" : stars(starCount));
        starLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 13));
        starLabel.setForeground(Theme.gold());
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        nameRow.setOpaque(false);
        nameRow.add(who);
        nameRow.add(starLabel);

        JTextArea body = makeWrappedText(text, Fonts.body(14), Theme.muted(), 68);
        body.setRows(Math.max(2, (text.length() + 67) / 68));
        JPanel content = new JPanel(new BorderLayout(0, 2));
        content.setOpaque(false);
        content.add(nameRow, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        row.add(content, BorderLayout.CENTER);
        return row;
    }

    /*
     * สร้างฟอร์มเขียนรีวิวพร้อมปุ่มเลือกคะแนนและส่งข้อความ
     */
    private JComponent buildWriteReviewCard() {
        if (!AdminSession.isLoggedIn()) {
            int unreadCount;
            try {
                unreadCount = ReviewLogic.unreadChapterCount(book);
            } catch (IllegalStateException error) {
                showStorageError(error);
                return reviewLockedCard(-1);
            }
            if (!ReviewLogic.canWriteReview(book, unreadCount)) {
                return reviewLockedCard(unreadCount);
            }
        }

        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Theme.card());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(18, 22, 23, 27)));

        JLabel header = new JLabel(AdminSession.isLoggedIn()
                ? "แอดมินแสดงความคิดเห็น" : "เขียนรีวิว");
        header.setFont(Fonts.title(18));
        header.setForeground(Theme.accent());
        card.add(header, BorderLayout.NORTH);

        boolean isAdmin = AdminSession.isLoggedIn();
        // สมาชิกเลือกคะแนนดาว ส่วนแอดมินส่งความคิดเห็นอย่างเดียวได้
        JPanel starRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        starRow.setOpaque(false);
        JLabel starTitle = new JLabel("ให้คะแนน");
        starTitle.setFont(Fonts.bold(13));
        starTitle.setForeground(Theme.text());
        if (!isAdmin) {
            starRow.add(starTitle);
        }
        // ใช้อาร์เรย์หนึ่งช่องเพื่อให้ action ของปุ่มอัปเดตคะแนนที่เลือกได้
        final int[] selectedRating = {0};
        JLabel selectedLabel = new JLabel("กรุณาเลือกคะแนนก่อน");
        selectedLabel.setFont(Fonts.body(12));
        selectedLabel.setForeground(Theme.muted());
        PillButton[] starButtons = new PillButton[5];
        for (int n = 1; !isAdmin && n <= 5; n++) {
            final int rating = n;
            PillButton star = new PillButton("☆ " + n + " ดาว", PillButton.OUTLINE);
            starButtons[n - 1] = star;
            star.setToolTipText("ให้คะแนน " + n + " ดาว");
            star.addActionListener(e -> {
                // บันทึกคะแนนที่เลือก แล้วเปลี่ยนสัญลักษณ์ให้เห็นปุ่มปัจจุบัน
                selectedRating[0] = rating;
                selectedLabel.setText("เลือกแล้ว " + rating + " ดาว");
                for (int i = 0; i < starButtons.length; i++) {
                    boolean selected = i + 1 == rating;
                    starButtons[i].setText((selected ? "★ " : "☆ ") + (i + 1) + " ดาว");
                    starButtons[i].setForeground(selected ? Theme.accent() : Theme.text());
                }
            });
            starRow.add(star);
        }
        if (!isAdmin) {
            starRow.add(selectedLabel);
        }

        // ช่องพิมพ์ความคิดเห็น
        HintTextBox areaBox = new HintTextBox(4, 40,
                isAdmin ? "เขียนความคิดเห็นสำหรับสมาชิกทุกคน…" : "แบ่งปันความรู้สึกของคุณต่อเรื่องนี้…");

        JPanel sendRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        sendRow.setOpaque(false);
        PillButton send = new PillButton(isAdmin ? "ส่งความคิดเห็น" : "ส่งรีวิว", PillButton.PRIMARY);
        // ส่งรีวิวเมื่อผู้ใช้กรอกข้อความแล้วเท่านั้น
        send.addActionListener(e -> {
            String reviewText = areaBox.getTextArea().getText().trim();
            try {
                String reviewerName = UserSession.username();
                String reviewerEmail = UserSession.accountKey();
                if (AdminSession.isLoggedIn()) {
                    reviewerName = AdminSession.username();
                    reviewerEmail = "admin";
                }
                ReviewLogic.submit(book, reviewerName, reviewerEmail,
                        selectedRating[0], reviewText);
                // addReview บันทึกและโหลดรีวิวจาก CSV กลับมาแล้ว
                Nav.openDetail(this, book);
            } catch (java.io.IOException error) {
                JOptionPane.showMessageDialog(this, error.getMessage(),
                        "ส่งรีวิวไม่ได้", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        sendRow.add(send);

        JPanel form = new JPanel(new BorderLayout(0, 12));
        form.setOpaque(false);
        if (!isAdmin) {
            form.add(starRow, BorderLayout.NORTH);
        }
        form.add(areaBox, BorderLayout.CENTER);
        form.add(sendRow, BorderLayout.SOUTH);
        card.add(form, BorderLayout.CENTER);
        return card;
    }

    /** แสดงข้อความแทนฟอร์ม ถ้ายังอ่านและทำเครื่องหมายครบทุกตอนไม่ได้ */
    private JComponent reviewLockedCard(int unreadCount) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(Theme.card());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.line()), new EmptyBorder(18, 22, 20, 27)));
        JLabel title = new JLabel("เขียนรีวิว");
        title.setFont(Fonts.title(18));
        title.setForeground(Theme.accent());
        card.add(title, BorderLayout.NORTH);

        String message = "กรุณาอ่านให้จบครบทุกตอน และทำเครื่องหมายว่าอ่านแล้วทุกตอน เพื่อคอมเมนต์";
        if (unreadCount >= 0 && !book.chapterTitles.isEmpty()) {
            message += "\nเหลืออีก " + unreadCount + " ตอน";
        } else if (unreadCount < 0) {
            message = "อ่านสถานะไม่ได้ กรุณาตรวจไฟล์ read_chapters.csv";
        } else {
            message = "หนังสือเล่มนี้ยังไม่มีตอนให้อ่าน";
        }
        JTextArea instruction = makeWrappedText(message, Fonts.body(14), Theme.muted(), 64);
        instruction.setRows(2);
        card.add(instruction, BorderLayout.CENTER);
        return card;
    }

    /** แสดงข้อความหลายบรรทัดด้วย JTextArea ของ Swing */
    private static JTextArea makeWrappedText(String value, java.awt.Font font,
                                              Color color, int columns) {
        JTextArea area = new JTextArea(value);
        area.setFont(font);
        area.setForeground(color);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setColumns(columns);
        area.setBorder(null);
        return area;
    }

    /** แจ้งผู้ใช้เมื่ออ่านหรือเขียนไฟล์ข้อมูลไม่สำเร็จ */
    private void showStorageError(IllegalStateException error) {
        JOptionPane.showMessageDialog(this, error.getMessage(), "อ่าน/เขียน CSV ไม่สำเร็จ",
                JOptionPane.ERROR_MESSAGE);
    }

    /*
     * สร้างดาวห้าตำแหน่ง โดยเติมดาวทึบตามคะแนนและใช้ดาวโปร่งในตำแหน่งที่เหลือ
     * จำกัดจำนวนตำแหน่งไว้ห้าดวงให้ตรงกับรูปแบบคะแนนของแอป
     */
    private static String stars(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= count ? "★" : "☆");
        }
        return sb.toString();
    }

    /*
     * เพิ่มการ์ดลงในคอลัมน์แนวตั้งและแทรกระยะห่างตามต้องการ
     * ทุกชิ้นถูกจัดชิดซ้ายเพื่อให้ขอบการ์ดเรียงตรงกัน
     */
    private static void addToColumn(JPanel column, JComponent c, int gap) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(c);
        column.add(Box.createVerticalStrut(gap));
    }

    // ===== 3. เมธอดช่วยจัดข้อความและเปิดหน้าทดลอง =====
    /**
     * เปิดหน้ารายละเอียดเพื่อทดลองหนังสือที่ต้องการ
     * หากไม่ส่งเลขหนังสือมา จะแสดงกล่องให้เลือกจากรายการ
     *
     * @param args เลขหนังสือที่เริ่มนับจาก 1; หากไม่ใส่จะแสดงหน้าต่างให้เลือก
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            java.util.List<Book> books = BookCatalogLogic.allBooks();
            if (books.isEmpty()) {
                JOptionPane.showMessageDialog(null, "ยังไม่มีข้อมูลหนังสือ");
                return;
            }

            if (args.length > 0) {
                try {
                    int number = Integer.parseInt(args[0]);
                    number = Math.max(1, Math.min(books.size(), number));
                    new DetailScreen(books.get(number - 1));
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "กรุณาระบุเลขเรื่องเป็นตัวเลข เช่น 1");
                }
                return;
            }

            JComboBox<Book> selector = new JComboBox<>(books.toArray(new Book[0]));
            selector.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                        JList<?> list, Object value, int index, boolean selected, boolean focus) {
                    super.getListCellRendererComponent(list, value, index, selected, focus);
                    setText(value instanceof Book ? ((Book) value).titleTh : "");
                    return this;
                }
            });
            int result = JOptionPane.showConfirmDialog(
                    null, selector, "เลือกหนังสือที่ต้องการดู", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                new DetailScreen((Book) selector.getSelectedItem());
            }
        });
    }
}
