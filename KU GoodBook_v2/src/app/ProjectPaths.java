package app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** หาตำแหน่งโฟลเดอร์โปรเจ็กต์ เพื่อให้ทุกส่วนบันทึก CSV ไว้ที่เดียวกัน */
public final class ProjectPaths {

    private ProjectPaths() {
        // คลาสนี้มีแต่เมธอดสำหรับหาตำแหน่ง จึงไม่ต้องสร้าง object
    }

    /** คืนตำแหน่ง data ของโปรเจ็กต์ โดยไล่จากโฟลเดอร์ที่ใช้เปิดโปรแกรมขึ้นไป */
    public static Path dataFolder() {
        Path startFolder = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        Path projectFolder = findProjectFolder(startFolder);

        if (projectFolder != null) {
            return projectFolder.resolve("data");
        }

        // ถ้าหาโปรเจ็กต์ไม่เจอ ให้คงวิธีเดิมและใช้ตำแหน่งที่เปิดโปรแกรม
        return startFolder.resolve("data");
    }

    /** หาโฟลเดอร์โปรเจ็กต์ ทั้งโฟลเดอร์ปัจจุบัน โฟลเดอร์แม่ และโฟลเดอร์ลูก */
    private static Path findProjectFolder(Path startFolder) {
        Path folder = startFolder;

        while (folder != null) {
            if (isProjectFolder(folder)) {
                return folder;
            }

            // รองรับกรณีเปิดโปรแกรมจากโฟลเดอร์ Downloads หรือโฟลเดอร์แม่หลายชั้น
            Path childProject = findProjectInChildren(folder, 2);
            if (childProject != null) {
                return childProject;
            }

            folder = folder.getParent();
        }

        return null;
    }

    /** ค้นหาโปรเจ็กต์ในโฟลเดอร์ลูกลงไปไม่เกินจำนวนชั้นที่กำหนด */
    private static Path findProjectInChildren(Path folder, int levelsLeft) {
        if (levelsLeft == 0) {
            return null;
        }

        java.io.File[] children = folder.toFile().listFiles();
        if (children == null) {
            return null;
        }

        for (java.io.File child : children) {
            if (!child.isDirectory()) {
                continue;
            }

            Path childFolder = child.toPath();
            if (isProjectFolder(childFolder)) {
                return childFolder;
            }

            Path nestedProject = findProjectInChildren(childFolder, levelsLeft - 1);
            if (nestedProject != null) {
                return nestedProject;
            }
        }
        return null;
    }

    /** ใช้ไฟล์โค้ดจริงเป็นตัวระบุ เพื่อไม่ให้เลือกโปรเจ็กต์ข้างเคียงผิด */
    private static boolean isProjectFolder(Path folder) {
        return Files.isRegularFile(folder.resolve("src/app/Main.java"))
                && Files.isRegularFile(folder.resolve("src/storage/account/AccountStorage.java"))
                && Files.isDirectory(folder.resolve("data"));
    }
}
