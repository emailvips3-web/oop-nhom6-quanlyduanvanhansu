package vn.pxu.oop.pm.model;

import org.junit.jupiter.api.Test;
import vn.pxu.oop.pm.enums.EmployeeType;
import vn.pxu.oop.pm.exception.ValidationException;
import vn.pxu.oop.pm.factory.EmployeeFactory;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeModelTest {
    @Test void rejectsInvalidEmail() {
        assertThrows(ValidationException.class, () -> new Developer("NV1", "An", "invalid", 100));
    }
    @Test void rejectsNegativeSalary() {
        assertThrows(ValidationException.class, () -> new Tester("NV1", "An", "an@example.org", -1));
    }
    @Test void constructorsAndOverloadingWork() {
        Developer developer = new Developer("NV1", "An", "an@example.org", 1000);
        assertEquals("Java", developer.getPrimaryLanguage());
        assertEquals(0, developer.getExperienceYears());
    }
    @Test void polymorphismUsesConcreteCost() {
        Employee developer = new Developer("NV1", "An", "an@example.org", 1000, "Java", 5);
        Employee tester = new Tester("NV2", "Binh", "binh@example.org", 1000, "API", true);
        Employee manager = new Manager("NV3", "Chi", "chi@example.org", 1000, "PMO", 200);
        assertAll(() -> assertEquals(1100, developer.calculateMonthlyCost(), 0.01),
                () -> assertEquals(1050, tester.calculateMonthlyCost(), 0.01),
                () -> assertEquals(1200, manager.calculateMonthlyCost(), 0.01));
    }
    @Test void factoryChoosesCorrectSubclass() {
        assertInstanceOf(Manager.class, EmployeeFactory.create(EmployeeType.MANAGER,
                "NV9", "An", "an@example.org", 1000, "PMO", 100));
    }
    @Test void factoryRejectsFractionalYears() {
        assertThrows(ValidationException.class, () -> EmployeeFactory.create(EmployeeType.DEVELOPER,
                "NV1", "An", "an@example.org", 1000, "Java", 2.5));
    }
    @Test void equalityByIdentifier() {
        Employee a = new Developer("NV1", "An", "an@example.org", 100);
        Employee b = new Tester("nv1", "Binh", "binh@example.org", 150);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
