package vn.pxu.oop.pm.model;

import vn.pxu.oop.pm.util.Check;

public final class Tester extends Employee {
    private String testSpecialty;
    private boolean automation;

    public Tester(String id, String fullName, String email, double baseSalary) {
        this(id, fullName, email, baseSalary, "Functional", false);
    }
    public Tester(String id, String fullName, String email, double baseSalary, String testSpecialty, boolean automation) {
        super(id, fullName, email, baseSalary);
        setTestSpecialty(testSpecialty);
        setAutomation(automation);
    }
    public String getTestSpecialty() { return testSpecialty; }
    public boolean isAutomation() { return automation; }
    public void setTestSpecialty(String testSpecialty) { this.testSpecialty = Check.required(testSpecialty, "Chuyên môn kiểm thử"); }
    public void setAutomation(boolean automation) { this.automation = automation; }
    @Override public String getRoleName() { return "Tester"; }
    @Override public double calculateMonthlyCost() { return getBaseSalary() * (automation ? 1.05 : 1.0); }
    @Override public String describeResponsibility() { return "Kiểm thử " + testSpecialty + (automation ? " tự động" : " thủ công"); }
    @Override public String toString() { return super.toString() + " | " + describeResponsibility(); }
}
