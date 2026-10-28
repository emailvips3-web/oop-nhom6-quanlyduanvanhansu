package vn.pxu.oop.pm.util;

import vn.pxu.oop.pm.exception.ValidationException;

import java.util.regex.Pattern;

public final class Check {
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private Check() { }

    public static String required(String text, String field) {
        if (text == null || text.isBlank()) throw new ValidationException(field + " không được để trống.");
        String value = text.trim();
        if (value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0)
            throw new ValidationException(field + " không được chứa ký tự xuống dòng.");
        if (value.length() > 180) throw new ValidationException(field + " vượt quá 180 ký tự.");
        return value;
    }

    public static String optional(String text, String field) {
        if (text == null || text.isBlank()) return "";
        return required(text, field);
    }

    public static String email(String text) {
        String value = required(text, "Email");
        if (!EMAIL.matcher(value).matches()) throw new ValidationException("Email không đúng định dạng.");
        return value;
    }

    public static double nonNegative(double value, String field) {
        if (!Double.isFinite(value) || value < 0) throw new ValidationException(field + " phải >= 0 và hữu hạn.");
        return value;
    }

    public static double positive(double value, String field) {
        if (!Double.isFinite(value) || value <= 0) throw new ValidationException(field + " phải > 0 và hữu hạn.");
        return value;
    }
}
