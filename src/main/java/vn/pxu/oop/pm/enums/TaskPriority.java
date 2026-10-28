package vn.pxu.oop.pm.enums;

public enum TaskPriority {
    LOW("Thấp"), MEDIUM("Trung bình"), HIGH("Cao");
    private final String label;
    TaskPriority(String label) { this.label = label; }
    public String getLabel() { return label; }
}
