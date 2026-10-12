package books;

// ไกด์ไฟล์: หน้าที่: เป็นแบบข้อมูลของหนังสือหนึ่งเล่ม ซึ่งหน้าจอและ Logic ใช้ร่วมกัน
// ฟิลด์ด้านบนเก็บชื่อ ผู้แต่ง หมวดหมู่ สถานะ คะแนน ปก และรายการตอน/รีวิว
// เมื่อต้องเพิ่มคุณสมบัติหนังสือ ให้เพิ่มฟิลด์และปรับ constructor รวมถึง AdminStorage ที่อ่าน/เขียน CSV

import java.awt.Color;
import java.util.ArrayList;

/**
 * เก็บข้อมูลของหนังสือหนึ่งเรื่อง เช่น ชื่อ ผู้แต่ง ตอน และรีวิว
 *
 * <p>ข้อมูลตอนแยกเก็บเป็นรายการชื่อกับรายการเนื้อหา ตำแหน่งของชื่อ
 * ต้องตรงกับตำแหน่งของเนื้อหาเสมอ เช่น ชื่อตอนตำแหน่งที่ 0 จะคู่กับ
 * เนื้อหาตำแหน่งที่ 0 ด้วย เมธอด addChapter ช่วยเพิ่มข้อมูลทั้งคู่พร้อมกัน</p>
 */
public class Book {
    /** รหัสที่ใช้แยกหนังสือแต่ละเรื่อง */
    public final String id;

    /** ชื่อภาษาไทยที่ใช้แสดงในหน้าจอ */
    public final String titleTh;

    /** ชื่อภาษาอังกฤษของหนังสือ */
    public final String titleEn;

    /** ชื่อผู้แต่ง */
    public final String author;

    /** หมวดที่ใช้จัดกลุ่มหนังสือ */
    public final String category;

    /** สถานะของเรื่อง เช่น "จบแล้ว" */
    public final String status;

    /** ข้อความสั้น ๆ ที่บอกเนื้อหาโดยรวมของเรื่อง */
    public final String blurb;

    /** ปีที่พิมพ์หนังสือ */
    public final int year;

    /** คะแนนตัวอย่างที่แสดงในการ์ดและหน้ารายละเอียด */
    public final double rating;

    /** สีหลักที่ใช้วาดปกหนังสือ */
    public final Color color;

    /** ที่อยู่รูปปกในโฟลเดอร์ data/covers หรือค่าว่างถ้าไม่มีรูป */
    public final String coverPath;

    /** รายชื่อตอน เรียงจากตอนแรกไปตอนสุดท้าย */
    public final ArrayList<String> chapterTitles = new ArrayList<>();

    /** เนื้อหาของแต่ละตอน โดยลำดับต้องตรงกับ chapterTitles */
    public final ArrayList<String> chapterTexts = new ArrayList<>();

    /** รีวิวที่โหลดจาก reviews.csv แต่ละช่องเก็บชื่อผู้รีวิว คะแนน และข้อความ */
    public final ArrayList<String[]> reviews = new ArrayList<>();

    /**
     * สร้างหนังสือพร้อมข้อมูลที่ใช้แสดงบนหน้าจอ
     * ตอนและรีวิวจะเพิ่มภายหลังด้วย addChapter และ addReview
     *
     * @param id รหัสหนังสือ
     * @param titleTh ชื่อหนังสือภาษาไทย
     * @param titleEn ชื่อหนังสือภาษาอังกฤษ
     * @param author ชื่อผู้แต่ง
     * @param category หมวดหมู่
     * @param status สถานะการเผยแพร่
     * @param year ปีพิมพ์
     * @param rating คะแนนเฉลี่ย
     * @param colorRgb ตัวเลขสีที่จะใช้วาดปก
     * @param blurb เรื่องย่อสั้น ๆ
     */
    public Book(String id, String titleTh, String titleEn, String author, String category,
                String status, int year, double rating, int colorRgb, String blurb) {
        this(id, titleTh, titleEn, author, category, status, year, rating,
                colorRgb, blurb, "");
    }

    /** สร้างหนังสือพร้อมที่อยู่รูปปกจริง */
    public Book(String id, String titleTh, String titleEn, String author, String category,
                String status, int year, double rating, int colorRgb, String blurb, String coverPath) {
        this.id = id;
        this.titleTh = titleTh;
        this.titleEn = titleEn;
        this.author = author;
        this.category = category;
        this.status = status;
        this.year = year;
        this.rating = rating;
        this.color = new Color(colorRgb);
        this.blurb = blurb;
        this.coverPath = coverPath == null ? "" : coverPath;
    }

    /**
     * เพิ่มชื่อตอนและเนื้อหาให้เป็นคู่เดียวกัน
     * การใช้เมธอดนี้ช่วยให้ชื่อและเนื้อหาไม่สลับตำแหน่งกัน
     *
     * @param title ชื่อตอน
     * @param text เนื้อหาตอน
     */
    /** เพิ่มชื่อตอนและเนื้อหาไว้ตำแหน่งเดียวกันในรายการ */
    public void addChapter(String title, String text) {
        chapterTitles.add(title);
        chapterTexts.add(text);
    }

    /**
     * เพิ่มรีวิวหนึ่งรายการ โดยเก็บคะแนนเป็นข้อความร่วมกับชื่อและรีวิว
     *
     * @param name ชื่อผู้รีวิว
     * @param stars จำนวนดาว
     * @param text ข้อความรีวิว
     */
    /** เพิ่มรีวิวพร้อมชื่อผู้ใช้ คะแนนดาว และข้อความ */
    public void addReview(String name, int stars, String text) {
        reviews.add(new String[]{name, String.valueOf(stars), text});
    }

    /** คำนวณคะแนนเฉลี่ยจากรีวิวจริง ถ้ายังไม่มีรีวิวให้คืนค่า 0 */
    public double averageReviewRating() {
        int total = 0;
        int ratedReviews = 0;
        for (String[] review : reviews) {
            int rating = Integer.parseInt(review[1]);
            if (rating > 0) {
                total += rating;
                ratedReviews++;
            }
        }
        return ratedReviews == 0 ? 0 : (double) total / ratedReviews;
    }

    /** คืนจำนวนรีวิวที่มีคะแนนดาว ไม่รวมความคิดเห็นที่ไม่มีคะแนน */
    public int ratedReviewCount() {
        int count = 0;
        for (String[] review : reviews) {
            if (Integer.parseInt(review[1]) > 0) {
                count++;
            }
        }
        return count;
    }
}
