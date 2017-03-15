import org.w3c.dom.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class Analyzer {

    static final String[] danStr = {"新人", "９級", "８級", "７級", "６級", "５級", "４級", "３級", "２級", "１級", "初段", "二段", "三段", "四段", "五段", "六段", "七段", "八段", "九段", "十段", "天鳳"};

    static ArrayList<Scene> oriScenes = new ArrayList<>();

    static String[] players = new String[4];
    static boolean isSanma = false;
    static String[] dan = new String[4];
    static int[] rate = new int[4];

    static int[] point = new int[4];
    static int[] syanten = new int[4];
    static int[][] tehai = new int[4][34];
    static TreeSet<Integer>[] stehai = new TreeSet[4];
    static ArrayList<Integer>[] dahai = new ArrayList[4];
    static ArrayList<Boolean>[] tedashi = new ArrayList[4];
    static int[] reach = new int[4];
    static int bakaze = 0;
    static int kyoku = -1;
    static int honba = 0;

    static int prev = -1;

    public static ArrayList<Scene> findOriScenes(Document document) throws IOException {

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
//            } else if ("AGARI".equals(nodeName)) { TODO
//
//            } else if ("RYUUKYOKU".equals(nodeName)) {
            } else if ("AGARI".equals(nodeName) || "RYUUKYOKU".equals(nodeName)) {
                //TODO:remove
                oriScenes.add(new Scene(isSanma,
                        0,
                        players.clone(),
                        dan.clone(),
                        rate.clone(),
                        point.clone(),
                        stehai.clone(),
                        null,
                        dahai.clone(),
                        tedashi.clone(),
                        reach.clone(),
                        bakaze,
                        kyoku,
                        honba,
                        0));
            } else if ("N".equals(nodeName)) {
                analyzeN();
            } else if (nodeName.matches("[T-W]\\d+")) {
                analyzeT(nodeName);
            } else if (nodeName.matches("[D-G]\\d+")) {
                analyzeD(nodeName);
            } else if ("REACH".equals(nodeName)) {
                analyzeREACH(node);
            }

            index++;
        }

        return oriScenes;
    }

    private static void analyzeGO(Node node) {
        Node typeNode = node.getAttributes().getNamedItem("type");
        int type = Integer.parseInt(typeNode.getNodeValue());
        isSanma = (type & 0x10) != 0;
    }

    private static void analyzeUN(Node node) throws IOException {
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

    private static void analyzeINIT(Node node) {
        for (int i = 0; i < 4; i++) {
            Arrays.fill(tehai[i], 0);
            stehai[i] = new TreeSet<>();
            dahai[i] = new ArrayList<>();
            tedashi[i] = new ArrayList<>();
        }
        Arrays.fill(reach, -1);

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

    private static void analyzeT(String nodeName) {
        int playerId = nodeName.charAt(0) - 'T';
        int hai = Integer.parseInt(nodeName.substring(1));
        stehai[playerId].add(hai);
        tehai[playerId][hai / 4]++;
        prev = hai;
    }

    private static void analyzeD(String nodeName) {
        int playerId = nodeName.charAt(0) - 'D';
        int hai = Integer.parseInt(nodeName.substring(1));
        stehai[playerId].remove(hai);
        tehai[playerId][hai / 4]--;
        dahai[playerId].add(hai);
        tedashi[playerId].add(prev != hai);
    }

    private static void analyzeN() {
        prev = -1;
    }

    private static void analyzeREACH(Node node) {
        Node whoNode = node.getAttributes().getNamedItem("who");
        int who = Integer.parseInt(whoNode.getNodeValue());
        reach[who] = dahai[who].size();
    }
}
