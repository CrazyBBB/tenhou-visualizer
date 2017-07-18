package tenhouvisualizer.app;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ObservableObjectValue;

public class BindingHelper {
    public static StringBinding covertOrDefault(ObservableObjectValue observableObjectValue, String defaultMessage) {
        return Bindings.when(Bindings.isNotNull(observableObjectValue))
                .then(Bindings.convert(observableObjectValue))
                .otherwise(defaultMessage);
    }
}
