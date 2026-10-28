package vn.pxu.oop.pm.model;

import vn.pxu.oop.pm.enums.ProjectStatus;
import vn.pxu.oop.pm.exception.BusinessRuleException;
import vn.pxu.oop.pm.exception.EntityNotFoundException;
import vn.pxu.oop.pm.exception.ValidationException;
import vn.pxu.oop.pm.util.Check;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Project implements Identifiable {
    private final String id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private final List<Task> tasks = new ArrayList<>();
    private final List<ProjectMember> members = new ArrayList<>();

    public Project(String id, String name, LocalDate startDate, LocalDate endDate) {
        this(id, name, "", startDate, endDate, ProjectStatus.PLANNED);
    }
    public Project(String id, String name, String description, LocalDate startDate,
                   LocalDate endDate, ProjectStatus status) {
        this.id = Check.required(id, "Mã dự án");
        setName(name);
        setDescription(description);
        setDates(startDate, endDate);
        setStatus(status);
    }
    @Override public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public ProjectStatus getStatus() { return status; }
    public List<Task> getTasks() { return Collections.unmodifiableList(tasks); }
    public List<ProjectMember> getMembers() { return Collections.unmodifiableList(members); }
    public void setName(String name) { this.name = Check.required(name, "Tên dự án"); }
    public void setDescription(String description) { this.description = Check.optional(description, "Mô tả dự án"); }
    public void setStatus(ProjectStatus status) { this.status = Objects.requireNonNull(status, "Trạng thái không được null"); }
    public void setDates(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "Ngày bắt đầu không được null");
        Objects.requireNonNull(end, "Ngày kết thúc không được null");
        if (end.isBefore(start)) throw new ValidationException("Ngày kết thúc phải >= ngày bắt đầu.");
        this.startDate = start;
        this.endDate = end;
    }

    public boolean hasMember(String employeeId) {
        return members.stream().anyMatch(m -> m.getEmployeeId().equalsIgnoreCase(employeeId));
    }
    public void addMember(ProjectMember member) {
        if (!id.equalsIgnoreCase(member.getProjectId())) throw new ValidationException("Thành viên thuộc dự án khác.");
        if (hasMember(member.getEmployeeId())) throw new BusinessRuleException("Nhân viên đã tham gia dự án.");
        members.add(member);
    }
    public void removeMember(String employeeId) {
        boolean removed = members.removeIf(m -> m.getEmployeeId().equalsIgnoreCase(employeeId));
        if (!removed) throw new EntityNotFoundException("Nhân viên chưa tham gia dự án: " + employeeId);
    }
    public Task getTask(String taskId) {
        return tasks.stream().filter(t -> t.hasId(taskId)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Không thấy công việc: " + taskId));
    }
    public void addTask(Task task) {
        if (!id.equalsIgnoreCase(task.getProjectId())) throw new ValidationException("Công việc thuộc dự án khác.");
        if (tasks.stream().anyMatch(t -> t.hasId(task.getId()))) throw new BusinessRuleException("Trùng mã công việc.");
        tasks.add(task);
    }
    public void removeTask(String taskId) {
        if (!tasks.removeIf(t -> t.hasId(taskId))) throw new EntityNotFoundException("Không thấy công việc: " + taskId);
    }
    @Override public String toString() {
        return id + " | " + name + " | " + status.getLabel() + " | " + startDate + " → " + endDate
                + " | " + members.size() + " thành viên | " + tasks.size() + " task";
    }
}
