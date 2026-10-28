package vn.pxu.oop.pm;

import vn.pxu.oop.pm.cli.ConsoleMenu;
import vn.pxu.oop.pm.exception.StorageException;
import vn.pxu.oop.pm.service.InMemoryDatabase;
import vn.pxu.oop.pm.storage.FileDataStore;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        System.setOut(new java.io.PrintStream(System.out, true, StandardCharsets.UTF_8));
        if (args.length != 0 && (args.length != 2 || !"--data-dir".equals(args[0]))) {
            System.err.println("Cách dùng: Main [--data-dir <thu_muc_du_lieu>]");
            return;
        }
        Path path = args.length == 2 ? Path.of(args[1]) : Path.of("data");
        FileDataStore store = new FileDataStore(path);
        try {
            InMemoryDatabase db = store.load();
            new ConsoleMenu(db, store, new Scanner(System.in, StandardCharsets.UTF_8)).run();
        } catch (StorageException | IllegalArgumentException e) {
            System.err.println("KHÔNG THỂ TẢI DỮ LIỆU: " + e.getMessage());
            System.err.println("Kiểm tra file CSV trước khi chạy lại. Chương trình không ghi đè dữ liệu đang lỗi.");
        }
    }
}
