package vn.pxu.oop.pm.factory;

import vn.pxu.oop.pm.enums.EmployeeType;
import vn.pxu.oop.pm.exception.ValidationException;
import vn.pxu.oop.pm.model.*;

import java.util.Objects;

/** Factory: tập trung quy tắc khởi tạo nhân viên theo vai trò. */
public final class EmployeeFactory {
    private EmployeeFactory() { }

    public static Employee create(EmployeeType type, String id, String name, String email, double salary) {
        return create(type, id, name, email, salary, "", 0);
    }

    /**
     * detail: Developer = ngôn ngữ; Tester = chuyên môn; Manager = bộ phận.
     * extra: Developer = số năm kinh nghiệm; Tester = 0/1 tự động; Manager = phụ cấp.
     */
    public static Employee create(EmployeeType type, String id, String name, String email, double salary,
                                  String detail, double extra) {
        Objects.requireNonNull(type, "Vai trò không được null");
        return switch (type) {
            case DEVELOPER -> {
                if (!Double.isFinite(extra) || extra != Math.rint(extra) || extra < 0 || extra > 50)
                    throw new ValidationException("Số năm kinh nghiệm phải là số nguyên 0-50.");
                yield new Developer(id, name, email, salary, detail.isBlank() ? "Java" : detail, (int) extra);
            }
            case TESTER -> {
                if (extra != 0 && extra != 1) throw new ValidationException("Kiểm thử tự động chỉ nhận 0 hoặc 1.");
                yield new Tester(id, name, email, salary, detail.isBlank() ? "Functional" : detail, extra == 1);
            }
            case MANAGER -> new Manager(id, name, email, salary, detail.isBlank() ? "Dự án" : detail, extra);
        };
    }
}
