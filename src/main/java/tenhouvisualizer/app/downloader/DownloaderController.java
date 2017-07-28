package tenhouvisualizer.app.downloader;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenhouvisualizer.domain.model.InfoSchema;
import tenhouvisualizer.domain.service.DatabaseService;
import tenhouvisualizer.domain.service.DownloadService;
import tenhouvisualizer.Main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class DownloaderController implements Initializable {

    private final static Logger log = LoggerFactory.getLogger(DownloaderController.class);

    private static final int maxInfosPerPage = 100;
    private static final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    private static final DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日HH時台");
    private static final DateTimeFormatter minuteFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
    public BorderPane root;

    private DatabaseService databaseService;
    public TabPane tabPane;
    public ListView<Integer> yearListView;
    public ListView<LocalDate> dateListView;
    public ListView<LocalDateTime> hourListView;
    private final DownloadService service = new DownloadService();
    public TableView<InfoSchema> tableView;
    public Label statusBarLabel;
    public Tab pastYearsTab;
    public Tab currentYearTab;
    public Tab currentWeekTab;
    public TableColumn<InfoSchema, String> downloadColumn;
    public TableColumn<InfoSchema, String> dateTimeColumn;
    public TableColumn<InfoSchema, String> firstColumn;
    public TableColumn<InfoSchema, String> secondColumn;
    public TableColumn<InfoSchema, String> thirdColumn;
    public TableColumn<InfoSchema, String> fourthColumn;
    public TableColumn<InfoSchema, String> maColumn;
    public TableColumn<InfoSchema, String> souColumn;
    public TextField filterField;
    public ProgressBar progressBar;
    public Label progressLabel;
    public Button indexButton;
    public Button prevButton;
    public Button nextButton;
    public Button clearButton;
    public CheckBox sanmaCheckBox;
    public CheckBox yonmaCheckBox;
    public CheckBox tonpuCheckBox;
    public CheckBox tonnanCheckBox;

    private final StringProperty textToHighlight = new SimpleStringProperty(null);

    private int pageIndex = 0;
    private String playerName = "";
    private boolean isContentSanma = true;
    private boolean isContentYonma = true;
    private boolean isContentTonPu = true;
    private boolean isContentTonnan = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.databaseService = Main.databaseService;

        this.clearButton.visibleProperty().bind(Bindings.isNotEmpty(this.filterField.textProperty()));
        this.yearListView.setCellFactory(e -> new ListCell<Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item + "年");
            }
        });
        this.dateListView.setCellFactory(e -> new ListCell<LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.format(dayFormatter));
            }
        });
        this.hourListView.setCellFactory(e -> new ListCell<LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item.format(hourFormatter));
            }
        });

        this.yearListView.getItems().addAll(this.service.createDownloadableYearList());
        this.dateListView.getItems().addAll(this.service.createDownloadableDateList());
        this.hourListView.getItems().addAll(this.service.createDownloadableHourList());

        changeResult();
        this.tableView.setItems(this.service.infoSchemas);

        this.downloadColumn.setCellValueFactory(e ->
                new SimpleStringProperty(this.service.isDownloaded(e.getValue()) ? "✓" : "")
        );
        this.dateTimeColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().dateTime.format(minuteFormatter)));
        this.maColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().isSanma ? "三" : "四"));
        this.souColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().isTonnan ? "南" : "東"));
        this.firstColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().first));
        this.secondColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().second));
        this.thirdColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().third));
        this.fourthColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().fourth));

        this.tableView.setRowFactory(e -> new InfoSchemaTableRow(this, this.service));

        this.firstColumn.setCellFactory(e -> new HighlightCell(textToHighlight));
        this.secondColumn.setCellFactory(e -> new HighlightCell(textToHighlight));
        this.thirdColumn.setCellFactory(e -> new HighlightCell(textToHighlight));
        this.fourthColumn.setCellFactory(e -> new HighlightCell(textToHighlight));
    }

    public void downloadIndex(ActionEvent actionEvent) {
        try {
            if (this.tabPane.getSelectionModel().getSelectedItem() == this.pastYearsTab) {
                Integer year = this.yearListView.getSelectionModel().getSelectedItem();
                if (year != null) {
                    if (this.databaseService.existsIdInMJLOGINDEX(year.toString())) return;
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.initOwner(root.getScene().getWindow());
                    alert.setHeaderText("ダウンロードの確認");
                    alert.getDialogPane().getStylesheets().add(this.getClass().getResource("/darcula.css").toExternalForm());
                    String str = "昨年以前のインデックスは年単位でダウンロードするので" +
                            "時間がかかります。よろしいですか？";
                    alert.setContentText(str);
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        Task task = this.service.createDownloadYearTask(year);
                        this.progressBar.progressProperty().bind(task.progressProperty());
                        this.progressLabel.textProperty().bind(task.messageProperty());
                        this.indexButton.disableProperty().bind(task.runningProperty());
                        task.setOnSucceeded(a -> {
                            this.changeResult();
                            this.databaseService.saveMjlogIndex(year.toString());
                            this.yearListView.getItems().remove(year);
                        });
                        new Thread(task).start();
                    }
                }
            } else if (this.tabPane.getSelectionModel().getSelectedItem() == this.currentYearTab) {
                LocalDate localDate = this.dateListView.getSelectionModel().getSelectedItem();
                if (localDate != null) {
                    if (this.databaseService.existsIdInMJLOGINDEX(localDate.toString())) return;
                    this.service.downloadDate(localDate);
                    this.databaseService.saveMjlogIndex(localDate.toString());
                    this.dateListView.getItems().remove(localDate);
                    this.dateListView.getSelectionModel().clearSelection();
                }
            } else if (this.tabPane.getSelectionModel().getSelectedItem() == this.currentWeekTab) {
                LocalDateTime localDateTime = this.hourListView.getSelectionModel().getSelectedItem();
                if (localDateTime != null) {
                    if (this.databaseService.existsIdInMJLOGINDEX(localDateTime.toString())) return;
                    this.service.downloadHour(localDateTime);
                    this.databaseService.saveMjlogIndex(localDateTime.toString());
                    this.hourListView.getItems().remove(localDateTime);
                    this.hourListView.getSelectionModel().clearSelection();
                }
            }
            changeResult();
        } catch (IOException e) {
            log.error("インデックスのダウンロード中にエラーが発生したっぽい", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initOwner(root.getScene().getWindow());
            alert.getDialogPane().getStylesheets().add(this.getClass().getResource("/darcula.css").toExternalForm());
            alert.getDialogPane().setHeaderText("インデックス追加の失敗");
            alert.getDialogPane().setContentText("インデックスを追加することができませんでした");
            alert.show();
        }
    }

    public void downloadMjlog(ActionEvent actionEvent) {
        if (this.tableView.getSelectionModel().getSelectedItem() != null) {
            InfoSchema infoSchema = this.tableView.getSelectionModel().getSelectedItem();
            if (!this.databaseService.existsIdInMJLOG(infoSchema.id)) {
                try {
                    this.service.downloadMjlogToDatabase(infoSchema);
                } catch (IOException | SQLException e) {
                    log.error("ログのダウンロード中にエラーが発生したっぽい", e);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initOwner(root.getScene().getWindow());
                    alert.getDialogPane().getStylesheets().add(this.getClass().getResource("/darcula.css").toExternalForm());
                    alert.getDialogPane().setHeaderText("牌譜追加の失敗");
                    alert.getDialogPane().setContentText("牌譜を追加することができませんでした");
                    alert.show();
                }
                this.tableView.getItems().set(this.tableView.getSelectionModel().getFocusedIndex(), infoSchema);
            }
        }
    }

    public void dump(ActionEvent actionEvent) throws SQLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("SQLite Files", "*.sqlite"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File selectedFile = fileChooser.showSaveDialog(this.dateListView.getScene().getWindow());
        if (selectedFile != null) {
            this.databaseService.dump(selectedFile);
        }
    }

    public void exportMjlog(ActionEvent actionEvent) throws IOException {
        if (this.tableView.getSelectionModel().getSelectedItem() != null) {
            InfoSchema infoSchema = this.tableView.getSelectionModel().getSelectedItem();
            String content = this.databaseService.findMjlogById(infoSchema.getId());
            if (content != null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(infoSchema.id);
                fileChooser.setTitle("Save mjlog File");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("mjlog Files", "*.mjlog"));
                File selectedFile = fileChooser.showSaveDialog(this.dateListView.getScene().getWindow());
                if (selectedFile != null) {
                    Files.copy(new ByteArrayInputStream(content.getBytes()), selectedFile.toPath());
                }
            }
        }
    }

    public void removeMjlog(ActionEvent actionEvent) {
        if (this.tableView.getSelectionModel().getSelectedItem() != null) {
            InfoSchema infoSchema = this.tableView.getSelectionModel().getSelectedItem();
            if (this.databaseService.existsIdInMJLOG(infoSchema.id)) {
                this.databaseService.removeMjlogById(infoSchema.id);
                this.service.removeInfoSchema(infoSchema);
                this.tableView.getItems().set(this.tableView.getSelectionModel().getFocusedIndex(), infoSchema);
            }
        }
    }

    public void clearFilterField(ActionEvent actionEvent) {
        this.filterField.clear();
        this.filterField.requestFocus();
    }

    public void onExit(ActionEvent actionEvent) {
        Stage stage = (Stage) this.tabPane.getScene().getWindow();
        stage.close();
    }

    public void goPrevPage(ActionEvent actionEvent) {
        pageIndex--;
        changeResult();
    }

    public void goNextPage(ActionEvent actionEvent) {
        pageIndex++;
        changeResult();
    }

    private void changeResult() {
        if (pageIndex <= 0) {
            this.prevButton.setDisable(true);
        } else {
            this.prevButton.setDisable(false);
        }
        int count = this.databaseService.countInfosByCriteria(playerName, isContentSanma, isContentYonma, isContentTonPu, isContentTonnan);
        if (pageIndex + 1 >= (count + maxInfosPerPage - 1) / maxInfosPerPage) {
            this.nextButton.setDisable(true);
        } else {
            this.nextButton.setDisable(false);
        }
        this.service.infoSchemas.clear();
        List<InfoSchema> list = this.databaseService.findInfosByCriteria(playerName, isContentSanma, isContentYonma,
                isContentTonPu, isContentTonnan, maxInfosPerPage, pageIndex * maxInfosPerPage);
        this.service.infoSchemas.addAll(list);
        int start = count == 0 ? 0 : pageIndex * maxInfosPerPage + 1;
        int end = start + maxInfosPerPage - 1 < count ? start + maxInfosPerPage - 1 : count;
        this.statusBarLabel.setText(count + "件中" + start + "件~" + end + "件");
        this.tableView.scrollTo(0);
    }

    public void search(ActionEvent actionEvent) {
        pageIndex = 0;
        playerName = this.filterField.getText();
        isContentSanma = this.sanmaCheckBox.selectedProperty().get();
        isContentYonma = this.yonmaCheckBox.selectedProperty().get();
        isContentTonPu = this.tonpuCheckBox.selectedProperty().get();
        isContentTonnan = this.tonnanCheckBox.selectedProperty().get();
        textToHighlight.setValue("".equals(playerName) ? null : playerName);
        changeResult();
    }

    public void clear(ActionEvent actionEvent) {
        this.filterField.clear();
        this.sanmaCheckBox.selectedProperty().setValue(true);
        this.yonmaCheckBox.selectedProperty().setValue(true);
        this.tonpuCheckBox.selectedProperty().setValue(true);
        this.tonnanCheckBox.selectedProperty().setValue(true);
        search(null);
    }
}
