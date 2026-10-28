package vn.pxu.oop.pm.model;

import vn.pxu.oop.pm.util.Check;
import java.time.LocalDate;
import java.util.Objects;

/** Aggregation: hồ sơ Employee tồn tại độc lập với việc tham gia dự án. */
public final class ProjectMember {
    private final String projectId;
    private final Employee employee;
    private LocalDate joinedOn;

    public ProjectMember(String projectId, Employee employee) {
        this(projectId, employee, LocalDate.now());
    }
    public ProjectMember(String projectId, Employee employee, LocalDate joinedOn) {
        this.projectId = Check.required(projectId, "Mã dự án");
        this.employee = Objects.requireNonNull(employee, "Nhân viên không được null");
        setJoinedOn(joinedOn);
    }
    public String getProjectId() { return projectId; }
    public String getEmployeeId() { return employee.getId(); }
    public Employee getEmployee() { return employee; }
    public LocalDate getJoinedOn() { return joinedOn; }
    public void setJoinedOn(LocalDate joinedOn) {
        this.joinedOn = Objects.requireNonNull(joinedOn, "Ngày tham gia không được null");
    }
    @Override public String toString() { return employee.getId() + " (tham gia " + joinedOn + ")"; }
}
