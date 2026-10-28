package vn.pxu.oop.pm.model;

import vn.pxu.oop.pm.util.Check;
import vn.pxu.oop.pm.exception.ValidationException;

public final class Developer extends Employee {
    private String primaryLanguage;
    private int experienceYears;

    public Developer(String id, String fullName, String email, double baseSalary) {
        this(id, fullName, email, baseSalary, "Java", 0);
    }
    public Developer(String id, String fullName, String email, double baseSalary, String primaryLanguage, int experienceYears) {
        super(id, fullName, email, baseSalary);
        setPrimaryLanguage(primaryLanguage);
        setExperienceYears(experienceYears);
    }
    public String getPrimaryLanguage() { return primaryLanguage; }
    public int getExperienceYears() { return experienceYears; }
    public void setPrimaryLanguage(String primaryLanguage) { this.primaryLanguage = Check.required(primaryLanguage, "Ngôn ngữ chính"); }
    public void setExperienceYears(int experienceYears) {
        if (experienceYears < 0 || experienceYears > 50) throw new ValidationException("Số năm kinh nghiệm phải trong khoảng 0-50.");
        this.experienceYears = experienceYears;
    }
    @Override public String getRoleName() { return "Developer"; }
    @Override public double calculateMonthlyCost() { return getBaseSalary() * (1.0 + Math.min(experienceYears, 10) * 0.02); }
    @Override public String describeResponsibility() { return "Phát triển phần mềm bằng " + primaryLanguage; }
    @Override public String toString() { return super.toString() + " | " + primaryLanguage + " (" + experienceYears + " năm)"; }
}
