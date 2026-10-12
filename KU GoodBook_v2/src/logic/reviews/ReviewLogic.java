package logic.reviews;

import books.Book;
import logic.library.LibraryLogic;
import storage.CsvStorage;
import java.io.IOException;

/** ตรวจเงื่อนไขรีวิวก่อนส่งข้อมูลไปบันทึก */
public final class ReviewLogic {

    private ReviewLogic() {
    }

    /** คืนจำนวนตอนที่ยังอ่านไม่จบ */
    public static int unreadChapterCount(Book book) {
        return LibraryLogic.unreadChapterCount(book);
    }

    /** คืน true เมื่อหนังสือมีตอนและผู้ใช้อ่านครบทุกตอนแล้ว */
    public static boolean canWriteReview(Book book, int unreadCount) {
        // ต้องมีตอนอย่างน้อยหนึ่งตอน และไม่มีตอนที่ยังอ่านไม่จบ
        return !book.chapterTitles.isEmpty() && unreadCount == 0;
    }

    /** ตรวจเงื่อนไขและส่งรีวิวไปยังส่วนบันทึก CSV */
    public static void submit(Book book, String username, String email,
                              int rating, String comment) throws IOException {
        // จำกัดคะแนนให้อยู่ในช่วงหนึ่งถึงห้าดาว
        if (rating < 1 || rating > 5) {
            throw new IOException("กรุณาเลือกคะแนนตั้งแต่ 1 ถึง 5 ดาว");
        }
        // ไม่รับรีวิวที่ไม่มีข้อความ
        if (comment == null || comment.trim().isEmpty()) {
            throw new IOException("กรุณาเขียนข้อความรีวิวก่อนส่ง");
        }
        // ตรวจสถานะอ่านจาก CSV อีกครั้งก่อนบันทึก เพื่อไม่ให้ข้ามเงื่อนไขจากหน้าจอ
        if (book.chapterTitles.isEmpty() || LibraryLogic.unreadChapterCount(book) > 0) {
            throw new IOException("กรุณาอ่านให้จบครบทุกตอน และทำเครื่องหมายว่าอ่านแล้วทุกตอน เพื่อคอมเมนต์");
        }

        // บันทึกรีวิวหลังตรวจทุกเงื่อนไขผ่านแล้ว
        CsvStorage.addReview(book, username, email, rating, comment.trim());
    }
}
