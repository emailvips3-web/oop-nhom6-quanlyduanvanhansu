package vn.pxu.oop.pm.cli;

import vn.pxu.oop.pm.enums.*;
import vn.pxu.oop.pm.factory.EmployeeFactory;
import vn.pxu.oop.pm.model.Employee;
import vn.pxu.oop.pm.model.Project;
import vn.pxu.oop.pm.model.ProjectMember;
import vn.pxu.oop.pm.model.Task;
import vn.pxu.oop.pm.service.*;
import vn.pxu.oop.pm.storage.FileDataStore;
import vn.pxu.oop.pm.strategy.MonthlyTeamCostStrategy;
import vn.pxu.oop.pm.strategy.ProjectCostStrategy;
import vn.pxu.oop.pm.strategy.TaskEffortCostStrategy;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Scanner;

public final class ConsoleMenu {
    private final ConsoleInput input;
    private final InMemoryDatabase db;
    private final EmployeeService employees;
    private final ProjectService projects;
    private final TaskService tasks;
    private final ReportService reports;
    private final FileDataStore store;

    public ConsoleMenu(InMemoryDatabase db, FileDataStore store, Scanner scanner) {
        this.db = db;
        this.store = store;
        this.input = new ConsoleInput(scanner);
        this.employees = new EmployeeService(db);
        this.projects = new ProjectService(db);
        this.tasks = new TaskService(db);
        this.reports = new ReportService(db);
    }

