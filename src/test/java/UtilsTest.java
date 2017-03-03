/**
 * Created by m-yamamt on 2017/03/04.
 */
import org.junit.Test;
import static org.junit.Assert.*;

public class UtilsTest {
    @Test
    public void testComputeTiitoituSyanten_Tenpai() {
        int[] tehai = {2, 2, 2, 2, 2, 2, 1, 1, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0};
        int expected = 0;
        assertEquals(expected, Utils.computeTiitoituSyanten(tehai));
    }
    @Test
    public void testComputeTiitoituSyanten_Barabara() {
        int[] tehai = {1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0};
        int expected = 6;
        assertEquals(expected, Utils.computeTiitoituSyanten(tehai));
    }
}
