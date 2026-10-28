package vn.pxu.oop.pm.enums;

public enum ProjectStatus {
    PLANNED("Dự kiến"), IN_PROGRESS("Đang thực hiện"), COMPLETED("Hoàn thành");
    private final String label;
    ProjectStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
