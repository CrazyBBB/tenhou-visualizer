package tenhouvisualizer.domain.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import tenhouvisualizer.domain.task.DownloadYearTask;
import tenhouvisualizer.Main;
import tenhouvisualizer.domain.model.InfoSchema;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class DownloadService {
    public final ObservableList<InfoSchema> infoSchemas = FXCollections.observableArrayList();
    private final Set<String> storedInfoSchemaIds = new HashSet<>();
    private final static Pattern mjlogPattern = Pattern.compile("log=([^\"]+)");
    private final static Pattern playerPattern = Pattern.compile("(.+)\\(([+\\-\\d.]+)\\)");

    private final DatabaseService databaseService;

    public DownloadService() {
        this.databaseService = Main.databaseService;
        this.storedInfoSchemaIds.addAll(databaseService.findAllMjlogIds());
    }

    public DownloadYearTask createDownloadYearTask(int year) {
        return new DownloadYearTask(year);
    }

    public void downloadDate(LocalDate localDate) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        String urlPrefix = "http://tenhou.net/sc/raw/dat/2017/scc";
        String urlPostfix = ".html.gz";
        download(urlPrefix + localDate.format(formatter) + urlPostfix, localDate);
    }

    public void downloadHour(LocalDateTime localDateTime) throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String urlPrefix = "http://tenhou.net/sc/raw/dat/scc";
        String urlPostfix = ".html.gz";
        download(urlPrefix + localDateTime.format(formatter) + urlPostfix, localDateTime.toLocalDate());
    }

    private void download(String urlString, LocalDate localDate) throws IOException {
        URL url = new URL(urlString);
        try (InputStream is = url.openStream();
             GZIPInputStream gzis = new GZIPInputStream(is);
             InputStreamReader isr = new InputStreamReader(gzis, StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            List<InfoSchema> infos = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    InfoSchema info = parseLineToInfo(line, localDate);
                    if (info != null) {
                        infos.add(info);
                    }
                }
            }

            this.databaseService.saveInfos(infos);
        }
    }

    private InfoSchema parseLineToInfo(String line, LocalDate localDate) {
        String[] columns = line.split(" \\| ");
        Matcher matcher = mjlogPattern.matcher(columns[3]);
        if (matcher.find()) {
            String id = matcher.group(1);
            if (this.databaseService.existsIdInINFO(id)) return null;

            boolean isSanma = columns[2].substring(0, 1).equals("三");
            boolean isTonnan = columns[2].substring(2, 3).equals("南");
            int minute = Integer.parseInt(columns[1]);
            String[] playerAndScore = columns[4].split(" ");
            String[] players = new String[4];
            int[] scores = new int[4];
            for (int i = 0; i < playerAndScore.length; i++) {
                Matcher playerMatcher = playerPattern.matcher(playerAndScore[i]);
                if (playerMatcher.find()) {
                    players[i] = playerMatcher.group(1);
                    scores[i] = (int) Float.parseFloat(playerMatcher.group(2));
                }
            }
            LocalTime localTime = LocalTime.from(DateTimeFormatter.ofPattern("HH:mm").parse(columns[0]));
            LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
            return new InfoSchema(
                    id,
                    isSanma,
                    isTonnan,
                    minute,
                    localDateTime,
                    players[0],
                    players[1],
                    players[2],
                    players[3],
                    scores[0],
                    scores[1],
                    scores[2],
                    scores[3]
            );
        }

        return null;
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


    public void downloadMjlogToDatabase(InfoSchema schema) throws IOException, SQLException {
        URL url = new URL("http://tenhou.net/0/log/?" + schema.id);
        try (InputStream is = url.openStream();
             InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            String content = consumeReader(isr);
            databaseService.saveMjlog(schema.id, content);
            this.storedInfoSchemaIds.add(schema.id);
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

    public boolean isDownloaded(InfoSchema infoSchema) {
        return this.storedInfoSchemaIds.contains(infoSchema.id);
    }

    public void removeInfoSchema(InfoSchema infoSchema) {
        this.storedInfoSchemaIds.remove(infoSchema.id);
    }

    public List<Integer> createDownloadableYearList() {
        List<Integer> downloadableYearList = new ArrayList<>();
        Set<String> mjlogIndexIds = this.databaseService.findAllMjlogIndexIds();
        int from = 2009;
        int to = LocalDate.now().getYear();
        for (Integer i = from; i < to; i++) {
            if (!mjlogIndexIds.contains(i.toString())) {
                downloadableYearList.add(i);
            }
        }
        return downloadableYearList;
    }

    public List<LocalDate> createDownloadableDateList() {
        List<LocalDate> downloadableDateList = new ArrayList<>();
        Set<String> mjlogIndexIds = this.databaseService.findAllMjlogIndexIds();
        LocalDate from = LocalDate.of(LocalDate.now().getYear(), 1, 1);
        LocalDate to = LocalDate.now().minusDays(7);
        for (LocalDate i = from; to.isAfter(i); i = i.plusDays(1)) {
            if (!mjlogIndexIds.contains(i.toString())) {
                downloadableDateList.add(i);
            }
        }
        return downloadableDateList;
    }

    public List<LocalDateTime> createDownloadableHourList() {
        List<LocalDateTime> downloadableHourList = new ArrayList<>();
        Set<String> mjlogIndexIds = this.databaseService.findAllMjlogIndexIds();
        LocalDateTime from = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
        LocalDateTime to = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        for (LocalDateTime i = from; to.isAfter(i); i = i.plusHours(1)) {
            if (!mjlogIndexIds.contains(i.toString())) {
                downloadableHourList.add(i);
            }
        }
        return downloadableHourList;
    }
}
