package vn.pxu.oop.pm.model;

import vn.pxu.oop.pm.enums.TaskPriority;
import vn.pxu.oop.pm.enums.TaskStatus;
import vn.pxu.oop.pm.util.Check;

import java.util.Objects;

public final class Task implements Identifiable {
    private final String id;
    private final String projectId;
    private String title;
    private TaskPriority priority;
    private TaskStatus status;
    private double estimatedHours;
    private String assigneeId;

    public Task(String id, String projectId, String title) {
        this(id, projectId, title, TaskPriority.MEDIUM, TaskStatus.TODO, 8, null);
    }
    public Task(String id, String projectId, String title, TaskPriority priority, TaskStatus status,
                double estimatedHours, String assigneeId) {
        this.id = Check.required(id, "Mã công việc");
        this.projectId = Check.required(projectId, "Mã dự án");
        setTitle(title);
        setPriority(priority);
        setStatus(status);
        setEstimatedHours(estimatedHours);
        setAssigneeId(assigneeId);
    }
    @Override public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getTitle() { return title; }
    public TaskPriority getPriority() { return priority; }
    public TaskStatus getStatus() { return status; }
    public double getEstimatedHours() { return estimatedHours; }
    public String getAssigneeId() { return assigneeId; }
    public void setTitle(String title) { this.title = Check.required(title, "Tên công việc"); }
    public void setPriority(TaskPriority priority) { this.priority = Objects.requireNonNull(priority, "Độ ưu tiên không được null"); }
    public void setStatus(TaskStatus status) { this.status = Objects.requireNonNull(status, "Trạng thái không được null"); }
    public void setEstimatedHours(double hours) { this.estimatedHours = Check.positive(hours, "Số giờ dự kiến"); }
    public void setAssigneeId(String employeeId) {
        this.assigneeId = employeeId == null || employeeId.isBlank() ? null : Check.required(employeeId, "Mã người phụ trách");
    }
    @Override public String toString() {
        return id + " | " + title + " | " + status.getLabel() + " | " + priority.getLabel()
                + " | " + estimatedHours + "h | " + (assigneeId == null ? "Chưa giao" : assigneeId);
    }
}
