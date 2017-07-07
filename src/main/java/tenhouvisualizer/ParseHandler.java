package tenhouvisualizer;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

public class ParseHandler extends DefaultHandler {

    private static final String[] danStr = {"新人", "９級", "８級", "７級", "６級", "５級", "４級", "３級", "２級", "１級",
            "初段", "二段", "三段", "四段", "五段", "六段", "七段", "八段", "九段", "十段", "天鳳"};

    private static final String[] yakuStr = {
            //// 一飜
            "門前清自摸和","立直","一発","槍槓","嶺上開花",
            "海底摸月","河底撈魚","平和","断幺九","一盃口",
            "自風 東","自風 南","自風 西","自風 北",
            "場風 東","場風 南","場風 西","場風 北",
            "役牌 白","役牌 發","役牌 中",
            //// 二飜
            "両立直","七対子","混全帯幺九","一気通貫","三色同順",
            "三色同刻","三槓子","対々和","三暗刻","小三元","混老頭",
            //// 三飜
            "二盃口","純全帯幺九","混一色",
            //// 六飜
            "清一色",
            //// 満貫
            "人和",
            //// 役満
            "天和","地和","大三元","四暗刻","四暗刻単騎","字一色",
            "緑一色","清老頭","九蓮宝燈","純正九蓮宝燈","国士無双",
            "国士無双１３面","大四喜","小四喜","四槓子",
            //// 懸賞役
            "ドラ","裏ドラ","赤ドラ"
    };

    private boolean isSanma;
    private int taku;
    private boolean isTonnan;
    private boolean isSoku;
    private boolean isUseAka;
    private boolean isAriAri;
    private String[] playerNames = new String[4];
    private int[] playerRates = new int[4];
    private String[] playerDans = new String[4];

    private int kyoku = -1;
    private int honba = 0;

    private IAnalyzer analyzer;

    ParseHandler(IAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public void startElement(String uri, String localName, String tagName, Attributes attributes) {
        if ("SHUFFLE".equals(tagName)) {
            visitSHUFFLE();
        } else if ("GO".equals(tagName)) {
            visitGO(attributes);
        } else if ("UN".equals(tagName)) {
            visitUN(attributes);
        } else if ("TAIKYOKU".equals(tagName)) {
            visitTAIKYOKU();
        } else if ("INIT".equals(tagName)) {
            visitINIT(attributes);
        } else if ("AGARI".equals(tagName)) {
            visitAGARI(attributes);
        } else if ("RYUUKYOKU".equals(tagName)) {
            visitRYUUKYOKU(attributes);
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

    private void visitSHUFFLE() {
        // no op
    }

    private void visitGO(Attributes attributes) {
        int type = Integer.parseInt(attributes.getValue("type"));
        isSanma     = (type & 0b00010000) != 0;
        boolean takuBit1    = (type & 0b00100000) != 0;
        boolean takuBit2    = (type & 0b10000000) != 0;
        if (takuBit1) {
            if (takuBit2) {
                taku = 3;
            } else {
                taku = 2;
            }
        } else {
            if (takuBit2) {
                taku = 1;
            } else {
                taku = 0;
            }
        }
        isTonnan    = (type & 0b00001000) != 0;
        isSoku      = (type & 0b01000000) != 0;
        isUseAka    = (type & 0b00000010) == 0;
        isAriAri    = (type & 0b00000100) == 0;
    }

    private void visitUN(Attributes attributes) {
        String danCsv = attributes.getValue("dan");

        // 復帰時のUN要素でない
        if (danCsv != null) {
            playerNames = new String[4];
            playerRates = new int[4];
            playerDans = new String[4];

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
        }
    }

    private void visitTAIKYOKU() {
        analyzer.startGame(isSanma, taku, isTonnan, isSoku, isUseAka, isAriAri, playerNames, playerRates, playerDans);
    }

    private void visitINIT(Attributes attributes) {
        int[] playerPoints = new int[4];
        String pointCsv = attributes.getValue("ten");
        String[] splitedPointCsv = pointCsv.split(",");
        for (int j = 0; j < 4; j++) {
            playerPoints[j] = Integer.valueOf(splitedPointCsv[j]) * 100;
        }

        int oya = Integer.parseInt(attributes.getValue("oya"));

        String seedCsv = attributes.getValue("seed");
        String[] splitedSeedCsv = seedCsv.split(",");
        int seedElementFirst = Integer.valueOf(splitedSeedCsv[0]);
        if (seedElementFirst % 4 + 1 == kyoku) {
            honba++;
        } else {
            honba = 0;
        }
        int bakaze = seedElementFirst / 4;
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

        analyzer.startKyoku(playerPoints, playerHaipais, oya, bakaze, kyoku, honba, firstDora);
    }

    private void visitTUVW(String tagName) {
        int position = tagName.charAt(0) - 'T';
        int tsumoHai = Integer.parseInt(tagName.substring(1));
        analyzer.draw(position, tsumoHai);
    }

    private void visitDEFG(String tagName) {
        int position = tagName.charAt(0) - 'D';
        int kiriHai = Integer.parseInt(tagName.substring(1));
        analyzer.discard(position, kiriHai);
    }

    private void visitN(Attributes attributes) {
        int m = Integer.parseInt(attributes.getValue("m"));
        int position = Integer.parseInt(attributes.getValue("who"));

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
    }

    private void visitREACH(Attributes attributes) {
        int position = Integer.parseInt(attributes.getValue("who"));
        int step = Integer.parseInt(attributes.getValue("step"));
        analyzer.reach(position, step);
    }

    private void visitDORA(Attributes attributes) {
        int newDora = Integer.parseInt(attributes.getValue("hai"));
        analyzer.addDora(newDora);
    }

    private void visitAGARI(Attributes attributes) {
        analyzer.agari();
        analyzer.endKyoku();

        String owariCsv = attributes.getValue("owari");
        owari(owariCsv);
    }

    private void visitRYUUKYOKU(Attributes attributes) {
        analyzer.ryuukyoku();
        analyzer.endKyoku();

        String owariCsv = attributes.getValue("owari");
        owari(owariCsv);
    }

    private void owari(String owariCsv) {
        if (owariCsv != null) {
            String[] splitedOwariCsv = owariCsv.split(",");
            int[] playerPoints = new int[4];
            for (int i = 0; i < 4; i++) {
                playerPoints[i] = Integer.parseInt(splitedOwariCsv[i]) * 100;
            }

            analyzer.endGame(playerPoints);
        }
    }
}
