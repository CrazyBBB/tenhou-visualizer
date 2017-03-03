/**
 * Created by m-yamamt on 2017/03/04.
 */
public class Utils {

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

    public static int computeKokusiSyanten(int[] tehai) {
        int kokusiToitu = 0;
        int syantenKokusi = 13;

        for (int i = 0; i < 34; i++) {
            if (i % 9 == 0 || i % 9 == 8 || i >= 27) {
                if (tehai[i] >= 1) syantenKokusi--;
                if (tehai[i] >= 2) kokusiToitu = 1;
            }
        }

        syantenKokusi -= kokusiToitu;
        return syantenKokusi;
    }
}
