package tenhodownloader;

import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadYearTask extends Task {
    private int year;

    private URLConnection connection;

    public DownloadYearTask(int year) {
        this.year = year;
    }

    @Override
    protected Object call() throws Exception {
        try {
            URL url = new URL("http://tenhou.net/sc/raw/scraw" + year + ".zip");
            URLConnection connection = url.openConnection();
            connection.connect();

            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File tmpFile = File.createTempFile("mjlog", ".zip", tmpDir);

            downloadZipAndAddIndex(url, tmpFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void downloadZipAndAddIndex(URL url, File tmpFile) throws IOException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
            long workDone = 0;
            long workMax = connection.getContentLength();

            byte[] buffer = new byte[4096];
            int length;

            while ((length = bufferedInputStream.read(buffer)) > 0) {
                workDone += length;
                fileOutputStream.write(buffer, 0, length);
                updateProgress(workDone, workMax);
            }

            addIndex(tmpFile);
        } finally {
            if (new File(url.getFile()).delete()) {
                System.out.println("delete temp file");
            }
        }
    }

    private void addIndex(File tmpFile) {
        try (FileInputStream fis = new FileInputStream(tmpFile);
            ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                int size = (int) entry.getSize();
                if (size < 0) continue;
                byte[] buf = new byte[size];
                if (zis.read(buf) != size) {
                    throw new RuntimeException();
                }

                String htmlFileName = entry.getName();
                String htmlString = new String(buf);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
