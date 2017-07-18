package tenhouvisualizer.app.main;

import javafx.scene.control.TreeView;
import tenhouvisualizer.domain.analyzer.Analyzer;
import tenhouvisualizer.domain.analyzer.ParseHandler;
import tenhouvisualizer.domain.model.Kyoku;
import tenhouvisualizer.domain.model.Mjlog;
import tenhouvisualizer.domain.model.MahjongScene;

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
            for (int i = 0; i < kyoku.scenes.size(); i++) {
                MahjongScene scene = kyoku.scenes.get(i);
                MjlogTreeItem grandchild = new MjlogTreeItem(new Mjlog(scene.toString(), scene, i + 1));
                child.getChildren().add(grandchild);
            }
            root.getChildren().add(child);
        }
        this.setRoot(root);
    }
}
