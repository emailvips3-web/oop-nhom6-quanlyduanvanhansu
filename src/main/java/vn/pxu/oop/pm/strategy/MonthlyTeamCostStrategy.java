package vn.pxu.oop.pm.strategy;

import vn.pxu.oop.pm.model.Employee;
import vn.pxu.oop.pm.model.Project;

import java.util.Locale;
import java.util.Map;

/** Chi phí giả định dùng toàn bộ nhân sự phân công trong một tháng. */
public final class MonthlyTeamCostStrategy implements ProjectCostStrategy {
    @Override public String name() { return "Ngân sách nhân sự 1 tháng"; }
    @Override public double estimate(Project project, Map<String, Employee> employees) {
        return project.getMembers().stream().mapToDouble(m -> {
            Employee employee = employees.get(m.getEmployeeId().toUpperCase(Locale.ROOT));
            return employee == null ? 0 : employee.calculateMonthlyCost();
        }).sum();
    }
    @Override public String details() { return "Tổng chi phí nhân sự tham gia dự án, giả định toàn thời gian trong 1 tháng."; }
}
