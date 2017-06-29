package tenhodownloader;

import org.junit.Test;
import static org.junit.Assert.*;

import java.sql.SQLException;

public class DatabaseServiceTest {
    @Test
    public void existId() throws Exception {
        String id = "2017010101gm-0000-0000-00000000";

        DatabaseService databaseService = new DatabaseService(null);
        assertFalse(databaseService.existsId(id));

        String content = DatabaseServiceTest.class.getResource("/mjlog/test.mjlog").toExternalForm();
        databaseService.saveMjlog(id, content);

        assertTrue(databaseService.existsId(id));
    }

    @Test
    public void testDBInitialize() throws SQLException, ClassNotFoundException {
        DatabaseService databaseService = new DatabaseService(null);
    }


}