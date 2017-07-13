package tenhodownloader;

import org.junit.Test;
import tenhouvisualizer.domain.service.DatabaseService;

import static org.junit.Assert.*;

import java.sql.SQLException;

public class DatabaseServiceTest {
    @Test
    public void existId() throws Exception {
        String id = "2017010101gm-0000-0000-00000000";

        DatabaseService databaseService = new DatabaseService(null);
        assertFalse(databaseService.existsIdInMJLOG(id));

        String content = DatabaseServiceTest.class.getResource("/mjlog/test.mjlog").toExternalForm();
        databaseService.saveMjlog(id, content);

        assertTrue(databaseService.existsIdInMJLOG(id));
    }

    @Test
    public void testDBInitialize() throws SQLException, ClassNotFoundException {
        DatabaseService databaseService = new DatabaseService(null);
    }


}