package tenhouvisualizer.domain.task;

import javafx.concurrent.Task;
import org.jetbrains.annotations.NotNull;
import tenhouvisualizer.Main;
import tenhouvisualizer.domain.model.InfoSchema;
import tenhouvisualizer.domain.service.DatabaseService;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class DownloadYearTask extends Task {
    private int year;

    private URLConnection connection;

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Pattern mjlogPattern = Pattern.compile("log=([^\"]+)");
    private static final Pattern playerPattern = Pattern.compile("(.+)\\([+\\-\\d.]+\\)");

    private final DatabaseService databaseService;

    public DownloadYearTask(int year) {
        this.year = year;
        this.databaseService = Main.databaseService;
    }

    @Override
    protected Object call() {
        try {
            URL url = new URL("http://tenhou.net/sc/raw/scraw" + year + ".zip");
            connection = url.openConnection();
            connection.connect();

            File tmpFile = File.createTempFile("mjlog", ".zip");
            tmpFile.deleteOnExit();

            downloadZipAndAddIndices(url, tmpFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private void downloadZipAndAddIndices(URL url, File tmpFile) throws IOException {
        try (InputStream is = url.openStream();
             BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
             FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
            long workDone = 0;
            long workMax = connection.getContentLength();

            byte[] buffer = new byte[4096];
            int length;

            while ((length = bufferedInputStream.read(buffer)) >= 0) {
                workDone += length;
                fileOutputStream.write(buffer, 0, length);
                updateProgress(workDone, workMax);
                updateMessage("ダウンロード中 " + String.format("%.1f", 100.0 * workDone / workMax) + "%");
            }

        } catch (Exception e) {
            updateMessage("");
            throw new RuntimeException(e);
        }

        addIndices(tmpFile);
    }

    private void addIndices(File tmpFile) {
        try (ZipFile zipFile = new ZipFile(tmpFile)) {
            long workDone = 0;
            long workMax = 0;

            for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ){
                ZipEntry zipEntry = e.nextElement();
                String htmlFileName = new File(zipEntry.getName()).getName();
                if (htmlFileName.startsWith("scc")) workMax++;
            }

            for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ){
                ZipEntry zipEntry = e.nextElement();
                String htmlFileName = getFileName(zipEntry.getName());
                if (htmlFileName.startsWith("scc")) {
                    workDone++;
                    updateProgress(workDone, workMax);
                    updateMessage("インデックス追加中 " + String.format("%.1f", 100.0 * workDone / workMax) + "%");

                    //    ********
                    // scc20100813.html
                    String dateString = htmlFileName.substring(3, 11);
                    final LocalDate localDate = LocalDate.parse(dateString, dateTimeFormatter);

                    if (htmlFileName.endsWith("gz")) {
                        try (InputStream is = zipFile.getInputStream(zipEntry);
                             GZIPInputStream gzis = new GZIPInputStream(is);
                             InputStreamReader sr = new InputStreamReader(gzis);
                             BufferedReader br = new BufferedReader(sr)) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                if (!line.isEmpty()) {
                                    addIndex(line, localDate);
                                }
                            }
                        }
                    } else {
                        try (InputStream is = zipFile.getInputStream(zipEntry);
                             InputStreamReader sr = new InputStreamReader(is);
                             BufferedReader br = new BufferedReader(sr)) {
                            String line;
                            while ((line = br.readLine()) != null) {
                                if (!line.isEmpty()) {
                                    addIndex(line, localDate);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            updateMessage("");
        }
    }

    @NotNull
    static String getFileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    private void addIndex(String line, LocalDate localDate) {
        String[] columns = line.split(" \\| ");
        Matcher matcher = mjlogPattern.matcher(columns[3]);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (databaseService.existsIdInINFO(id)) return;

            String ma = columns[2].substring(0, 1);
            String sou = columns[2].substring(2, 3);
            String[] playerAndScore = columns[4].split(" ");
            String[] players = new String[4];
            for (int i = 0; i < playerAndScore.length; i++) {
                Matcher playerMatcher = playerPattern.matcher(playerAndScore[i]);
                if (playerMatcher.find()) players[i] = playerMatcher.group(1);
            }
            if (players[3] == null) players[3] = "";
            LocalTime localTime = LocalTime.parse(columns[0]);
            LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
            InfoSchema infoSchema = new InfoSchema(id, ma, sou, players[0], players[1], players[2], players[3], localDateTime);
            databaseService.saveInfo(infoSchema);
        }
    }
}
