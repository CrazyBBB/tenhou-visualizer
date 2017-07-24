package tenhouvisualizer.app.main;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenhouvisualizer.Main;
import tenhouvisualizer.app.BindingHelper;
import tenhouvisualizer.domain.AnimationGifWriter;
import tenhouvisualizer.domain.model.InfoSchema;
import tenhouvisualizer.domain.model.MahjongScene;
import tenhouvisualizer.domain.model.Mjlog;
import tenhouvisualizer.domain.service.DatabaseService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class AppController implements Initializable {

    private final static Logger log = LoggerFactory.getLogger(AppController.class);

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd");

    @FXML
    private BorderPane root;
    @FXML
    private ScrollPane scrollPane;
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
    private TableColumn<InfoSchema, String> firstColumn;
    @FXML
    private TableColumn<InfoSchema, String> secondColumn;
    @FXML
    private TableColumn<InfoSchema, String> thirdColumn;
    @FXML
    private TableColumn<InfoSchema, String> fourthColumn;
    @FXML
    private TableColumn<InfoSchema, String> maColumn;
    @FXML
    private TableColumn<InfoSchema, String> souColumn;
    @FXML
    private Button rotateRightButton;
    @FXML
    private Button rotateLeftButton;

    private ObservableList<InfoSchema> infoSchemas = FXCollections.observableArrayList();

    private final DatabaseService databaseService;

    private MahjongScene currentScene = null;
    private int numberOfRotation = 0;

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

        this.label.textProperty().bind(BindingHelper.covertOrDefault(this.tableView.getSelectionModel().selectedItemProperty(), ""));

        this.scrollPane.widthProperty().addListener(obs -> resizeBoardControl());
        this.scrollPane.heightProperty().addListener(obs -> resizeBoardControl());

        this.tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldInfo, newInfo) -> {
            if (newInfo != null) {
                numberOfRotation = 0;
                String xmlStr = this.databaseService.findMjlogById(newInfo.getId());
                if (xmlStr != null) {
                    byte[] xml = xmlStr.getBytes();
                    this.mjlogTreeControl.showMjlogContent(xml, 0);
                    this.mjlogTreeControl.getSelectionModel().selectFirst();
                }
            }
        });

        this.mjlogTreeControl.getSelectionModel().selectedItemProperty().addListener((obs, oldMjlog, newMjlog) -> {
            if (newMjlog != null) {
                if (newMjlog.isLeaf()) {
                    currentScene = newMjlog.getValue().getScene();
                    this.boardControl.drawScene(currentScene, numberOfRotation);
                    this.label2.setText(newMjlog.getValue().getIdx() + "/" + newMjlog.getParent().getChildren().size()
                            + " " + newMjlog.getParent().toString());
                } else {
                    this.mjlogTreeControl.getSelectionModel().getSelectedItem().setExpanded(true);
                    this.label2.setText(newMjlog.toString());
                    currentScene = newMjlog.getChildren().get(0).getValue().getScene();
                    this.boardControl.drawScene(newMjlog.getChildren().get(0).getValue().getScene(), numberOfRotation);
                }
            }
        });
    }

    private void resizeBoardControl() {
        double w = this.scrollPane.getWidth() - 5;
        double h = this.scrollPane.getHeight() - 30;

        this.boardControl.setWidth(Math.min(w, h));
        this.boardControl.setHeight(Math.min(w, h));
        if (currentScene != null) {
            this.boardControl.drawScene(currentScene, numberOfRotation);
        } else {
            this.boardControl.drawScene();
        }
    }

    public void rotateLeft(ActionEvent actionEvent) {
        if (this.mjlogTreeControl.getSelectionModel().getSelectedItem() != null) {
            numberOfRotation += 3;
            MjlogTreeItem item = (MjlogTreeItem) this.mjlogTreeControl.getSelectionModel().getSelectedItem();
            if (item.isLeaf()) {
                this.boardControl.drawScene(item.getValue().getScene(), numberOfRotation);
            } else {
                this.boardControl.drawScene(item.getChildren().get(0).getValue().getScene(), numberOfRotation);
            }
            this.mjlogTreeControl.requestFocus();
        }
    }

    public void rotateRight(ActionEvent actionEvent) {
        if (this.mjlogTreeControl.getSelectionModel().getSelectedItem() != null) {
            numberOfRotation += 1;
            MjlogTreeItem item = (MjlogTreeItem) this.mjlogTreeControl.getSelectionModel().getSelectedItem();
            if (item.isLeaf()) {
                this.boardControl.drawScene(item.getValue().getScene(), numberOfRotation);
            } else {
                this.boardControl.drawScene(item.getChildren().get(0).getValue().getScene(), numberOfRotation);
            }
            this.mjlogTreeControl.requestFocus();
        }
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

        stage.setOnHiding(event -> {
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
        stage.setTitle("解析");
        stage.show();
    }

    public void saveAsImage(ActionEvent actionEvent) throws IOException {
        if (this.tableView.getSelectionModel().getSelectedItem() == null) {
            // TableViewのアイテムが選択されてなければ、何もしない
            return;
        }
        TreeItem<Mjlog> mjlogTreeItem = this.mjlogTreeControl.getSelectionModel().getSelectedItem();
        if (mjlogTreeItem == null) {
            // TreeViewのアイテムが選択されてなければ、何もしない
            return;
        }
        if (mjlogTreeItem.isLeaf()) {
            // sceneツリービューで、シーンを選択していれば、静止画として保存
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("選択中のシーンを画像として保存");
            fileChooser.setInitialDirectory(new File("."));
            FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("JPEG", "*.jpg");
            FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG", "*.png");
            FileChooser.ExtensionFilter gifFilter = new FileChooser.ExtensionFilter("GIF", "*.gif");
            fileChooser.getExtensionFilters().addAll(jpgFilter, pngFilter, gifFilter);
            File file = fileChooser.showSaveDialog(this.root.getScene().getWindow());
            if (file != null) {
                int width = 600;
                int height = 600;
                BoardControl bc = new BoardControl();
                bc.setWidth(width);
                bc.setHeight(height);
                WritableImage writableImage = new WritableImage(width, height);
                bc.drawScene(mjlogTreeItem.getValue().getScene());
                bc.snapshot(null, writableImage);
                FileChooser.ExtensionFilter selectedExtensionFilter = fileChooser.getSelectedExtensionFilter();
                String formatName = selectedExtensionFilter.getDescription();
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), formatName, file);
            }
        } else {
            // sceneツリービューで、ゲームを選択していれば、アニメーションGIFとして保存
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("選択中の局をアニメーション画像として保存");
            fileChooser.setInitialDirectory(new File("."));
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Animation GIF", "*.gif"));
            File file = fileChooser.showSaveDialog(this.root.getScene().getWindow());
            if (file != null) {
                int width = 600;
                int height = 600;
                BoardControl bc = new BoardControl();
                bc.setWidth(width);
                bc.setHeight(height);
                try (AnimationGifWriter writer = new AnimationGifWriter(file, width, height)) {
                    WritableImage writableImage = new WritableImage(width, height);
                    BufferedImage bufferedImage = new BufferedImage(width, height, TYPE_INT_ARGB_PRE);
                    for (TreeItem<Mjlog> i : mjlogTreeItem.getChildren()) {
                        bc.drawScene(i.getValue().getScene());
                        bc.snapshot(null, writableImage);
                        SwingFXUtils.fromFXImage(writableImage, bufferedImage);
                        writer.writeImage(bufferedImage);
                    }
                }
            }
        }
    }
}