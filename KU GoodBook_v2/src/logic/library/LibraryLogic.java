package logic.library;

import app.UserSession;
import books.Book;
import storage.CsvStorage;
import java.io.IOException;
import java.util.List;

/** จัดการกติกาและสถานะรายการโปรดกับการอ่านของผู้ใช้ */
public final class LibraryLogic {

    private LibraryLogic() {
    }

    /** ตรวจว่าหนังสืออยู่ในรายการโปรดของผู้ใช้ปัจจุบันหรือไม่ */
    public static boolean isFavorite(Book book) {
        try {
            return CsvStorage.isFavorite(UserSession.accountKey(), book);
        } catch (IOException error) {
            throw new IllegalStateException("อ่าน favorites.csv ไม่สำเร็จ", error);
        }
    }

    /** สลับสถานะหนังสือระหว่างเพิ่มและนำออกจากรายการโปรด */
    public static void toggleFavorite(Book book) {
        try {
            // ใช้อีเมลปัจจุบันเป็นกุญแจแยกข้อมูลของผู้ใช้แต่ละคน
            String email = UserSession.accountKey();
            // ถ้ามีรายการนี้แล้วให้เอาออก ถ้ายังไม่มีให้เพิ่มเข้า CSV
            if (CsvStorage.isFavorite(email, book)) {
                CsvStorage.removeFavorite(email, book);
            } else {
                CsvStorage.addFavorite(email, book);
            }
        } catch (IOException error) {
            throw new IllegalStateException("บันทึก favorites.csv ไม่สำเร็จ", error);
        }
    }

    /** คืนรายการหนังสือโปรดของผู้ใช้ปัจจุบัน */
    public static List<Book> favorites() {
        try {
            return CsvStorage.loadFavorites(UserSession.accountKey());
        } catch (IOException error) {
            throw new IllegalStateException("อ่าน favorites.csv ไม่สำเร็จ", error);
        }
    }

    /** ตรวจว่าผู้ใช้ทำเครื่องหมายอ่านตอนนี้แล้วหรือยัง */
    public static boolean isChapterRead(Book book, int chapterIndex) {
        try {
            return CsvStorage.isChapterRead(UserSession.accountKey(), book, chapterIndex);
        } catch (IOException error) {
            throw new IllegalStateException("อ่าน read_chapters.csv ไม่สำเร็จ", error);
        }
    }

    /** คืนตำแหน่งตอนแรกที่ยังอ่านไม่จบ หรือ -1 ถ้าอ่านครบแล้ว */
    public static int nextUnreadChapter(Book book) {
        // เรียงตรวจจากตอนแรกไปตอนสุดท้ายเพื่อหาตอนที่ควรอ่านต่อ
        for (int i = 0; i < book.chapterTitles.size(); i++) {
            if (!isChapterRead(book, i)) {
                return i;
            }
        }
        return -1;
    }

    /** นับจำนวนตอนที่ผู้ใช้ยังอ่านไม่จบ */
    public static int unreadChapterCount(Book book) {
        int count = 0;
        // เพิ่มตัวนับทุกครั้งที่พบตอนซึ่งยังไม่มีสถานะอ่านจบ
        for (int i = 0; i < book.chapterTitles.size(); i++) {
            if (!isChapterRead(book, i)) {
                count++;
            }
        }
        return count;
    }

    /** บันทึกว่าผู้ใช้อ่านตอนนี้จบแล้ว */
    public static void markChapterRead(Book book, int chapterIndex) {
        try {
            CsvStorage.markChapterRead(UserSession.accountKey(), book, chapterIndex);
        } catch (IOException error) {
            throw new IllegalStateException("บันทึก read_chapters.csv ไม่สำเร็จ", error);
        }
    }
}
