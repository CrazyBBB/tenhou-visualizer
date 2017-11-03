package tenhouvisualizer.domain.analyzer;

import tenhouvisualizer.domain.model.Kyoku;
import tenhouvisualizer.domain.model.Naki;
import tenhouvisualizer.domain.model.MahjongScene;
import tenhouvisualizer.domain.MahjongUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

public class Analyzer implements IAnalyzer, SceneContainer {
    private final static String[] haiStr = {
            "1m", "1m", "1m", "1m", "2m", "2m", "2m", "2m", "3m", "3m", "3m", "3m",
            "4m", "4m", "4m", "4m", "赤5m", "5m", "5m", "5m", "6m", "6m", "6m", "6m",
            "7m", "7m", "7m", "7m", "8m", "8m", "8m", "8m", "9m", "9m", "9m", "9m",
            "1p", "1p", "1p", "1p", "2p", "2p", "2p", "2p", "3p", "3p", "3p", "3p",
            "4p", "4p", "4p", "4p", "赤5p", "5p", "5p", "5p", "6p", "6p", "6p", "6p",
            "7p", "7p", "7p", "7p", "8p", "8p", "8p", "8p", "9p", "9p", "9p", "9p",
            "1s", "1s", "1s", "1s", "2s", "2s", "2s", "2s", "3s", "3s", "3s", "3s",
            "4s", "4s", "4s", "4s", "赤5s", "5s", "5s", "5s", "6s", "6s", "6s", "6s",
            "7s", "7s", "7s", "7s", "8s", "8s", "8s", "8s", "9s", "9s", "9s", "9s",
            "東", "東", "東", "東", "南", "南", "南", "南",
            "西", "西", "西", "西", "北", "北", "北", "北",
            "白", "白", "白", "白", "發", "發", "發", "發", "中", "中", "中", "中"
    };
    private final static String[] kazeStr = {"東", "南", "西", "北"};

    private List<Kyoku> kyokuList = new ArrayList<>();
    private List<MahjongScene> mahjongScenes;

    private boolean isSanma = false;
    private String[] playerNames = new String[4];
    private int[] playerRates = new int[4];
    private String[] playerDans = new String[4];

    private int[] playerPoints = new int[4];
    private int[][] tehai = new int[4][34];
    private ArrayList<TreeSet<Integer>> stehai = new ArrayList<>(4);
    private ArrayList<ArrayList<Integer>> dahai = new ArrayList<>(4);
    private ArrayList<ArrayList<Boolean>> tedashi = new ArrayList<>(4);
    private ArrayList<ArrayList<Naki>> naki = new ArrayList<>(4);
    private int[] tsumo = new int[4];
    private int[] da = new int[4];
    private boolean daTedashi = false;
    private boolean daReach = false;
    private int[] reach = new int[4];
    private int[] kita = new int[4];
    private int bakaze = 0;
    private int kyoku = -1;
    private int honba = 0;
    private int kyotaku = 0;
    private int oya = 0;
    private int ma = 0;
    private ArrayList<Integer> doraDisplays;
    private int nokori;

    private int prev = -1;

    private int heroPosition;

    public Analyzer(int heroPosition) {
        this.heroPosition = heroPosition;
    }

    private void saveScene(int playerId, String str) {
        ArrayList<TreeSet<Integer>> tmpStehai = new ArrayList<>(4);
        ArrayList<ArrayList<Integer>> tmpDahai = new ArrayList<>(4);
        ArrayList<ArrayList<Boolean>> tmpTedashi = new ArrayList<>(4);
        ArrayList<ArrayList<Naki>> tmpNaki = new ArrayList<>(4);

        for (int i = 0; i < 4; i++) {
            tmpStehai.add(new TreeSet<>(stehai.get(i)));
            tmpDahai.add(new ArrayList<>(dahai.get(i)));
            tmpTedashi.add(new ArrayList<>(tedashi.get(i)));
            tmpNaki.add(new ArrayList<>(naki.get(i)));
        }

        mahjongScenes.add(new MahjongScene(
                isSanma,
                playerId,
                playerNames.clone(),
                playerDans.clone(),
                playerRates.clone(),
                playerPoints.clone(),
                tmpStehai,
                tmpNaki,
                tmpDahai,
                tmpTedashi,
                tsumo.clone(),
                da.clone(),
                daTedashi,
                daReach,
                reach.clone(),
                kita.clone(),
                bakaze,
                kyoku,
                honba,
                kyotaku,
                new ArrayList<>(doraDisplays),
                nokori,
                str));
    }

