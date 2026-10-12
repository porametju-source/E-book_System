package books;

// ไกด์ไฟล์: หน้าที่: เก็บข้อมูลของตอนหนึ่งตอน ได้แก่ title และ content
// constructor รับชื่อและเนื้อหาตอน; ฟิลด์เป็น final จึงเปลี่ยนค่าไม่ได้หลังสร้าง
// ถ้าจะเพิ่มข้อมูลของตอน เช่น เวลาเผยแพร่ ต้องปรับจุดอ่าน/เขียนตอนใน AdminStorage ด้วย

/** เก็บชื่อตอนและเนื้อหาของตอนเดียวกัน */
public class Chapter {
    public final String title;
    public final String content;

    public Chapter(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
