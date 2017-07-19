package tenhouvisualizer.app.downloader;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenhouvisualizer.domain.model.InfoSchema;

public class HighlightCell extends TableCell<InfoSchema, String> {

    private static final BackgroundFill BACKGROUND_FILL = new BackgroundFill(Color.gray(0.4, 0.5), null, null);
    private final static Logger log = LoggerFactory.getLogger(DownloaderController.class);

    public HighlightCell(StringProperty textToHighlight) {
        super();

        this.backgroundProperty().bind(Bindings.when(Bindings.and(Bindings.isNotNull(textToHighlight), Bindings.equal(itemProperty(), textToHighlight)))
                .then(new Background(BACKGROUND_FILL))
                .otherwise(Background.EMPTY));
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty ? null : item);
    }
}
