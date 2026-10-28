package vn.pxu.oop.pm.storage;

import java.util.ArrayList;
import java.util.List;

/** CSV thuần Java: escape dấu phẩy, dấu nháy kép; không cho phép xuống dòng trong trường. */
public final class CsvCodec {
    private CsvCodec() { }

    public static String encode(List<String> values) {
        return values.stream().map(value -> {
            String safe = value == null ? "" : value;
            if (safe.contains("\n") || safe.contains("\r"))
                throw new IllegalArgumentException("CSV không hỗ trợ xuống dòng bên trong giá trị.");
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }).reduce((left, right) -> left + "," + right).orElse("");
    }

    public static List<String> parse(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder part = new StringBuilder();
        boolean quoted = false;
        boolean closedQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (quoted) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        part.append('"'); i++;
                    } else { quoted = false; closedQuote = true; }
                } else part.append(ch);
            } else if (ch == ',') {
                parts.add(part.toString()); part.setLength(0); closedQuote = false;
            } else if (ch == '"' && part.length() == 0 && !closedQuote) {
                quoted = true;
            } else if (closedQuote || ch == '"') {
                throw new IllegalArgumentException("CSV sai cú pháp dấu nháy kép.");
            } else part.append(ch);
        }
        if (quoted) throw new IllegalArgumentException("CSV thiếu dấu nháy đóng.");
        parts.add(part.toString());
        return parts;
    }
}
