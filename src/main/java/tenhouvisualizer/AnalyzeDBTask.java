package tenhouvisualizer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AnalyzeDBTask extends Task<List<Scene>> {
    private ListView<Scene> listView;
    private Label label;
    AnalyzeDBTask(ListView<Scene> listView, Label label) {
        this.listView = listView;
        this.label = label;
    }
    @Override
    protected List<Scene> call() throws Exception {
        Platform.runLater(() -> this.listView.getItems().clear());

        Set<String> set = App.databaseService.findAllMjlogContents();

        long workDone = 0;
        long workMax = set.size();
        for (String content : set) {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            Analyzer analyzer = new Analyzer(0);
            saxParser.parse(new ByteArrayInputStream(content.getBytes()), analyzer);
            ArrayList<Scene> scenes = analyzer.getOriScenes();
            workDone++;
            final long tmp = workDone;
            Platform.runLater(() -> {
                listView.getItems().addAll(scenes);
                label.setText(tmp + "/" + workMax);
            });
            updateProgress(workDone, workMax);
        }
        return null;
    }
}
