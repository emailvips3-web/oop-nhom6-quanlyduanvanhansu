package vn.pxu.oop.pm.enums;

public enum TaskStatus {
    TODO("Cần làm"), IN_PROGRESS("Đang làm"), DONE("Hoàn thành");
    private final String label;
    TaskStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
