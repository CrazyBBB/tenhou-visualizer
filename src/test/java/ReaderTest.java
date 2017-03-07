/**
 * Created by m-yamamt on 2017/03/04.
 */

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ReaderTest {

    @Test
    public void testConvertXmlFileToDocument() throws IOException, SAXException, ParserConfigurationException {
        String xmlFilePath = "./src/test/mjlog/test.mjlog";
        Document document = Reader.convertXmlFileToDocument(new FileInputStream(xmlFilePath));
        Element element = document.getDocumentElement();
        String tagName = element.getTagName();
        String expected = "mjloggm";
        assertEquals(expected, tagName);
    }
}
