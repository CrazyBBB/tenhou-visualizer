package tenhouvisualizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tenhodownloader.DatabaseService;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class App extends Application {

    public static DatabaseService databaseService;

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

        Parent root = FXMLLoader.load(getClass().getResource("/app.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Tenhou Visualizer");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        App.databaseService.close();
    }
}