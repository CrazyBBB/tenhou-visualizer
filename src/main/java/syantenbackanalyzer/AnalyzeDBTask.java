package syantenbackanalyzer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import tenhouvisualizer.Scene;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import tenhouvisualizer.App;

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

        List<String> list = App.databaseService.findAllMjlogContents();
        if (list.size() == 0) {
            Platform.runLater(() -> {
                label.setText("0/0");
            });
            updateProgress(0, 0);
            return null;
        }

        long workDone = 0;
        long workMax = list.size();
        for (String content : list) {
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
