package storage.admin;

// ไกด์ไฟล์: หน้าที่: จัดเก็บบัญชีแอดมิน หนังสือ ตอน รูปปก และสถานะหนังสือที่ลบ
// addBook/updateBook/deleteBook เปลี่ยนข้อมูลจริง; loadBooks โหลดกลับเข้า BookData
// เมธอด bookRow/HEADER กำหนดคอลัมน์ books.csv; เมธอด saveChapter และ loadChapters จัดการไฟล์ตอน

import app.ProjectPaths;
import books.Book;
import books.BookData;
import books.Chapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** อ่านและเขียนบัญชีแอดมิน หนังสือ และตอนใน CSV */
public final class AdminStorage {
    private static final Path DATA = ProjectPaths.dataFolder();
    private static final Path ADMINS = DATA.resolve("admins.csv");
    private static final Path BOOKS = DATA.resolve("books.csv");
    private static final Path CHAPTER_FOLDERS = DATA.resolve("book_chapters");
    private static final Path DELETED_BOOKS = DATA.resolve("deleted_books.csv");

    private static final String[] ADMIN_HEADER = {"username", "email", "password"};
    private static final String[] BOOK_HEADER = {
        "id", "titleTh", "titleEn", "author", "category", "status", "year", "blurb", "coverPath"
    };
    private static final String[] CHAPTER_HEADER = {"chapterIndex", "chapterTitle", "content"};
    private static final String[] DELETED_HEADER = {"bookId"};

    // ===== เตรียมไฟล์และจัดการบัญชีแอดมิน =====
    private AdminStorage() {
    }

    /** สร้างไฟล์แอดมินที่มีเฉพาะหัวตาราง หากยังไม่มีไฟล์ */
    public static void initialize() throws IOException {
        Files.createDirectories(DATA);
        makeFile(ADMINS, ADMIN_HEADER);
        makeFile(BOOKS, BOOK_HEADER);
        makeFile(DELETED_BOOKS, DELETED_HEADER);
        Files.createDirectories(CHAPTER_FOLDERS);
    }

    /** คืนชื่อแอดมินเมื่ออีเมลและรหัสผ่านตรงกับ admins.csv */
    public static String login(String email, String password) throws IOException {
        initialize();
        for (String[] admin : readRows(ADMINS)) {
            if (admin.length >= 3 && admin[1].equalsIgnoreCase(email.trim())
                    && admin[2].equals(password)) {
                return admin[0];
            }
        }
        return null;
    }

    /** โหลดเนื้อหาตอนจากไฟล์แยกของแต่ละหนังสือ และหนังสือใหม่จาก books.csv */
    public static void loadBooks() throws IOException {
        initialize();
        // ตัดหนังสือที่แอดมินลบออก เพื่อไม่ให้หนังสือตัวอย่างกลับมาเมื่อเปิดแอปใหม่
        for (String[] row : readRows(DELETED_BOOKS)) {
            if (row.length > 0) removeBookFromMemory(row[0]);
        }

        // ข้อมูลใน CSV เป็นข้อมูลล่าสุด ใช้แทนหนังสือเริ่มต้นที่มีรหัสเดียวกัน
        for (String[] row : readRows(BOOKS)) {
            if (row.length < 9) {
                continue;
            }
            try {
                removeBookFromMemory(row[0]);
                Book book = new Book(row[0], row[1], row[2], row[3], row[4], row[5],
                        Integer.parseInt(row[6]), 0, 0x386641, row[7], row[8]);
                loadChapters(book);
                BookData.ALL.add(book);
            } catch (NumberFormatException error) {
                // ข้ามแถวที่ปีใน CSV ไม่ใช่ตัวเลข
            }
        }

        for (Book book : BookData.ALL) {
            if (!hasBookRow(book.id)) loadChapters(book);
        }
    }

    // ===== โหลดและบันทึกหนังสือกับเนื้อหาแต่ละตอน =====
    /** โหลดไฟล์ chapter_1.csv, chapter_2.csv ต่อกันตามลำดับตอน */
    private static void loadChapters(Book book) throws IOException {
        Path bookFolder = CHAPTER_FOLDERS.resolve(bookFolderName(book));
        int chapterNumber = 1;

        while (Files.exists(bookFolder.resolve("chapter_" + chapterNumber + ".csv"))) {
            Path chapterFile = bookFolder.resolve("chapter_" + chapterNumber + ".csv");
            List<String[]> rows = readRows(chapterFile);
            if (!rows.isEmpty() && rows.get(0).length >= 3) {
                String[] chapter = rows.get(0);
                book.addChapter(chapter[1], decodeLineBreaks(chapter[2]));
            }
            chapterNumber++;
        }
    }

