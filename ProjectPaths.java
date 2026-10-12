package app;

// ไกด์ไฟล์: หน้าที่: คำนวณตำแหน่งโฟลเดอร์ข้อมูลของแอปจากตำแหน่งโปรเจกต์
// เมธอด dataFolder เป็นจุดหลักที่ Storage ทุกส่วนใช้หาไฟล์ CSV และโฟลเดอร์ข้อมูล
// หากย้ายที่เก็บข้อมูล ให้แก้กฎในไฟล์นี้และตรวจสอบคลาสใน package storage ด้วย

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** รวมวิธีหาโฟลเดอร์ data ของโปรเจกต์ไว้ที่เดียว */
public final class ProjectPaths {

    private ProjectPaths() {
    }

    /**
     * หาโฟลเดอร์โปรเจกต์จากตำแหน่งที่เปิดโปรแกรม
     * ถ้าเปิดจากโฟลเดอร์ย่อย จะไล่ขึ้นไปหาโฟลเดอร์แม่
     */
    public static Path dataFolder() {
        Path currentFolder = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        Path projectFolder = currentFolder;

        while (projectFolder != null) {
            Path mainFile = projectFolder.resolve("src/app/Main.java");
            Path dataFolder = projectFolder.resolve("data");

            if (Files.exists(mainFile) && Files.isDirectory(dataFolder)) {
                return dataFolder;
            }

            projectFolder = projectFolder.getParent();
        }

        // ถ้าหาโฟลเดอร์โปรเจกต์ไม่เจอ ให้ใช้ data จากตำแหน่งที่เปิดโปรแกรม
        return currentFolder.resolve("data");
    }
}
