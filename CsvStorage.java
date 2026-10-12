package storage;

// ไกด์ไฟล์: หน้าที่: อ่านและเขียน CSV ของรายการโปรด ประวัติอ่าน และรีวิว
// เมธอด initialize/ensureFile เตรียมไฟล์; เมธอด parseRow/writeRow จัดการรูปแบบ CSV
// เมธอด add/remove/load... เป็นจุดแก้ข้อมูลแต่ละฟีเจอร์; ต้องรักษาลำดับคอลัมน์ของ HEADER ให้ตรงกับการอ่าน

import app.ProjectPaths;
import books.Book;
import books.BookData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** อ่านและเขียน CSV ของบัญชี รายการโปรด ประวัติอ่าน และรีวิว */
public final class CsvStorage {
    /** ใช้โฟลเดอร์ data เดียวกับส่วนบัญชี */
    private static final Path DATA_FOLDER = ProjectPaths.dataFolder();

    /** ชื่อคอลัมน์ในไฟล์รีวิว */
    private static final String[] REVIEW_HEADER = {
        "bookId", "bookTitle", "username", "email", "rating", "comment"
    };

    /** ชื่อคอลัมน์ในไฟล์รายการโปรด */
    private static final String[] FAVORITE_HEADER = {"email", "bookId", "bookTitle"};

    /** ชื่อคอลัมน์ในไฟล์ประวัติอ่านรายตอน */
    private static final String[] READ_HEADER = {
        "email", "bookId", "bookTitle", "chapterIndex", "chapterTitle"
    };

    /** ป้องกันไม่ให้สร้างคลาสนี้ เพราะใช้เมธอด static */
    private CsvStorage() {
    }

    /** สร้างโฟลเดอร์และไฟล์ CSV ที่จำเป็น แล้วโหลดรีวิวจาก CSV */
    public static void initialize() throws IOException {
        // สร้างโฟลเดอร์กลางของข้อมูลก่อนเปิดหรือเขียนไฟล์ใด ๆ
        Files.createDirectories(DATA_FOLDER);
        // เตรียมไฟล์แต่ละชนิดพร้อมหัวตาราง หากยังไม่มีไฟล์
        ensureFile("reviews.csv", REVIEW_HEADER);
        ensureFile("favorites.csv", FAVORITE_HEADER);
        ensureFile("read_chapters.csv", READ_HEADER);
        reloadReviews();
    }

