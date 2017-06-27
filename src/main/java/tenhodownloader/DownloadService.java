package tenhodownloader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class DownloadService {
    public final ObservableList<InfoSchema> infoSchemas = FXCollections.observableArrayList();
    private final Set<String> storedInfoSchemas = new HashSet<>();

    public DownloadService() {
        try {
            this.storedInfoSchemas.addAll(Main.databaseService.findAllMjlogIds());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void download(LocalDate localDate) {
        DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
        String urlPrefix = "http://tenhou.net/sc/raw/dat/2017/scc";
        String urlPostfix = ".html.gz";
        try {
            URL url = new URL(urlPrefix + localDate.format(formatter) + urlPostfix);
            try (InputStream is = url.openStream();
                 GZIPInputStream gzis = new GZIPInputStream(is);
                 InputStreamReader isr = new InputStreamReader(gzis);
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                Pattern mjlogPattern = Pattern.compile("log=([^\"]+)");
                while ((line = br.readLine()) != null) {
                    if (!line.isEmpty()) {
                        String[] columns = line.split(" \\| ");
                        Matcher matcher = mjlogPattern.matcher(columns[3]);
                        matcher.find();
                        String mjlog = matcher.group(1);
                        int time = Integer.parseInt(columns[1]);
                        String taku = columns[2];
                        String players = columns[4].substring(0, columns[4].length() - 4);
                        infoSchemas.add(new InfoSchema(null, time, mjlog, mjlog, players));
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


    public void downloadMjlogToDatabase(InfoSchema schema) {
        try {
            URL url = new URL("http://tenhou.net/0/log/?" + schema.id);
            try (InputStream is = url.openStream();
            InputStreamReader isr = new InputStreamReader(is)) {
                String content = consumeReader(isr);
                Main.databaseService.saveMjlog(schema.id, content);
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

    public boolean isDownloaded(InfoSchema schema) {
        return this.storedInfoSchemas.contains(schema.id);
    }
}