    public void run() {
        System.out.println("\n=== QUẢN LÝ DỰ ÁN VÀ NHÂN SỰ - JAVA OOP ===");
        System.out.println("Dữ liệu: " + store.getDirectory());
        try {
            while (true) {
                System.out.println("\n1. Nhân sự   2. Dự án   3. Công việc   4. Phân công   5. Thống kê   6. Lưu dữ liệu   0. Thoát");
                int option = input.integer("Chức năng", 0, 6);
                if (option == 0) {
                    System.out.println("Đã thoát chương trình.");
                    return;
                }
                try {
                    switch (option) {
                        case 1 -> employeeMenu();
                        case 2 -> projectMenu();
                        case 3 -> taskMenu();
                        case 4 -> assignmentMenu();
                        case 5 -> reportMenu();
                        case 6 -> persist();
                        default -> throw new IllegalStateException("Menu không hợp lệ");
                    }
                } catch (RuntimeException e) {
                    System.out.println("  ! Lỗi: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException eof) {
            System.out.println("\nĐã kết thúc luồng nhập. Dữ liệu của các thay đổi thành công đã được lưu.");
        }
    }

    private void employeeMenu() {
        while (true) {
            System.out.println("\n-- NHÂN SỰ --");
            System.out.println("1. Danh sách  2. Thêm  3. Sửa  4. Xóa  5. Tìm kiếm  0. Quay lại");
            int choice = input.integer("Chọn", 0, 5);
            if (choice == 0) return;
            try {
                switch (choice) {
                    case 1 -> display(employees.all());
                    case 2 -> addEmployee();
                    case 3 -> updateEmployee();
                    case 4 -> deleteEmployee();
                    case 5 -> display(employees.search(input.line("Từ khóa")));
                    default -> { }
                }
            } catch (NoSuchElementException e) { throw e; }
            catch (RuntimeException e) { showError(e); }
        }
    }
    private void addEmployee() {
        EmployeeType role = input.choose("Vai trò", EmployeeType.values());
        String id = input.required("Mã nhân viên (NV01)");
        String name = input.required("Họ tên");
        String email = input.required("Email");
        double salary = input.number("Lương cơ bản (VND/tháng)", 0);
        String detail;
        double extra;
        switch (role) {
            case DEVELOPER -> {
                detail = input.required("Ngôn ngữ chính");
                extra = input.integer("Số năm kinh nghiệm", 0, 50);
            }
            case TESTER -> {
                detail = input.required("Chuyên môn kiểm thử");
                extra = input.integer("Automation: 0=không, 1=có", 0, 1);
            }
            case MANAGER -> {
                detail = input.required("Bộ phận");
                extra = input.number("Phụ cấp trách nhiệm", 0);
            }
            default -> throw new IllegalStateException("Vai trò chưa hỗ trợ");
        }
        Employee employee = EmployeeFactory.create(role, id, name, email, salary, detail, extra);
        mutate(() -> employees.add(employee));
    }
    private void updateEmployee() {
        String id = input.required("Mã nhân viên cần sửa");
        Employee old = employees.get(id);
        System.out.println("Hiện tại: " + old);
        String name = input.required("Họ tên mới");
        String email = input.required("Email mới");
        double salary = input.number("Lương cơ bản mới", 0);
        mutate(() -> employees.update(id, name, email, salary));
    }
    private void deleteEmployee() {
        String id = input.required("Mã nhân viên cần xóa");
        if (input.confirm("Xác nhận xóa " + id + "?")) mutate(() -> employees.delete(id));
    }
    private void projectMenu() {
        while (true) {
            System.out.println("\n-- DỰ ÁN --");
            System.out.println("1. Danh sách  2. Tạo  3. Sửa  4. Xóa  5. Tìm kiếm  6. Đổi trạng thái  0. Quay lại");
            int choice = input.integer("Chọn", 0, 6);
            if (choice == 0) return;
            try {
                switch (choice) {
                    case 1 -> display(projects.all());
                    case 2 -> addProject();
                    case 3 -> updateProject();
                    case 4 -> deleteProject();
                    case 5 -> display(projects.search(input.line("Từ khóa")));
                    case 6 -> changeProjectStatus();
                    default -> { }
                }
            } catch (NoSuchElementException e) { throw e; }
            catch (RuntimeException e) { showError(e); }
        }
    }
    private void addProject() {
        String id = input.required("Mã dự án (DA01)");
        String name = input.required("Tên dự án");
        String description = input.line("Mô tả (có thể bỏ trống)");
        var start = input.date("Ngày bắt đầu");
        var end = input.date("Ngày kết thúc");
        Project project = new Project(id, name, description, start, end, ProjectStatus.PLANNED);
        mutate(() -> projects.add(project));
    }
    private void updateProject() {
        String id = input.required("Mã dự án cần sửa");
        System.out.println("Hiện tại: " + projects.get(id));
        String name = input.required("Tên mới");
        String description = input.line("Mô tả mới");
        var start = input.date("Ngày bắt đầu mới");
        var end = input.date("Ngày kết thúc mới");
        mutate(() -> projects.update(id, name, description, start, end));
    }
    private void deleteProject() {
        String id = input.required("Mã dự án cần xóa");
        System.out.println("Cảnh báo: xóa luôn task và phân công thuộc dự án này.");
        if (input.confirm("Xác nhận xóa " + id + "?")) mutate(() -> projects.delete(id));
    }
    private void changeProjectStatus() {
        String id = input.required("Mã dự án");
        ProjectStatus status = input.choose("Trạng thái mới", ProjectStatus.values());
        mutate(() -> projects.changeStatus(id, status));
    }
    private void taskMenu() {
        while (true) {
            System.out.println("\n-- CÔNG VIỆC --");
            System.out.println("1. Theo dự án  2. Tạo  3. Sửa  4. Xóa  5. Tìm kiếm  6. Đổi trạng thái  7. Giao/bỏ giao  0. Quay lại");
            int choice = input.integer("Chọn", 0, 7);
            if (choice == 0) return;
            try {
                switch (choice) {
                    case 1 -> display(tasks.listByProject(input.required("Mã dự án")));
                    case 2 -> addTask();
                    case 3 -> updateTask();
                    case 4 -> deleteTask();
                    case 5 -> display(tasks.search(input.line("Từ khóa")));
                    case 6 -> changeTaskStatus();
                    case 7 -> assignTask();
                    default -> { }
                }
            } catch (NoSuchElementException e) { throw e; }
            catch (RuntimeException e) { showError(e); }
        }
    }
    private void addTask() {
        String projectId = input.required("Mã dự án");
        String id = input.required("Mã công việc (CV01)");
        String title = input.required("Tên công việc");
        TaskPriority priority = input.choose("Độ ưu tiên", TaskPriority.values());
        double hours = input.number("Số giờ dự kiến (>0)", 0.01);
        Task task = new Task(id, projectId, title, priority, TaskStatus.TODO, hours, null);
        mutate(() -> tasks.add(projectId, task));
    }
    private void updateTask() {
        String id = input.required("Mã công việc cần sửa");
        System.out.println("Hiện tại: " + tasks.get(id));
        String title = input.required("Tên mới");
        TaskPriority priority = input.choose("Độ ưu tiên mới", TaskPriority.values());
        double hours = input.number("Số giờ dự kiến mới (>0)", 0.01);
        mutate(() -> tasks.update(id, title, priority, hours));
    }
    private void deleteTask() {
        String id = input.required("Mã công việc cần xóa");
        if (input.confirm("Xác nhận xóa " + id + "?")) mutate(() -> tasks.delete(id));
    }
    private void changeTaskStatus() {
        String id = input.required("Mã công việc");
        TaskStatus status = input.choose("Trạng thái mới", TaskStatus.values());
        mutate(() -> tasks.changeStatus(id, status));
    }
    private void assignTask() {
        String id = input.required("Mã công việc");
        String employeeId = input.line("Mã người phụ trách (để trống để bỏ giao)");
        mutate(() -> tasks.assign(id, employeeId));
    }
    private void assignmentMenu() {
        while (true) {
            System.out.println("\n-- PHÂN CÔNG NHÂN SỰ --");
            System.out.println("1. Xem  2. Thêm  3. Gỡ  4. Sửa ngày tham gia  5. Tìm thành viên  6. Thống kê  0. Quay lại");
            int choice = input.integer("Chọn", 0, 6);
            if (choice == 0) return;
            try {
                switch (choice) {
                    case 1 -> {
                        String id = input.required("Mã dự án");
                        Collection<ProjectMember> members = projects.get(id).getMembers();
                        if (members.isEmpty()) System.out.println("(Chưa có thành viên)");
                        for (ProjectMember m : members)
                            System.out.println("- " + m + " | " + employees.get(m.getEmployeeId()).getFullName());
                    }
                    case 2 -> {
                        String id = input.required("Mã dự án");
                        String employeeId = input.required("Mã nhân viên");
                        mutate(() -> projects.assignMember(id, employeeId));
                    }
                    case 3 -> {
                        String id = input.required("Mã dự án");
                        String employeeId = input.required("Mã nhân viên");
                        mutate(() -> projects.removeMember(id, employeeId));
                    }
                    case 4 -> {
                        String id = input.required("Mã dự án");
                        String employeeId = input.required("Mã nhân viên");
                        var joinedOn = input.date("Ngày tham gia mới");
                        mutate(() -> projects.updateMemberDate(id, employeeId, joinedOn));
                    }
                    case 5 -> {
                        String id = input.required("Mã dự án");
                        String keyword = input.line("Tìm theo mã hoặc họ tên");
                        display(projects.searchMembers(id, keyword));
                    }
                    case 6 -> {
                        String id = input.required("Mã dự án");
                        System.out.println("Dự án " + id + " có " + projects.countMembers(id) + " thành viên.");
                    }
                    default -> { }
                }
            } catch (NoSuchElementException e) { throw e; }
            catch (RuntimeException e) { showError(e); }
        }
    }
    private void reportMenu() {
        while (true) {
            System.out.println("\n-- THỐNG KÊ --");
            System.out.println("1. Nhân sự theo vai trò  2. Tiến độ dự án  3. Ước tính chi phí  4. Số task đang phụ trách  0. Quay lại");
            int choice = input.integer("Chọn", 0, 4);
            if (choice == 0) return;
            try {
                switch (choice) {
                    case 1 -> reports.employeesByRole().forEach((role, count) -> System.out.println(role + ": " + count));
                    case 2 -> {
                        String id = input.required("Mã dự án");
                        ReportService.ProjectProgress s = reports.progress(id);
                        System.out.printf("Dự án %s: %d/%d task hoàn thành (%.1f%%)%n", id, s.completedTasks(), s.totalTasks(), s.percentComplete());
                    }
                    case 3 -> {
                        String id = input.required("Mã dự án");
                        int type = input.integer("1=Chi phí nhân sự 1 tháng; 2=Chi phí theo effort task", 1, 2);
                        ProjectCostStrategy strategy = type == 1 ? new MonthlyTeamCostStrategy() : new TaskEffortCostStrategy();
                        System.out.printf("%s: %,.0f VND%n", strategy.name(), reports.estimateCost(id, strategy));
                        System.out.println(strategy.details());
                    }
                    case 4 -> reports.workload().forEach((id, count) ->
                            System.out.println(id + " - " + employees.get(id).getFullName() + ": " + count + " task chưa hoàn thành"));
                    default -> { }
                }
            } catch (NoSuchElementException e) { throw e; }
            catch (RuntimeException e) { showError(e); }
        }
    }
    private void mutate(Runnable action) {
        action.run();
        persist();
        System.out.println("  ✓ Thực hiện thành công.");
    }
    private void persist() {
        store.save(db);
        System.out.println("  ✓ Đã lưu CSV vào " + store.getDirectory());
    }
    private void showError(RuntimeException e) { System.out.println("  ! " + e.getMessage()); }
    private void display(Collection<?> items) {
        if (items.isEmpty()) { System.out.println("(Danh sách trống)"); return; }
        items.forEach(item -> System.out.println("- " + item));
        System.out.println("Tổng: " + items.size());
    }
}
