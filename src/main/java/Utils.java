/**
 * Created by m-yamamt on 2017/03/04.
 */
public class Utils {
    /**
     * チートイツのシャンテン数を計算する
     * @param tehai
     * @return チートイツのシャンテン数
     */
    public static int computeTiitoituSyanten(int[] tehai) {
        int toitu = 0;
        int syurui = 0;
        int syantenTiitoi;

        for (int i = 0; i < 34; i++) {
            if (tehai[i] >= 1) syurui++;
            if (tehai[i] >= 2) toitu++;
        }

        syantenTiitoi = 6 - toitu;

        if (syurui < 7) syantenTiitoi += 7 - syurui;
        return syantenTiitoi;
    }
}
