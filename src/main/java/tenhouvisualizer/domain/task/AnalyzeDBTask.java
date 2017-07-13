package tenhouvisualizer.domain.task;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import tenhouvisualizer.Main;
import tenhouvisualizer.domain.analyzer.ParseHandler;
import tenhouvisualizer.domain.model.Scene;
import tenhouvisualizer.domain.analyzer.SyantenAnalyzer;
import tenhouvisualizer.domain.service.DatabaseService;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class AnalyzeDBTask extends Task {
    private final ObservableList<Scene> observableList;
    private final DatabaseService databaseService;

    public AnalyzeDBTask(ObservableList<Scene> observableList) {
        this.observableList = observableList;
        this.databaseService = Main.databaseService;
    }

    @Override
    protected Void call() throws Exception {
        Platform.runLater(this.observableList::clear);

        List<String> list = databaseService.findAllMjlogContents();
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
            SyantenAnalyzer analyzer = new SyantenAnalyzer(0);
            ParseHandler parseHandler = new ParseHandler(analyzer);
            saxParser.parse(new ByteArrayInputStream(content.getBytes()), parseHandler);
            ArrayList<Scene> scenes = analyzer.getOriScenes();
            workDone++;
            Platform.runLater(() -> observableList.addAll(scenes));
            updateMessage(workDone + "/" + workMax);
            updateProgress(workDone, workMax);
        }
        return null;
    }
}
