package vn.pxu.oop.pm.service;

import vn.pxu.oop.pm.exception.BusinessRuleException;
import vn.pxu.oop.pm.exception.EntityNotFoundException;
import vn.pxu.oop.pm.model.Employee;
import vn.pxu.oop.pm.model.Project;
import vn.pxu.oop.pm.model.Task;

import java.util.*;

/** Bộ nhớ dùng chung cho các service. Dữ liệu được persist bởi FileDataStore. */
public final class InMemoryDatabase {
    private final Map<String, Employee> employees = new LinkedHashMap<>();
    private final Map<String, Project> projects = new LinkedHashMap<>();

    private static String key(String id) { return id == null ? "" : id.trim().toUpperCase(Locale.ROOT); }

    public void addEmployee(Employee employee) {
        String id = key(employee.getId());
        if (employees.containsKey(id)) throw new BusinessRuleException("Mã nhân viên đã tồn tại: " + employee.getId());
        employees.put(id, employee);
    }
    public void addProject(Project project) {
        String id = key(project.getId());
        if (projects.containsKey(id)) throw new BusinessRuleException("Mã dự án đã tồn tại: " + project.getId());
        projects.put(id, project);
    }
    public Collection<Employee> employees() { return Collections.unmodifiableCollection(employees.values()); }
    public Collection<Project> projects() { return Collections.unmodifiableCollection(projects.values()); }
    public Employee employee(String id) {
        Employee employee = employees.get(key(id));
        if (employee == null) throw new EntityNotFoundException("Không thấy nhân viên: " + id);
        return employee;
    }
    public Project project(String id) {
        Project project = projects.get(key(id));
        if (project == null) throw new EntityNotFoundException("Không thấy dự án: " + id);
        return project;
    }
    public void removeEmployee(String id) { employee(id); employees.remove(key(id)); }
    public void removeProject(String id) { project(id); projects.remove(key(id)); }
    public TaskRef task(String id) {
        for (Project project : projects.values()) {
            for (Task task : project.getTasks()) {
                if (task.hasId(id)) return new TaskRef(project, task);
            }
        }
        throw new EntityNotFoundException("Không thấy công việc: " + id);
    }
    public boolean taskIdExists(String id) {
        return projects.values().stream().flatMap(p -> p.getTasks().stream()).anyMatch(t -> t.hasId(id));
    }
    public Map<String, Employee> employeeIndex() { return Collections.unmodifiableMap(employees); }
    public record TaskRef(Project project, Task task) { }
}
