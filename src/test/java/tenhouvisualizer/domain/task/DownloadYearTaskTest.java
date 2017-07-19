package tenhouvisualizer.domain.task;

import org.junit.Test;

import static org.junit.Assert.*;
import static tenhouvisualizer.domain.task.DownloadYearTask.getFileNameWithExtension;

public class DownloadYearTaskTest {
    @Test
    public void getFileNameTest01() throws Exception {
        String path = "aaa/bbb.html";
        assertEquals("bbb.html", getFileNameWithExtension(path));
    }

    @Test
    public void getFileNameTest02() throws Exception {
        String path = "bbb.html";
        assertEquals("bbb.html", getFileNameWithExtension(path));
    }
}