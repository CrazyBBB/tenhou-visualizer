import java.util.ArrayList;

/**
 * Created by m-yamamt on 2017/03/04.
 */
public class Scene {
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

    static String[] bakazeStr = {"��", "��", "��"};

    public Scene(int playerId,
                 String[] players,
                 int[] point,
                 ArrayList<Integer>[] tehai,
                 ArrayList<Naki>[] naki,
                 ArrayList<Integer>[] dahai,
                 ArrayList<Boolean>[] tedashi,
                 int[] reach,
                 int bakaze,
                 int kyoku,
                 int honba,
                 int kyotaku) {
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
        String str = bakazeStr[bakaze] + kyoku + "��";
        str += honba + "�{��: ";
        str += String.join(",", players);
        return str; //TODO:�ύX
        // return String.join(",", players);
    }
}
