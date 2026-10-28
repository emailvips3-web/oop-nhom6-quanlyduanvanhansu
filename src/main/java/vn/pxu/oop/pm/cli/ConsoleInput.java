package vn.pxu.oop.pm.cli;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** Đọc input có kiểm tra; nhập sai không làm thoát chương trình. */
public final class ConsoleInput {
    private final Scanner scanner;
    public ConsoleInput(Scanner scanner) { this.scanner = scanner; }

    public String line(String prompt) {
        System.out.print(prompt + ": ");
        if (!scanner.hasNextLine()) throw new NoSuchElementException("Đã hết dữ liệu nhập.");
        return scanner.nextLine().trim();
    }
    public String required(String prompt) {
        while (true) {
            String value = line(prompt);
            if (!value.isBlank()) return value;
            System.out.println("  ! Không được để trống.");
        }
    }
    public int integer(String prompt, int min, int max) {
        while (true) {
            String value = line(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number >= min && number <= max) return number;
            } catch (NumberFormatException e) {
                System.out.println("  ! Đây không phải số nguyên hợp lệ.");
                continue;
            }
            System.out.println("  ! Vui lòng nhập số nguyên từ " + min + " đến " + max + ".");
        }
    }
    public double number(String prompt, double min) {
        while (true) {
            String value = line(prompt).replace(',', '.');
            try {
                double number = Double.parseDouble(value);
                if (Double.isFinite(number) && number >= min) return number;
            } catch (NumberFormatException e) {
                System.out.println("  ! Đây không phải số hợp lệ.");
                continue;
            }
            System.out.println("  ! Vui lòng nhập số >= " + min + ".");
        }
    }
    public LocalDate date(String prompt) {
        while (true) {
            try { return LocalDate.parse(line(prompt + " (yyyy-MM-dd)")); }
            catch (DateTimeParseException e) { System.out.println("  ! Ngày không hợp lệ (ví dụ 2026-11-15)."); }
        }
    }
    public boolean confirm(String prompt) {
        return line(prompt + " [y/N]").equalsIgnoreCase("y");
    }
    public <E extends Enum<E>> E choose(String title, E[] choices) {
        System.out.println("  " + title + ":");
        for (int i = 0; i < choices.length; i++) {
            System.out.println("  " + (i + 1) + ". " + choices[i].name());
        }
        return choices[integer("Chọn", 1, choices.length) - 1];
    }
}
