package tenhouvisualizer.app.syantenanalyzer;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import tenhouvisualizer.Main;
import tenhouvisualizer.domain.task.AnalyzeDBTask;
import tenhouvisualizer.domain.task.AnalyzeZipTask;
import tenhouvisualizer.app.main.BoardControl;
import tenhouvisualizer.domain.model.MahjongScene;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class SyantenAnalyzerController implements Initializable {

    private final static Logger log = LoggerFactory.getLogger(SyantenAnalyzerController.class);

    public ProgressBar progressBar;
    public MenuItem openMenuItem;
    @FXML
    private BorderPane root;
    @FXML
    private Label label;
    @FXML
    private Label progressLabel;
    @FXML
    private ListView<MahjongScene> listView;
    @FXML
    private BoardControl boardControl;

    private File lastSelectedFile = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.boardControl.drawScene();
//        this.progressLabel.textProperty().bind(Bindings.concat(
//                Bindings.convert(Bindings.size(this.listView.getItems())),
//                new SimpleStringProperty("/"),
//                new SimpleStringProperty("NaN")) );
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                this.boardControl.drawScene(newScene);
                this.label.setText(newScene.toString());
            }
        });

        showWinnerAndLoser();
    }

//    @FXML
//    public void onExit(ActionEvent actionEvent) {
//        Platform.exit();
//    }

    @FXML
    public void analyzeZIP(ActionEvent actionEvent) throws IOException, ParserConfigurationException, SAXException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(lastSelectedFile == null ? new File(".") : lastSelectedFile);
        File selectedFile = fc.showOpenDialog(root.getScene().getWindow());

        if (selectedFile != null) {
            lastSelectedFile = new File(selectedFile.getParent());

            Task task = new AnalyzeZipTask(selectedFile, listView.getItems());
            this.progressBar.progressProperty().bind(task.progressProperty());
            this.progressLabel.textProperty().bind(task.messageProperty());
            this.openMenuItem.disableProperty().bind(task.runningProperty());
            new Thread(task).start();
        }
    }

    @FXML
    public void analyzeDB(ActionEvent actionEvent) {
        Task task = new AnalyzeDBTask(listView.getItems());
        this.progressBar.progressProperty().bind(task.progressProperty());
        this.progressLabel.textProperty().bind(task.messageProperty());
        this.openMenuItem.disableProperty().bind(task.runningProperty());
        new Thread(task).start();
    }

    void showWinnerAndLoser() {
        List<List<String>> list = Main.databaseService.findSanmaWinnerAndLoser();
        Map<String, Integer> map = new HashMap<>();
        for (List<String> winnerAndLoser : list) {
            String winnerAndLoserString = winnerAndLoser.get(1) + " -> " + winnerAndLoser.get(0);
            map.put(winnerAndLoserString, map.getOrDefault(winnerAndLoserString, 0) + 1);
        }

        List<WinnerAndLoserCount> counts = new ArrayList<>();
        Set<String> keys = map.keySet();
        for (String key : keys) {
            counts.add(new WinnerAndLoserCount(key, map.get(key)));
        }

        Collections.sort(counts);
        for (int i = 0; i < 10; i++) {
            log.debug(counts.get(i).count + "回 \t" + counts.get(i).winnerAndLoserString);
        }
    }

    class WinnerAndLoserCount implements Comparable<WinnerAndLoserCount> {
        String winnerAndLoserString;
        int count;

        public WinnerAndLoserCount(String winnerAndLoserString, int count) {
            this.winnerAndLoserString = winnerAndLoserString;
            this.count = count;
        }

        @Override
        public int compareTo(@NotNull SyantenAnalyzerController.WinnerAndLoserCount o) {
            return -(count - o.count);
        }
    }
}