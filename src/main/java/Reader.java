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

/**
 * Created by m-yamamt on 2017/03/05.
 */
public class Reader {

    public static Document convertXmlFileToDocument(InputStream is) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }

    public static ArrayList<InputStream> unzip(File file) {
        ArrayList<InputStream> list = new ArrayList<>();
        try  {
            FileInputStream fis = new FileInputStream(file);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry entry = null;
            while ((entry = zis.getNextEntry()) != null) {
                int size = (int) entry.getSize();
                byte[] buf = new byte[size];
                zis.read(buf);
                list.add(new ByteArrayInputStream(buf));
            }
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static InputStream gunzip(InputStream is) throws IOException {
        GZIPInputStream gis = new GZIPInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        byte[] buf = new byte[1024];
        while ((len = gis.read(buf)) > 0) {
            baos.write(buf, 0, len);
        }
        InputStream gunzipedIs = new ByteArrayInputStream(baos.toByteArray());
        gis.close();
        baos.close();
        return gunzipedIs;
    }

    //TODO: remove
    public static void main(String[] args) {
        ArrayList<InputStream> list = Reader.unzip(new File("C:\\Users\\m-yamamt\\Downloads\\mjlog_pf3-20_n13.zip"));
        for (InputStream is : list) {
            try {
                Document document = convertXmlFileToDocument(gunzip(is));
                Analyzer.findOriScenes(document);
            } catch (IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }
        }
    }
}
