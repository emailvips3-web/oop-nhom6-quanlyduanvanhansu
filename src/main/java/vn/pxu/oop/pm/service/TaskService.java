package vn.pxu.oop.pm.service;

import vn.pxu.oop.pm.enums.TaskPriority;
import vn.pxu.oop.pm.enums.TaskStatus;
import vn.pxu.oop.pm.exception.BusinessRuleException;
import vn.pxu.oop.pm.model.Project;
import vn.pxu.oop.pm.model.Task;
import vn.pxu.oop.pm.util.Check;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class TaskService {
    private final InMemoryDatabase db;
    public TaskService(InMemoryDatabase db) { this.db = db; }

    public Task get(String id) { return db.task(id).task(); }
    public void add(String projectId, Task task) {
        Project project = db.project(projectId);
        ProjectService.ensureEditable(project);
        if (db.taskIdExists(task.getId())) throw new BusinessRuleException("Mã công việc đã tồn tại trong hệ thống.");
        if (task.getAssigneeId() != null && !project.hasMember(task.getAssigneeId()))
            throw new BusinessRuleException("Người phụ trách chưa tham gia dự án.");
        if (task.getStatus() == TaskStatus.DONE && task.getAssigneeId() == null)
            throw new BusinessRuleException("Task hoàn thành phải có người phụ trách.");
        project.addTask(task);
    }
    public List<Task> listByProject(String projectId) {
        return db.project(projectId).getTasks().stream().sorted(Comparator.comparing(Task::getId)).toList();
    }
    public List<Task> search(String keyword) { return search(keyword, null); }
    public List<Task> search(String keyword, TaskStatus statusFilter) {
        String q = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT).trim();
        return db.projects().stream().flatMap(p -> p.getTasks().stream())
                .filter(t -> (statusFilter == null || t.getStatus() == statusFilter)
                        && (t.getId().toLowerCase(Locale.ROOT).contains(q)
                        || t.getTitle().toLowerCase(Locale.ROOT).contains(q)))
                .sorted(Comparator.comparing(Task::getId)).toList();
    }
    public void update(String id, String title, TaskPriority priority, double hours) {
        InMemoryDatabase.TaskRef ref = db.task(id);
        ProjectService.ensureEditable(ref.project());
        String goodTitle = Check.required(title, "Tên công việc");
        Objects.requireNonNull(priority, "Độ ưu tiên không được null");
        double goodHours = Check.positive(hours, "Số giờ dự kiến");
        ref.task().setTitle(goodTitle);
        ref.task().setPriority(priority);
        ref.task().setEstimatedHours(goodHours);
    }
    public void changeStatus(String id, TaskStatus status) {
        InMemoryDatabase.TaskRef ref = db.task(id);
        ProjectService.ensureEditable(ref.project());
        Objects.requireNonNull(status, "Trạng thái không được null");
        if (status == TaskStatus.DONE && ref.task().getAssigneeId() == null)
            throw new BusinessRuleException("Hãy giao công việc cho nhân viên trước khi hoàn thành.");
        ref.task().setStatus(status);
    }
    public void assign(String id, String employeeId) {
        InMemoryDatabase.TaskRef ref = db.task(id);
        ProjectService.ensureEditable(ref.project());
        if (employeeId == null || employeeId.isBlank()) {
            if (ref.task().getStatus() == TaskStatus.DONE)
                throw new BusinessRuleException("Không thể bỏ giao công việc đã hoàn thành. Hãy đổi trạng thái trước.");
            ref.task().setAssigneeId(null);
            return;
        }
        db.employee(employeeId);
        if (!ref.project().hasMember(employeeId)) throw new BusinessRuleException("Nhân viên chưa thuộc dự án.");
        ref.task().setAssigneeId(employeeId);
    }
    public void delete(String id) {
        InMemoryDatabase.TaskRef ref = db.task(id);
        ProjectService.ensureEditable(ref.project());
        ref.project().removeTask(id);
    }
}
