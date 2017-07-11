package syantenbackanalyzer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import tenhouvisualizer.Main;
import tenhouvisualizer.ParseHandler;
import tenhouvisualizer.Scene;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class AnalyzeDBTask extends Task<List<Scene>> {
    private ListView<Scene> listView;

    AnalyzeDBTask(ListView<Scene> listView) {
        this.listView = listView;
    }

    @Override
    protected List<Scene> call() throws Exception {
        Platform.runLater(() -> this.listView.getItems().clear());

        List<String> list = Main.databaseService.findAllMjlogContents();
        if (list.size() == 0) {
            updateMessage("0/0");
            updateProgress(0, 0);
            return null;
        }

        long workDone = 0;
        long workMax = list.size();
        for (String content : list) {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            Analyzer analyzer = new Analyzer(0);
            ParseHandler parseHandler = new ParseHandler(analyzer);
            saxParser.parse(new ByteArrayInputStream(content.getBytes()), parseHandler);
            ArrayList<Scene> scenes = analyzer.getOriScenes();
            workDone++;
            Platform.runLater(() -> listView.getItems().addAll(scenes));
            updateMessage(workDone + "/" + workMax);
            updateProgress(workDone, workMax);
        }
        return null;
    }
}
