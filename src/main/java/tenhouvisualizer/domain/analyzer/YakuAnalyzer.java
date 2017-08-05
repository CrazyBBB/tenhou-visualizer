package tenhouvisualizer.domain.analyzer;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class YakuAnalyzer implements IAnalyzer, ComputationContainer {
    private List<Pair> computationList = new ArrayList<>();

    private String playerName;
    private int position = -1;

    public YakuAnalyzer() {
        this.playerName = null;
    }

    public YakuAnalyzer(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public List<Pair> getComputationList() {
        return computationList;
    }

    @Override
    public void startGame(boolean isSanma, int taku, boolean isTonnan, boolean isSoku, boolean isUseAka, boolean isAriAri, String[] playerNames, int[] playerRates, String[] playerDans) {
        int n = isSanma ? 3 : 4;
        for (int i = 0; i < n; i++) {
            if (playerNames[i].equals(playerName)) position = i;
        }
    }

    @Override
    public void agari(int position, int from, ArrayList<String> yaku, int han, int hu, int score, int[] increaseAndDecrease) {
        if (this.position == position) {
            for (String y : yaku) {
                computationList.add(new Pair<>(y, 1));
            }
        }
    }
}
