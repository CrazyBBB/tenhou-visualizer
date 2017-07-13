package tenhouvisualizer.domain.analyzer;

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
            "自風東","自風南","自風西","自風北",
            "場風東","場風南","場風西","場風北",
            "役牌白","役牌發","役牌中",
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

    public ParseHandler(IAnalyzer analyzer) {
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
        int firstDoraDisplay = Integer.valueOf(splitedSeedCsv[5]);

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

        analyzer.startKyoku(playerPoints, playerHaipais, oya, bakaze, kyoku, honba, firstDoraDisplay);
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

        if ((m >> 2 & 1) == 1) {
            // チー
            parseChow(position, m);
        } else if ((m >> 3 & 1) == 1) {
            // ポン
            parsePong(position, m);
        } else if ((m >> 4 & 1) == 1) {
            // 加カン
            parseKakan(position, m);
        } else if ((m >> 5 & 1) == 1) {
            // 北
            parseKita(position);
        } else if ((m & 3) == 0) {
            // 暗カン
            parseAnkan(position, m);
        } else {
            // 明カン
            parseMinkan(position, m);
        }
    }

    private void parseChow(int position, int m) {
        int from = 3 - (m & 3); // 0: 上家, 1: 対面, 2: 下家, (3: 暗カンを表す)
        int tmp = (m >> 10) & 63;
        int r = tmp % 3; // 下から何番目の牌を鳴いたか

        tmp /= 3;
        tmp = tmp / 7 * 9 + tmp % 7;
        tmp *= 4; // 一番下の牌

        int[] h = new int[3];
        h[0] = tmp + ((m >> 3) & 3);
        h[1] = tmp + 4 + ((m >> 5) & 3);
        h[2] = tmp + 8 + ((m >> 7) & 3);

        int[] selfHai;
        int nakiHai;
        if (r == 0) {
            selfHai = new int[]{h[1], h[2]};
            nakiHai = h[0];
        } else if (r == 1) {
            selfHai = new int[]{h[0], h[2]};
            nakiHai = h[1];
        } else if (r == 2) {
            selfHai = new int[]{h[0], h[1]};
            nakiHai = h[2];
        } else {
            throw new RuntimeException();
        }

        analyzer.chow(position, from, selfHai, nakiHai);
    }

    private void parsePong(int position, int m) {
        int from = 3 - (m & 3); // 0: 上家, 1: 対面, 2: 下家, (3: 暗カンを表す)

        int unused = (m >> 5) & 3;
        int tmp = (m >> 9) & 127;
        int r = tmp % 3;

        tmp /= 3;
        tmp *= 4;

        int[] selfHai = new int[2];
        int count = 0;
        int idx = 0;
        for (int i = 0; i < 4; i++) {
            if (i == unused) continue;
            if (count != r) {
                selfHai[idx++] = tmp + i;
            }
            count++;
        }
        int nakiHai = tmp + r;

        analyzer.pong(position, from, selfHai, nakiHai);
    }

    private void parseKakan(int position, int m) {
        int from = 3 - (m & 3); // 0: 上家, 1: 対面, 2: 下家, (3: 暗カンを表す)
        int unused = (m >> 5) & 3;
        int tmp = (m >> 9) & 127;
        int r = tmp % 3;

        tmp /= 3;
        tmp *= 4;

        int[] selfHai = new int[2];
        int count = 0;
        int idx = 0;
        for (int i = 0; i < 4; i++) {
            if (i == unused) continue;
            if (count != r) {
                selfHai[idx++] = tmp + i;
            }
            count++;
        }
        int nakiHai = tmp + r;
        int addHai = tmp + unused;
        analyzer.kakan(position, from, selfHai, nakiHai, addHai);
    }

    private void parseKita(int position) {
        analyzer.kita(position);
    }

    private void parseAnkan(int position, int m) {
        int tmp = (m >> 8) & 255;

        tmp = tmp / 4 * 4;

        int[] selfHai = {tmp + 1, tmp, tmp + 2, tmp + 3};

        analyzer.ankan(position, selfHai);
    }

    private void parseMinkan(int position, int m) {
        int from = 3 - (m & 3); // 0: 上家, 1: 対面, 2: 下家, (3: 暗カンを表す)
        int nakiHai = (m >> 8) & 255; // 鳴いた牌

        int haiFirst = nakiHai / 4 * 4;

        int[] selfHai = new int[3];
        int idx = 0;
        for (int i = 0; i < 3; i++) {
            if (haiFirst + idx == nakiHai) idx++;
            selfHai[i] = haiFirst + idx;
            idx++;
        }

        analyzer.minkan(position, from, selfHai, nakiHai);
    }

    private void visitREACH(Attributes attributes) {
        int position = Integer.parseInt(attributes.getValue("who"));
        int step = Integer.parseInt(attributes.getValue("step"));
        analyzer.reach(position, step);
    }

    private void visitDORA(Attributes attributes) {
        int newDoraDisplay = Integer.parseInt(attributes.getValue("hai"));
        analyzer.addDora(newDoraDisplay);
    }

    private void visitAGARI(Attributes attributes) {
        int position = Integer.parseInt(attributes.getValue("who"));
        int from = Integer.parseInt(attributes.getValue("fromWho"));
        String scoreCsv = attributes.getValue("ten");
        String[] splitedScoreCsv = scoreCsv.split(",");
        int hu = Integer.parseInt(splitedScoreCsv[0]);
        int score = Integer.parseInt(splitedScoreCsv[1]);
        int han = 0;
        ArrayList<String> yaku = new ArrayList<>();
        String yakuCsv = attributes.getValue("yaku");
        if (yakuCsv != null) {
            String[] splitedYakuCsv = yakuCsv.split(",");
            for (int i = 0; i < splitedYakuCsv.length; i += 2) {
                int yakuId = Integer.parseInt(splitedYakuCsv[i]);
                int n = Integer.parseInt(splitedYakuCsv[i + 1]);
                if (yakuId < 52) {
                    yaku.add(yakuStr[yakuId]);
                } else {
                    yaku.add(yakuStr[yakuId] + (n >= 2 ? n : ""));
                }
                han += n;
            }
        }
        String yakumanCsv = attributes.getValue("yakuman");
        if (yakumanCsv != null) {
            String[] splitedYakumanCsv = yakumanCsv.split(",");
            for (String aSplitedYakumanCsv : splitedYakumanCsv) {
                yaku.add(yakuStr[Integer.parseInt(aSplitedYakumanCsv)]);
            }
            han = splitedYakumanCsv.length * 13;
        }
        int[] increaseAndDecrease = new int[4];
        String scCsv = attributes.getValue("sc");
        String[] splitedScCsv = scCsv.split(",");
        for (int i = 0; i < 4; i++) {
            increaseAndDecrease[i] = Integer.parseInt(splitedScCsv[2 * i]) * 100;
        }
        analyzer.agari(position, from, yaku, han, hu, score, increaseAndDecrease);

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
                playerPoints[i] = (int) Float.parseFloat(splitedOwariCsv[i]) * 100;
            }

            analyzer.endGame(playerPoints);
        }
    }
}
