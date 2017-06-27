package tenhodownloader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tenhouvisualizer.App;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        try {
            App.databaseService = new DatabaseService(new File("./tenhouvisualizer.sqlite"));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        Parent root = FXMLLoader.load(getClass().getResource("/Downloader.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Tenhou Downloader");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        App.databaseService.close();
    }
}