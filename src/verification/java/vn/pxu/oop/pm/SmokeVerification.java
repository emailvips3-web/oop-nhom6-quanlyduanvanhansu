package vn.pxu.oop.pm;

import vn.pxu.oop.pm.enums.*;
import vn.pxu.oop.pm.exception.*;
import vn.pxu.oop.pm.factory.EmployeeFactory;
import vn.pxu.oop.pm.model.*;
import vn.pxu.oop.pm.service.*;
import vn.pxu.oop.pm.storage.*;
import vn.pxu.oop.pm.strategy.*;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;

/** Không yêu cầu JUnit/Maven; kiểm tra trực tiếp bằng javac + java (Smoke check). */
public final class SmokeVerification {
    private int passed;
    private SmokeVerification() { }
    private void ok(boolean condition, String name) {
        if (!condition) throw new AssertionError("FAIL: " + name);
        passed++;
    }
    private void throwsType(Class<? extends Throwable> type, Runnable action, String name) {
        try { action.run(); } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError("FAIL: " + name + ": " + t, t);
        }
        throw new AssertionError("FAIL: " + name + ": không ném exception");
    }
    private void run() throws Exception {
        throwsType(ValidationException.class, () -> new Developer("NV1", "A", "bad", 100), "Email validation");
        throwsType(ValidationException.class, () -> new Tester("NV1", "A", "a@b.com", -1), "Salary validation");
        Employee factory = EmployeeFactory.create(EmployeeType.MANAGER, "NV1", "A", "a@b.com", 100, "PMO", 50);
        ok(factory instanceof Manager, "Factory subclass");
        ok(factory.calculateMonthlyCost() == 150, "Polymorphism salary");
        ok(factory.hasId("nv1"), "Interface default method");
        throwsType(ValidationException.class, () -> new Project("P", "Dự án",
                LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 1)), "Date validation");
        ok(CsvCodec.parse(CsvCodec.encode(List.of("A,B", "c\"d", "Việt Nam"))).equals(List.of("A,B", "c\"d", "Việt Nam")), "CSV escaping");
        InMemoryDatabase db = new InMemoryDatabase();
        EmployeeService es = new EmployeeService(db);
        ProjectService ps = new ProjectService(db);
        TaskService ts = new TaskService(db);
        ReportService rs = new ReportService(db);
        Employee e1 = new Developer("NV1", "A", "a@b.com", 16000000);
        Employee e2 = new Tester("NV2", "B", "b@b.com", 12000000);
        es.add(e1); es.add(e2);
        throwsType(BusinessRuleException.class, () -> es.add(new Tester("NV3", "C", "A@b.com", 100)), "Unique email");
        ps.add(new Project("DA1", "Dự án", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        ps.assignMember("DA1", "NV1");
        ok(ps.get("DA1").getMembers().get(0).getEmployee() == e1, "Aggregation reference");
        throwsType(BusinessRuleException.class, () -> es.delete("NV1"), "Member cannot delete");
        ts.add("DA1", new Task("CV1", "DA1", "Code"));
        throwsType(BusinessRuleException.class, () -> ts.assign("CV1", "NV2"), "Assignee in project");
        throwsType(BusinessRuleException.class, () -> ts.changeStatus("CV1", TaskStatus.DONE), "DONE needs assignee");
        throwsType(BusinessRuleException.class, () -> ps.changeStatus("DA1", ProjectStatus.COMPLETED), "Complete needs done");
        ts.assign("CV1", "NV1");ts.changeStatus("CV1", TaskStatus.DONE);
        ok(rs.progress("DA1").percentComplete() == 100, "Progress report");
        ok(rs.estimateCost("DA1", new MonthlyTeamCostStrategy()) == 16000000, "Monthly strategy");
        ok(rs.estimateCost("DA1", new TaskEffortCostStrategy()) == 800000, "Effort strategy");
        ok(ts.search("Code").size() == 1 && ts.search("Code", TaskStatus.TODO).isEmpty(), "Overload search");
        ps.changeStatus("DA1", ProjectStatus.COMPLETED);
        throwsType(BusinessRuleException.class, () -> ts.delete("CV1"), "Completed project locked");
        Path temp = Files.createTempDirectory("pm-verification-");
        try {
            FileDataStore store = new FileDataStore(temp);
            store.save(db);
            InMemoryDatabase restored = store.load();
            ok(restored.employee("NV1").getFullName().equals("A"), "Restore employee");
            ok(restored.project("DA1").getStatus() == ProjectStatus.COMPLETED, "Restore project status");
            ok(restored.project("DA1").getMembers().get(0).getEmployee() == restored.employee("NV1"), "Restore aggregation ref");
            ok(restored.task("CV1").task().getStatus() == TaskStatus.DONE, "Restore task");
            Files.writeString(temp.resolve("employees.csv"), "invalid header\n");
            throwsType(StorageException.class, store::load, "Reject corrupted CSV");
        } finally {
            try (var files = Files.list(temp)) { files.forEach(p -> { try { Files.deleteIfExists(p); } catch (Exception ignored) { } }); }
            Files.deleteIfExists(temp);
        }
        ps.delete("DA1");
        throwsType(EntityNotFoundException.class, () -> ts.get("CV1"), "Composition cascade");
        es.delete("NV1");
        ok(es.all().size() == 1, "Delete unassigned staff");
        System.out.println("PASS: " + passed + " smoke checks (không cần Maven/JUnit)");
    }
    public static void main(String[] args) throws Exception { new SmokeVerification().run(); }
}
