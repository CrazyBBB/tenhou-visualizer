package tenhouvisualizer.domain.analyzer;

import javafx.util.Pair;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
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
        try (InputStream is = YakuAnalyzerTest.class.getResourceAsStream("/mjlog/test.mjlog")) {
            saxParser.parse(is, parseHandler);
        }

        List<Pair<String, Integer>> actual = analyzer.getComputationList();
        List<Pair<String, Integer>> expected = Arrays.asList(new Pair<>("立直", 1), new Pair<>("裏ドラ", 1), new Pair<>("ドラ4", 1), new Pair<>("赤ドラ", 1));
        assertEquals(expected, actual);
    }

}