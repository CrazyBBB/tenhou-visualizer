package tenhouvisualizer.app.downloader;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableCell;
import tenhouvisualizer.domain.model.InfoSchema;

import java.util.Objects;

public class HighlightCell extends TableCell<InfoSchema, String> {
    private final StringProperty textToProperty;

    public HighlightCell(StringProperty textToProperty) {
        super();

        this.textToProperty = textToProperty;

        this.styleProperty().bind(Bindings.when(Bindings.equal(itemProperty(), this.textToProperty))
                .then("-fx-background-color: red")
                .otherwise("-fx-background-color: none"));
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
