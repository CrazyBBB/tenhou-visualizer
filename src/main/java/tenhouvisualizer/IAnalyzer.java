package tenhouvisualizer;

import java.util.ArrayList;

/**
 * 解析用インターフェース
 */
public interface IAnalyzer {
    void startGame(boolean isSanma, Utils.Taku taku, boolean isTonnan, boolean isSoku, boolean isUseAka, boolean isAriAri,
                   String[] playerNames, int[] playerRates, String[] playerDans);
    void endGame(int[] playerPoints);

    void startKyoku(int[] playerPoints, ArrayList<ArrayList<Integer>> playerHaipais, Utils.KAZE oya, Utils.KAZE bakaze,
                    int kyoku, int honba, int firstDora);
    void endKyoku();

    void draw(Utils.KAZE position, int tsumoHai);
    void discard(Utils.KAZE position, int kiriHai);

    void pong();
    void chow();
    void ankan();
    void minkan();
    void kakan();
    void reach(Utils.KAZE position, int step);
    void addDora(int newDora);

    void agari();
    void ryuukyoku();
}
