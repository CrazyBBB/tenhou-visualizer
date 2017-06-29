package tenhodownloader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DatabaseService implements Closeable {
    private final Connection connection;
    private final PreparedStatement insertMjlogStatement;
    private final PreparedStatement findAllMjlogStatement;
    private final PreparedStatement findAllMjlogContent;
    public DatabaseService(File file) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + (file == null ? "" : file));
        initialize();
        this.insertMjlogStatement = connection.prepareStatement("INSERT INTO MJLOG VALUES(?, ?);");
        this.findAllMjlogStatement = connection.prepareStatement("SELECT id FROM MJLOG;");
        this.findAllMjlogContent = connection.prepareStatement("SELECT content FROM MJLOG;");
    }

    private void initialize() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS MJLOG(id TEXT PRIMARY KEY, content text);";
        Statement statement = this.connection.createStatement();
        statement.execute(sql);
    }

    void saveMjlog(String id, String content) throws SQLException {
        this.insertMjlogStatement.setString(1, id);
        this.insertMjlogStatement.setString(2, content);
        this.insertMjlogStatement.executeUpdate();
    }

    Set<String> findAllMjlogIds() throws SQLException {
        ResultSet rs = this.findAllMjlogStatement.executeQuery();
        Set<String> result = new HashSet<>();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
    }

    public Set<String> findAllMjlogContents() throws SQLException {
        ResultSet rs = this.findAllMjlogContent.executeQuery();
        Set<String> result = new HashSet<>();
        while (rs.next()) {
            result.add(rs.getString(1));
        }
        return result;
    }

    public boolean existsId(String id) {
        String sqlStr = "SELECT id FROM MJLOG WHERE id = ?;";
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
