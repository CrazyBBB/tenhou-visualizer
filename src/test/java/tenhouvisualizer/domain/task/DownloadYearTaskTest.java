package tenhouvisualizer.domain.task;

import org.junit.Test;

import static org.junit.Assert.*;
import static tenhouvisualizer.domain.task.DownloadYearTask.getFileName;

public class DownloadYearTaskTest {
    @Test
    public void getFileNameTest01() throws Exception {
        String path = "aaa/bbb.html";
        assertEquals("bbb.html", getFileName(path));
    }

    @Test
    public void getFileNameTest02() throws Exception {
        String path = "bbb.html";
        assertEquals("bbb.html", getFileName(path));
    }
}