package tenhouvisualizer.domain.task;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenhouvisualizer.domain.analyzer.ParseHandler;
import tenhouvisualizer.domain.model.MahjongScene;
import tenhouvisualizer.domain.analyzer.SyantenAnalyzer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AnalyzeZipTask extends Task {

    private final static Logger log = LoggerFactory.getLogger(AnalyzeZipTask.class);

    private final File selectedFile;
    private final ObservableList<MahjongScene> observableList;

    public AnalyzeZipTask(File selectedFile, ObservableList<MahjongScene> observableList) {
        this.selectedFile = Objects.requireNonNull(selectedFile);
        this.observableList = observableList;
    }

    @Override
    protected Void call() throws Exception {
        Platform.runLater(this.observableList::clear);
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxParserFactory.newSAXParser();
        try (ZipFile zipFile = new ZipFile(this.selectedFile)) {
            int workMax = zipFile.size();
            long workDone = 0;
            for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements();) {
                ZipEntry zipEntry = e.nextElement();
                int position = zipEntry.getName().charAt(zipEntry.getName().length() - 7) - '0';
                SyantenAnalyzer analyzer = new SyantenAnalyzer(position);
                ParseHandler parseHandler = new ParseHandler(analyzer);
                try (InputStream is = zipFile.getInputStream(zipEntry);
                    GZIPInputStream gzis = new GZIPInputStream(is)) {
                    saxParser.parse(gzis, parseHandler);
                }
                ArrayList<MahjongScene> scenes = analyzer.getOriScenes();
                workDone++;
                Platform.runLater(() -> observableList.addAll(scenes));
                updateMessage(workDone + "/" + workMax);
                updateProgress(workDone, workMax);
            }
        }
        return null;
    }
}
