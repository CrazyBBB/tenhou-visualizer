import org.junit.Test;
import tenhouvisualizer.domain.MahjongUtils;

import static org.junit.Assert.*;

public class MahjongUtilsTest {
    @Test
    public void testComputeTiitoituSyanten_Tenpai() {
        int[] tehai = {2, 2, 2, 2, 2, 2, 1, 1, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0};
        int expected = 0;
        assertEquals(expected, MahjongUtils.computeTiitoituSyanten(tehai));
    }
    @Test
    public void testComputeTiitoituSyanten_Barabara() {
        int[] tehai = {1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 1, 1, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0};
        int expected = 6;
        assertEquals(expected, MahjongUtils.computeTiitoituSyanten(tehai));
    }

    @Test
    public void testComputeKokusiSyanten_13Menmachi() {
        int[] tehai = {1, 1, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 1, 1, 1, 1, 1, 1};
        int expected = 0;
        assertEquals(expected, MahjongUtils.computeKokusiSyanten(tehai));
    }
    @Test
    public void testComputeKokusiSyanten_Tenpai() {
        int[] tehai = {1, 1, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 1, 1, 1, 1, 2, 0};
        int expected = 0;
        assertEquals(expected, MahjongUtils.computeKokusiSyanten(tehai));
    }
    @Test
    public void testComputeKokusiSyanten_Tanyao() {
        int[] tehai = {0, 2, 2, 2, 2, 2, 2, 2, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0};
        int expected = 13;
        assertEquals(expected, MahjongUtils.computeKokusiSyanten(tehai));
    }

    @Test
    public void testComputeSyanten_13Menmachi() {
        int[] tehai = {1, 1, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 1, 1, 1, 1, 1, 1};
        int expected = 0;
        assertEquals(expected, MahjongUtils.computeSyanten(tehai, 0));
    }
    @Test
    public void testComputeSyanten_Agari() {
        int[] tehai = {1, 1, 1, 1, 1, 1, 1, 1, 1,
                1, 1, 1, 2, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0};
        int expected = -1;
        assertEquals(expected, MahjongUtils.computeSyanten(tehai, 0));
    }
    @Test
    public void testComputeSyanten_Random1() {
        int[] tehai = {1, 1, 0, 1, 0, 1, 2, 0, 0,
                1, 0, 1, 2, 0, 0, 0, 0, 0,
                0, 1, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 1, 1, 1};
        int expected = 3;
        assertEquals(expected, MahjongUtils.computeSyanten(tehai, 0));
    }
    @Test
    public void testComputeSyanten_HadakaTanki() {
        int[] tehai = {0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0};
        int expected = 0;
        assertEquals(expected, MahjongUtils.computeSyanten(tehai, 4));
    }
}
