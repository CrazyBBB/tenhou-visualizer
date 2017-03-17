import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Reader {

    public static Document convertXmlFileToDocument(byte[] xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(xml);
        Document document = builder.parse(is);
        is.close();
        return document;
    }

    public static ArrayList<byte[]> unzip(File file) throws IOException {
        ArrayList<byte[]> list = new ArrayList<>();
        FileInputStream fis = new FileInputStream(file);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry entry = null;
        while ((entry = zis.getNextEntry()) != null) {
            int size = (int) entry.getSize();
            if (size < 0) continue;
            byte[] buf = new byte[size];
            zis.read(buf);
            list.add(buf);
        }
        zis.close();
        fis.close();
        return list;
    }

    public static byte[] gunzip(byte[] str) throws IOException {
        InputStream is = new ByteArrayInputStream(str);
        byte[] ba = null;
        try {
            GZIPInputStream gis = new GZIPInputStream(is);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = gis.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }
            ba = baos.toByteArray();
            baos.close();
            gis.close();
        } catch (EOFException e) {
            // nop
        }
        is.close();
        return ba;
    }
}
