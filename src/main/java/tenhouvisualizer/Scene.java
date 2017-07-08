package tenhouvisualizer;

import java.util.ArrayList;
import java.util.TreeSet;

public class Scene {
    boolean isSanma;
    int playerId;
    String[] players;
    String[] dan;
    int[] rate;

    int[] point;
    ArrayList<TreeSet<Integer>> stehai;
    ArrayList<ArrayList<Naki>> naki;
    ArrayList<ArrayList<Integer>> dahai;
    ArrayList<ArrayList<Boolean>> tedashi;
    int[] reach;
    int[] kita;
    int bakaze;
    int kyoku;
    int honba;
    int kyotaku;
    ArrayList<Integer> dora;

    private String str;

    static final String[] bakazeStr = {"東", "南", "西", "北"};
    static final String[] maStr = {"三", "四"};

    public Scene(boolean isSanma, int playerId, String[] players, String[] dan, int[] rate, int[] point, ArrayList<TreeSet<Integer>> stehai, ArrayList<ArrayList<Naki>> naki, ArrayList<ArrayList<Integer>> dahai, ArrayList<ArrayList<Boolean>> tedashi, int[] reach, int[] kita, int bakaze, int kyoku, int honba, int kyotaku, ArrayList<Integer> dora) {
        this.isSanma = isSanma;
        this.playerId = playerId;
        this.players = players;
        this.dan = dan;
        this.rate = rate;
        this.point = point;
        this.stehai = stehai;
        this.naki = naki;
        this.dahai = dahai;
        this.tedashi = tedashi;
        this.reach = reach;
        this.kita = kita;
        this.bakaze = bakaze;
        this.kyoku = kyoku;
        this.honba = honba;
        this.kyotaku = kyotaku;
        this.dora = dora;
    }

    public Scene(String str, boolean isSanma, int playerId, String[] players, String[] dan, int[] rate, int[] point, ArrayList<TreeSet<Integer>> stehai, ArrayList<ArrayList<Naki>> naki, ArrayList<ArrayList<Integer>> dahai, ArrayList<ArrayList<Boolean>> tedashi, int[] reach, int[] kita, int bakaze, int kyoku, int honba, int kyotaku, ArrayList<Integer> dora) {
        this.str = str;
        this.isSanma = isSanma;
        this.playerId = playerId;
        this.players = players;
        this.dan = dan;
        this.rate = rate;
        this.point = point;
        this.stehai = stehai;
        this.naki = naki;
        this.dahai = dahai;
        this.tedashi = tedashi;
        this.reach = reach;
        this.kita = kita;
        this.bakaze = bakaze;
        this.kyoku = kyoku;
        this.honba = honba;
        this.kyotaku = kyotaku;
        this.dora = dora;
    }

    public String getZikaze(int playerId) {
        int n = isSanma ? 3 : 4;
        return bakazeStr[(playerId - kyoku + 1 + n) % n];
    }

    @Override
    public String toString() {
        if (str != null) return str;

        return "[" + maStr[isSanma ? 0 : 1] + "]" +
                players[playerId] + dan[playerId] +
                "(" + point[playerId] + "点, " +
                bakazeStr[bakaze] + kyoku + "-" +
                honba + "局" +
                dahai.get(playerId).size() + "巡目)";
    }


    public String getBaStr() {
        return bakazeStr[bakaze] + kyoku + "局" +
                honba + "本場";
    }
}