    @Override
    public List<Kyoku> getKyokusList() {
        return kyokuList;
    }

    @Override
    public void startGame(boolean isSanma, int taku, boolean isTonnan, boolean isSoku, boolean isUseAka, boolean isAriAri, String[] playerNames, int[] playerRates, String[] playerDans) {
        this.isSanma = isSanma;
        this.playerNames = playerNames;
        this.playerRates = playerRates;
        this.playerDans = playerDans;

        ma = isSanma ? 3 : 4;
    }

    @Override
    public void endGame(int[] playerPoints) {

    }

    @Override
    public void startKyoku(int[] playerPoints, ArrayList<ArrayList<Integer>> playerHaipais, int oya, int bakaze, int kyoku, int honba, int kyotaku, int firstDoraDisplay) {
        this.playerPoints = playerPoints;
        this.bakaze = bakaze;
        this.kyoku = kyoku;
        this.honba = honba;
        this.kyotaku = kyotaku;
        this.oya = oya;

        mahjongScenes = new ArrayList<>();

        stehai.clear();
        dahai.clear();
        tedashi.clear();
        naki.clear();
        for (int i = 0; i < 4; i++) {
            Arrays.fill(tehai[i], 0);
            stehai.add(new TreeSet<>());
            dahai.add(new ArrayList<>());
            tedashi.add(new ArrayList<>());
            naki.add(new ArrayList<>());
        }
        Arrays.fill(tsumo, -1);
        Arrays.fill(da, -1);
        Arrays.fill(reach, -1);
        Arrays.fill(kita, 0);
        doraDisplays = new ArrayList<>();
        doraDisplays.add(firstDoraDisplay);

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < playerHaipais.get(i).size(); j++) {
                int hai = playerHaipais.get(i).get(j);
                stehai.get(i).add(hai);
                tehai[i][hai / 4]++;
            }
        }

        nokori = isSanma ? 27 * 4 - 13 * 3 - 14 : 34 * 4 - 13 * 4 - 14;
    }

    @Override
    public void endKyoku() {

    }

    @Override
    public void draw(int position, int tsumoHai) {
        nokori--;
        tsumo[position] = tsumoHai;
        saveScene(heroPosition, kazeStr[(position - oya + ma) % ma] + "家 ツモ" + haiStr[tsumoHai]);

        tehai[position][tsumoHai / 4]++;
        prev = tsumoHai;
    }

    @Override
    public void discard(int position, int kiriHai) {
        tehai[position][kiriHai / 4]--;
        da[position] = kiriHai;
        daTedashi = prev != kiriHai;
        boolean isTenpai = MahjongUtils.computeSyanten(tehai[position], naki.get(position).size()) == 0;

        saveScene(heroPosition, kazeStr[(position - oya + ma) % ma] + "家 " +
                (daReach ? "リーチ" : "") + "打" + haiStr[kiriHai] + (isTenpai ? "*" : ""));

        if (tsumo[position] != -1) {
            stehai.get(position).add(tsumo[position]);
            Arrays.fill(tsumo, -1);
        }
        stehai.get(position).remove(kiriHai);
        dahai.get(position).add(kiriHai);
        tedashi.get(position).add(prev != kiriHai);
        Arrays.fill(da, -1);
        daReach = false;
    }

    @Override
    public void chow(int position, int from, int[] selfHai, int nakiHai) {
        int[] hai = {nakiHai, selfHai[0], selfHai[1]};
        naki.get(position).add(new Naki(hai, 0, from));

        for (int i = 0; i < 2; i++) {
            tehai[position][selfHai[i] / 4]--;
            stehai.get(position).remove(selfHai[i]);
        }

        saveScene(heroPosition, kazeStr[(position - oya + ma) % ma] + "家 チー");
        prev = -1;
    }

    @Override
    public void pong(int position, int from, int[] selfHai, int nakiHai) {
        int[] hai;
        if (from == 0) {
            hai = new int[]{nakiHai, selfHai[0], selfHai[1]};
        } else if (from == 1) {
            hai = new int[]{selfHai[0], nakiHai, selfHai[1]};
        } else if (from == 2) {
            hai = new int[]{selfHai[0], selfHai[1], nakiHai};
        } else {
            throw new RuntimeException();
        }
        naki.get(position).add(new Naki(hai, 1, from));

        for (int i = 0; i < 2; i++) {
            tehai[position][selfHai[i] / 4]--;
            stehai.get(position).remove(selfHai[i]);
        }
        saveScene(heroPosition, kazeStr[(position - oya + ma) % ma] + "家 ポン");
        prev = -1;
    }

    @Override
    public void ankan(int position, int[] selfHai) {
        stehai.get(position).add(tsumo[position]);
        Arrays.fill(tsumo, -1);
        naki.get(position).add(new Naki(selfHai, 2, -1));

        for (int i = 0; i < 4; i++) {
            tehai[position][selfHai[i] / 4]--;
            stehai.get(position).remove(selfHai[i]);
        }
        saveScene(heroPosition, kazeStr[(position - oya + ma) % ma] + "家 暗カン");
        prev = -1;
    }

    @Override
    public void minkan(int position, int from, int[] selfHai, int nakiHai) {
        int[] hai;
        if (from == 0) {
            hai = new int[]{nakiHai, selfHai[0], selfHai[1], selfHai[2]};
        } else if (from == 1) {
            hai = new int[]{selfHai[0], nakiHai, selfHai[1], selfHai[2]};
        } else if (from == 2) {
            hai = new int[]{selfHai[0], selfHai[1], selfHai[2], nakiHai};
        } else {
            throw new RuntimeException();
        }

        naki.get(position).add(new Naki(hai, 3, from == 2 ? from + 1 : from));

        for (int i = 0; i < 3; i++) {
            tehai[position][selfHai[i] / 4]--;
            stehai.get(position).remove(selfHai[i]);
        }
        saveScene(heroPosition, kazeStr[(position - oya + ma) % ma] + "家 明カン");
        prev = -1;
    }

    @Override
    public void kakan(int position, int from, int[] selfHai, int nakiHai, int addHai) {
        int[] hai;
        if (from == 0) {
            hai = new int[]{nakiHai, selfHai[0], selfHai[1], addHai};
        } else if (from == 1) {
            hai = new int[]{selfHai[0], nakiHai, selfHai[1], addHai};
        } else if (from == 2) {
            hai = new int[]{selfHai[0], selfHai[1], nakiHai, addHai};
        } else {
            throw new RuntimeException();
        }
        for (int i = 0; i < naki.get(position).size(); i++) {
            if (naki.get(position).get(i).hai[0] / 4 == addHai / 4) {
                naki.get(position).set(i, new Naki(hai, 4, from));
            }
        }

        tehai[position][addHai / 4]--;
        stehai.get(position).remove(addHai);
        saveScene(heroPosition, kazeStr[(position - oya + ma) % ma] + "家 加カン");
        prev = -1;
    }

    @Override
    public void kita(int position) {
        stehai.get(position).add(tsumo[position]);
        Arrays.fill(tsumo, -1);
        kita[position]++;

        tehai[position][30]--;
        for (int i = 120; i <= 123; i++) {
            if (stehai.get(position).contains(i)) {
                stehai.get(position).remove(i);
                break;
            }
        }
        saveScene(heroPosition, kazeStr[(position - oya + ma) % ma] + "家 キタ");
        prev = -1;
    }

    @Override
    public void reach(int position, int step) {
        if (step == 1) {
            reach[position] = dahai.get(position).size();
            daReach = true;
        } else {
            kyotaku++;
            playerPoints[position] -= 1000;
        }
    }

    @Override
    public void addDora(int newDoraDisplay) {
        doraDisplays.add(newDoraDisplay);
    }

    @Override
    public void agari(int position, int from, ArrayList<String> yaku, int han, int hu, int score, int[] increaseAndDecrease) {
        if (!mahjongScenes.isEmpty()) {
            String summary = kazeStr[bakaze] + kyoku + "局" + honba + "本場 " + playerNames[position];
            if (position == from) {
                summary += " ツモ " + String.join("", yaku) + " " + score + "点";
            } else {
                summary += " ロン " + String.join("", yaku) + " " + score + "点 " + playerNames[from];
            }
            kyokuList.add(new Kyoku(summary, new ArrayList<>(mahjongScenes)));
        }
    }

    @Override
    public void ryuukyoku() {
        if (!mahjongScenes.isEmpty()) {
            String summary = kazeStr[bakaze] + kyoku + "局" + honba + "本場 流局";
            kyokuList.add(new Kyoku(summary, new ArrayList<>(mahjongScenes)));
        }
    }
}
