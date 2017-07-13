package tenhouvisualizer.domain.task;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import tenhouvisualizer.domain.MjlogReader;
import tenhouvisualizer.domain.model.MjlogFile;
import tenhouvisualizer.domain.analyzer.ParseHandler;
import tenhouvisualizer.domain.model.Scene;
import tenhouvisualizer.domain.analyzer.SyantenAnalyzer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class AnalyzeZipTask extends Task {
    private final File selectedFile;
    private final ObservableList<Scene> observableList;

    public AnalyzeZipTask(File selectedFile, ObservableList<Scene> observableList) {
        this.selectedFile = Objects.requireNonNull(selectedFile);
        this.observableList = observableList;
    }

    @Override
    protected Void call() throws Exception {
        Platform.runLater(this.observableList::clear);

        ArrayList<MjlogFile> list = MjlogReader.unzip(selectedFile);
        long workDone = 0;
        long workMax = list.size();
        for (MjlogFile mjlogFile : list) {
            byte[] gunzipedXml = MjlogReader.gunzip(mjlogFile.getXml());
            if (gunzipedXml == null) continue;
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            SyantenAnalyzer analyzer = new SyantenAnalyzer(mjlogFile.getPosition());
            ParseHandler parseHandler = new ParseHandler(analyzer);
            saxParser.parse(new ByteArrayInputStream(gunzipedXml), parseHandler);
            ArrayList<Scene> scenes = analyzer.getOriScenes();
            workDone++;
            Platform.runLater(() -> observableList.addAll(scenes));
            updateMessage(workDone + "/" + workMax);
            updateProgress(workDone, workMax);
        }
        return null;
    }
}
