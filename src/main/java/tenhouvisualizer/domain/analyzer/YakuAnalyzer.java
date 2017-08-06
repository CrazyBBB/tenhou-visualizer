package tenhouvisualizer.domain.analyzer;

import javafx.util.Pair;

import java.util.*;

public class YakuAnalyzer implements IAnalyzer, ComputationContainer {
    private HashMap<String, Integer> yakuCountMap = new HashMap<>();

    private String playerName;
    private int position = -1;

    public YakuAnalyzer() {
        this.playerName = null;
    }

    public YakuAnalyzer(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public List<Pair<String, Integer>> getComputationList() {
        List<Pair<String, Integer>> computationList = new ArrayList<>();
        Set<String> set = yakuCountMap.keySet();
        for (String yakuString : set) {
            computationList.add(new Pair<>(yakuString, yakuCountMap.get(yakuString)));
        }
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
                yakuCountMap.merge(y, 1, Integer::sum);
            }
        }
    }
}
