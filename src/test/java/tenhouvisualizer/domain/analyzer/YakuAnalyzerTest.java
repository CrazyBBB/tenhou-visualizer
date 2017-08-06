package tenhouvisualizer.domain.analyzer;

import javafx.util.Pair;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.*;

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

        List<Pair> list = analyzer.getComputationList();
        assertEquals("立直=1", list.get(0).toString());
        assertEquals("裏ドラ=1", list.get(1).toString());
        assertEquals("ドラ4=1", list.get(2).toString());
        assertEquals("赤ドラ=1", list.get(3).toString());
    }

}