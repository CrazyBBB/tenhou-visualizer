package tenhouvisualizer;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class ParseHandler extends DefaultHandler {

    private static final String[] danStr = {"新人", "９級", "８級", "７級", "６級", "５級", "４級", "３級", "２級", "１級",
            "初段", "二段", "三段", "四段", "五段", "六段", "七段", "八段", "九段", "十段", "天鳳"};

    private int kyoku = -1;
    private int honba = 0;

    private IAnalyzer analyzer;

    ParseHandler(IAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public void startElement(String uri, String localName, String tagName, Attributes attributes) {
        if ("SHUFFLE".equals(tagName)) {
            visitSHUFFLE(attributes);
        } else if ("GO".equals(tagName)) {
            visitGO(attributes);
        } else if ("UN".equals(tagName)) {
            visitUN(attributes);
        } else if ("TAIKYOKU".equals(tagName)) {
            visitTAIKYOKU(attributes);
        } else if ("INIT".equals(tagName)) {
            visitINIT(attributes);
        } else if ("AGARI".equals(tagName)) {

        } else if ("RYUUKYOKU".equals(tagName)) {

        } else if ("N".equals(tagName)) {
            visitN(attributes);
        } else if (tagName.matches("[T-W]\\d+")) {
            visitTUVW(tagName);
        } else if (tagName.matches("[D-G]\\d+")) {
            visitDEFG(tagName);
        } else if ("REACH".equals(tagName)) {
            visitREACH(attributes);
        } else if ("DORA".equals(tagName)) {
            visitDORA(attributes);
        }
    }

    @Override
    public void endElement(String uri, String localName, String tagName) {
        if ("mjlogjm".equals(tagName)) {

        }
    }

    private void visitSHUFFLE(Attributes attributes) {
        analyzer.analyzeSHUFFLE(attributes.getValue("seed"));
    }

    private void visitGO(Attributes attributes) {
        int type = Integer.parseInt(attributes.getValue("type"));
        boolean isSanma     = (type & 0b00010000) != 0;
        boolean takuBit1    = (type & 0b00100000) != 0;
        boolean takuBit2    = (type & 0b10000000) != 0;
        Utils.Taku taku;
        if (takuBit1) {
            if (takuBit2) {
                taku = Utils.Taku.HOUOU;
            } else {
                taku = Utils.Taku.TOKUJOU;
            }
        } else {
            if (takuBit2) {
                taku = Utils.Taku.JOU;
            } else {
                taku = Utils.Taku.PAN;
            }
        }
        boolean isTonnan    = (type & 0b00001000) != 0;
        boolean isSoku      = (type & 0b01000000) != 0;
        boolean isUseAka    = (type & 0b00000010) == 0;
        boolean isAriAri    = (type & 0b00000100) == 0;
        analyzer.analyzeGO(isSanma, taku, isTonnan, isSoku, isUseAka, isAriAri);
    }

    private void visitUN(Attributes attributes) {
        String danCsv = attributes.getValue("dan");

        // 復帰時のUN要素でない
        if (danCsv != null) {
            String[] playerNames = new String[4];
            int[] playerRates = new int[4];
            String[] playerDans = new String[4];

            for (int i = 0; i < 4; i++) {
                try {
                    String name = URLDecoder.decode(attributes.getValue("n" + i), "UTF-8");
                    playerNames[i] = name;
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }

            String rateCsv = attributes.getValue("rate");
            String[] splitedRateCsv = rateCsv.split(",");
            for (int j = 0; j < 4; j++) {
                playerRates[j] = Float.valueOf(splitedRateCsv[j]).intValue();
            }

            String[] splitedDanCsv = danCsv.split(",");
            for (int j = 0; j < 4; j++) {
                playerDans[j] = danStr[Integer.valueOf(splitedDanCsv[j])];
            }

            analyzer.analyzeUN(playerNames, playerRates, playerDans);
        }
    }

    private void visitTAIKYOKU(Attributes attributes) {
        int p = Integer.parseInt(attributes.getValue("oya"));
        Utils.KAZE oya = Utils.KAZE.values()[p];
        analyzer.analyzeTAIKYOKU(oya);
    }

    private void visitINIT(Attributes attributes) {
        int[] playerPoints = new int[4];
        String pointCsv = attributes.getValue("ten");
        String[] splitedPointCsv = pointCsv.split(",");
        for (int j = 0; j < 4; j++) {
            playerPoints[j] = Integer.valueOf(splitedPointCsv[j]) * 100;
        }

        int p = Integer.parseInt(attributes.getValue("oya"));
        Utils.KAZE oya = Utils.KAZE.values()[p];

        String seedCsv = attributes.getValue("seed");
        String[] splitedSeedCsv = seedCsv.split(",");
        int seedElementFirst = Integer.valueOf(splitedSeedCsv[0]);
        if (seedElementFirst % 4 + 1 == kyoku) {
            honba++;
        } else {
            honba = 0;
        }
        Utils.KAZE bakaze = Utils.KAZE.values()[seedElementFirst / 4];
        kyoku = seedElementFirst % 4 + 1;
        int firstDora = Integer.valueOf(splitedSeedCsv[5]);

        ArrayList<ArrayList<Integer>> playerHaipais = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            playerHaipais.add(new ArrayList<>());

            String haiCsv = attributes.getValue("hai" + i);
            if ("".equals(haiCsv)) continue;

            String[] splitedHaiCsv = haiCsv.split(",");
            for (int j = 0; j < 13; j++) {
                int hai = Integer.parseInt(splitedHaiCsv[j]);
                playerHaipais.get(i).add(hai);
            }
        }

        analyzer.analyzeINIT(playerPoints, playerHaipais, oya, bakaze, kyoku, honba, firstDora);
    }

    private void visitTUVW(String tagName) {
        Utils.KAZE position = Utils.KAZE.values()[tagName.charAt(0) - 'T'];
        int tsumoHai = Integer.parseInt(tagName.substring(1));
        analyzer.analyzeTUVW(position, tsumoHai);
    }

    private void visitDEFG(String tagName) {
        Utils.KAZE position = Utils.KAZE.values()[tagName.charAt(0) - 'D'];
        int kiriHai = Integer.parseInt(tagName.substring(1));
        analyzer.analyzeDEFG(position, kiriHai);
    }

    private void visitN(Attributes attributes) {
        int m = Integer.parseInt(attributes.getValue("m"));
        Utils.KAZE position = Utils.KAZE.values()[Integer.parseInt(attributes.getValue("who"))];

        Naki naki = null;
        boolean isKita = false;

        int nakiFrom = 3 - (m & 3); // 0: 上家, 1: 対面, 2: 下家, (3: 暗槓を表す)
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

            naki = new Naki(hai, 0, nakiFrom);
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
                hai[(nakiFrom + i) % 3] = h[(r + i) % 3];
            }

            naki = new Naki(hai, 1, nakiFrom);
        } else if ((m >> 4 & 1) == 1) {
            // 加槓
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

            int[] hai = new int[4];
            for (int i = 0; i < 3; i++) {
                hai[(nakiFrom + i) % 3] = h[(r + i) % 3];
            }
            hai[3] = t + unused;

            naki = new Naki(hai, 4, nakiFrom);
        } else if ((m >> 5 & 1) == 1) {
            // 北
            isKita = true;
        } else if (nakiFrom == 3) {
            // 暗槓
            int t = (m >> 8) & 255;

            t = t / 4 * 4;

            int[] hai = {t + 1, t, t + 2, t + 3};

            naki = new Naki(hai, 2, -1);
        } else {
            // 明槓
            int t = (m >> 8) & 255; // 鳴いた牌

            int t2 = t / 4 * 4;

            if (nakiFrom == 2) nakiFrom++;

            int[] hai = new int[4];
            int idx = 0;
            for (int i = 0; i < 4; i++) {
                if (i == nakiFrom) {
                    hai[i] = t;
                } else {
                    if (t2 + idx == t) idx++;
                    hai[i] = t2 + idx;
                    idx++;
                }
            }

            naki = new Naki(hai, 3, nakiFrom);
        }

        analyzer.analyzeN(position, isKita, naki);
    }

    private void visitREACH(Attributes attributes) {
        Utils.KAZE position = Utils.KAZE.values()[Integer.parseInt(attributes.getValue("who"))];
        int step = Integer.parseInt(attributes.getValue("step"));
        analyzer.analyzeREACH(position, step);
    }

    private void visitDORA(Attributes attributes) {
        int newDora = Integer.parseInt(attributes.getValue("hai"));
        analyzer.analyzeDORA(newDora);
    }
}
