package vn.pxu.oop.pm.service;

import vn.pxu.oop.pm.enums.EmployeeType;
import vn.pxu.oop.pm.enums.TaskStatus;
import vn.pxu.oop.pm.model.Employee;
import vn.pxu.oop.pm.model.Project;
import vn.pxu.oop.pm.strategy.ProjectCostStrategy;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ReportService {
    private final InMemoryDatabase db;
    public ReportService(InMemoryDatabase db) { this.db = db; }

    public Map<String, Long> employeesByRole() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (EmployeeType type : EmployeeType.values()) stats.put(type.getLabel(), 0L);
        for (Employee employee : db.employees()) {
            String key = java.util.Arrays.stream(EmployeeType.values())
                    .filter(type -> type.name().equalsIgnoreCase(employee.getRoleName()))
                    .map(EmployeeType::getLabel).findFirst().orElse(employee.getRoleName());
            stats.merge(key, 1L, Long::sum);
        }
        return stats;
    }
    public ProjectProgress progress(String projectId) {
        Project project = db.project(projectId);
        int total = project.getTasks().size();
        int done = (int) project.getTasks().stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        double percent = total == 0 ? 0 : done * 100.0 / total;
        return new ProjectProgress(total, done, percent);
    }
    public double estimateCost(String projectId, ProjectCostStrategy strategy) {
        return strategy.estimate(db.project(projectId), db.employeeIndex());
    }
    public Map<String, Long> workload() {
        Map<String, Long> workloads = new LinkedHashMap<>();
        for (Employee employee : db.employees()) workloads.put(employee.getId(), 0L);
        for (Project project : db.projects()) {
            project.getTasks().stream().filter(t -> t.getAssigneeId() != null && t.getStatus() != TaskStatus.DONE)
                    .forEach(t -> workloads.merge(t.getAssigneeId(), 1L, Long::sum));
        }
        return workloads;
    }
    public record ProjectProgress(int totalTasks, int completedTasks, double percentComplete) { }
}
