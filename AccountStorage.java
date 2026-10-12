package storage.account;

// ไกด์ไฟล์: หน้าที่: อ่านและบันทึกบัญชีสมาชิกใน data/users.csv
// register ปฏิเสธอีเมลและชื่อผู้ใช้ที่ซ้ำ; normalizeEmail/normalizeUsername ทำให้เปรียบเทียบตัวพิมพ์ได้ตรงกัน
// login อ่านบัญชีและคืนชื่อ/อีเมล; หากเปลี่ยนคอลัมน์ ต้องปรับ HEADER, parse/write และจุดใช้งานพร้อมกัน

import app.ProjectPaths;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** จัดการข้อมูลสมัครสมาชิกและเข้าสู่ระบบ แยกจากส่วนหนังสือ */
public final class AccountStorage {

    // ไฟล์บัญชีอยู่ในโฟลเดอร์ data ของโปรเจ็กต์ ไม่ขึ้นกับตำแหน่งที่เปิดโปรแกรม
    private static final Path USER_FILE = ProjectPaths.dataFolder().resolve("users.csv");
    private static final String[] HEADER = {"username", "email", "password"};

    private AccountStorage() {
        // ไม่ต้องสร้าง object เพราะเรียกใช้เมธอดแบบ static
    }

    /** สร้างไฟล์ users.csv และหัวตารางเมื่อยังไม่มีไฟล์ */
    public static void initialize() throws IOException {
        // สร้างโฟลเดอร์ data ก่อนสร้างไฟล์ users.csv
        Files.createDirectories(USER_FILE.getParent());

        // ถ้าไฟล์ยังไม่มีข้อมูล ให้สร้างหัวตารางไว้ก่อน
        if (!Files.exists(USER_FILE) || Files.size(USER_FILE) == 0) {
            BufferedWriter writer = Files.newBufferedWriter(
                    USER_FILE, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            writeRow(writer, HEADER);
            writer.close();
        }
    }

    /** คืนตำแหน่งเต็มของไฟล์บัญชี เพื่อให้ตรวจดูไฟล์ที่บันทึกได้ */
    public static Path userFilePath() {
        return USER_FILE.toAbsolutePath();
    }

    /** สมัครบัญชีใหม่ ถ้าอีเมลหรือชื่อผู้ใช้ซ้ำจะคืนค่า false */
    public static boolean register(String username, String email, String password)
            throws IOException {
        // อ่านบัญชีที่มีอยู่ก่อน เพื่อเอาไว้ตรวจข้อมูลซ้ำ
        List<String[]> users = readUsers();
        // ลบช่องว่างหัวท้ายและเปลี่ยนอีเมลเป็นตัวเล็ก เพื่อเปรียบเทียบได้ตรงกัน
        String cleanEmail = normalizeEmail(email);
        // ทำแบบเดียวกันกับชื่อผู้ใช้ เช่น BookFan กับ bookfan จะถือว่าซ้ำ
        String cleanUsername = normalizeUsername(username);

        // วนดูบัญชีเดิมทีละบัญชี ก่อนจะบันทึกบัญชีใหม่
        for (String[] user : users) {
            // user[1] คือช่องอีเมล และต้องมีอย่างน้อย 2 ช่องก่อนอ่าน
            if (user.length >= 2 && normalizeEmail(user[1]).equals(cleanEmail)) {
                // เจออีเมลซ้ำ จึงไม่บันทึกและส่งผลว่าไม่สำเร็จกลับไป
                return false;
            }
            // user[0] คือช่องชื่อผู้ใช้ และต้องมีอย่างน้อย 1 ช่องก่อนอ่าน
            if (user.length >= 1 && normalizeUsername(user[0]).equals(cleanUsername)) {
                // เจอชื่อผู้ใช้ซ้ำ จึงไม่บันทึกและส่งผลว่าไม่สำเร็จกลับไป
                return false;
            }
        }

        // เปิดไฟล์ในโหมดต่อท้าย เพื่อเก็บบัญชีเดิมไว้ทั้งหมด
        BufferedWriter writer = Files.newBufferedWriter(
                USER_FILE, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        // เรียงข้อมูลให้ตรงกับหัวตาราง: ชื่อผู้ใช้, อีเมล, รหัสผ่าน
        String[] newUser = {username.trim(), cleanEmail, password};
        // เขียนข้อมูลใหม่เป็นแถว CSV หนึ่งแถว
        writeRow(writer, newUser);
        // ปิดไฟล์หลังเขียนเสร็จ เพื่อให้บันทึกข้อมูลครบ
        writer.close();
        // แจ้งกลับว่าบัญชีสมัครสำเร็จ
        return true;
    }

    /** ตรวจอีเมลกับรหัสผ่าน ถ้าถูกต้องคืนชื่อผู้ใช้และอีเมล */
    public static String[] login(String email, String password) throws IOException {
        List<String[]> users = readUsers();
        // ปรับอีเมลให้เป็นรูปแบบเดียวกับตอนสมัคร
        String cleanEmail = normalizeEmail(email);

        // คืนข้อมูลบัญชีเมื่อทั้งอีเมลและรหัสผ่านตรงกัน
        for (String[] user : users) {
            if (user.length >= 3
                    && normalizeEmail(user[1]).equals(cleanEmail)
                    && user[2].equals(password)) {
                return new String[]{user[0], user[1]};
            }
        }
        return null;
    }

    /** อ่านทุกแถวในไฟล์ โดยข้ามแถวแรกที่เป็นหัวตาราง */
    private static List<String[]> readUsers() throws IOException {
        // ให้แน่ใจว่ามีไฟล์และหัวตารางก่อนอ่าน
        initialize();
        // เก็บบัญชีแต่ละแถวโดยไม่รวมแถวหัวตาราง
        List<String[]> users = new ArrayList<String[]>();
        BufferedReader reader = Files.newBufferedReader(USER_FILE, StandardCharsets.UTF_8);
        String line;
        // ใช้ตัวแปรนี้ข้ามบรรทัดแรกที่เป็นชื่อคอลัมน์
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
            } else if (!line.trim().isEmpty()) {
                // แปลงบรรทัด CSV เป็นอาร์เรย์ชื่อผู้ใช้ อีเมล และรหัสผ่าน
                users.add(parseRow(line));
            }
        }
        reader.close();
        return users;
    }

