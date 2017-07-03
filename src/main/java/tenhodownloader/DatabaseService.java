package tenhodownloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseService implements Closeable {
    private final Connection connection;
    private final PreparedStatement insertMjlogStatement;
    private final PreparedStatement insertInfoStatement;
    private final PreparedStatement findAllMjlogStatement;
    private final PreparedStatement findAllMjlogContent;
    private final PreparedStatement findMjlogByIdStatement;
    private final PreparedStatement findAllInfoStatement;
    private final PreparedStatement findAllExistsInfoStatement;
    public DatabaseService(@Nullable File file) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + (file == null ? "" : file));
        initialize();
        this.insertMjlogStatement = connection.prepareStatement("INSERT INTO MJLOG VALUES(?, ?);");
        this.insertInfoStatement = connection.prepareStatement("INSERT INTO INFO VALUES(?, ?, ?, ?, ?, ?, ?, ?);");
        this.findAllMjlogStatement = connection.prepareStatement("SELECT id FROM MJLOG;");
        this.findAllMjlogContent = connection.prepareStatement("SELECT content FROM MJLOG;");
        this.findMjlogByIdStatement = connection.prepareStatement("SELECT content FROM MJLOG WHERE id = ?;");
        this.findAllInfoStatement = connection.prepareStatement("SELECT * FROM INFO;");
        this.findAllExistsInfoStatement = connection.prepareStatement("SELECT * FROM INFO WHERE id in (SELECT id FROM MJLOG);");
    }

    private void initialize() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS MJLOG(id TEXT PRIMARY KEY, content TEXT);";
        Statement statement = this.connection.createStatement();
        statement.execute(sql);

        sql = "CREATE TABLE IF NOT EXISTS INFO(id TEXT PRIMARY KEY, ma TEXT, sou TEXT," +
                                                    " first TEXT, second TEXT, third TEXT, fourth TEXT, date_time TEXT);";
        statement = this.connection.createStatement();
        statement.execute(sql);
    }

    void saveMjlog(String id, String content) throws SQLException {
        this.insertMjlogStatement.setString(1, id);
        this.insertMjlogStatement.setString(2, content);
        this.insertMjlogStatement.executeUpdate();
    }

    void saveInfo(String id, String ma, String sou,
                    String first, String second, String third, String fourth, String dateTime) {
        try {
            this.insertInfoStatement.setString(1, id);
            this.insertInfoStatement.setString(2, ma);
            this.insertInfoStatement.setString(3, sou);
            this.insertInfoStatement.setString(4, first);
            this.insertInfoStatement.setString(5, second);
            this.insertInfoStatement.setString(6, third);
            this.insertInfoStatement.setString(7, fourth);
            this.insertInfoStatement.setString(8, dateTime);
            this.insertInfoStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    @NotNull Set<String> findAllMjlogIds() throws SQLException {
        ResultSet rs = this.findAllMjlogStatement.executeQuery();
        Set<String> result = new HashSet<>();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
    }

    @NotNull
    public List<String> findAllMjlogContents() throws SQLException {
        ResultSet rs = this.findAllMjlogContent.executeQuery();
        List<String> result = new ArrayList<>();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
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

    @NotNull
    public List<InfoSchema> findAllInfos() {
        List<InfoSchema> list = new ArrayList<>();
        try {
            ResultSet rs = this.findAllInfoStatement.executeQuery();
            while (rs.next()) {
                list.add(new InfoSchema(
                        rs.getString("id"),
                        rs.getString("ma"),
                        rs.getString("sou"),
                        rs.getString("first"),
                        rs.getString("second"),
                        rs.getString("third"),
                        rs.getString("fourth"),
                        LocalDateTime.now() // todo
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
        return list;
    }

    @NotNull
    public List<InfoSchema> findAllExistsInfos() {
        List<InfoSchema> list = new ArrayList<>();
        try {
            ResultSet rs = this.findAllExistsInfoStatement.executeQuery();
            while (rs.next()) {
                list.add(new InfoSchema(
                        rs.getString("id"),
                        rs.getString("ma"),
                        rs.getString("sou"),
                        rs.getString("first"),
                        rs.getString("second"),
                        rs.getString("third"),
                        rs.getString("fourth"),
                        LocalDateTime.now() // todo
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }
        return list;
    }

    public boolean existsIdInMJLOG(String id) {
        String sqlStr = "SELECT id FROM MJLOG WHERE id = ?;";
        return existsId(id, sqlStr);
    }

    public boolean existsIdInINFO(String id) {
        String sqlStr = "SELECT id FROM INFO WHERE id = ?;";
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
