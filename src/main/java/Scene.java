import java.util.ArrayList;

/**
 * Created by m-yamamt on 2017/03/04.
 */
public class Scene {
    String[] players;
    ArrayList<Integer>[] tehai;
    ArrayList<Naki>[] naki;
    ArrayList<Integer>[] dahai;
    ArrayList<Boolean>[] tedashi;
    int[] reach;
    int bakaze;
    int kyoku;
    int honba;
    int kyotaku;

    public Scene(String[] players,
                 ArrayList<Integer>[] tehai,
                 ArrayList<Naki>[] naki,
                 ArrayList<Integer>[] dahai,
                 ArrayList<Boolean>[] tedashi,
                 int[] reach,
                 int bakaze,
                 int kyoku,
                 int honba,
                 int kyotaku) {
        this.players = players;
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
}
