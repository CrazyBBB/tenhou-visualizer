import org.w3c.dom.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by m-yamamt on 2017/03/04.
 */
public class Analyzer {
    public static ArrayList<Scene> findOriScenes(Document document) throws IOException {
        ArrayList<Scene> oriScenes = new ArrayList<>();

        String[] players = new String[4];
        int[] syanten = new int[4];
        int[][] tehai = new int[4][34];

        Element element = document.getDocumentElement();
        NodeList nodeList = element.getChildNodes();
        int len = nodeList.getLength();
        int index = 0;

        while (index < len) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();

            if ("SHUFFLE".equals(nodeName)) {

            } else if ("GO".equals(nodeName)) {

            } else if ("UN".equals(nodeName)) {
                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attribute = attributes.item(i);
                    String key = attribute.getNodeName();
                    if (key.matches("n\\d")) {
                        String name = URLDecoder.decode(attribute.getNodeValue(), "UTF-8");
                        players[Integer.parseInt(key.substring(1))] = name;
                    }
                }
            } else if ("TAIKYOKU".equals(nodeName)) {

            } else if ("INIT".equals(nodeName)) {
                for (int i = 0; i < 4; i++) {
                    Arrays.fill(tehai[i], 0);
                }

                NamedNodeMap attributes = node.getAttributes();
                for (int i = 0; i < attributes.getLength(); i++) {
                    Node attribute = attributes.item(i);
                    String key = attribute.getNodeName();
                    if (key.matches("hai\\d")) {
                        int playerId = Integer.parseInt(key.substring(3));
                        String value = attribute.getNodeValue();
                        String[] hais = value.split(",");

                        for (int j = 0; j < 13; j++) {
                            tehai[playerId][Integer.parseInt(hais[j])]++;
                        }
                    }
                }
            } else if ("AGARI".equals(nodeName)) {

            } else if ("RYUUKYOKU".equals(nodeName)) {

            } else if ("N".equals(nodeName)) {

            } else {

            }
        }

        return oriScenes;
    }
}