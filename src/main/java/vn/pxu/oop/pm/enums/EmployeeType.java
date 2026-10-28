package vn.pxu.oop.pm.enums;

public enum EmployeeType {
    DEVELOPER("Lập trình viên"), TESTER("Kiểm thử viên"), MANAGER("Quản lý");
    private final String label;
    EmployeeType(String label) { this.label = label; }
    public String getLabel() { return label; }
}
