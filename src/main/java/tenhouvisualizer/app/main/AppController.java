package tenhouvisualizer.app.main;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenhouvisualizer.Main;
import tenhouvisualizer.app.BindingHelper;
import tenhouvisualizer.domain.AnimationGifWriter;
import tenhouvisualizer.domain.model.InfoSchema;
import tenhouvisualizer.domain.model.Mjlog;
import tenhouvisualizer.domain.service.DatabaseService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE;

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
    private UkeireTableView ukeireTableView;
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

    public AppController() {
        this.databaseService = Main.databaseService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        List<InfoSchema> list = this.databaseService.findAllExistsInfos();
        this.infoSchemas.addAll(list);
        this.tableView.setItems(this.infoSchemas);

        this.dataTimeColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getDateTime().format(dateFormatter)));
        this.maColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().isSanma() ? "三" : "四"));
        this.souColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().isTonnan() ? "南" : "東"));
        this.firstColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getFirst()));
        this.secondColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getSecond()));
        this.thirdColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getThird()));
        this.fourthColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getFourth()));

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
                this.boardControl.initViewpoint();
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
                    this.boardControl.drawScene(newMjlog.getValue().getScene());
                    this.ukeireTableView.showUkeire(newMjlog.getValue().getScene());
                    this.label2.setText(newMjlog.getValue().getIdx() + "/" + newMjlog.getParent().getChildren().size()
                            + " " + newMjlog.getParent().toString());
                } else {
                    this.mjlogTreeControl.getSelectionModel().getSelectedItem().setExpanded(true);
                    this.label2.setText(newMjlog.toString());
                    this.boardControl.drawScene(newMjlog.getChildren().get(0).getValue().getScene());
                    this.ukeireTableView.init();
                }
            }
        });
    }

    private void resizeBoardControl() {
        double w = this.scrollPane.getWidth() - 5;
        double h = this.scrollPane.getHeight() - 30;

        this.boardControl.setWidth(Math.min(w, h));
        this.boardControl.setHeight(Math.min(w, h));
        this.boardControl.redrawScene();
    }

    public void moveViewpointLeft(ActionEvent actionEvent) {
        this.boardControl.moveViewpointLeft();
        this.mjlogTreeControl.requestFocus();
    }

    public void moveViewpointRight(ActionEvent actionEvent) {
        this.boardControl.moveViewpointRight();
        this.mjlogTreeControl.requestFocus();
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
        root.getStylesheets().add(this.getClass().getResource(Main.properties.getProperty("css")).toExternalForm());
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
        root.getStylesheets().add(this.getClass().getResource(Main.properties.getProperty("css")).toExternalForm());
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        stage.setScene(scene);
        stage.setTitle("解析");
        stage.show();
    }

    public void openDonationRanker(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initOwner(this.tableView.getScene().getWindow());

        Parent root = FXMLLoader.load(getClass().getResource("/DonationRanker.fxml"));
        root.getStylesheets().add(this.getClass().getResource(Main.properties.getProperty("css")).toExternalForm());
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        stage.setScene(scene);
        stage.setTitle("貢ぎランク");
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

    public void showAbout(ActionEvent actionEvent) throws URISyntaxException {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Tenhou Visualizer について");
        dialog.initOwner(this.root.getScene().getWindow());
        dialog.getDialogPane().getStylesheets().add(this.getClass().getResource(Main.properties.getProperty("css")).toExternalForm());
        dialog.getDialogPane().setGraphic(new ImageView(new Image("/logo.png")));
        dialog.getDialogPane().setHeaderText("TenhouVisualizer v0.3");
        final Hyperlink oss = new Hyperlink("open-source software");
        final URI uri = new URI("https://crazybbb.github.io/tenhou-visualizer/thirdparty");
        oss.setOnAction(e -> {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException e1) {
                throw new UncheckedIOException(e1);
            }
        });
        dialog.getDialogPane().setContent(new TextFlow(new Label("Powered by "), oss));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
}