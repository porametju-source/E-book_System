package logic.books;

// ไกด์ไฟล์: หน้าที่: รวมกฎการอ่าน เพิ่ม และลบหมวดหมู่ พร้อมตรวจสิทธิ์แอดมิน
// allCategories รวมหมวดเริ่มต้นกับข้อมูลที่บันทึก, addCategory/deleteCategory เปลี่ยนข้อมูล
// ตรวจรูปแบบและความซ้ำของชื่อหมวดในคลาสนี้; การอ่าน/เขียน categories.csv อยู่ใน CategoryStorage

import app.AdminSession;
import books.Book;
import books.BookData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import storage.category.CategoryStorage;

/** รวมและตรวจการทำงานเกี่ยวกับชื่อหมวดหมู่ */
public final class CategoryLogic {
    private CategoryLogic() {
    }

    /** คืนหมวดเริ่มต้น หมวดจากหนังสือ และหมวดที่แอดมินบันทึกไว้ */
    public static List<String> allCategories(String[] defaultNames) throws IOException {
        List<String> categories = new ArrayList<String>();
        // เริ่มด้วยหมวดหลักที่หน้า UI ต้องแสดงเสมอ
        for (String name : defaultNames) addUnique(categories, name);
        // รวมหมวดที่พบในหนังสือ เผื่อข้อมูลเก่าที่ไม่มี categories.csv
        for (Book book : BookData.ALL) addUnique(categories, book.category);
        // รวมหมวดที่แอดมินสร้างไว้ แม้ยังไม่มีหนังสือในหมวดนั้น
        for (String name : CategoryStorage.loadCategories()) addUnique(categories, name);
        return categories;
    }

    /** บันทึกหมวดใหม่ได้เมื่อแอดมินเข้าสู่ระบบแล้ว */
    public static void addCategory(String name) throws IOException {
        // ป้องกันผู้ใช้ทั่วไปเรียกส่วนบันทึกหมวดหมู่ของแอดมิน
        if (!AdminSession.isLoggedIn()) {
            throw new IOException("กรุณาเข้าสู่ระบบแอดมินก่อนเพิ่มหมวดหมู่");
        }
        // ป้องกันชื่อว่างก่อนส่งไปเขียนไฟล์
        if (name == null || name.trim().isEmpty()) {
            throw new IOException("กรุณาพิมพ์ชื่อหมวดหมู่");
        }
        // บันทึกชื่อหมวดลง CSV แยกจากหนังสือ
        CategoryStorage.addCategory(name);
    }

    /** ลบหมวดที่ไม่มีหนังสือใช้อยู่ และอนุญาตเฉพาะแอดมิน */
    public static void deleteCategory(String name) throws IOException {
        if (!AdminSession.isLoggedIn()) {
            throw new IOException("กรุณาเข้าสู่ระบบแอดมินก่อนลบหมวดหมู่");
        }
        if (name == null || name.trim().isEmpty() || "ทั้งหมด".equals(name.trim())) {
            throw new IOException("หมวดหมู่นี้ไม่สามารถลบได้");
        }

        for (Book book : BookData.ALL) {
            if (book.category.equalsIgnoreCase(name.trim())) {
                throw new IOException("ยังลบหมวดนี้ไม่ได้ เพราะมีหนังสือใช้งานอยู่");
            }
        }

        if (!CategoryStorage.deleteCategory(name)) {
            throw new IOException("ไม่พบหมวดหมู่ที่ต้องการลบ");
        }
    }

    /** เพิ่มชื่อหมวดเมื่อยังไม่มีชื่อเดียวกัน โดยไม่สนตัวพิมพ์เล็กหรือใหญ่ */
    private static void addUnique(List<String> names, String value) {
        String cleanValue = value == null ? "" : value.trim();
        // ไม่เก็บชื่อว่างเข้าไปในรายการ
        if (cleanValue.isEmpty()) return;
        // ข้ามชื่อที่มีอยู่แล้ว เพื่อไม่ให้การ์ดหมวดซ้ำ
        for (String name : names) {
            if (name.equalsIgnoreCase(cleanValue)) return;
        }
        names.add(cleanValue);
    }
}
