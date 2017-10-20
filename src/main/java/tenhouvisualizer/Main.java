package tenhouvisualizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenhouvisualizer.domain.service.DatabaseService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class Main extends Application {

    private final static Logger log = LoggerFactory.getLogger(Main.class);

    public static DatabaseService databaseService;
    public static Properties properties;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        log.info("start!");

        properties = new Properties();
        File configationFile = new File("./tenhouvisualizer.properties");
        if (configationFile.isFile()) {
            try (InputStream is = new FileInputStream(configationFile)) {
                properties.load(is);
            }
        }
        if (!properties.containsKey("css")) {
            properties.setProperty("css", "/darcula.css");
        }

        try {
            Main.databaseService = new DatabaseService(new File("./tenhouvisualizer.sqlite"));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        Parent root = FXMLLoader.load(getClass().getResource("/app.fxml"));
        root.getStylesheets().add(this.getClass().getResource(Main.properties.getProperty("css")).toExternalForm());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Tenhou Visualizer");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Main.databaseService.close();
        log.info("stop!");
    }
}