package vn.pxu.oop.pm.service;

import vn.pxu.oop.pm.exception.BusinessRuleException;
import vn.pxu.oop.pm.model.Employee;
import vn.pxu.oop.pm.model.Project;
import vn.pxu.oop.pm.util.Check;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class EmployeeService {
    private final InMemoryDatabase db;
    public EmployeeService(InMemoryDatabase db) { this.db = db; }

    public void add(Employee employee) {
        checkEmailUnique(employee.getEmail(), null);
        db.addEmployee(employee);
    }
    public Employee get(String id) { return db.employee(id); }
    public Collection<Employee> all() { return db.employees(); }
    public List<Employee> search(String keyword) {
        String q = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT).trim();
        return db.employees().stream()
                .filter(e -> e.getId().toLowerCase(Locale.ROOT).contains(q)
                        || e.getFullName().toLowerCase(Locale.ROOT).contains(q)
                        || e.getRoleName().toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparing(Employee::getId)).toList();
    }
    public void update(String id, String name, String email, double salary) {
        Employee employee = db.employee(id);
        String goodName = Check.required(name, "Họ tên");
        String goodEmail = Check.email(email);
        double goodSalary = Check.nonNegative(salary, "Lương cơ bản");
        checkEmailUnique(goodEmail, id);
        employee.setFullName(goodName);
        employee.setEmail(goodEmail);
        employee.setBaseSalary(goodSalary);
    }
    public void delete(String id) {
        db.employee(id);
        for (Project project : db.projects()) {
            if (project.hasMember(id)) {
                throw new BusinessRuleException("Không thể xóa nhân viên đang thuộc dự án " + project.getId());
            }
            if (project.getTasks().stream().anyMatch(t -> id.equalsIgnoreCase(t.getAssigneeId()))) {
                throw new BusinessRuleException("Không thể xóa nhân viên đang được giao công việc trong " + project.getId());
            }
        }
        db.removeEmployee(id);
    }
    private void checkEmailUnique(String email, String excludedId) {
        boolean exists = db.employees().stream().anyMatch(e -> e.getEmail().equalsIgnoreCase(email)
                && (excludedId == null || !e.hasId(excludedId)));
        if (exists) throw new BusinessRuleException("Email đã được sử dụng bởi nhân viên khác.");
    }
}
