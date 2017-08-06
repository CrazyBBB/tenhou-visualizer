package tenhouvisualizer.domain.analyzer;

import javafx.util.Pair;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class YakuAnalyzerTest {
    @Test
    public void getComputationList() throws Exception {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        saxParser = saxParserFactory.newSAXParser();
        YakuAnalyzer analyzer = new YakuAnalyzer("CrazyBBB");
        ParseHandler parseHandler = new ParseHandler(analyzer);
        FileInputStream fileInputStream = new FileInputStream(new File(YakuAnalyzerTest.class.getResource("/mjlog/test.mjlog").toURI()));
        saxParser.parse(fileInputStream, parseHandler);

        List<Pair<String, Integer>> actual = analyzer.getComputationList();
        List<Pair<String, Integer>> expected = Arrays.asList(new Pair<String, Integer>("立直", 1), new Pair<String, Integer>("裏ドラ", 1), new Pair<String, Integer>("ドラ4", 1), new Pair<String, Integer>("赤ドラ", 1));
        assertEquals(expected, actual);
    }

}