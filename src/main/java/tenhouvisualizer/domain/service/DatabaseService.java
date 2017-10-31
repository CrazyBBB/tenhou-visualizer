package tenhouvisualizer.domain.service;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenhouvisualizer.domain.model.InfoSchema;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseService implements Closeable {

    private final static Logger log = LoggerFactory.getLogger(DatabaseService.class);

    private final static int MAX_BATCH_SIZE = 100;

    private final Connection connection;
    private final PreparedStatement insertMjlogStatement;
    private final PreparedStatement findAllMjlogStatement;
    private final PreparedStatement findAllMjlogContent;
    private final PreparedStatement findMjlogByIdStatement;
    private final PreparedStatement removeMjlogByIdStatement;
    private final PreparedStatement insertInfoStatement;
    private final PreparedStatement findAllInfoStatement;
    private final PreparedStatement findAllExistsInfoStatement;
    private final PreparedStatement insertMjlogIndexStatement;
    private final PreparedStatement findAllMjlogIndexStatement;
    public DatabaseService(@Nullable File file) throws ClassNotFoundException, SQLException {
        long start = System.currentTimeMillis();

        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + (file == null ? "" : file));
        initialize();
        this.insertMjlogStatement = connection.prepareStatement("INSERT INTO MJLOG VALUES(?, ?);");
        this.findAllMjlogStatement = connection.prepareStatement("SELECT id FROM MJLOG;");
        this.findAllMjlogContent = connection.prepareStatement("SELECT content FROM MJLOG;");
        this.findMjlogByIdStatement = connection.prepareStatement("SELECT content FROM MJLOG WHERE id = ?;");
        this.removeMjlogByIdStatement = connection.prepareStatement("DELETE FROM MJLOG WHERE id = ?;");
        this.insertInfoStatement = connection.prepareStatement("INSERT INTO INFO VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
        this.findAllInfoStatement = connection.prepareStatement("SELECT * FROM INFO;");
        this.findAllExistsInfoStatement = connection.prepareStatement("SELECT * FROM INFO WHERE id in (SELECT id FROM MJLOG);");
        this.insertMjlogIndexStatement = connection.prepareStatement("INSERT INTO MJLOGINDEX VALUES(?);");
        this.findAllMjlogIndexStatement = connection.prepareStatement("SELECT id FROM MJLOGINDEX;");

        long end = System.currentTimeMillis();
        log.info("time to initialize db: {}", end - start);
    }

    private void initialize() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS MJLOG(id TEXT PRIMARY KEY, content TEXT);";
        Statement statement = this.connection.createStatement();
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS INFO(id TEXT PRIMARY KEY, is_sanma BOOLEAN, is_tonnan BOOLEAN," +
                " date_time DATETIME, minute INT, first TEXT, second TEXT, third TEXT, fourth TEXT," +
                " first_score INT, second_score INT, third_score INT, fourth_score INT);";
        statement = this.connection.createStatement();
        statement.execute(sql);

        sql = "CREATE INDEX IF NOT EXISTS INDEXDATE ON INFO (date_time)";
        statement = this.connection.createStatement();
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS MJLOGINDEX(id TEXT PRIMARY KEY);";
        statement = this.connection.createStatement();
        statement.execute(sql);
    }

    void saveMjlog(String id, String content) throws SQLException {
        this.insertMjlogStatement.setString(1, id);
        this.insertMjlogStatement.setString(2, content);
        this.insertMjlogStatement.executeUpdate();
    }

    public void saveInfos(List<InfoSchema> infos) {
        try {
            this.connection.setAutoCommit(false);
            for (int i = 0; i < infos.size(); i++) {
                this.insertInfoStatement.setString(1, infos.get(i).getId());
                this.insertInfoStatement.setInt(2, infos.get(i).isSanma() ? 1 : 0);
                this.insertInfoStatement.setInt(3, infos.get(i).isTonnan() ? 1 : 0);
                this.insertInfoStatement.setString(4, infos.get(i).getDateTime().toString());
                this.insertInfoStatement.setInt(5, infos.get(i).getMinute());
                this.insertInfoStatement.setString(6, infos.get(i).getFirst());
                this.insertInfoStatement.setString(7, infos.get(i).getSecond());
                this.insertInfoStatement.setString(8, infos.get(i).getThird());
                this.insertInfoStatement.setString(9, infos.get(i).getFourth());
                this.insertInfoStatement.setInt(10, infos.get(i).getFirstScore());
                this.insertInfoStatement.setInt(11, infos.get(i).getSecondScore());
                this.insertInfoStatement.setInt(12, infos.get(i).getThirdScore());
                this.insertInfoStatement.setInt(13, infos.get(i).getFourthScore());
                this.insertInfoStatement.addBatch();

                // MAX_BATCH_SIZE 件ごとにバッチ処理
                if (i % MAX_BATCH_SIZE == MAX_BATCH_SIZE - 1 || i == infos.size() - 1) {
                    this.insertInfoStatement.executeBatch();
                }
            }
            this.connection.commit();
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public void saveMjlogIndex(String id) {
        try {
            this.insertMjlogIndexStatement.setString(1, id);
            this.insertMjlogIndexStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull Set<String> findAllMjlogIds() {
        try (ResultSet rs = this.findAllMjlogStatement.executeQuery()) {
            Set<String> result = new HashSet<>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public List<String> findAllMjlogContents() {
        try (ResultSet rs = this.findAllMjlogContent.executeQuery()) {
            List<String> result = new ArrayList<>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> findAllMjlogIndexIds() {
        try (ResultSet rs = this.findAllMjlogIndexStatement.executeQuery()) {
            HashSet<String> result = new HashSet<>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public String findMjlogById(@NotNull String id) {
        try {
            findMjlogByIdStatement.setString(1, id);
            ResultSet rs = this.findMjlogByIdStatement.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
        return null;
    }

    public void removeMjlogById(@NotNull String id) {
        try {
            removeMjlogByIdStatement.setString(1, id);
            removeMjlogByIdStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public List<InfoSchema> findAllInfos() {
        try (ResultSet rs = this.findAllInfoStatement.executeQuery()) {
            List<InfoSchema> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new InfoSchema.Builder(
                        rs.getString("id"),
                        rs.getInt("is_sanma") == 1,
                        rs.getInt("is_tonnan") == 1,
                        LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(rs.getString("date_time"))),
                        rs.getString("first"),
                        rs.getString("second"),
                        rs.getString("third"),
                        rs.getString("fourth")
                ).build());
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @NotNull
    public List<InfoSchema> findAllExistsInfos() {
        try (ResultSet rs = this.findAllExistsInfoStatement.executeQuery()) {
            List<InfoSchema> list = new ArrayList<>();
            while (rs.next()) {
                list.add(new InfoSchema.Builder(
                        rs.getString("id"),
                        rs.getInt("is_sanma") == 1,
                        rs.getInt("is_tonnan") == 1,
                        LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(rs.getString("date_time"))),
                        rs.getString("first"),
                        rs.getString("second"),
                        rs.getString("third"),
                        rs.getString("fourth")
                ).build());
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public List<String[]> findWinnerAndLoser(boolean isSanma, boolean isTonnan, String playerName) {
            String sqlStr = "SELECT first, " + (isSanma ? "third" : "fourth")
                + " FROM info WHERE is_sanma = " + (isSanma ? 1 : 0)
                + " AND is_tonnan = " + (isTonnan ? 1 : 0);
            if (!"".equals(playerName)) {
                sqlStr += " AND (first = ? OR ";
                sqlStr += (isSanma ? "third" : "fourth") + " = ?)";
            }

            try (PreparedStatement preparedStatement = this.connection.prepareStatement(sqlStr)) {
                if (!"".equals(playerName)) {
                    preparedStatement.setString(1, playerName);
                    preparedStatement.setString(2, playerName);
                }
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    List<String[]> list = new ArrayList<>();
                    while (rs.next()) {
                        String[] winnerAndLoser = new String[]{rs.getString(1), rs.getString(2)};
                        list.add(winnerAndLoser);
                    }
                    return list;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }

    public List<InfoSchema> findInfosByCriteria(String playerName, boolean isContentSanma, boolean isContentYonma,
                                                boolean isContentTonPu, boolean isContentTonnan, int limit, int offset) {
        List<InfoSchema> result = new ArrayList<>();
        String maCriterionString;
        if (isContentSanma) {
            if (isContentYonma) {
                maCriterionString = "1 = 1";
            } else {
                maCriterionString = "is_sanma = 1";
            }
        } else {
            if (isContentYonma) {
                maCriterionString = "is_sanma = 0";
            } else {
                return result;
            }
        }

        String souCriterionString;
        if (isContentTonPu) {
            if (isContentTonnan) {
                souCriterionString = "1 = 1";
            } else {
                souCriterionString = "is_tonnan = 0";
            }
        } else {
            if (isContentTonnan) {
                souCriterionString = "is_tonnan = 1";
            } else {
                return result;
            }
        }

        String playerCriterionString;
        if ("".equals(playerName)) {
            playerCriterionString = "";
        } else {
            playerCriterionString = " AND (first = ? OR second = ? OR third = ? OR fourth = ?)";
        }

        String sqlStr = "SELECT * FROM INFO WHERE " + souCriterionString + " AND " +
                maCriterionString + playerCriterionString + " ORDER BY date_time DESC LIMIT " + limit + " OFFSET " + offset + ";";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sqlStr)) {
            if (!"".equals(playerName)) {
                preparedStatement.setString(1, playerName);
                preparedStatement.setString(2, playerName);
                preparedStatement.setString(3, playerName);
                preparedStatement.setString(4, playerName);
            }
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    result.add(new InfoSchema.Builder(
                            rs.getString("id"),
                            rs.getInt("is_sanma") == 1,
                            rs.getInt("is_tonnan") == 1,
                            LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(rs.getString("date_time"))),
                            rs.getString("first"),
                            rs.getString("second"),
                            rs.getString("third"),
                            rs.getString("fourth")
                    ).build());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public boolean existsIdInMJLOG(String id) {
        String sqlStr = "SELECT id FROM MJLOG WHERE id = ?;";
        return existsId(id, sqlStr);
    }

    public boolean existsIdInINFO(String id) {
        String sqlStr = "SELECT id FROM INFO WHERE id = ?;";
        return existsId(id, sqlStr);
    }

    public boolean existsIdInMJLOGINDEX(String id) {
        String sqlStr = "SELECT id FROM MJLOGINDEX WHERE id = ?;";
        return existsId(id, sqlStr);
    }

    private boolean existsId(String id, String sqlStr) {
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sqlStr)) {
            preparedStatement.setString(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int countInfosByCriteria(String playerName, boolean isContentSanma, boolean isContentYonma, boolean isContentTonPu, boolean isContentTonnan) {
        String maCriterionString;
        if (isContentSanma) {
            if (isContentYonma) {
                maCriterionString = "1 = 1";
            } else {
                maCriterionString = "is_sanma = 1";
            }
        } else {
            if (isContentYonma) {
                maCriterionString = "is_sanma = 0";
            } else {
                return 0;
            }
        }

        String souCriterionString;
        if (isContentTonPu) {
            if (isContentTonnan) {
                souCriterionString = "1 = 1";
            } else {
                souCriterionString = "is_tonnan = 0";
            }
        } else {
            if (isContentTonnan) {
                souCriterionString = "is_tonnan = 1";
            } else {
                return 0;
            }
        }

        String playerCriterionString;
        if ("".equals(playerName)) {
            playerCriterionString = "";
        } else {
            playerCriterionString = " AND (first = ? OR second = ? OR third = ? OR fourth = ?)";
        }

        String sqlStr = "SELECT count(*) FROM INFO WHERE " + souCriterionString + " AND " + maCriterionString
                + playerCriterionString + ";";
        try (PreparedStatement countMaxPagesStatement = connection.prepareStatement(sqlStr)) {
            if (!"".equals(playerName)) {
                countMaxPagesStatement.setString(1, playerName);
                countMaxPagesStatement.setString(2, playerName);
                countMaxPagesStatement.setString(3, playerName);
                countMaxPagesStatement.setString(4, playerName);
            }
            ResultSet resultSet = countMaxPagesStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                throw new RuntimeException();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void dump(File file) throws SQLException {
        Statement statement = this.connection.createStatement();
        statement.execute("backup to " + file);
    }

    @Override
    public void close() throws IOException {
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
