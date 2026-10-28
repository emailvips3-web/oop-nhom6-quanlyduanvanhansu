package vn.pxu.oop.pm.strategy;

import vn.pxu.oop.pm.model.Employee;
import vn.pxu.oop.pm.model.Project;

import java.util.Locale;
import java.util.Map;

/** Chi phí theo effort = số giờ ước tính * (chi phí 1 tháng / 160 giờ). */
public final class TaskEffortCostStrategy implements ProjectCostStrategy {
    public static final double HOURS_PER_MONTH = 160.0;
    @Override public String name() { return "Chi phí theo giờ task"; }
    @Override public double estimate(Project project, Map<String, Employee> employees) {
        return project.getTasks().stream().mapToDouble(t -> {
            if (t.getAssigneeId() == null) return 0;
            Employee employee = employees.get(t.getAssigneeId().toUpperCase(Locale.ROOT));
            return employee == null ? 0 : employee.calculateMonthlyCost() / HOURS_PER_MONTH * t.getEstimatedHours();
        }).sum();
    }
    @Override public String details() { return "Chỉ tính task đã giao, giả định 160 giờ làm việc/tháng."; }
}
