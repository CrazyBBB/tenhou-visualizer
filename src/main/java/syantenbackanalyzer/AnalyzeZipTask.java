package syantenbackanalyzer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import tenhouvisualizer.MjlogFile;
import tenhouvisualizer.ParseHandler;
import tenhouvisualizer.Scene;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnalyzeZipTask extends Task<List<Scene>> {
    private final File selectedFile;
    private ListView<Scene> listView;

    AnalyzeZipTask(File selectedFile, ListView<Scene> listView) {
        this.selectedFile = Objects.requireNonNull(selectedFile);
        this.listView = listView;
    }

    @Override
    protected List<Scene> call() throws Exception {
        Platform.runLater(() -> this.listView.getItems().clear());

        ArrayList<MjlogFile> list = Reader.unzip(selectedFile);
        long workDone = 0;
        long workMax = list.size();
        for (MjlogFile mjlogFile : list) {
            byte[] gunzipedXml = Reader.gunzip(mjlogFile.getXml());
            if (gunzipedXml == null) continue;
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            Analyzer analyzer = new Analyzer(mjlogFile.getPosition());
            ParseHandler parseHandler = new ParseHandler(analyzer);
            saxParser.parse(new ByteArrayInputStream(gunzipedXml), parseHandler);
            ArrayList<Scene> scenes = analyzer.getOriScenes();
            workDone++;
            Platform.runLater(() -> listView.getItems().addAll(scenes));
            updateMessage(workDone + "/" + workMax);
            updateProgress(workDone, workMax);
        }
        return null;
    }
}
