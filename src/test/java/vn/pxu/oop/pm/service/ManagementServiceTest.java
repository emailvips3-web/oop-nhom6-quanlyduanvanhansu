package vn.pxu.oop.pm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.pxu.oop.pm.enums.*;
import vn.pxu.oop.pm.exception.BusinessRuleException;
import vn.pxu.oop.pm.exception.EntityNotFoundException;
import vn.pxu.oop.pm.model.*;
import vn.pxu.oop.pm.strategy.*;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ManagementServiceTest {
    private InMemoryDatabase db;
    private EmployeeService employees;
    private ProjectService projects;
    private TaskService tasks;
    private ReportService reports;

    @BeforeEach void setUp() {
        db = new InMemoryDatabase();
        employees = new EmployeeService(db);
        projects = new ProjectService(db);
        tasks = new TaskService(db);
        reports = new ReportService(db);
        employees.add(new Developer("NV1", "An", "an@example.org", 16000000));
        employees.add(new Tester("NV2", "Binh", "binh@example.org", 12000000));
        projects.add(new Project("DA1", "Du an", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
    }
    @Test void duplicateEmailFails() {
        assertThrows(BusinessRuleException.class, () -> employees.add(new Manager("NV3", "Chi", "AN@example.org", 2000)));
    }
    @Test void duplicateEmployeeIdFails() {
        assertThrows(BusinessRuleException.class, () -> employees.add(new Tester("nv1", "Chi", "chi@example.org", 100)));
    }
    @Test void assignedMemberCannotBeDeleted() {
        projects.assignMember("DA1", "NV1");
        assertThrows(BusinessRuleException.class, () -> employees.delete("NV1"));
    }
    @Test void taskAssigneeMustBelongToProject() {
        tasks.add("DA1", new Task("CV1", "DA1", "Code"));
        assertThrows(BusinessRuleException.class, () -> tasks.assign("CV1", "NV1"));
    }
    @Test void completedTaskNeedsAssignee() {
        tasks.add("DA1", new Task("CV1", "DA1", "Code"));
        assertThrows(BusinessRuleException.class, () -> tasks.changeStatus("CV1", TaskStatus.DONE));
    }
    @Test void projectCanCompleteOnlyAfterTasksDone() {
        projects.assignMember("DA1", "NV1");
        tasks.add("DA1", new Task("CV1", "DA1", "Code"));
        assertThrows(BusinessRuleException.class, () -> projects.changeStatus("DA1", ProjectStatus.COMPLETED));
        tasks.assign("CV1", "NV1");
        tasks.changeStatus("CV1", TaskStatus.DONE);
        projects.changeStatus("DA1", ProjectStatus.COMPLETED);
        assertEquals(ProjectStatus.COMPLETED, projects.get("DA1").getStatus());
    }
    @Test void removingMemberWithAssignedTaskFails() {
        projects.assignMember("DA1", "NV1");
        tasks.add("DA1", new Task("CV1", "DA1", "Code"));
        tasks.assign("CV1", "NV1");
        assertThrows(BusinessRuleException.class, () -> projects.removeMember("DA1", "NV1"));
    }
    @Test void deletingProjectCascadesTasksAndMembership() {
        projects.assignMember("DA1", "NV1");
        tasks.add("DA1", new Task("CV1", "DA1", "Code"));
        projects.delete("DA1");
        assertThrows(EntityNotFoundException.class, () -> tasks.get("CV1"));
        assertDoesNotThrow(() -> employees.delete("NV1"));
    }
    @Test void taskIdUniqueAcrossProjects() {
        projects.add(new Project("DA2", "Du an 2", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        tasks.add("DA1", new Task("CV1", "DA1", "Code"));
        assertThrows(BusinessRuleException.class, () -> tasks.add("DA2", new Task("CV1", "DA2", "Test")));
    }
    @Test void calculatesProgressAndCostStrategies() {
        projects.assignMember("DA1", "NV1");
        tasks.add("DA1", new Task("CV1", "DA1", "Code"));
        tasks.assign("CV1", "NV1");
        tasks.changeStatus("CV1", TaskStatus.DONE);
        assertEquals(100, reports.progress("DA1").percentComplete(), 0.001);
        assertEquals(16000000, reports.estimateCost("DA1", new MonthlyTeamCostStrategy()), 0.001);
        assertEquals(800000, reports.estimateCost("DA1", new TaskEffortCostStrategy()), 0.001);
    }
    @Test void memberCanBeSearchedAndItsJoinDateUpdated() {
        projects.assignMember("DA1", "NV1");
        LocalDate changed = LocalDate.of(2026, 10, 11);
        projects.updateMemberDate("DA1", "NV1", changed);
        assertAll(() -> assertEquals(1, projects.countMembers("DA1")),
                () -> assertEquals("NV1", projects.searchMembers("DA1", "An").get(0).getEmployeeId()),
                () -> assertEquals(changed, projects.get("DA1").getMembers().get(0).getJoinedOn()));
    }
    @Test void completedProjectLocksMemberDateChanges() {
        projects.assignMember("DA1", "NV1");
        tasks.add("DA1", new Task("CV1", "DA1", "Code"));
        tasks.assign("CV1", "NV1");
        tasks.changeStatus("CV1", TaskStatus.DONE);
        projects.changeStatus("DA1", ProjectStatus.COMPLETED);
        assertThrows(BusinessRuleException.class,
                () -> projects.updateMemberDate("DA1", "NV1", LocalDate.of(2026, 10, 11)));
    }
    @Test void strategyOverloadCanFilterTasks() {
        tasks.add("DA1", new Task("CV1", "DA1", "Code"));
        assertEquals(1, tasks.search("Code").size());
        assertEquals(0, tasks.search("Code", TaskStatus.DONE).size());
    }
}
