package vn.pxu.oop.pm.model;

import org.junit.jupiter.api.Test;
import vn.pxu.oop.pm.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProjectModelTest {
    @Test void rejectsReversedDates() {
        assertThrows(ValidationException.class, () -> new Project("P1", "Du an",
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 11, 1)));
    }
    @Test void taskRejectsZeroHours() {
        assertThrows(ValidationException.class, () -> new Task("T1", "P1", "Tieu de",
                vn.pxu.oop.pm.enums.TaskPriority.HIGH, vn.pxu.oop.pm.enums.TaskStatus.TODO, 0, null));
    }
    @Test void memberReferencesSameEmployeeObject() {
        Employee employee = new Developer("NV1", "An", "an@example.org", 100);
        ProjectMember member = new ProjectMember("P1", employee);
        assertSame(employee, member.getEmployee());
    }
}
