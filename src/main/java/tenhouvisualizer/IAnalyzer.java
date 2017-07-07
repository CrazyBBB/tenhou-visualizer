package tenhouvisualizer;

import java.util.ArrayList;

/**
 * 解析用インターフェース
 */
public interface IAnalyzer {
    void startGame(boolean isSanma, int taku, boolean isTonnan, boolean isSoku, boolean isUseAka, boolean isAriAri,
                   String[] playerNames, int[] playerRates, String[] playerDans);
    void endGame(int[] playerPoints);

    void startKyoku(int[] playerPoints, ArrayList<ArrayList<Integer>> playerHaipais, int oya, int bakaze,
                    int kyoku, int honba, int firstDora);
    void endKyoku();

    void draw(int position, int tsumoHai);
    void discard(int position, int kiriHai);

    void pong(int position);
    void chow();
    void ankan();
    void minkan();
    void kakan();
    void reach(int position, int step);
    void addDora(int newDora);

    void agari();
    void ryuukyoku();
}