    /** สร้างไฟล์ใหม่พร้อมแถวหัวตาราง ถ้าไฟล์ยังไม่มีอยู่ */
    private static void ensureFile(String fileName, String[] header) throws IOException {
        // ต่อชื่อไฟล์เข้ากับโฟลเดอร์ data ของโปรเจกต์
        Path file = DATA_FOLDER.resolve(fileName);
        // สร้างไฟล์ใหม่หรือเติมหัวตารางให้ไฟล์ว่าง
        if (!Files.exists(file) || Files.size(file) == 0) {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    file, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                writeRow(writer, header);
            }
        }
    }

    /** อ่านข้อมูลทุกแถวโดยข้ามแถวหัวตาราง */
    private static List<String[]> readRows(String fileName, String[] header) throws IOException {
        // ตรวจให้แน่ใจว่าไฟล์พร้อมอ่านก่อนเปิด
        ensureFile(fileName, header);
        // เก็บแต่ละแถวเป็นอาร์เรย์ของคอลัมน์
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(
                DATA_FOLDER.resolve(fileName), StandardCharsets.UTF_8)) {
            String line;
            // แถวแรกเป็นชื่อคอลัมน์ จึงข้ามก่อนเริ่มเก็บข้อมูล
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    // แยกค่าในบรรทัดตามกฎ CSV แล้วเพิ่มลงรายการผลลัพธ์
                    rows.add(parseRow(line));
                }
            }
        }
        return rows;
    }

    /** แปลงข้อความ CSV หนึ่งบรรทัดให้เป็นรายการคอลัมน์ */
    private static String[] parseRow(String line) {
        // เก็บคอลัมน์ที่อ่านเสร็จ และค่าของคอลัมน์ปัจจุบัน
        List<String> columns = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            // อ่านทีละตัวอักษรเพื่อไม่ตัด comma ที่อยู่ในข้อความอ้างอิง
            char current = line.charAt(i);

            if (current == '"') {
                // เครื่องหมายคำพูดคู่ใน CSV หมายถึงเครื่องหมายคำพูดหนึ่งตัวในข้อมูล
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (current == ',' && !insideQuotes) {
                columns.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }

        columns.add(value.toString());
        // แปลงรายการคอลัมน์เป็นอาร์เรย์ให้โค้ดส่วนอื่นใช้ง่าย
        return columns.toArray(new String[0]);
    }

    /** เขียนรายการคอลัมน์เป็น CSV หนึ่งบรรทัด */
    private static void writeRow(BufferedWriter writer, String[] columns) throws IOException {
        for (int i = 0; i < columns.length; i++) {
            // คั่นค่าด้วย comma ยกเว้นคอลัมน์แรก
            if (i > 0) {
                writer.write(',');
            }
            writer.write(escape(columns[i]));
        }
        writer.newLine();
    }

    /** ใส่เครื่องหมายคำพูดเมื่อข้อมูลมี comma หรือ quote เพื่อไม่ให้คอลัมน์เลื่อน */
    private static String escape(String value) {
        // แทนค่า null ด้วยข้อความว่าง เพราะ CSV ไม่เก็บ null โดยตรง
        String safeValue = value == null ? "" : value;
        // เก็บข้อมูลหนึ่งรายการต่อบรรทัดโดยแทนการขึ้นบรรทัดใหม่เป็นช่องว่าง
        safeValue = safeValue.replace("\r", " ").replace("\n", " ");

        if (safeValue.contains(",") || safeValue.contains("\"")) {
            safeValue = safeValue.replace("\"", "\"\"");
            return "\"" + safeValue + "\"";
        }
        return safeValue;
    }

    /** เพิ่มข้อมูลหนึ่งแถวต่อท้ายไฟล์ CSV */
    private static void appendRow(String fileName, String[] header, String[] row) throws IOException {
        // เตรียมไฟล์ก่อนต่อท้าย เพื่อให้มีหัวตารางเสมอ
        ensureFile(fileName, header);
        try (BufferedWriter writer = Files.newBufferedWriter(
                DATA_FOLDER.resolve(fileName),
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writeRow(writer, row);
        }
    }

    /** คืนอีเมลรูปแบบเดียวกันเพื่อใช้เปรียบเทียบและผูกข้อมูล */
    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    // ===== รายการโปรด =====
    /** ตรวจว่าหนังสืออยู่ในรายการโปรดของอีเมลนี้หรือไม่ */
    public static boolean isFavorite(String email, Book book) throws IOException {
        // อ่านรายการโปรดทั้งหมดแล้วหาแถวที่ตรงกับทั้งผู้ใช้และหนังสือ
        List<String[]> rows = readRows("favorites.csv", FAVORITE_HEADER);
        for (String[] row : rows) {
            if (row.length >= 2
                    && normalizeEmail(row[0]).equals(normalizeEmail(email))
                    && row[1].equals(book.id)) {
                return true;
            }
        }
        return false;
    }

    /** เพิ่มแถวหนังสือหนึ่งรายการลงใน CSV รายการโปรด */
    public static void addFavorite(String email, Book book) throws IOException {
        appendRow("favorites.csv", FAVORITE_HEADER, new String[]{
            normalizeEmail(email), book.id, book.titleTh
        });
    }

    /** ลบแถวหนังสือออกจาก CSV รายการโปรด */
    public static void removeFavorite(String email, Book book) throws IOException {
        // อ่านรายการเดิมก่อนสร้างรายการใหม่ที่ตัดรายการเป้าหมายออก
        List<String[]> rows = readRows("favorites.csv", FAVORITE_HEADER);
        String emailKey = normalizeEmail(email);
        List<String[]> updatedRows = new ArrayList<>();

        for (String[] row : rows) {
            // เก็บแถวที่ไม่ใช่ผู้ใช้และหนังสือคู่ที่ต้องการลบ
            boolean isSameFavorite = row.length >= 2
                    && normalizeEmail(row[0]).equals(emailKey)
                    && row[1].equals(book.id);
            if (!isSameFavorite) {
                updatedRows.add(row);
            }
        }
        writeRows("favorites.csv", FAVORITE_HEADER, updatedRows);
    }

    /** คืนรายการหนังสือโปรดของผู้ใช้ เรียงตามลำดับใน BookData */
    public static List<Book> loadFavorites(String email) throws IOException {
        // ใช้ CSV เป็นแหล่งข้อมูล แล้วจับคู่รหัสหนังสือกับรายการหนังสือในแอป
        List<String[]> rows = readRows("favorites.csv", FAVORITE_HEADER);
        List<Book> result = new ArrayList<>();

        for (Book book : BookData.ALL) {
            for (String[] row : rows) {
                if (row.length >= 2
                        && normalizeEmail(row[0]).equals(normalizeEmail(email))
                        && row[1].equals(book.id)) {
                    result.add(book);
                    break;
                }
            }
        }
        return result;
    }

    // ===== สถานะอ่านรายตอน =====
    /** ตรวจว่าผู้ใช้เคยทำเครื่องหมายอ่านตอนนี้แล้วหรือไม่ */
    public static boolean isChapterRead(String email, Book book, int chapterIndex) throws IOException {
        // อ่านประวัติและเตรียมอีเมลในรูปแบบเดียวกันเพื่อเทียบข้อมูล
        List<String[]> rows = readRows("read_chapters.csv", READ_HEADER);
        String emailKey = normalizeEmail(email);

        for (String[] row : rows) {
            if (row.length >= 4
                    && normalizeEmail(row[0]).equals(emailKey)
                    && row[1].equals(book.id)
                    && row[3].equals(String.valueOf(chapterIndex))) {
                return true;
            }
        }
        return false;
    }

    /** บันทึกตอนที่อ่านแล้ว ถ้ายังไม่มีแถวนี้ในไฟล์ */
    public static void markChapterRead(String email, Book book, int chapterIndex) throws IOException {
        // ไม่เขียนแถวซ้ำถ้าตอนนี้ถูกทำเครื่องหมายอ่านแล้ว
        if (isChapterRead(email, book, chapterIndex)) {
            return;
        }

        String chapterTitle = book.chapterTitles.get(chapterIndex);
        appendRow("read_chapters.csv", READ_HEADER, new String[]{
            normalizeEmail(email), book.id, book.titleTh,
            String.valueOf(chapterIndex), chapterTitle
        });
    }

    // ===== รีวิว =====
    /** เพิ่มรีวิวลงไฟล์ แล้วโหลดข้อมูลจากไฟล์กลับมาใช้แสดงผล */
    public static void addReview(Book book, String username, String email,
                                 int rating, String comment) throws IOException {
        // เก็บรหัสและชื่อหนังสือพร้อมข้อมูลผู้รีวิว คะแนน และข้อความ
        appendRow("reviews.csv", REVIEW_HEADER, new String[]{
            book.id, book.titleTh, username, normalizeEmail(email),
            String.valueOf(rating), comment
        });
        reloadReviews();
    }

    /** อ่านรีวิวจาก CSV ใหม่ และแทนข้อมูลรีวิวเดิมที่หน้าจอใช้อยู่ */
    private static void reloadReviews() throws IOException {
        // โหลดรีวิวจากไฟล์เพื่อให้หน้าจอแสดงข้อมูลล่าสุด
        List<String[]> rows = readRows("reviews.csv", REVIEW_HEADER);

        // ล้างข้อมูลเดิมก่อน เพื่อไม่ให้รีวิวซ้ำในหน้าจอเมื่อโหลดไฟล์อีกครั้ง
        for (Book book : BookData.ALL) {
            book.reviews.clear();
        }

        for (String[] row : rows) {
            // ข้ามแถวที่คอลัมน์ไม่ครบตามรูปแบบรีวิว
            if (row.length < 6) {
                continue;
            }

            for (Book book : BookData.ALL) {
                // เติมรีวิวให้หนังสือที่มีรหัสตรงกับแถวใน CSV
                if (book.id.equals(row[0])) {
                    try {
                        int rating = Integer.parseInt(row[4]);
                        book.addReview(row[2], rating, row[5]);
                    } catch (NumberFormatException e) {
                        // ข้ามแถวที่คะแนนใน CSV ไม่ใช่ตัวเลข
                    }
                    break;
                }
            }
        }
    }

    /** เขียนหัวตารางและข้อมูลทั้งหมดทับไฟล์เดิม ใช้เมื่อลบรายการโปรด */
    private static void writeRows(String fileName, String[] header,
                                  List<String[]> rows) throws IOException {
        // เขียนไฟล์ใหม่ตั้งแต่ต้นด้วยหัวตารางและข้อมูลที่เหลือ
        Path file = DATA_FOLDER.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            writeRow(writer, header);
            for (String[] row : rows) {
                writeRow(writer, row);
            }
        }
    }
}
