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
        sb.append(bakazeStr[bakaze]).append(kyoku).append("局");
        sb.append(honba).append("本場: ");

        int n = isSanma ? 3 : 4;
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(", ");
            sb.append(players[i]).append("(").append(point[i]).append(")");
        }

        return sb.toString(); //TODO:変更
        // return String.join(",", players);
    }


    public String getBaStr() {
        StringBuilder sb = new StringBuilder();
        sb.append(bakazeStr[bakaze]).append(kyoku).append("局");
        sb.append(honba).append("本場");

        return sb.toString();
    }
}
