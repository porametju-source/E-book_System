package logic.books;

// ไกด์ไฟล์: หน้าที่: เตรียมรายการหนังสือให้หน้าแรกและหน้าหมวดหมู่นำไปแสดง
// allBooks คืนรายการ, searchAndSort ค้นหา/เรียง, booksInCategory กรองตามหมวดหมู่
// แก้เงื่อนไขค้นหาและลำดับเรียงในคลาสนี้; การวาดการ์ดอยู่ใน ui.pages และ ui.components

import books.Book;
import books.BookData;
import java.util.ArrayList;
import java.util.List;

/** ค้นหา กรอง และเรียงข้อมูลหนังสือ โดยไม่จัดการหน้าจอ */
public final class BookCatalogLogic {

    private BookCatalogLogic() {
    }

    /** คืนหนังสือทั้งหมดที่โหลดเข้าสู่ระบบแล้ว */
    public static List<Book> allBooks() {
        return new ArrayList<Book>(BookData.ALL);
    }

    /** คืนหนังสือที่ตรงกับคำค้นหาและเรียงตามตัวเลือก */
    public static List<Book> searchAndSort(List<Book> source, String query,
                                           String sort, boolean searchCategory) {
        String cleanQuery = query.trim().toLowerCase();
        List<Book> result = new ArrayList<Book>();

        for (Book book : source) {
            String text = book.titleTh + " " + book.titleEn + " " + book.author;
            if (searchCategory) {
                text += " " + book.category;
            }
            if (text.toLowerCase().contains(cleanQuery)) {
                result.add(book);
            }
        }

        sortBooks(result, sort);
        return result;
    }

    /** คืนชื่อหมวดเริ่มต้นและเพิ่มหมวดที่ผู้ดูแลสร้างไว้ */
    public static List<String> categoryNames(String[] defaultNames) {
        List<String> result = new ArrayList<String>();
        for (String name : defaultNames) {
            if (!result.contains(name)) {
                result.add(name);
            }
        }
        for (Book book : BookData.ALL) {
            if (!result.contains(book.category)) {
                result.add(book.category);
            }
        }
        return result;
    }

    /** คืนหนังสือในหมวดที่ตรงกับคำค้นหา */
    public static List<Book> booksInCategory(String category, String query) {
        return searchAndSort(BookData.byCategory(category), query, "ลำดับเดิม", true);
    }

    /** คืนหนังสือเล่มแรกในหมวด หรือ null ถ้าหมวดนั้นยังไม่มีหนังสือ */
    public static Book firstBookInCategory(String category) {
        List<Book> books = BookData.byCategory(category);
        return books.isEmpty() ? null : books.get(0);
    }

    /** เรียงรายการตามตัวเลือกชื่อเรื่องหรือคะแนน โดยสลับตำแหน่งในรายการที่รับมา */
    private static void sortBooks(List<Book> books, String sort) {
        for (int i = 0; i < books.size(); i++) {
            for (int j = i + 1; j < books.size(); j++) {
                boolean shouldSwap = false;
                if ("ชื่อเรื่อง A-Z".equals(sort)) {
                    shouldSwap = books.get(i).titleTh.compareTo(books.get(j).titleTh) > 0;
                } else if ("คะแนนรีวิวสูงสุด".equals(sort)) {
                    shouldSwap = books.get(i).averageReviewRating()
                            < books.get(j).averageReviewRating();
                }

                if (shouldSwap) {
                    Book firstBook = books.get(i);
                    books.set(i, books.get(j));
                    books.set(j, firstBook);
                }
            }
        }
    }
}
