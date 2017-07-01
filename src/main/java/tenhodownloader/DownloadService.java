package tenhodownloader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tenhouvisualizer.App;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class DownloadService {
    final ObservableList<InfoSchema> infoSchemas = FXCollections.observableArrayList();
    private final Set<String> storedInfoSchemas = new HashSet<>();
    private final Pattern mjlogPattern = Pattern.compile("log=([^\"]+)");
    private final Pattern playerPattern = Pattern.compile("(.+)\\([+\\-\\d.]+\\)");

    DownloadService() {
        try {
            this.storedInfoSchemas.addAll(App.databaseService.findAllMjlogIds());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void downloadDate(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        String urlPrefix = "http://tenhou.net/sc/raw/dat/2017/scc";
        String urlPostfix = ".html.gz";
        download(urlPrefix + localDate.format(formatter) + urlPostfix);
    }

    void downloadHour(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String urlPrefix = "http://tenhou.net/sc/raw/dat/scc";
        String urlPostfix = ".html.gz";
        download(urlPrefix + localDateTime.format(formatter) + urlPostfix);
    }

    private void download(String urlString) {
        try {
            URL url = new URL(urlString);
            try (InputStream is = url.openStream();
                 GZIPInputStream gzis = new GZIPInputStream(is);
                 InputStreamReader isr = new InputStreamReader(gzis);
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.isEmpty()) {
                        String[] columns = line.split(" \\| ");
                        Matcher matcher = mjlogPattern.matcher(columns[3]);
                        if (matcher.find()) {
                            String id = matcher.group(1);
                            if (App.databaseService.existsIdInINFO(id)) continue;

                            String ma = columns[2].substring(0, 1);
                            String sou = columns[2].substring(2, 3);
                            String[] playerAndScore = columns[4].split(" ");
                            String[] players = new String[4];
                            for (int i = 0; i < playerAndScore.length; i++) {
                                Matcher playerMatcher = playerPattern.matcher(playerAndScore[i]);
                                if (playerMatcher.find()) players[i] = playerMatcher.group(1);
                            }
                            if (players[3] == null) players[3] = "";
                            infoSchemas.add(new InfoSchema(id, ma, sou, players[0], players[1], players[2], players[3], LocalDateTime.now())); // todo datetime
                            App.databaseService.saveInfo(id, ma, sou, players[0], players[1], players[2], players[3], LocalDateTime.now().toString());
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void downloadMjlog(InfoSchema schema) {
        try {
            URL url = new URL("http://tenhou.net/0/log/?" + schema.id);
            try (InputStream is = url.openStream()) {
                Path path = Paths.get(schema.id);
                Files.copy(is, path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    void downloadMjlogToDatabase(InfoSchema schema) {
        try {
            URL url = new URL("http://tenhou.net/0/log/?" + schema.id);
            try (InputStream is = url.openStream();
            InputStreamReader isr = new InputStreamReader(is)) {
                String content = consumeReader(isr);
                App.databaseService.saveMjlog(schema.id, content);
                this.storedInfoSchemas.add(schema.id);
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static String consumeReader(InputStreamReader isr) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] chars = new char[256];
        int readSize;
        while ((readSize = isr.read(chars)) > 0) {
            sb.append(chars, 0, readSize);
        }
        return sb.toString();
    }

    boolean isDownloaded(InfoSchema schema) {
        return this.storedInfoSchemas.contains(schema.id);
    }
}
