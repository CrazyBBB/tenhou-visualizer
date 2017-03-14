import java.util.ArrayList;

public class Scene {
    boolean isSanma;
    int playerId;
    String[] players;

    int[] point;
    ArrayList<Integer>[] tehai;
    ArrayList<Naki>[] naki;
    ArrayList<Integer>[] dahai;
    ArrayList<Boolean>[] tedashi;
    int[] reach;
    int bakaze;
    int kyoku;
    int honba;
    int kyotaku;

    static final String[] bakazeStr = {"東", "南", "西"};
    static final String[] maStr = {"三", "四"};

    public Scene(boolean isSanma, int playerId, String[] players, int[] point, ArrayList<Integer>[] tehai, ArrayList<Naki>[] naki, ArrayList<Integer>[] dahai, ArrayList<Boolean>[] tedashi, int[] reach, int bakaze, int kyoku, int honba, int kyotaku) {
        this.isSanma = isSanma;
        this.playerId = playerId;
        this.players = players;
        this.point = point;
        this.tehai = tehai;
        this.naki = naki;
        this.dahai = dahai;
        this.tedashi = tedashi;
        this.reach = reach;
        this.bakaze = bakaze;
        this.kyoku = kyoku;
        this.honba = honba;
        this.kyotaku = kyotaku;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(maStr[isSanma ? 0 : 1]).append("]");
        sb.append(bakazeStr[bakaze]).append(kyoku).append("局");
        sb.append(honba).append("本場: ");

        int n = isSanma ? 3 : 4;
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(",");
            sb.append(players[i]).append("(").append(point[i]).append(")");
        }

        return sb.toString(); //TODO:変更
        // return String.join(",", players);
    }
}
