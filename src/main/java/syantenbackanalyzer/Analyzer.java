package syantenbackanalyzer;

import tenhouvisualizer.IAnalyzer;
import tenhouvisualizer.Naki;
import tenhouvisualizer.Scene;
import tenhouvisualizer.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class Analyzer implements IAnalyzer {

    private ArrayList<Scene> oriScenes = new ArrayList<>();

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
    private int[] reach = new int[4];
    private int[] kita = new int[4];
    private int bakaze = 0;
    private int kyoku = -1;
    private int honba = 0;
    private ArrayList<Integer> doraDisplays;
    private boolean used = false;

    private int prev = -1;

    private int heroPosition;

    Analyzer(int heroPosition) {
        this.heroPosition = heroPosition;
    }

    private void saveScene(int playerId) {
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

        oriScenes.add(new Scene(isSanma,
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
                reach.clone(),
                kita.clone(),
                bakaze,
                kyoku,
                honba,
                0,
                new ArrayList<>(doraDisplays)));
    }

    public ArrayList<Scene> getOriScenes() {
        return oriScenes;
    }

    @Override
    public void startGame(boolean isSanma, int taku, boolean isTonnan, boolean isSoku, boolean isUseAka, boolean isAriAri, String[] playerNames, int[] playerRates, String[] playerDans) {
        this.isSanma = isSanma;
        this.playerNames = playerNames;
        this.playerRates = playerRates;
        this.playerDans = playerDans;
    }

    @Override
    public void endGame(int[] playerPoints) {

    }

    @Override
    public void startKyoku(int[] playerPoints, ArrayList<ArrayList<Integer>> playerHaipais, int oya, int bakaze, int kyoku, int honba, int firstDoraDisplay) {
        this.playerPoints = playerPoints;
        this.bakaze = bakaze;
        this.kyoku = kyoku;
        this.honba = honba;

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
        used = false;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < playerHaipais.get(i).size(); j++) {
                int hai = playerHaipais.get(i).get(j);
                stehai.get(i).add(hai);
                tehai[i][hai / 4]++;
            }
        }
    }

    @Override
    public void endKyoku() {

    }

    @Override
    public void draw(int position, int tsumoHai) {
        stehai.get(position).add(tsumoHai);
        tehai[position][tsumoHai / 4]++;
        prev = tsumoHai;
    }

    @Override
    public void discard(int position, int kiriHai) {
        int beforeSyanten = 0;
        if (position == heroPosition && !used) {
            beforeSyanten = Utils.computeSyanten(tehai[position], naki.get(position).size());
        }

        stehai.get(position).remove(kiriHai);
        tehai[position][kiriHai / 4]--;
        da[position] = kiriHai;
        daTedashi = prev != kiriHai;

        if (position == heroPosition && !used) {
            int afterSyanten = Utils.computeSyanten(tehai[position], naki.get(position).size());
            if (beforeSyanten < afterSyanten) {
                saveScene(position);
                used = true;
            }
        }

        dahai.get(position).add(kiriHai);
        tedashi.get(position).add(prev != kiriHai);
        Arrays.fill(da, -1);
    }

    @Override
    public void chow(int position, int from, int[] selfHai, int nakiHai) {
        int[] hai = {nakiHai, selfHai[0], selfHai[1]};
        naki.get(position).add(new Naki(hai, 0, from));

        for (int i = 0; i < 2; i++) {
            tehai[position][selfHai[i] / 4]--;
            stehai.get(position).remove(selfHai[i]);
        }
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
        prev = -1;
    }

    @Override
    public void ankan(int position, int[] selfHai) {
        naki.get(position).add(new Naki(selfHai, 2, -1));

        for (int i = 0; i < 3; i++) {
            tehai[position][selfHai[i] / 4]--;
            stehai.get(position).remove(selfHai[i]);
        }
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
        prev = -1;
    }

    @Override
    public void kita(int position) {
        kita[position]++;

        tehai[position][30]--;
        for (int i = 120; i <= 123 ; i++) {
            if (stehai.get(position).contains(i)) {
                stehai.get(position).remove(i);
                break;
            }
        }
        prev = -1;
    }

    @Override
    public void reach(int position, int step) {
        if (step == 1) {
            reach[position] = dahai.get(position).size();
        } else {
            playerPoints[position] -= 1000;
        }
    }

    @Override
    public void addDora(int newDoraDisplay) {
        doraDisplays.add(newDoraDisplay);
    }

    @Override
    public void agari(int position, int from, ArrayList<String> yaku, int han, int hu, int score, int[] increaseAndDecrease) {

    }

    @Override
    public void ryuukyoku() {

    }
}
