package tenhodownloader;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import tenhouvisualizer.Main;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class DownloadYearTask extends Task {
    private Label progressLabel;
    private int year;

    private URLConnection connection;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final Pattern mjlogPattern = Pattern.compile("log=([^\"]+)");
    private final Pattern playerPattern = Pattern.compile("(.+)\\([+\\-\\d.]+\\)");

    DownloadYearTask(Label progressLabel, int year) {
        this.progressLabel = progressLabel;
        this.year = year;
    }

    @Override
    protected Object call() {
        try {
            URL url = new URL("http://tenhou.net/sc/raw/scraw" + year + ".zip");
            connection = url.openConnection();
            connection.connect();

            File tmpDir = new File(System.getProperty("java.io.tmpdir"));
            File tmpFile = File.createTempFile("mjlog", ".zip", tmpDir);

            downloadZipAndAddIndices(url, tmpFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void downloadZipAndAddIndices(URL url, File tmpFile) throws IOException {
        System.out.println("download start: " + url);
        Platform.runLater(() -> progressLabel.setText("ダウンロード中..."));
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
            System.out.println("download end: " + url);

            System.out.println("add indices start");
            addIndices(tmpFile);
            System.out.println("add indices end");
        } catch (Exception e) {
            Platform.runLater(() -> progressLabel.setText(""));
            throw new RuntimeException(e);
        } finally {
            if (tmpFile.delete()) {
                System.out.println("delete temp file");
            }
        }
    }

    private void addIndices(File tmpFile) {
        Platform.runLater(() -> progressLabel.setText("インデックス追加中..."));
        try (FileInputStream fileInputStream = new FileInputStream(tmpFile);
             ZipInputStream zipInputStream = new ZipInputStream(fileInputStream)) {
            ZipFile zipFile = new ZipFile(tmpFile);
            long workDone = 0;
            long workMax = zipFile.size();

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                workDone++;
                updateProgress(workDone, workMax);

                String htmlFileName = new File(entry.getName()).getName();

                int size = (int) entry.getSize();
                byte[] buf = new byte[size];
                int readSize;
                int offset = 0;
                while ((readSize = zipInputStream.read(buf, offset, size - offset)) > 0) {
                    offset += readSize;
                }

                if (htmlFileName.startsWith("scc")) {
                    //    ********
                    // scc20100813.html
                    String dateString = htmlFileName.substring(3, 11);
                    final LocalDate localDate = LocalDate.parse(dateString, dateTimeFormatter);

                    if (htmlFileName.endsWith("gz")) {
                         buf = syantenbackanalyzer.Reader.gunzip(buf);
                    }

                    String htmlString = new String(buf);
                    StringReader stringReader = new StringReader(htmlString);
                    BufferedReader bufferedReader = new BufferedReader(stringReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        if (!line.isEmpty()) {
                            addIndex(line, localDate);
                        }
                    }
                }
            }

            Platform.runLater(() -> progressLabel.setText(""));
        } catch (IOException e) {
            Platform.runLater(() -> progressLabel.setText(""));
            throw new RuntimeException(e);
        }
    }

    private void addIndex(String line, LocalDate localDate) {
        String[] columns = line.split(" \\| ");
        Matcher matcher = mjlogPattern.matcher(columns[3]);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (Main.databaseService.existsIdInINFO(id)) return;

            String ma = columns[2].substring(0, 1);
            String sou = columns[2].substring(2, 3);
            String[] playerAndScore = columns[4].split(" ");
            String[] players = new String[4];
            for (int i = 0; i < playerAndScore.length; i++) {
                Matcher playerMatcher = playerPattern.matcher(playerAndScore[i]);
                if (playerMatcher.find()) players[i] = playerMatcher.group(1);
            }
            if (players[3] == null) players[3] = "";
            LocalTime localTime = LocalTime.from(DateTimeFormatter.ofPattern("HH:mm").parse(columns[0]));
            LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
            InfoSchema infoSchema = new InfoSchema(id, ma, sou, players[0], players[1], players[2], players[3], localDateTime);
            Main.databaseService.saveInfo(infoSchema);
        }
    }
}
