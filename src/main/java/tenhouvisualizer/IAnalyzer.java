package tenhouvisualizer;

import java.util.ArrayList;

/**
 * 解析用インターフェース
 */
public interface IAnalyzer {
    void analyzeSHUFFLE(String seed);
    void analyzeGO(boolean isSanma, Utils.Taku taku, boolean isTonnan, boolean isSoku, boolean isUseAka, boolean isAriAri);
    void analyzeUN(String[] playerNames, int[] playerRates, String[] playerDans);
    void analyzeTAIKYOKU(Utils.KAZE oya);
    void analyzeINIT(int[] playerPoints, ArrayList<ArrayList<Integer>> playerHaipais, Utils.KAZE oya, Utils.KAZE bakaze,
                        int kyoku, int honba, int firstDora);
    void analyzeTUVW(Utils.KAZE position, int tsumoHai);
    void analyzeDEFG(Utils.KAZE position, int kiriHai);
    void analyzeREACH(Utils.KAZE position, int step);
    void analyzeDORA(int newDora);
    void analyzeN(Utils.KAZE position, boolean isKita, Naki naki);

    void startGame();
    void endGame();

    void startKyoku();
    void endKyoku(boolean isAgari, boolean isNagashiMangan);

    void draw();
    void discard();

    void pong();
    void chow();
    void ankan();
    void minkan();
    void kakan();
}
