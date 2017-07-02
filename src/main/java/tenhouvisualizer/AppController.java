package tenhouvisualizer;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import tenhodownloader.InfoSchema;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AppController implements Initializable {
    public ProgressBar progressBar;
    @FXML
    private BorderPane root;
    @FXML
    private Label label;
    @FXML
    private Label label2;
    @FXML
    private ListView<InfoSchema> listView;
    @FXML
    private BoardControl boardControl;
    @FXML
    private MjlogTreeControl mjlogTreeControl;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.listView.getItems().addAll(Main.databaseService.findAllExistsInfos());

        this.boardControl.drawScene();
//        this.label2.textProperty().bind(Bindings.concat(
//                Bindings.convert(Bindings.size(this.listView.getItems())),
//                new SimpleStringProperty("/"),
//                new SimpleStringProperty("NaN")) );
        this.listView.getSelectionModel().selectedItemProperty().addListener((obs, oldInfo, newInfo) -> {
            String xmlStr = Main.databaseService.findMjlogWithId(newInfo.getId());
            if (xmlStr != null) {
                byte[] xml = xmlStr.getBytes();
                this.mjlogTreeControl.showMjlogContent(xml, 0);
                this.mjlogTreeControl.getSelectionModel().select(this.mjlogTreeControl.getRoot()
                                                                    .getChildren().get(0).getChildren().get(0));
            }
        });

        this.mjlogTreeControl.getSelectionModel().selectedItemProperty().addListener((obs, oldMjlog, newMjlog) -> {
            if (newMjlog != null) {
                if (newMjlog.isLeaf()) {
                    this.boardControl.drawScene(newMjlog.getValue().getScene());
                } else {
                    this.mjlogTreeControl.getSelectionModel().getSelectedItem().setExpanded(true);
                }
            }
        });
    }

    @FXML
    public void onExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    @FXML
    public void openDownloader(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(listView.getScene().getWindow());

        Parent root = FXMLLoader.load(getClass().getResource("/Downloader.fxml"));
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        stage.setScene(scene);
        stage.setTitle("鳳凰卓牌譜ダウンロード");
        stage.show();

        stage.setOnHiding(event ->  {
            listView.getItems().clear();
            listView.getItems().addAll(Main.databaseService.findAllExistsInfos());
        });
    }

    @FXML
    public void openSyantenBackAnalyzer(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(listView.getScene().getWindow());

        Parent root = FXMLLoader.load(getClass().getResource("/SyantenBackAnalyzer.fxml"));
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        stage.setScene(scene);
        stage.setTitle("シャンテン後退解析");
        stage.show();
    }
}