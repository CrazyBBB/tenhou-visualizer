import java.util.ArrayList;
import java.util.TreeSet;

public class Scene {
    boolean isSanma;
    int playerId;
    String[] players;
    String[] dan;
    int[] rate;

    int[] point;
    TreeSet<Integer>[] stehai;
    ArrayList<Naki>[] naki;
    ArrayList<Integer>[] dahai;
    ArrayList<Boolean>[] tedashi;
    int[] reach;
    int[] kita;
    int bakaze;
    int kyoku;
    int honba;
    int kyotaku;
    ArrayList<Integer> dora;

    static final String[] bakazeStr = {"東", "南", "西", "北"};
    static final String[] maStr = {"三", "四"};

    public Scene(boolean isSanma, int playerId, String[] players, String[] dan, int[] rate, int[] point, TreeSet<Integer>[] stehai, ArrayList<Naki>[] naki, ArrayList<Integer>[] dahai, ArrayList<Boolean>[] tedashi, int[] reach, int[] kita, int bakaze, int kyoku, int honba, int kyotaku, ArrayList<Integer> dora) {
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
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(maStr[isSanma ? 0 : 1]).append("]");
        sb.append(players[playerId]).append(dan[playerId]);
        sb.append("(").append(point[playerId]).append("点, ");
        sb.append(bakazeStr[bakaze]).append(kyoku).append("-");
        sb.append(honba).append("局");
        sb.append(dahai[playerId].size()).append("巡目)");

        return sb.toString();
    }


    public String getBaStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(bakazeStr[bakaze]).append(kyoku).append("局");
        sb.append(honba).append("本場");

        return sb.toString();
    }
}
