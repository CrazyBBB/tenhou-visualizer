package syantenbackanalyzer;

import tenhouvisualizer.MjlogFile;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class Reader {

    static ArrayList<MjlogFile> unzip(File file) throws IOException {
        ArrayList<MjlogFile> list = new ArrayList<>();
        FileInputStream fis = new FileInputStream(file);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            int size = (int) entry.getSize();
            if (size < 0) continue;
            byte[] buf = new byte[size];
            zis.read(buf);

            String path = entry.toString();
            //                                                         *
            // ex) mjlog_pf3-20_n14/2017010818gm-00b9-0000-bcbde0df&tw=0.mjlog
            int position = path.charAt(path.length() - 7) - '0';

            list.add(new MjlogFile(buf, position));
        }
        zis.close();
        fis.close();
        return list;
    }

    static byte[] gunzip(byte[] str) throws IOException {
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
        } catch (IOException e) {
            // nop
        }
        is.close();
        return ba;
    }
}
