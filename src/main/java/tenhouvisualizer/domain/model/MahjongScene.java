package tenhouvisualizer.domain.model;

import java.util.ArrayList;
import java.util.TreeSet;

public class MahjongScene {
    public boolean isSanma;
    public int heroPosition;
    public String[] playerNames;
    public String[] playerDans;
    public int[] playerRates;

    public int[] playerPoints;
    public ArrayList<TreeSet<Integer>> tehaiSets;
    public ArrayList<ArrayList<Naki>> naki;
    public ArrayList<ArrayList<Integer>> stehaiLists;
    public ArrayList<ArrayList<Boolean>> tedashiLists;
    public int[] tsumo;
    public int[] da;
    public boolean daTedasi;
    public boolean daReach;
    public int[] reach;
    public int[] kita;
    int bakaze;
    int kyoku;
    public int honba;
    public int kyotaku;
    public ArrayList<Integer> dora;
    public int nokori;

    private String str;

    static final String[] bakazeStr = {"東", "南", "西", "北"};
    static final String[] maStr = {"三", "四"};

    public MahjongScene(boolean isSanma, int heroPosition, String[] playerNames, String[] playerDans, int[] playerRates, int[] playerPoints, ArrayList<TreeSet<Integer>> tehaiSets, ArrayList<ArrayList<Naki>> naki, ArrayList<ArrayList<Integer>> stehaiLists, ArrayList<ArrayList<Boolean>> tedashiLists, int[] tsumo, int[] da, boolean daTedasi, boolean daReach, int[] reach, int[] kita, int bakaze, int kyoku, int honba, int kyotaku, ArrayList<Integer> dora, int nokori) {
        this.isSanma = isSanma;
        this.heroPosition = heroPosition;
        this.playerNames = playerNames;
        this.playerDans = playerDans;
        this.playerRates = playerRates;
        this.playerPoints = playerPoints;
        this.tehaiSets = tehaiSets;
        this.naki = naki;
        this.stehaiLists = stehaiLists;
        this.tedashiLists = tedashiLists;
        this.tsumo = tsumo;
        this.da = da;
        this.daTedasi = daTedasi;
        this.daReach = daReach;
        this.reach = reach;
        this.kita = kita;
        this.bakaze = bakaze;
        this.kyoku = kyoku;
        this.honba = honba;
        this.kyotaku = kyotaku;
        this.dora = dora;
        this.nokori = nokori;
    }

    public MahjongScene(boolean isSanma, int heroPosition, String[] playerNames, String[] playerDans, int[] playerRates, int[] playerPoints, ArrayList<TreeSet<Integer>> tehaiSets, ArrayList<ArrayList<Naki>> naki, ArrayList<ArrayList<Integer>> stehaiLists, ArrayList<ArrayList<Boolean>> tedashiLists, int[] tsumo, int[] da, boolean daTedasi, boolean daReach, int[] reach, int[] kita, int bakaze, int kyoku, int honba, int kyotaku, ArrayList<Integer> dora, int nokori, String str) {

        this.isSanma = isSanma;
        this.heroPosition = heroPosition;
        this.playerNames = playerNames;
        this.playerDans = playerDans;
        this.playerRates = playerRates;
        this.playerPoints = playerPoints;
        this.tehaiSets = tehaiSets;
        this.naki = naki;
        this.stehaiLists = stehaiLists;
        this.tedashiLists = tedashiLists;
        this.tsumo = tsumo;
        this.da = da;
        this.daTedasi = daTedasi;
        this.daReach = daReach;
        this.reach = reach;
        this.kita = kita;
        this.bakaze = bakaze;
        this.kyoku = kyoku;
        this.honba = honba;
        this.kyotaku = kyotaku;
        this.dora = dora;
        this.nokori = nokori;
        this.str = str;
    }

    public String getZikaze(int playerId) {
        int n = isSanma ? 3 : 4;
        return bakazeStr[(playerId - kyoku + 1 + n) % n];
    }

    @Override
    public String toString() {
        if (str != null) return str;

        return "[" + maStr[isSanma ? 0 : 1] + "]" +
                playerNames[heroPosition] + playerDans[heroPosition] +
                "(" + playerPoints[heroPosition] + "点, " +
                bakazeStr[bakaze] + kyoku + "-" +
                honba + "局" +
                stehaiLists.get(heroPosition).size() + "巡目)";
    }


    public String getBaStr() {
        return bakazeStr[bakaze] + kyoku + "局";
    }
}
