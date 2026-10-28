package vn.pxu.oop.pm.strategy;

import vn.pxu.oop.pm.model.Employee;
import vn.pxu.oop.pm.model.Project;

import java.util.Map;

public interface ProjectCostStrategy {
    String name();
    double estimate(Project project, Map<String, Employee> employees);

    default String details() { return "Ước tính tham khảo, không phải dữ liệu kế toán."; }
}
