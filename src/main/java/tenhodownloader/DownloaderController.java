package tenhodownloader;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
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
import java.util.ResourceBundle;

public class DownloaderController implements Initializable {
    private static final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日");
    private static final DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日H時台");
    private static final DateTimeFormatter minuteFormatter = DateTimeFormatter.ofPattern("yyyy年M月d日H時m分");

    public TabPane tabPane;
    public ListView<LocalDate> dateListView;
    public ListView<LocalDateTime> hourListView;
    private final DownloadService service = new DownloadService();
    public TableView<InfoSchema> tableView;
    public Label statusBarLabel;
    public Tab currentYearTab;
    public Tab currentWeekTab;
    public TableColumn<InfoSchema, String> downloadColumn;
    public TableColumn<InfoSchema, String> dateTimeColumn;
    public TableColumn<InfoSchema, String>  firstColumn;
    public TableColumn<InfoSchema, String>  secondColumn;
    public TableColumn<InfoSchema, String>  thirdColumn;
    public TableColumn<InfoSchema, String>  fourthColumn;
    public TableColumn<InfoSchema, String>  maColumn;
    public TableColumn<InfoSchema, String> souColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.dateListView.setCellFactory(e -> new ListCell<LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item.format(dayFormatter));
                }
            }
        });
        this.hourListView.setCellFactory(e -> new ListCell<LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item.format(hourFormatter));
                }
            }
        });
        {
            LocalDate from = LocalDate.of(2017, 1, 1);
            LocalDate to = LocalDate.now().minusDays(7);
            for (LocalDate i = from; to.isAfter(i); i = i.plusDays(1)) {
                this.dateListView.getItems().add(i);
            }
        }

        {
            LocalDateTime from = LocalDateTime.of(LocalDate.now().minusDays(7), LocalTime.MIN);
            LocalDateTime to = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
            for (LocalDateTime i = from; to.isAfter(i); i = i.plusHours(1)) {
                this.hourListView.getItems().add(i);
            }
        }

        List<InfoSchema> list = Main.databaseService.findAllInfos();
        this.service.infoSchemas.addAll(list);
        this.tableView.setItems(this.service.infoSchemas);

        this.downloadColumn.setCellValueFactory(e ->
                new SimpleStringProperty(this.service.isDownloaded(e.getValue()) ? "✓" : "")
        );
        this.dateTimeColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().dateTime.format(minuteFormatter)));
        this.maColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().ma));
        this.souColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().sou));
        this.firstColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().first));
        this.secondColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().second));
        this.thirdColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().third));
        this.fourthColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().fourth));

        this.downloadColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.066));
        this.dateTimeColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.2));
        this.maColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.066));
        this.souColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.066));
        this.firstColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        this.secondColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        this.thirdColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        this.fourthColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));

        this.statusBarLabel.textProperty().bind(Bindings.convert(Bindings.size(this.tableView.getItems())));
    }

    public void downloadIndex(ActionEvent actionEvent) {
        if (tabPane.getSelectionModel().getSelectedItem() == currentYearTab) {
            if (dateListView.getSelectionModel().getSelectedItem() != null) {
                this.service.downloadDate(dateListView.getSelectionModel().getSelectedItem());
            }
        } else if (tabPane.getSelectionModel().getSelectedItem() == currentWeekTab) {
            if (hourListView.getSelectionModel().getSelectedItem() != null) {
                this.service.downloadHour(hourListView.getSelectionModel().getSelectedItem());
            }
        }
    }

    public void downloadMjlog(ActionEvent actionEvent) {
        if (tableView.getSelectionModel().getSelectedItem() != null) {
            InfoSchema infoSchema = tableView.getSelectionModel().getSelectedItem();
            if (!Main.databaseService.existsIdInMJLOG(infoSchema.id)) {
                this.service.downloadMjlogToDatabase(infoSchema);
                tableView.getItems().set(tableView.getSelectionModel().getFocusedIndex(), infoSchema);
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
            Main.databaseService.dump(selectedFile);
        }
    }

    public void exportMjlog(ActionEvent actionEvent) throws IOException {
        if (tableView.getSelectionModel().getSelectedItem() != null) {
            InfoSchema infoSchema = tableView.getSelectionModel().getSelectedItem();
            String content = Main.databaseService.findMjlogById(infoSchema.getId());
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
}
