package vn.pxu.oop.pm.model;

import vn.pxu.oop.pm.util.Check;

public final class Manager extends Employee {
    private String department;
    private double responsibilityAllowance;

    public Manager(String id, String fullName, String email, double baseSalary) {
        this(id, fullName, email, baseSalary, "Dự án", 0);
    }
    public Manager(String id, String fullName, String email, double baseSalary, String department, double responsibilityAllowance) {
        super(id, fullName, email, baseSalary);
        setDepartment(department);
        setResponsibilityAllowance(responsibilityAllowance);
    }
    public String getDepartment() { return department; }
    public double getResponsibilityAllowance() { return responsibilityAllowance; }
    public void setDepartment(String department) { this.department = Check.required(department, "Bộ phận"); }
    public void setResponsibilityAllowance(double responsibilityAllowance) { this.responsibilityAllowance = Check.nonNegative(responsibilityAllowance, "Phụ cấp"); }
    @Override public String getRoleName() { return "Manager"; }
    @Override public double calculateMonthlyCost() { return getBaseSalary() + responsibilityAllowance; }
    @Override public String describeResponsibility() { return "Quản lý bộ phận " + department; }
    @Override public String toString() { return super.toString() + " | " + department + " | phụ cấp " + responsibilityAllowance; }
}
