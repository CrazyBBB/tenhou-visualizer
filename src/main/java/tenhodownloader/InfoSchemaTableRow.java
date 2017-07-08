package tenhodownloader;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;

public class InfoSchemaTableRow extends TableRow<InfoSchema> {
    private final DownloaderController controller;

    public InfoSchemaTableRow(DownloaderController downloaderController) {
        this.controller = downloaderController;
    }

    @Override
    protected void updateItem(InfoSchema item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            this.setContextMenu(null);
        } else {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem filterWithFirst = new MenuItem("Filter with " + item.first);
            filterWithFirst.setOnAction(e -> updateFilter(item.first));
            MenuItem filterWithSecond = new MenuItem("Filter with " + item.second);
            filterWithSecond.setOnAction(e -> updateFilter(item.second));
            MenuItem filterWithThird = new MenuItem("Filter with " + item.third);
            filterWithThird.setOnAction(e -> updateFilter(item.third));
            contextMenu.getItems().addAll(filterWithFirst, filterWithSecond, filterWithThird);
            if (!"".equals(item.fourth)) {
                MenuItem filterWithFourth = new MenuItem("Filter with " + item.fourth);
                filterWithFourth.setOnAction(e -> updateFilter(item.fourth));
                contextMenu.getItems().add(filterWithFourth);
            }
            this.setContextMenu(contextMenu);
        }
    }

    private void updateFilter(String filter) {
        this.controller.filterField.setText(filter);
    }

}

