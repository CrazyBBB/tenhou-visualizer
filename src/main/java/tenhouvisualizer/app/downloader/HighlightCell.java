package tenhouvisualizer.app.downloader;

import javafx.beans.property.StringProperty;
import javafx.scene.control.TableCell;
import tenhouvisualizer.domain.model.InfoSchema;

import java.util.Objects;

public class HighlightCell extends TableCell<InfoSchema, String> {
    private final StringProperty textToProperty;

    public HighlightCell(StringProperty textToProperty) {
        super();

        this.textToProperty = textToProperty;
        this.textToProperty.addListener((obs, oldItem, newItem) -> {
            if (Objects.equals(getItem(), newItem) && !isEmpty()) {
                setStyle("-fx-background-color: red");
            } else {
                setStyle("-fx-background-color: blue");
            }
        });
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
        } else {
            setText(item);
        }
    }
}