    /** แบ่ง CSV เป็นคอลัมน์ โดยรองรับ comma ภายในเครื่องหมายคำพูด */
    private static String[] parseRow(String line) {
        // เก็บคอลัมน์ที่แยกแล้วและข้อความของช่องที่กำลังอ่าน
        List<String> values = new ArrayList<String>();
        StringBuilder value = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            // อ่านทีละอักขระ เพื่อรองรับ comma ที่อยู่ภายใน quote
            char current = line.charAt(i);

            if (current == '"') {
                if (insideQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    value.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (current == ',' && !insideQuotes) {
                values.add(value.toString());
                value.setLength(0);
            } else {
                value.append(current);
            }
        }

        values.add(value.toString());
        return values.toArray(new String[values.size()]);
    }

    /** เขียนชื่อ อีเมล หรือรหัสผ่านลง CSV หนึ่งแถว */
    private static void writeRow(BufferedWriter writer, String[] values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            // คั่นทุกคอลัมน์ด้วย comma
            if (i > 0) {
                writer.write(',');
            }

            String value = values[i];
            // แทน null และอักขระขึ้นบรรทัดใหม่ให้เขียนลง CSV ได้
            if (value == null) {
                value = "";
            }
            value = value.replace("\r", " ").replace("\n", " ");

            if (value.contains(",") || value.contains("\"")) {
                value = value.replace("\"", "\"\"");
                writer.write('"');
                writer.write(value);
                writer.write('"');
            } else {
                writer.write(value);
            }
        }
        writer.newLine();
    }

    /** เอาช่องว่างหัวท้ายออกและเปลี่ยนอีเมลเป็นตัวพิมพ์เล็ก */
    private static String normalizeEmail(String email) {
        // ตัดช่องว่างหัวท้ายและแปลงเป็นตัวเล็กก่อนเอาไปเปรียบเทียบ
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** เอาช่องว่างหัวท้ายออกและทำให้ชื่อผู้ใช้เป็นตัวพิมพ์เล็กก่อนเปรียบเทียบ */
    private static String normalizeUsername(String username) {
        // ตัดช่องว่างหัวท้ายและแปลงเป็นตัวเล็ก เพื่อกันชื่อซ้ำต่างตัวพิมพ์
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
