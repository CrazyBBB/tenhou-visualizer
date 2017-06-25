package tenhouvisualizer;

import javafx.concurrent.Task;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AnalyzeTask extends Task<List<Scene>> {
    private final File selectedFile;
    public AnalyzeTask(File selectedFile) {
        this.selectedFile = Objects.requireNonNull(selectedFile);
    }
    @Override
    protected List<Scene> call() throws Exception {
        List<Scene> result = new ArrayList<>();
        ArrayList<MjlogFile> list = Reader.unzip(selectedFile);
        long workDone = 0;
        long workMax = list.size();
        for (MjlogFile mjlogFile : list) {
            byte[] gunzipedXml = Reader.gunzip(mjlogFile.getXml());
            if (gunzipedXml == null) continue;
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();
            Analyzer analyzer = new Analyzer(mjlogFile.getPosition());
            saxParser.parse(new ByteArrayInputStream(gunzipedXml), analyzer);
            ArrayList<Scene> scenes = analyzer.getOriScenes();
            result.addAll(scenes);
            workDone++;
            updateProgress(workDone, workMax);
        }
        return result;
    }
}
