package vn.pxu.oop.pm.service;

import vn.pxu.oop.pm.enums.ProjectStatus;
import vn.pxu.oop.pm.enums.TaskStatus;
import vn.pxu.oop.pm.exception.BusinessRuleException;
import vn.pxu.oop.pm.exception.EntityNotFoundException;
import vn.pxu.oop.pm.model.Project;
import vn.pxu.oop.pm.model.ProjectMember;
import vn.pxu.oop.pm.util.Check;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class ProjectService {
    private final InMemoryDatabase db;
    public ProjectService(InMemoryDatabase db) { this.db = db; }

    public void add(Project project) { db.addProject(project); }
    public Project get(String id) { return db.project(id); }
    public Collection<Project> all() { return db.projects(); }
    public List<Project> search(String keyword) {
        String q = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT).trim();
        return db.projects().stream().filter(p -> p.getId().toLowerCase(Locale.ROOT).contains(q)
                || p.getName().toLowerCase(Locale.ROOT).contains(q))
                .sorted(Comparator.comparing(Project::getId)).toList();
    }
    public void update(String id, String name, String description, LocalDate start, LocalDate end) {
        Project project = db.project(id);
        String newName = Check.required(name, "Tên dự án");
        String newDescription = Check.optional(description, "Mô tả");
        if (start == null || end == null || end.isBefore(start))
            throw new vn.pxu.oop.pm.exception.ValidationException("Ngày kết thúc phải >= ngày bắt đầu.");
        project.setName(newName);
        project.setDescription(newDescription);
        project.setDates(start, end);
    }
    public void changeStatus(String id, ProjectStatus status) {
        Project project = db.project(id);
        if (status == ProjectStatus.COMPLETED) {
            if (project.getTasks().isEmpty()) throw new BusinessRuleException("Dự án chưa có công việc, chưa thể hoàn thành.");
            if (project.getTasks().stream().anyMatch(t -> t.getStatus() != TaskStatus.DONE))
                throw new BusinessRuleException("Cần hoàn thành tất cả công việc trước khi kết thúc dự án.");
        }
        project.setStatus(status);
    }
    public void delete(String id) {
        // Composition: xóa dự án cũng xóa Task và ProjectMember thuộc dự án đó.
        db.removeProject(id);
    }
    public void assignMember(String projectId, String employeeId) {
        Project project = db.project(projectId);
        ensureEditable(project);
        var employee = db.employee(employeeId);
        project.addMember(new ProjectMember(projectId, employee));
    }
    public List<ProjectMember> searchMembers(String projectId, String keyword) {
        Project project = db.project(projectId);
        String q = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT).trim();
        return project.getMembers().stream().filter(member ->
                member.getEmployeeId().toLowerCase(Locale.ROOT).contains(q)
                || member.getEmployee().getFullName().toLowerCase(Locale.ROOT).contains(q)).toList();
    }
    public void updateMemberDate(String projectId, String employeeId, LocalDate newDate) {
        Project project = db.project(projectId);
        ensureEditable(project);
        ProjectMember member = project.getMembers().stream()
                .filter(m -> m.getEmployeeId().equalsIgnoreCase(employeeId))
                .findFirst().orElseThrow(() -> new EntityNotFoundException("Nhân viên chưa thuộc dự án: " + employeeId));
        member.setJoinedOn(newDate);
    }
    public int countMembers(String projectId) { return db.project(projectId).getMembers().size(); }

    public void removeMember(String projectId, String employeeId) {
        Project project = db.project(projectId);
        ensureEditable(project);
        if (project.getTasks().stream().anyMatch(t -> employeeId.equalsIgnoreCase(t.getAssigneeId())))
            throw new BusinessRuleException("Nhân viên đang phụ trách task; cần bỏ giao task trước.");
        project.removeMember(employeeId);
    }
    public static void ensureEditable(Project project) {
        if (project.getStatus() == ProjectStatus.COMPLETED)
            throw new BusinessRuleException("Dự án đã hoàn thành. Hãy mở lại trạng thái để chỉnh sửa.");
    }
}
