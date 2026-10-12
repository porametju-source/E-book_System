package storage.category;

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

/** อ่านและบันทึกชื่อหมวดหมู่ในไฟล์ CSV แยกจากข้อมูลหนังสือ */
public final class CategoryStorage {
    private static final Path FILE = ProjectPaths.dataFolder().resolve("categories.csv");
    private static final String HEADER = "categoryName";

    private CategoryStorage() {
    }

    /** อ่านชื่อหมวดทั้งหมดจากไฟล์ */
    public static List<String> loadCategories() throws IOException {
        createFileIfNeeded();
        List<String> categories = new ArrayList<String>();
        try (BufferedReader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            String line = reader.readLine(); // ข้ามหัวตาราง
            while ((line = reader.readLine()) != null) {
                String name = parseValue(line).trim();
                if (!name.isEmpty() && !containsIgnoreCase(categories, name)) categories.add(name);
            }
        }
        return categories;
    }

    /** เพิ่มหมวดใหม่ทันที และไม่บันทึกชื่อซ้ำ */
    public static void addCategory(String name) throws IOException {
        String cleanName = name.trim().replace("\r", " ").replace("\n", " ");
        if (cleanName.isEmpty()) throw new IOException("กรุณาพิมพ์ชื่อหมวดหมู่");
        List<String> categories = loadCategories();
        if (containsIgnoreCase(categories, cleanName)) return;

        try (BufferedWriter writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8,
                StandardOpenOption.APPEND)) {
            writer.write(encodeValue(cleanName));
            writer.newLine();
        }
    }

    /** สร้างโฟลเดอร์ data และไฟล์หมวดพร้อมหัวตารางเมื่อยังไม่มี */
    private static void createFileIfNeeded() throws IOException {
        Files.createDirectories(FILE.getParent());
        if (!Files.exists(FILE) || Files.size(FILE) == 0) {
            try (BufferedWriter writer = Files.newBufferedWriter(FILE, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                writer.write(HEADER);
                writer.newLine();
            }
        }
    }

    /** ตรวจชื่อซ้ำโดยไม่แยกตัวพิมพ์เล็กและตัวพิมพ์ใหญ่ */
    private static boolean containsIgnoreCase(List<String> names, String value) {
        for (String name : names) {
            if (name.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    /** ครอบค่าด้วยเครื่องหมายคำพูดเมื่อมี comma หรือ quote */
    private static String encodeValue(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** อ่านค่าหนึ่งช่องจาก CSV และคืนเครื่องหมายคำพูดที่ถูก escape ให้ถูกต้อง */
    private static String parseValue(String line) {
        if (line.length() >= 2 && line.startsWith("\"") && line.endsWith("\"")) {
            return line.substring(1, line.length() - 1).replace("\"\"", "\"");
        }
        return line;
    }
}
