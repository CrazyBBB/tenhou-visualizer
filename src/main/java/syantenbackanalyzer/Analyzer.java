package syantenbackanalyzer;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import tenhouvisualizer.MjlogFile;
import tenhouvisualizer.Naki;
import tenhouvisualizer.Scene;
import tenhouvisualizer.Utils;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class Analyzer extends DefaultHandler {

    final String[] danStr = {"新人", "９級", "８級", "７級", "６級", "５級", "４級", "３級", "２級", "１級", "初段", "二段", "三段", "四段", "五段", "六段", "七段", "八段", "九段", "十段", "天鳳"};

    ArrayList<Scene> oriScenes = new ArrayList<>();

    String[] players = new String[4];
    boolean isSanma = false;
    String[] dan = new String[4];
    int[] rate = new int[4];

    int[] point = new int[4];
    int[][] tehai = new int[4][34];
    TreeSet<Integer>[] stehai = new TreeSet[4];
    ArrayList<Integer>[] dahai = new ArrayList[4];
    ArrayList<Boolean>[] tedashi = new ArrayList[4];
    ArrayList<Naki>[] naki = new ArrayList[4];
    int[] reach = new int[4];
    int[] kita = new int[4];
    int bakaze = 0;
    int kyoku = -1;
    int honba = 0;
    ArrayList<Integer> dora;

    boolean saved = false;

    int prev = -1;

    MjlogFile.Position position;

    public Analyzer(MjlogFile.Position position) {
        this.position = position;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        if ("SHUFFLE".equals(qName)) {
            // nop
        } else if ("GO".equals(qName)) {
            analyzeGO(attributes);
        } else if ("UN".equals(qName)) {
            try {
                analyzeUN(attributes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if ("TAIKYOKU".equals(qName)) {
            // nop
        } else if ("INIT".equals(qName)) {
            analyzeINIT(attributes);
        } else if ("AGARI".equals(qName)) {

        } else if ("RYUUKYOKU".equals(qName)) {

        } else if ("N".equals(qName)) {
            analyzeN(attributes);
        } else if (qName.matches("[T-W]\\d+")) {
            analyzeT(qName);
        } else if (qName.matches("[D-G]\\d+")) {
            analyzeD(qName);
        } else if ("REACH".equals(qName)) {
            analyzeREACH(attributes);
        } else if ("DORA".equals(qName)) {
            analyzeDORA(attributes);
        }
    }

    private void analyzeGO(Attributes attributes) {
        int type = Integer.parseInt(attributes.getValue("type"));
        isSanma = (type & 0x10) != 0;
    }

    private void analyzeUN(Attributes attributes) throws IOException {
        String danCsv = attributes.getValue("dan");

        // 復帰時のUN要素でない
        if (danCsv != null) {
            String[] splitedDanCsv = danCsv.split(",");
            for (int j = 0; j < 4; j++) {
                dan[j] = danStr[Integer.valueOf(splitedDanCsv[j])];
            }

            String rateCsv = attributes.getValue("rate");
            String[] splitedRateCsv = rateCsv.split(",");
            for (int j = 0; j < 4; j++) {
                rate[j] = Float.valueOf(splitedRateCsv[j]).intValue();
            }

            for (int i = 0; i < 4; i++) {
                String name = URLDecoder.decode(attributes.getValue("n" + i), "UTF-8");
                players[i] = name;
            }
        }
    }

    private void analyzeINIT(Attributes attributes) {
        for (int i = 0; i < 4; i++) {
            Arrays.fill(tehai[i], 0);
            stehai[i] = new TreeSet<>();
            dahai[i] = new ArrayList<>();
            tedashi[i] = new ArrayList<>();
            naki[i] = new ArrayList<>();
        }
        Arrays.fill(reach, -1);
        Arrays.fill(kita, 0);
        saved = false;
        dora = new ArrayList<>();

        String tenCsv = attributes.getValue("ten");
        String[] splitedTenCsv = tenCsv.split(",");
        for (int j = 0; j < 4; j++) {
            point[j] = Integer.valueOf(splitedTenCsv[j]) * 100;
        }

        String seedCsv = attributes.getValue("seed");
        String[] splitedSeedCsv = seedCsv.split(",");
        int seed0 = Integer.valueOf(splitedSeedCsv[0]);
        if (seed0 % 4 + 1 == kyoku) {
            honba++;
        } else {
            honba = 0;
        }
        bakaze = seed0 / 4;
        kyoku = seed0 % 4 + 1;
        int seed5 = Integer.valueOf(splitedSeedCsv[5]);
        dora.add(seed5);

        for (int i = 0; i < 4; i++) {
            String haiCsv = attributes.getValue("hai" + i);
            if ("".equals(haiCsv)) continue;

            String[] splitedHaiCsv = haiCsv.split(",");
            for (int j = 0; j < 13; j++) {
                int hai = Integer.parseInt(splitedHaiCsv[j]);
                stehai[i].add(hai);
                tehai[i][hai / 4]++;
            }
        }
    }

    private void analyzeT(String qName) {
        int playerId = qName.charAt(0) - 'T';
        int hai = Integer.parseInt(qName.substring(1));
        stehai[playerId].add(hai);
        tehai[playerId][hai / 4]++;
        prev = hai;
    }

    private void analyzeD(String qName) {
        int playerId = qName.charAt(0) - 'D';
        int beforeSyanten = 0;
        if (MjlogFile.Position.values()[playerId] == position && !saved) {
            beforeSyanten = Utils.computeSyanten(tehai[playerId], naki[playerId].size());
        }

        int hai = Integer.parseInt(qName.substring(1));
        stehai[playerId].remove(hai);
        tehai[playerId][hai / 4]--;
        dahai[playerId].add(hai);
        tedashi[playerId].add(prev != hai);

        if (MjlogFile.Position.values()[playerId] == position && !saved) {
            int afterSyanten = Utils.computeSyanten(tehai[playerId], naki[playerId].size());
            if (beforeSyanten < afterSyanten) {
                saveScene(playerId);
                saved = true;
            }
        }
    }

    private void analyzeN(Attributes attributes) {
        int m = Integer.parseInt(attributes.getValue("m"));
        int who = Integer.parseInt(attributes.getValue("who"));

        int kui = m & 3;
        if ((m >> 2 & 1) == 1) {
            int t = (m >> 10) & 63;
            int r = t % 3;

            t /= 3;
            t = t / 7 * 9 + t % 7;
            t *= 4;

            int[] h = new int[3];
            h[0] = t + 4 * 0 + ((m >> 3) & 3);
            h[1] = t + 4 * 1 + ((m >> 5) & 3);
            h[2] = t + 4 * 2 + ((m >> 7) & 3);

            int[] hai = null;
            if (r == 0) {
                hai = new int[]{h[0], h[1], h[2]};
            } else if (r == 1) {
                hai = new int[]{h[1], h[0], h[2]};
            } else if (r == 2) {
                hai = new int[]{h[2], h[0], h[1]};
            }

            naki[who].add(new Naki(hai, 0, 3 - kui));

            for (int i = 0; i < 3; i++) {
                if (i != r) {
                    tehai[who][h[i] / 4]--;
                    stehai[who].remove(h[i]);
                }
            }
        } else if ((m >> 3 & 1) == 1) {
            int unused = (m >> 5) & 3;
            int t = (m >> 9) & 127;
            int r = t % 3;

            t /= 3;
            t *= 4;

            int[] h = new int[3];
            int idx = 0;
            for (int i = 0; i < 4; i++) {
                if (i != unused) {
                    h[idx++] = t + i;
                }
            }

            int[] hai = new int[3];
            for (int i = 0; i < 3; i++) {
                hai[(3 - kui + i) % 3] = h[(r + i) % 3];
            }

            naki[who].add(new Naki(hai, 1, 3 - kui));

            for (int i = 0; i < 3; i++) {
                if (i != r) {
                    tehai[who][h[i] / 4]--;
                    stehai[who].remove(h[i]);
                }
            }
        } else if ((m >> 4 & 1) == 1) {

        } else if ((m >> 5 & 1) == 1) {
            kita[who]++;

            tehai[who][30]--;
            for (int i = 120; i <= 123 ; i++) {
                if (stehai[who].contains(i)) {
                    stehai[who].remove(i);
                    break;
                }
            }
        } else if (kui == 0) {
            int t = (m >> 8) & 255;

            t = t / 4 * 4;

            int[] hai = {t + 1, t, t + 2, t + 3};

            naki[who].add(new Naki(hai, 2, -1));

            for (int i = 0; i < 4; i++) {
                tehai[who][hai[i] / 4]--;
                stehai[who].remove(hai[i]);
            }
        } else {

        }
        prev = -1;
    }

    private void analyzeREACH(Attributes attributes) {
        int step = Integer.parseInt(attributes.getValue("step"));
        int who = Integer.parseInt(attributes.getValue("who"));
        if (step == 1) {
            reach[who] = dahai[who].size();
        } else {
            point[who] -= 1000;
        }
    }

    private void analyzeDORA(Attributes attributes) {
        int hai = Integer.parseInt(attributes.getValue("hai"));
        dora.add(hai);
    }

    private void saveScene(int playerId) {
        TreeSet<Integer>[] tmpStehai = new TreeSet[4];
        ArrayList<Integer>[] tmpDahai = new ArrayList[4];
        ArrayList<Boolean>[] tmpTedashi = new ArrayList[4];
        ArrayList<Naki>[] tmpNaki = new ArrayList[4];

        for (int i = 0; i < 4; i++) {
            tmpStehai[i] = new TreeSet<>(stehai[i]);
            tmpDahai[i] = new ArrayList<>(dahai[i]);
            tmpTedashi[i] = new ArrayList<>(tedashi[i]);
            tmpNaki[i] = new ArrayList<>(naki[i]);
        }

        oriScenes.add(new Scene(isSanma,
                playerId,
                players.clone(),
                dan.clone(),
                rate.clone(),
                point.clone(),
                tmpStehai,
                tmpNaki,
                tmpDahai,
                tmpTedashi,
                reach.clone(),
                kita.clone(),
                bakaze,
                kyoku,
                honba,
                0,
                new ArrayList<>(dora)));
    }

    public ArrayList<Scene> getOriScenes() {
        return oriScenes;
    }
}
