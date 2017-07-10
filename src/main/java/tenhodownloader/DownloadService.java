package tenhodownloader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import tenhouvisualizer.Main;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class DownloadService {
    final ObservableList<InfoSchema> infoSchemas = FXCollections.observableArrayList();
    private final Set<String> storedInfoSchemaIds = new HashSet<>();
    private final Pattern mjlogPattern = Pattern.compile("log=([^\"]+)");
    private final Pattern playerPattern = Pattern.compile("(.+)\\([+\\-\\d.]+\\)");

    DownloadService() {
        this.storedInfoSchemaIds.addAll(Main.databaseService.findAllMjlogIds());
    }

    Task createDownloadYearTask(int year) {
        return new DownloadYearTask(year);
    }

    void downloadDate(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        String urlPrefix = "http://tenhou.net/sc/raw/dat/2017/scc";
        String urlPostfix = ".html.gz";
        download(urlPrefix + localDate.format(formatter) + urlPostfix, localDate);
    }

    void downloadHour(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String urlPrefix = "http://tenhou.net/sc/raw/dat/scc";
        String urlPostfix = ".html.gz";
        download(urlPrefix + localDateTime.format(formatter) + urlPostfix, localDateTime.toLocalDate());
    }

    private void download(String urlString, LocalDate localDate) {
        try {
            URL url = new URL(urlString);
            try (InputStream is = url.openStream();
                 GZIPInputStream gzis = new GZIPInputStream(is);
                 InputStreamReader isr = new InputStreamReader(gzis);
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.isEmpty()) {
                        addIndex(line, localDate);
                    }
                }
            }
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add(this.getClass().getResource("/darcula.css").toExternalForm());
            alert.getDialogPane().setHeaderText("インデックス追加の失敗");
            alert.getDialogPane().setContentText("インデックスを追加することができませんでした");
            alert.show();
            throw new UncheckedIOException(e);
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
            infoSchemas.add(infoSchema);
            Main.databaseService.saveInfo(infoSchema);
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
                Main.databaseService.saveMjlog(schema.id, content);
                this.storedInfoSchemaIds.add(schema.id);
            }
        } catch (IOException | SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add(this.getClass().getResource("/darcula.css").toExternalForm());
            alert.getDialogPane().setHeaderText("牌譜追加の失敗");
            alert.getDialogPane().setContentText("牌譜を追加することができませんでした");
            alert.show();
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

    boolean isDownloaded(InfoSchema infoSchema) {
        return this.storedInfoSchemaIds.contains(infoSchema.id);
    }

    void removeInfoSchema(InfoSchema infoSchema) {
        this.storedInfoSchemaIds.remove(infoSchema.id);
    }
}
