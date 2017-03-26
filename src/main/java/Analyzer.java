import org.w3c.dom.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class Analyzer {

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

    boolean[] saved = new boolean[4];

    int prev = -1;

    public ArrayList<Scene> findOriScenes(Document document) throws IOException {

        Element element = document.getDocumentElement();
        NodeList nodeList = element.getChildNodes();
        int len = nodeList.getLength();
        int index = 0;

        while (index < len) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();

            if ("SHUFFLE".equals(nodeName)) {
                // nop
            } else if ("GO".equals(nodeName)) {
                analyzeGO(node);
            } else if ("UN".equals(nodeName)) {
                analyzeUN(node);
            } else if ("TAIKYOKU".equals(nodeName)) {
                // nop
            } else if ("INIT".equals(nodeName)) {
                analyzeINIT(node);
            } else if ("AGARI".equals(nodeName)) {

            } else if ("RYUUKYOKU".equals(nodeName)) {

            } else if ("N".equals(nodeName)) {
                analyzeN(node);
            } else if (nodeName.matches("[T-W]\\d+")) {
                analyzeT(nodeName);
            } else if (nodeName.matches("[D-G]\\d+")) {
                analyzeD(nodeName);
            } else if ("REACH".equals(nodeName)) {
                analyzeREACH(node);
            } else if ("DORA".equals(nodeName)) {
                analyzeDORA(node);
            }

            index++;
        }

        return oriScenes;
    }

    private void analyzeGO(Node node) {
        Node typeNode = node.getAttributes().getNamedItem("type");
        int type = Integer.parseInt(typeNode.getNodeValue());
        isSanma = (type & 0x10) != 0;
    }

    private void analyzeUN(Node node) throws IOException {
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String key = attribute.getNodeName();
            if (key.equals("dan")) {
                String[] tmp = attribute.getNodeValue().split(",");
                for (int j = 0; j < 4; j++) {
                    dan[j] = danStr[Integer.valueOf(tmp[j])];
                }
            } else if (key.equals("rate")) {
                String[] tmp = attribute.getNodeValue().split(",");
                for (int j = 0; j < 4; j++) {
                    rate[j] = Float.valueOf(tmp[j]).intValue();
                }
            } else if (key.matches("n\\d")) {
                String name = URLDecoder.decode(attribute.getNodeValue(), "UTF-8");
                players[Integer.parseInt(key.substring(1))] = name;
            }
        }
    }

    private void analyzeINIT(Node node) {
        for (int i = 0; i < 4; i++) {
            Arrays.fill(tehai[i], 0);
            stehai[i] = new TreeSet<>();
            dahai[i] = new ArrayList<>();
            tedashi[i] = new ArrayList<>();
            naki[i] = new ArrayList<>();
        }
        Arrays.fill(reach, -1);
        Arrays.fill(kita, 0);
        Arrays.fill(saved, false);
        dora = new ArrayList<>();

        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attribute = attributes.item(i);
            String key = attribute.getNodeName();
            if (key.equals("ten")) {
                String value = attribute.getNodeValue();
                String[] pointStr = value.split(",");
                for (int j = 0; j < 4; j++) {
                    point[j] = Integer.parseInt(pointStr[j]) * 100;
                }
            } else if (key.equals("seed")) {
                String value = attribute.getNodeValue();
                String[] seedStr = value.split(",");
                int seed0 = Integer.valueOf(seedStr[0]);
                if (seed0 % 4 + 1 == kyoku) {
                    honba++;
                } else {
                    honba = 0;
                }
                bakaze = seed0 / 4;
                kyoku = seed0 % 4 + 1;

                int seed5 = Integer.valueOf(seedStr[5]);
                dora.add(seed5);
            } else if (key.matches("hai\\d")) {
                int playerId = Integer.parseInt(key.substring(3));
                String value = attribute.getNodeValue();
                if ("".equals(value)) continue;
                String[] hais = value.split(",");

                for (int j = 0; j < 13; j++) {
                    int hai = Integer.parseInt(hais[j]);
                    stehai[playerId].add(hai);
                    tehai[playerId][hai / 4]++;
                }
            }
        }
    }

    private void analyzeT(String nodeName) {
        int playerId = nodeName.charAt(0) - 'T';
        int hai = Integer.parseInt(nodeName.substring(1));
        stehai[playerId].add(hai);
        tehai[playerId][hai / 4]++;
        prev = hai;
    }

    private void analyzeD(String nodeName) {
        int playerId = nodeName.charAt(0) - 'D';
        int beforeSyanten = Utils.computeSyanten(tehai[playerId], naki[playerId].size());

        int hai = Integer.parseInt(nodeName.substring(1));
        stehai[playerId].remove(hai);
        tehai[playerId][hai / 4]--;
        dahai[playerId].add(hai);
        tedashi[playerId].add(prev != hai);

        int afterSyanten = Utils.computeSyanten(tehai[playerId], naki[playerId].size());
        if (!saved[playerId] && beforeSyanten < afterSyanten) {
            saveScene(playerId);
            saved[playerId] = true;
        }
    }

    private void analyzeN(Node node) {
        Node mNode = node.getAttributes().getNamedItem("m");
        int m = Integer.parseInt(mNode.getNodeValue());
        Node whoNode = node.getAttributes().getNamedItem("who");
        int who = Integer.parseInt(whoNode.getNodeValue());

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

    private void analyzeREACH(Node node) {
        Node stepNode = node.getAttributes().getNamedItem("step");
        int step = Integer.parseInt(stepNode.getNodeValue());

        Node whoNode = node.getAttributes().getNamedItem("who");
        int who = Integer.parseInt(whoNode.getNodeValue());
        if (step == 1) {
            reach[who] = dahai[who].size();
        } else {
            point[who] -= 1000;
        }
    }

    private void analyzeDORA(Node node) {
        Node haiNode = node.getAttributes().getNamedItem("hai");
        int hai = Integer.parseInt(haiNode.getNodeValue());
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
}
