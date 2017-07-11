package tenhodownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + (file == null ? "" : file));
        initialize();
        this.insertMjlogStatement = connection.prepareStatement("INSERT INTO MJLOG VALUES(?, ?);");
        this.findAllMjlogStatement = connection.prepareStatement("SELECT id FROM MJLOG;");
        this.findAllMjlogContent = connection.prepareStatement("SELECT content FROM MJLOG;");
        this.findMjlogByIdStatement = connection.prepareStatement("SELECT content FROM MJLOG WHERE id = ?;");
        this.removeMjlogByIdStatement = connection.prepareStatement("DELETE FROM MJLOG WHERE id = ?;");
        this.insertInfoStatement = connection.prepareStatement("INSERT INTO INFO VALUES(?, ?, ?, ?, ?, ?, ?, ?);");
        this.findAllInfoStatement = connection.prepareStatement("SELECT * FROM INFO;");
        this.findAllExistsInfoStatement = connection.prepareStatement("SELECT * FROM INFO WHERE id in (SELECT id FROM MJLOG);");
        this.insertMjlogIndexStatement = connection.prepareStatement("INSERT INTO MJLOGINDEX VALUES(?);");
        this.findAllMjlogIndexStatement = connection.prepareStatement("SELECT id FROM MJLOGINDEX;");
    }

    private void initialize() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS MJLOG(id TEXT PRIMARY KEY, content TEXT);";
        Statement statement = this.connection.createStatement();
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS INFO(id TEXT PRIMARY KEY, ma TEXT, sou TEXT," +
                " first TEXT, second TEXT, third TEXT, fourth TEXT, date_time TEXT);";
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

    void saveInfo(InfoSchema infoSchema) {
        try {
            this.insertInfoStatement.setString(1, infoSchema.id);
            this.insertInfoStatement.setString(2, infoSchema.ma);
            this.insertInfoStatement.setString(3, infoSchema.sou);
            this.insertInfoStatement.setString(4, infoSchema.first);
            this.insertInfoStatement.setString(5, infoSchema.second);
            this.insertInfoStatement.setString(6, infoSchema.third);
            this.insertInfoStatement.setString(7, infoSchema.fourth);
            this.insertInfoStatement.setString(8, infoSchema.dateTime.toString());
            this.insertInfoStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    void saveMjlogIndex(String id) {
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
                list.add(new InfoSchema(
                        rs.getString("id"),
                        rs.getString("ma"),
                        rs.getString("sou"),
                        rs.getString("first"),
                        rs.getString("second"),
                        rs.getString("third"),
                        rs.getString("fourth"),
                        LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(rs.getString("date_time")))
                ));
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
                list.add(new InfoSchema(
                        rs.getString("id"),
                        rs.getString("ma"),
                        rs.getString("sou"),
                        rs.getString("first"),
                        rs.getString("second"),
                        rs.getString("third"),
                        rs.getString("fourth"),
                        LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(rs.getString("date_time")))
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException();
        }
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

    void dump(File file) throws SQLException {
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
