package tenhouvisualizer.app.main;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tenhouvisualizer.Main;
import tenhouvisualizer.app.BindingHelper;
import tenhouvisualizer.domain.model.InfoSchema;
import tenhouvisualizer.domain.service.DatabaseService;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AppController implements Initializable {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd");

    @FXML
    private BorderPane root;
    @FXML
    private Label label;
    @FXML
    private Label label2;
    @FXML
    private TableView<InfoSchema> tableView;
    @FXML
    private BoardControl boardControl;
    @FXML
    private MjlogTreeControl mjlogTreeControl;

    @FXML
    private TableColumn<InfoSchema, String> dataTimeColumn;
    @FXML
    private TableColumn<InfoSchema, String>  firstColumn;
    @FXML
    private TableColumn<InfoSchema, String>  secondColumn;
    @FXML
    private TableColumn<InfoSchema, String>  thirdColumn;
    @FXML
    private TableColumn<InfoSchema, String>  fourthColumn;
    @FXML
    private TableColumn<InfoSchema, String>  maColumn;
    @FXML
    private TableColumn<InfoSchema, String> souColumn;

    private ObservableList<InfoSchema> infoSchemas = FXCollections.observableArrayList();

    private final DatabaseService databaseService;

    public AppController() {
        this.databaseService = Main.databaseService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<InfoSchema> list = this.databaseService.findAllExistsInfos();
        this.infoSchemas.addAll(list);
        this.tableView.setItems(this.infoSchemas);

        this.dataTimeColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().dateTime.format(dateFormatter)));
        this.maColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().ma));
        this.souColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().sou));
        this.firstColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().first));
        this.secondColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().second));
        this.thirdColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().third));
        this.fourthColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().fourth));

        this.maColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.2));
        this.maColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.1));
        this.souColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.1));
        this.firstColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.15));
        this.secondColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.15));
        this.thirdColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.15));
        this.fourthColumn.prefWidthProperty().bind(this.tableView.widthProperty().multiply(0.15));

        this.label.textProperty().bind(BindingHelper.covertOtherwiseNull(this.tableView.getSelectionModel().selectedItemProperty(), ""));

        this.boardControl.drawScene();

        this.tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldInfo, newInfo) -> {
            if (newInfo != null) {
                String xmlStr = this.databaseService.findMjlogById(newInfo.getId());
                if (xmlStr != null) {
                    byte[] xml = xmlStr.getBytes();
                    this.mjlogTreeControl.showMjlogContent(xml, 0);
                    this.mjlogTreeControl.getSelectionModel().select(this.mjlogTreeControl.getRoot()
                            .getChildren().get(0).getChildren().get(0));
                }
            }
        });

        this.mjlogTreeControl.getSelectionModel().selectedItemProperty().addListener((obs, oldMjlog, newMjlog) -> {
            if (newMjlog != null) {
                if (newMjlog.isLeaf()) {
                    this.boardControl.drawScene(newMjlog.getValue().getScene());
                    this.label2.setText(newMjlog.getValue().getIdx() + "/" + newMjlog.getParent().getChildren().size()
                                            + " " + newMjlog.getParent().toString());
                } else {
                    this.mjlogTreeControl.getSelectionModel().getSelectedItem().setExpanded(true);
                    this.label2.setText(newMjlog.toString());
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
        stage.initOwner(this.tableView.getScene().getWindow());

        Parent root = FXMLLoader.load(getClass().getResource("/Downloader.fxml"));
        root.getStylesheets().add(this.getClass().getResource("/darcula.css").toExternalForm());
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        stage.setScene(scene);
        stage.setTitle("鳳凰卓牌譜ダウンロード");
        stage.show();

        stage.setOnHiding(event ->  {
            this.tableView.getItems().clear();
            this.tableView.getItems().addAll(this.databaseService.findAllExistsInfos());
        });
    }

    @FXML
    public void openSyantenBackAnalyzer(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(this.tableView.getScene().getWindow());

        Parent root = FXMLLoader.load(getClass().getResource("/SyantenBackAnalyzer.fxml"));
        root.getStylesheets().add(this.getClass().getResource("/darcula.css").toExternalForm());
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        stage.setScene(scene);
        stage.setTitle("シャンテン後退解析");
        stage.show();
    }
}