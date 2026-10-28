package vn.pxu.oop.pm.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.pxu.oop.pm.enums.ProjectStatus;
import vn.pxu.oop.pm.exception.StorageException;
import vn.pxu.oop.pm.model.Developer;
import vn.pxu.oop.pm.model.Project;
import vn.pxu.oop.pm.model.Task;
import vn.pxu.oop.pm.service.InMemoryDatabase;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileDataStoreTest {
    @TempDir Path tempDir;

    @Test void csvEscapesCommasAndQuotes() {
        List<String> value = List.of("A, B", "An \"Tester\"", "Việt Nam");
        assertEquals(value, CsvCodec.parse(CsvCodec.encode(value)));
    }
    @Test void savingAndLoadingKeepsRelations() {
        InMemoryDatabase db = new InMemoryDatabase();
        Developer dev = new Developer("NV1", "An", "an@example.org", 1000);
        db.addEmployee(dev);
        Project p = new Project("DA1", "Dự án", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 1));
        db.addProject(p);
        p.addMember(new vn.pxu.oop.pm.model.ProjectMember("DA1", dev));
        p.addTask(new Task("CV1", "DA1", "Lập trình"));
        FileDataStore store = new FileDataStore(tempDir);
        store.save(db);
        InMemoryDatabase restored = store.load();
        assertAll(() -> assertEquals(1, restored.employees().size()),
                () -> assertEquals(1, restored.project("DA1").getMembers().size()),
                () -> assertEquals(1, restored.project("DA1").getTasks().size()),
                () -> assertSame(restored.employee("NV1"), restored.project("DA1").getMembers().get(0).getEmployee()));
    }
    @Test void corruptedHeaderIsNotSilentlyIgnored() throws Exception {
        Files.writeString(tempDir.resolve("employees.csv"), "bad,header\n", StandardCharsets.UTF_8);
        assertThrows(StorageException.class, () -> new FileDataStore(tempDir).load());
    }
}
