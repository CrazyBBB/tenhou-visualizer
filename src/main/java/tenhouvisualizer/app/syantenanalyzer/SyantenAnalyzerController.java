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
import javafx.util.Pair;
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

        showWinnerAndLoser(false, 10, 10);
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

    void showWinnerAndLoser(boolean isSanma, int matchMin, int showMax) {
        List<String[]> list = Main.databaseService.findWinnerAndLoser(isSanma);

        Map<String, Integer> sumMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();
        for (String[] winnerAndLoser : list) {
            String loserAndWinnerString = winnerAndLoser[1] + " -> " + winnerAndLoser[0];
            String winnerAndLoserString = winnerAndLoser[0] + " -> " + winnerAndLoser[1];
            if (sumMap.containsKey(winnerAndLoserString)) {
                sumMap.merge(winnerAndLoserString, 1, Integer::sum);
            } else {
                sumMap.merge(loserAndWinnerString, 1, Integer::sum);
                countMap.merge(loserAndWinnerString, 1, Integer::sum);
            }
        }

        List<Pair<String, Double>> candidates = new ArrayList<>();
        Set<String> keys = sumMap.keySet();
        for (String key : keys) {
            int sum = sumMap.get(key);
            if (sum < matchMin) continue;

            int count = countMap.get(key);
            if (count == sum - count) continue;

            String tmpKey;
            double percent;
            if (count > sum - count) {
                tmpKey = key + " " + count + "/" + sum;
                percent = (double) count / sum;
            } else {
                String[] split = key.split(" -> ");
                tmpKey = split[1] + " -> " + split[0] + " " + (sum - count) + "/" + sum;
                percent = (double) (sum - count) / sum;
            }
            candidates.add(new Pair<>(tmpKey, percent));
        }

        candidates.sort((c1, c2) -> Double.compare(c2.getValue(), c1.getValue()));

        for (int i = 0; i < showMax; i++) {
            log.debug("{}", candidates.get(i).getKey());
        }
    }
}