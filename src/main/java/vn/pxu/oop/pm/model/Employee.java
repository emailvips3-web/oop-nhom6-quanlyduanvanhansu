package vn.pxu.oop.pm.model;

import vn.pxu.oop.pm.util.Check;

import java.util.Objects;

public abstract class Employee implements Identifiable {
    private final String id;
    private String fullName;
    private String email;
    private double baseSalary;

    protected Employee(String id, String fullName, String email) {
        this(id, fullName, email, 0);
    }

    protected Employee(String id, String fullName, String email, double baseSalary) {
        this.id = Check.required(id, "Mã nhân viên");
        setFullName(fullName);
        setEmail(email);
        setBaseSalary(baseSalary);
    }

    @Override public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public double getBaseSalary() { return baseSalary; }
    public void setFullName(String fullName) { this.fullName = Check.required(fullName, "Họ tên"); }
    public void setEmail(String email) { this.email = Check.email(email); }
    public void setBaseSalary(double baseSalary) { this.baseSalary = Check.nonNegative(baseSalary, "Lương cơ bản"); }

    public abstract String getRoleName();
    public abstract double calculateMonthlyCost();
    public abstract String describeResponsibility();

    @Override public String toString() {
        return String.format("%s | %s | %s | %s | lương %,.0f", id, fullName, getRoleName(), email, baseSalary);
    }

    @Override public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Employee other)) return false;
        return id.equalsIgnoreCase(other.id);
    }

    @Override public final int hashCode() { return Objects.hash(id.toUpperCase(java.util.Locale.ROOT)); }
}
