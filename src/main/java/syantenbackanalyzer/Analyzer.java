package syantenbackanalyzer;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import tenhouvisualizer.Naki;
import tenhouvisualizer.Scene;
import tenhouvisualizer.Utils;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class Analyzer extends DefaultHandler {

    private final String[] danStr = {"新人", "９級", "８級", "７級", "６級", "５級", "４級", "３級", "２級", "１級", "初段", "二段", "三段", "四段", "五段", "六段", "七段", "八段", "九段", "十段", "天鳳"};

    private ArrayList<Scene> oriScenes = new ArrayList<>();

    private String[] players = new String[4];
    private boolean isSanma = false;
    private String[] dan = new String[4];
    private int[] rate = new int[4];

    private int[] point = new int[4];
    private int[][] tehai = new int[4][34];
    private ArrayList<TreeSet<Integer>> stehai = new ArrayList<>(4);
    private ArrayList<ArrayList<Integer>> dahai = new ArrayList<>(4);
    private ArrayList<ArrayList<Boolean>> tedashi = new ArrayList<>(4);
    private ArrayList<ArrayList<Naki>> naki = new ArrayList<>(4);
    private int[] reach = new int[4];
    private int[] kita = new int[4];
    private int bakaze = 0;
    private int kyoku = -1;
    private int honba = 0;
    private ArrayList<Integer> dora;

    private boolean saved = false;

    private int prev = -1;

    private Utils.KAZE position;

    Analyzer(Utils.KAZE position) {
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
                stehai.get(i).add(hai);
                tehai[i][hai / 4]++;
            }
        }
    }

    private void analyzeT(String qName) {
        int playerId = qName.charAt(0) - 'T';
        int hai = Integer.parseInt(qName.substring(1));
        stehai.get(playerId).add(hai);
        tehai[playerId][hai / 4]++;
        prev = hai;
    }

    private void analyzeD(String qName) {
        int playerId = qName.charAt(0) - 'D';
        int beforeSyanten = 0;
        if (Utils.KAZE.values()[playerId] == position && !saved) {
            beforeSyanten = Utils.computeSyanten(tehai[playerId], naki.get(playerId).size());
        }

        int hai = Integer.parseInt(qName.substring(1));
        stehai.get(playerId).remove(hai);
        tehai[playerId][hai / 4]--;
        dahai.get(playerId).add(hai);
        tedashi.get(playerId).add(prev != hai);

        if (Utils.KAZE.values()[playerId] == position && !saved) {
            int afterSyanten = Utils.computeSyanten(tehai[playerId], naki.get(playerId).size());
            if (beforeSyanten < afterSyanten) {
                saveScene(playerId);
                saved = true;
            }
        }
    }

    private void analyzeN(Attributes attributes) {
        int m = Integer.parseInt(attributes.getValue("m"));
        int who = Integer.parseInt(attributes.getValue("who"));

        int kui = 3 - (m & 3); // 0: 上家, 1: 対面, 2: 下家, (3: 暗槓を表す)
        if ((m >> 2 & 1) == 1) {
            // 順子
            int t = (m >> 10) & 63;
            int r = t % 3;

            t /= 3;
            t = t / 7 * 9 + t % 7;
            t *= 4;

            int[] h = new int[3];
            h[0] = t + ((m >> 3) & 3);
            h[1] = t + 4 + ((m >> 5) & 3);
            h[2] = t + 8 + ((m >> 7) & 3);

            int[] hai = null;
            if (r == 0) {
                hai = new int[]{h[0], h[1], h[2]};
            } else if (r == 1) {
                hai = new int[]{h[1], h[0], h[2]};
            } else if (r == 2) {
                hai = new int[]{h[2], h[0], h[1]};
            }

            naki.get(who).add(new Naki(hai, 0, kui));

            for (int i = 0; i < 3; i++) {
                if (i != r) {
                    tehai[who][h[i] / 4]--;
                    stehai.get(who).remove(h[i]);
                }
            }
        } else if ((m >> 3 & 1) == 1) {
            // 刻子
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
                hai[(kui + i) % 3] = h[(r + i) % 3];
            }

            naki.get(who).add(new Naki(hai, 1, kui));

            for (int i = 0; i < 3; i++) {
                if (i != r) {
                    tehai[who][h[i] / 4]--;
                    stehai.get(who).remove(h[i]);
                }
            }
        } else if ((m >> 4 & 1) == 1) {
            // 加槓
            int unused = (m >> 5) & 3;
            int t = (m >> 9) & 127;

            t /= 3;
            t *= 4;

            for (int i = 0; i < naki.get(who).size(); i++) {
                if (naki.get(who).get(i).hai[0] / 4 == t / 4) {
                    int[] hai = {naki.get(who).get(i).hai[0], naki.get(who).get(i).hai[1], naki.get(who).get(i).hai[2], t + unused};
                    int nakiIdx = naki.get(who).get(i).nakiIdx;
                    naki.get(who).set(i, new Naki(hai, 4, nakiIdx));
                }
            }

            tehai[who][t / 4]--;
            stehai.get(who).remove(t + unused);
        } else if ((m >> 5 & 1) == 1) {
            // 北
            kita[who]++;

            tehai[who][30]--;
            for (int i = 120; i <= 123 ; i++) {
                if (stehai.get(who).contains(i)) {
                    stehai.get(who).remove(i);
                    break;
                }
            }
        } else if (kui == 3) {
            // 暗槓
            int t = (m >> 8) & 255;

            t = t / 4 * 4;

            int[] hai = {t + 1, t, t + 2, t + 3};

            naki.get(who).add(new Naki(hai, 2, -1));

            for (int i = 0; i < 4; i++) {
                tehai[who][hai[i] / 4]--;
                stehai.get(who).remove(hai[i]);
            }
        } else {
            // 明槓
            int t = (m >> 8) & 255; // 鳴いた牌

            int t2 = t / 4 * 4;

            if (kui == 2) kui++;

            int[] hai = new int[4];
            int idx = 0;
            for (int i = 0; i < 4; i++) {
                if (i == kui) {
                    hai[i] = t;
                } else {
                    if (t2 + idx == t) idx++;
                    hai[i] = t2 + idx;
                    idx++;
                }
            }

            naki.get(who).add(new Naki(hai, 3, kui));

            for (int i = 0; i < 4; i++) {
                tehai[who][hai[i] / 4]--;
                stehai.get(who).remove(hai[i]);
            }
        }
        prev = -1;
    }

    private void analyzeREACH(Attributes attributes) {
        int step = Integer.parseInt(attributes.getValue("step"));
        int who = Integer.parseInt(attributes.getValue("who"));
        if (step == 1) {
            reach[who] = dahai.get(who).size();
        } else {
            point[who] -= 1000;
        }
    }

    private void analyzeDORA(Attributes attributes) {
        int hai = Integer.parseInt(attributes.getValue("hai"));
        dora.add(hai);
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

    ArrayList<Scene> getOriScenes() {
        return oriScenes;
    }
}
