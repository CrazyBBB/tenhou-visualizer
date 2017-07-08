package tenhouvisualizer;

import javafx.scene.control.TreeView;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class MjlogTreeControl extends TreeView<Mjlog> {
    public MjlogTreeControl() {
        this.setShowRoot(false);
    }

    public void showMjlogContent(byte[] xml, int position)  {
        ArrayList<Kyoku> scenesList;

        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser;
            saxParser = saxParserFactory.newSAXParser();
            Analyzer analyzer = new Analyzer(position);
            ParseHandler parseHandler = new ParseHandler(analyzer);
            saxParser.parse(new ByteArrayInputStream(xml), parseHandler);
            scenesList = analyzer.getOriScenesList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MjlogTreeItem root = new MjlogTreeItem();
        for (Kyoku kyoku : scenesList) {
            MjlogTreeItem child = new MjlogTreeItem(new Mjlog(kyoku.summary));
            for (Scene scene : kyoku.scenes) {
                MjlogTreeItem grandchild = new MjlogTreeItem(new Mjlog(scene.toString(), scene));
                child.getChildren().add(grandchild);
            }
            root.getChildren().add(child);
        }
        this.setRoot(root);
    }
}
