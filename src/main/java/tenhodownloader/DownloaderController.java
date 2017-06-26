package tenhodownloader;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class DownloaderController implements Initializable {
    public ListView<LocalDate> listView;
    private final DownloadService service = new DownloadService();
    public TableView<InfoSchema> tableView;
    public TableColumn<InfoSchema, String> idColumn;
    public TableColumn<InfoSchema, String> playersColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LocalDate from = LocalDate.of(2017, 1, 1);
        LocalDate to = LocalDate.now().minusDays(10);
        for (LocalDate i = from; to.isAfter(i); i = i.plusDays(1)) {
            this.listView.getItems().add(i);
        }
        this.tableView.setItems(this.service.list);
        this.idColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().id));
        this.playersColumn.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().payers));
    }

    public void downloadIndex(ActionEvent actionEvent) {
        if (listView.getSelectionModel().getSelectedItem() != null) {
            this.service.download(listView.getSelectionModel().getSelectedItem());
        }
    }

    public void downloadMjlog(ActionEvent actionEvent) {
        if (tableView.getSelectionModel().getSelectedItem() != null) {
            this.service.downloadMjlog(tableView.getSelectionModel().getSelectedItem());
        }
    }
}