    /** บันทึกข้อมูลหนังสือและไฟล์ตอนก่อนเพิ่มหนังสือเข้าไปในรายการ */
    public static Book addBook(String title, String titleEn, String author, String category,
                               String status, int year, String blurb, Path cover,
                               List<Chapter> chapters)
            throws IOException {
        String id = nextId();
        String coverPath = copyCover(id, cover);
        Book book = new Book(id, title.trim(), titleEn.trim(), author.trim(), category.trim(),
                status, year, 0, 0x386641, blurb.trim(), coverPath);
        removeDeletedMarker(id);
        // เขียนไฟล์ตอนให้ครบก่อน เพื่อไม่ให้ books.csv มีหนังสือที่ยังไม่มีเนื้อหา
        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            saveChapter(book, i, chapter.title, chapter.content);
            book.addChapter(chapter.title, chapter.content);
        }

        // เพิ่มข้อมูลหนังสือหลังจากบันทึกตอนครบแล้ว
        appendRow(BOOKS, BOOK_HEADER, new String[]{book.id, book.titleTh, book.titleEn,
                book.author, book.category, book.status, String.valueOf(book.year),
                book.blurb, book.coverPath});
        BookData.ALL.add(book);
        return book;
    }

    /** บันทึกการแก้ไขข้อมูลและเขียนไฟล์ตอนใหม่ โดยใช้รหัสหนังสือเดิม */
    public static Book updateBook(Book oldBook, String title, String titleEn, String author,
                                  String category, String status, int year, String blurb,
                                  Path cover, List<Chapter> chapters) throws IOException {
        // ถ้าไม่ได้เลือกรูปใหม่ ให้เก็บที่อยู่รูปปกเดิมไว้
        String coverPath = cover == null ? oldBook.coverPath : copyCover(oldBook.id, cover);
        // สร้างข้อมูลเล่มใหม่จากค่าที่แอดมินแก้ โดยใช้รหัสหนังสือเดิม
        Book updated = new Book(oldBook.id, title.trim(), titleEn.trim(), author.trim(),
                category.trim(), status, year, oldBook.rating, oldBook.color.getRGB(),
                blurb.trim(), coverPath);
        // คัดลอกรีวิวเดิมไว้เพื่อให้ยังแสดงคะแนนและข้อความรีวิวหลังแก้หนังสือ
        for (String[] review : oldBook.reviews) {
            if (review.length >= 3) {
                updated.addReview(review[0], Integer.parseInt(review[1]), review[2]);
            }
        }

        // เขียนตอนใหม่ตามลำดับในฟอร์ม และเก็บตอนในออบเจ็กต์ที่จะใช้งานทันที
        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            saveChapter(updated, i, chapter.title, chapter.content);
            updated.addChapter(chapter.title, chapter.content);
        }

        // ถ้าชื่อเล่มเปลี่ยน โฟลเดอร์ตอนจะเปลี่ยนตามชื่อ จึงลบโฟลเดอร์เก่าหลังเขียนใหม่
        Path oldFolder = CHAPTER_FOLDERS.resolve(bookFolderName(oldBook));
        Path newFolder = CHAPTER_FOLDERS.resolve(bookFolderName(updated));
        if (!oldFolder.equals(newFolder)) {
            deleteFolder(oldFolder);
        } else {
            deleteExtraChapters(newFolder, chapters.size());
        }

        // อัปเดต CSV หลักและชื่อที่บันทึกคู่กับข้อมูลของผู้ใช้
        replaceBookRow(updated);
        updateBookTitle(DATA.resolve("reviews.csv"),
                new String[]{"bookId", "bookTitle", "username", "email", "rating", "comment"},
                0, 1, updated.id, updated.titleTh);
        updateBookTitle(DATA.resolve("favorites.csv"),
                new String[]{"email", "bookId", "bookTitle"}, 1, 2, updated.id, updated.titleTh);
        updateBookTitle(DATA.resolve("read_chapters.csv"),
                new String[]{"email", "bookId", "bookTitle", "chapterIndex", "chapterTitle"},
                1, 2, updated.id, updated.titleTh);
        // แทนหนังสือเดิมในรายการที่หน้าจอใช้อยู่
        int oldIndex = BookData.ALL.indexOf(oldBook);
        if (oldIndex >= 0) BookData.ALL.set(oldIndex, updated);
        removeDeletedMarker(updated.id);
        // ลบรูปเดิมเมื่อเลือกรูปใหม่ เพื่อไม่ทิ้งไฟล์ที่ไม่ได้ใช้งาน
        if (!oldBook.coverPath.equals(coverPath) && !oldBook.coverPath.isEmpty()) {
            Files.deleteIfExists(DATA.resolve(oldBook.coverPath));
        }
        return updated;
    }

    /** ลบหนังสือและข้อมูลที่ผูกกับหนังสือออกจากไฟล์ CSV */
    public static void deleteBook(Book book) throws IOException {
        // ลบแถวใน CSV หนังสือและบันทึกเครื่องหมายกันหนังสือตัวอย่างคืนมา
        removeRowsForId(BOOKS, BOOK_HEADER, 0, book.id);
        appendDeletedMarker(book.id);
        // ลบโฟลเดอร์ตอนและไฟล์รูปปกของเล่ม
        deleteFolder(CHAPTER_FOLDERS.resolve(bookFolderName(book)));
        if (!book.coverPath.isEmpty()) Files.deleteIfExists(DATA.resolve(book.coverPath));
        // ลบข้อมูลผู้ใช้ทุกชนิดที่ผูกกับรหัสหนังสือนี้
        removeRowsForId(DATA.resolve("reviews.csv"),
                new String[]{"bookId", "bookTitle", "username", "email", "rating", "comment"}, 0, book.id);
        removeRowsForId(DATA.resolve("favorites.csv"),
                new String[]{"email", "bookId", "bookTitle"}, 1, book.id);
        removeRowsForId(DATA.resolve("read_chapters.csv"),
                new String[]{"email", "bookId", "bookTitle", "chapterIndex", "chapterTitle"}, 1, book.id);
        // เอาหนังสือออกจากรายการในหน่วยความจำทันที
        removeBookFromMemory(book.id);
    }

    /** แทนข้อมูลหนังสือเดิมใน books.csv หรือเพิ่มแถวใหม่เมื่อยังไม่มี */
    private static void replaceBookRow(Book book) throws IOException {
        List<String[]> rows = readRows(BOOKS);
        List<String[]> updatedRows = new ArrayList<String[]>();
        boolean found = false;
        for (String[] row : rows) {
            if (row.length > 0 && row[0].equals(book.id)) {
                if (!found) updatedRows.add(bookRow(book));
                found = true;
            } else {
                updatedRows.add(row);
            }
        }
        if (!found) updatedRows.add(bookRow(book));
        writeRows(BOOKS, BOOK_HEADER, updatedRows);
    }

    /** แปลงข้อมูลหนังสือเป็นลำดับคอลัมน์เดียวกับหัวตาราง CSV */
    private static String[] bookRow(Book book) {
        return new String[]{book.id, book.titleTh, book.titleEn, book.author, book.category,
                book.status, String.valueOf(book.year), book.blurb, book.coverPath};
    }

    /** เขียนไฟล์ใหม่โดยตัดทุกแถวที่คอลัมน์รหัสตรงกับหนังสือเป้าหมาย */
    private static void removeRowsForId(Path file, String[] header, int column, String id)
            throws IOException {
        List<String[]> updatedRows = new ArrayList<String[]>();
        for (String[] row : readRows(file)) {
            if (row.length <= column || !row[column].equals(id)) updatedRows.add(row);
        }
        writeRows(file, header, updatedRows);
    }

    /** อัปเดตชื่อที่คัดลอกไว้ใน CSV รายการโปรด ประวัติอ่าน และรีวิว */
    private static void updateBookTitle(Path file, String[] header, int idColumn,
                                        int titleColumn, String id, String title)
            throws IOException {
        List<String[]> rows = readRows(file);
        for (String[] row : rows) {
            if (row.length > titleColumn && row[idColumn].equals(id)) row[titleColumn] = title;
        }
        writeRows(file, header, rows);
    }

    /** จำรหัสหนังสือที่ลบไว้ เพื่อกันหนังสือตัวอย่างกลับมาเมื่อเปิดแอปใหม่ */
    private static void appendDeletedMarker(String id) throws IOException {
        for (String[] row : readRows(DELETED_BOOKS)) {
            if (row.length > 0 && row[0].equals(id)) return;
        }
        appendRow(DELETED_BOOKS, DELETED_HEADER, new String[]{id});
    }

    /** ล้างเครื่องหมายลบเมื่อเพิ่มหนังสือที่ใช้รหัสนี้กลับเข้าระบบ */
    private static void removeDeletedMarker(String id) throws IOException {
        removeRowsForId(DELETED_BOOKS, DELETED_HEADER, 0, id);
    }

    /** ตรวจว่ามีข้อมูลหนังสือรหัสนี้อยู่ใน books.csv หรือไม่ */
    private static boolean hasBookRow(String id) throws IOException {
        for (String[] row : readRows(BOOKS)) {
            if (row.length > 0 && row[0].equals(id)) return true;
        }
        return false;
    }

    /** เอาหนังสือรหัสที่กำหนดออกจากรายการที่โปรแกรมกำลังใช้อยู่ */
    private static void removeBookFromMemory(String id) {
        Book existing = findBook(id);
        if (existing != null) BookData.ALL.remove(existing);
    }

    /** ลบไฟล์ตอนเก่าที่เกินจำนวนตอนใหม่หลังแอดมินแก้ไขหนังสือ */
    private static void deleteExtraChapters(Path folder, int chapterCount) throws IOException {
        if (!Files.isDirectory(folder)) return;
        for (int i = chapterCount + 1; Files.exists(folder.resolve("chapter_" + i + ".csv")); i++) {
            Files.delete(folder.resolve("chapter_" + i + ".csv"));
        }
    }

    /** ลบโฟลเดอร์และไฟล์ทั้งหมดจากข้างในออกจากล่างขึ้นบน */
    private static void deleteFolder(Path folder) throws IOException {
        if (!Files.exists(folder)) return;
        try (java.util.stream.Stream<Path> files = Files.walk(folder)) {
            Path[] paths = files.sorted(java.util.Comparator.reverseOrder()).toArray(Path[]::new);
            for (Path path : paths) Files.deleteIfExists(path);
        }
    }

    /** บันทึกตอนหนึ่งตอนเป็นไฟล์ CSV แยกในโฟลเดอร์ของหนังสือ */
    private static void saveChapter(Book book, int chapterIndex,
                                    String title, String content) throws IOException {
        Path bookFolder = CHAPTER_FOLDERS.resolve(bookFolderName(book));
        Files.createDirectories(bookFolder);
        Path chapterFile = bookFolder.resolve("chapter_" + (chapterIndex + 1) + ".csv");

        try (BufferedWriter writer = Files.newBufferedWriter(chapterFile, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeRow(writer, CHAPTER_HEADER);
            writeRow(writer, new String[]{String.valueOf(chapterIndex), title,
                    encodeLineBreaks(content)});
        }
    }

    /** ตั้งชื่อโฟลเดอร์เป็นรหัสและชื่อหนังสือ โดยแทนอักขระที่ห้ามใช้ในชื่อโฟลเดอร์ */
    private static String bookFolderName(Book book) {
        String safeTitle = book.titleTh.trim()
                .replace("/", "_").replace("\\", "_").replace(":", "_")
                .replace("*", "_").replace("?", "_").replace("\"", "_")
                .replace("<", "_").replace(">", "_").replace("|", "_");
        return book.id + "_" + safeTitle;
    }

    /** หาเลขรหัสหนังสือถัดไปที่ยังไม่ถูกใช้อยู่ในรายการ */
    private static String nextId() {
        int id = 1;
        while (findBook(String.valueOf(id)) != null) {
            id++;
        }
        return String.valueOf(id);
    }

    /** ค้นหาหนังสือในรายการปัจจุบันด้วยรหัส */
    private static Book findBook(String id) {
        for (Book book : BookData.ALL) {
            if (book.id.equals(id)) {
                return book;
            }
        }
        return null;
    }

    /** ตรวจนามสกุลและคัดลอกรูปปกเข้า data/covers */
    private static String copyCover(String id, Path source) throws IOException {
        if (source == null) {
            return "";
        }
        String name = source.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? ".png" : name.substring(dot).toLowerCase();
        if (!extension.equals(".png") && !extension.equals(".jpg")
                && !extension.equals(".jpeg") && !extension.equals(".gif")) {
            throw new IOException("เลือกรูป .png, .jpg, .jpeg หรือ .gif เท่านั้น");
        }
        Path coverFolder = DATA.resolve("covers");
        Files.createDirectories(coverFolder);
        Files.copy(source, coverFolder.resolve(id + extension), StandardCopyOption.REPLACE_EXISTING);
        return "covers/" + id + extension;
    }

    /** สร้าง CSV พร้อมหัวตารางเมื่อไฟล์ยังไม่มีหรือไฟล์ว่าง */
    private static void makeFile(Path file, String[] header) throws IOException {
        if (!Files.exists(file) || Files.size(file) == 0) {
            BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            writeRow(writer, header);
            writer.close();
        }
    }

    /** อ่านข้อมูล CSV ทีละแถวและแปลงแต่ละแถวเป็นรายการคอลัมน์ */
    private static List<String[]> readRows(Path file) throws IOException {
        List<String[]> rows = new ArrayList<String[]>();
        if (!Files.exists(file)) {
            return rows;
        }
        BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
        String line;
        boolean header = true;
        while ((line = reader.readLine()) != null) {
            if (header) {
                header = false;
            } else if (!line.trim().isEmpty()) {
                rows.add(parseRow(line));
            }
        }
        reader.close();
        return rows;
    }

    /** เพิ่มแถวข้อมูลต่อท้าย CSV โดยสร้างหัวตารางถ้าจำเป็น */
    private static void appendRow(Path file, String[] header, String[] values) throws IOException {
        makeFile(file, header);
        BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);
        writeRow(writer, values);
        writer.close();
    }

    /** เขียนหัวตารางและแถวข้อมูลทั้งหมดทับลงใน CSV */
    private static void writeRows(Path file, String[] header, List<String[]> rows) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeRow(writer, header);
            for (String[] row : rows) writeRow(writer, row);
        }
    }

    /** เขียนค่าแต่ละคอลัมน์พร้อมใส่เครื่องหมายคำพูดเมื่อมี comma หรือ quote */
    private static void writeRow(BufferedWriter writer, String[] values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            if (i > 0) writer.write(',');
            String value = values[i] == null ? "" : values[i].replace("\r", " ").replace("\n", " ");
            if (value.contains(",") || value.contains("\"")) {
                writer.write('"');
                writer.write(value.replace("\"", "\"\""));
                writer.write('"');
            } else {
                writer.write(value);
            }
        }
        writer.newLine();
    }

    /** แยกคอลัมน์ CSV โดยไม่ตัด comma ที่อยู่ภายในเครื่องหมายคำพูด */
    private static String[] parseRow(String line) {
        List<String> columns = new ArrayList<String>();
        StringBuilder value = new StringBuilder();
        boolean quotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    quotes = !quotes;
                }
            } else if (c == ',' && !quotes) {
                columns.add(value.toString());
                value.setLength(0);
            } else {
                value.append(c);
            }
        }
        columns.add(value.toString());
        return columns.toArray(new String[columns.size()]);
    }

    /** แปลงการขึ้นบรรทัดใหม่ให้เก็บได้ใน CSV แบบหนึ่งแถวต่อหนึ่งตอน */
    private static String encodeLineBreaks(String text) {
        return text.replace("\\", "\\\\")
                .replace("\r\n", "\\n")
                .replace("\r", "\\n")
                .replace("\n", "\\n");
    }

    /** แปลงรหัสบรรทัดใหม่ใน CSV กลับเป็นข้อความสำหรับหน้าอ่าน */
    private static String decodeLineBreaks(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '\\' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                if (next == 'n') {
                    result.append('\n');
                    i++;
                } else if (next == '\\') {
                    result.append('\\');
                    i++;
                } else {
                    result.append(current);
                }
            } else {
                result.append(current);
            }
        }
        return result.toString();
    }
}
