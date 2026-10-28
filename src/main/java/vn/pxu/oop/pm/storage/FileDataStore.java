package vn.pxu.oop.pm.storage;

import vn.pxu.oop.pm.enums.*;
import vn.pxu.oop.pm.exception.StorageException;
import vn.pxu.oop.pm.factory.EmployeeFactory;
import vn.pxu.oop.pm.model.*;
import vn.pxu.oop.pm.service.InMemoryDatabase;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

/** Lưu trữ dữ liệu vào 4 file CSV UTF-8, ghi file tạm rồi thay thế file đích. */
public final class FileDataStore {
    private static final List<String> EMPLOYEE_HEADER = List.of("type", "id", "fullName", "email", "baseSalary", "detail", "extra");
    private static final List<String> PROJECT_HEADER = List.of("id", "name", "description", "startDate", "endDate", "status");
    private static final List<String> MEMBER_HEADER = List.of("projectId", "employeeId", "joinedOn");
    private static final List<String> TASK_HEADER = List.of("id", "projectId", "title", "priority", "status", "estimatedHours", "assigneeId");
    private final Path directory;

    public FileDataStore(Path directory) {
        this.directory = Objects.requireNonNull(directory, "Thư mục dữ liệu không được null").toAbsolutePath().normalize();
    }
    public Path getDirectory() { return directory; }

    public InMemoryDatabase load() {
        InMemoryDatabase db = new InMemoryDatabase();
        read("employees.csv", EMPLOYEE_HEADER).forEach(row -> {
            EmployeeType type = EmployeeType.valueOf(row.get(0));
            Employee employee = EmployeeFactory.create(type, row.get(1), row.get(2), row.get(3),
                    Double.parseDouble(row.get(4)), row.get(5), Double.parseDouble(row.get(6)));
            db.addEmployee(employee);
        });
        read("projects.csv", PROJECT_HEADER).forEach(row -> db.addProject(new Project(
                row.get(0), row.get(1), row.get(2), LocalDate.parse(row.get(3)),
                LocalDate.parse(row.get(4)), ProjectStatus.valueOf(row.get(5)))));
        read("members.csv", MEMBER_HEADER).forEach(row -> {
            db.employee(row.get(1));
            db.project(row.get(0)).addMember(new ProjectMember(row.get(0), db.employee(row.get(1)), LocalDate.parse(row.get(2))));
        });
        read("tasks.csv", TASK_HEADER).forEach(row -> {
            Task task = new Task(row.get(0), row.get(1), row.get(2), TaskPriority.valueOf(row.get(3)),
                    TaskStatus.valueOf(row.get(4)), Double.parseDouble(row.get(5)),
                    row.get(6).isBlank() ? null : row.get(6));
            Project project = db.project(task.getProjectId());
            if (db.taskIdExists(task.getId())) throw new StorageException("Trùng mã công việc trong dữ liệu: " + task.getId());
            if (task.getAssigneeId() != null && !project.hasMember(task.getAssigneeId()))
                throw new StorageException("Nhân viên của task " + task.getId() + " không thuộc dự án.");
            if (task.getAssigneeId() == null && task.getStatus() == TaskStatus.DONE)
                throw new StorageException("Task DONE chưa được phân công: " + task.getId());
            project.addTask(task);
        });
        for (Project p : db.projects()) {
            if (p.getStatus() == ProjectStatus.COMPLETED && (p.getTasks().isEmpty()
                    || p.getTasks().stream().anyMatch(t -> t.getStatus() != TaskStatus.DONE)))
                throw new StorageException("Dự án COMPLETED chứa task chưa hoàn thành: " + p.getId());
        }
        return db;
    }

    public void save(InMemoryDatabase db) {
        List<List<String>> employees = new ArrayList<>();
        for (Employee e : db.employees()) {
            if (e instanceof Developer d) employees.add(List.of("DEVELOPER", e.getId(), e.getFullName(), e.getEmail(),
                    Double.toString(e.getBaseSalary()), d.getPrimaryLanguage(), Integer.toString(d.getExperienceYears())));
            else if (e instanceof Tester t) employees.add(List.of("TESTER", e.getId(), e.getFullName(), e.getEmail(),
                    Double.toString(e.getBaseSalary()), t.getTestSpecialty(), t.isAutomation() ? "1" : "0"));
            else if (e instanceof Manager m) employees.add(List.of("MANAGER", e.getId(), e.getFullName(), e.getEmail(),
                    Double.toString(e.getBaseSalary()), m.getDepartment(), Double.toString(m.getResponsibilityAllowance())));
            else throw new StorageException("Vai trò nhân viên chưa hỗ trợ: " + e.getClass().getName());
        }
        List<List<String>> projects = new ArrayList<>();
        List<List<String>> members = new ArrayList<>();
        List<List<String>> tasks = new ArrayList<>();
        for (Project p : db.projects()) {
            projects.add(List.of(p.getId(), p.getName(), p.getDescription(), p.getStartDate().toString(),
                    p.getEndDate().toString(), p.getStatus().name()));
            for (ProjectMember m : p.getMembers()) members.add(List.of(m.getProjectId(), m.getEmployeeId(), m.getJoinedOn().toString()));
            for (Task t : p.getTasks()) tasks.add(List.of(t.getId(), t.getProjectId(), t.getTitle(),
                    t.getPriority().name(), t.getStatus().name(), Double.toString(t.getEstimatedHours()),
                    t.getAssigneeId() == null ? "" : t.getAssigneeId()));
        }
        write("employees.csv", EMPLOYEE_HEADER, employees);
        write("projects.csv", PROJECT_HEADER, projects);
        write("members.csv", MEMBER_HEADER, members);
        write("tasks.csv", TASK_HEADER, tasks);
    }

    private List<List<String>> read(String file, List<String> expectedHeader) {
        Path path = directory.resolve(file);
        if (!Files.exists(path)) return List.of();
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            if (lines.isEmpty()) throw new StorageException("File rỗng, thiếu header: " + file);
            if (!CsvCodec.parse(lines.get(0)).equals(expectedHeader))
                throw new StorageException("Header không đúng định dạng trong " + file);
            List<List<String>> rows = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                if (lines.get(i).isBlank()) continue;
                try {
                    List<String> row = CsvCodec.parse(lines.get(i));
                    if (row.size() != expectedHeader.size())
                        throw new IllegalArgumentException("Số cột " + row.size() + " khác " + expectedHeader.size());
                    rows.add(row);
                } catch (RuntimeException e) {
                    throw new StorageException("Dữ liệu lỗi tại " + file + ", dòng " + (i + 1) + ": " + e.getMessage(), e);
                }
            }
            return rows;
        } catch (IOException e) { throw new StorageException("Không đọc được " + path, e); }
    }
    private void write(String file, List<String> header, List<List<String>> values) {
        Path output = directory.resolve(file);
        try {
            Files.createDirectories(directory);
            List<String> lines = new ArrayList<>();
            lines.add(CsvCodec.encode(header));
            for (List<String> row : values) lines.add(CsvCodec.encode(row));
            Path temporary = Files.createTempFile(directory, file + "-", ".tmp");
            try {
                Files.write(temporary, lines, StandardCharsets.UTF_8);
                try {
                    Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ex) {
                    Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally { Files.deleteIfExists(temporary); }
        } catch (IOException | IllegalArgumentException e) {
            throw new StorageException("Không lưu được " + output, e);
        }
    }
}
