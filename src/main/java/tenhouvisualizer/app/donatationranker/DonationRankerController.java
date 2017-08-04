package tenhouvisualizer.app.donatationranker;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tenhouvisualizer.Main;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class DonationRankerController implements Initializable {

    private final static Logger log = LoggerFactory.getLogger(DonationRankerController.class);

    public RadioButton sanmaRadioButton;
    public RadioButton yonmaRadioButton;
    public RadioButton tonpuRadioButton;
    public RadioButton tonnanRadioButton;
    public TextField filterField;
    public Button clearButton;
    public ToggleGroup maToggle;
    public ToggleGroup souToggle;

    public TableView<Donation> rankingTableView;
    public TableColumn<Donation, String> fromCol;
    public TableColumn<Donation, String> toCol;
    public TableColumn<Donation, String> valueCol;

    private final static int DEFAULT_MATCH_MIN = 10;
    private final static int DEFAULT_SHOW_MAX = 100;
    public TextField matchMinField;
    public TextField showMaxField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fromCol.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().from));
        toCol.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().to));
        valueCol.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().value));

        matchMinField.setText(String.valueOf(DEFAULT_MATCH_MIN));
        showMaxField.setText(String.valueOf(DEFAULT_SHOW_MAX));
    }

    public void search(ActionEvent actionEvent) {
        if (isNotSmallNumberString(matchMinField.getText())) {
            matchMinField.setText(String.valueOf(DEFAULT_MATCH_MIN));
        }
        if (isNotSmallNumberString(showMaxField.getText())) {
            showMaxField.setText(String.valueOf(DEFAULT_SHOW_MAX));
        }

        boolean isSanma = ((RadioButton) maToggle.getSelectedToggle()).getText().equals("三");
        boolean isTonnan = ((RadioButton) souToggle.getSelectedToggle()).getText().equals("東南");
        String playerName = filterField.getText();
        int matchMin = Integer.parseInt(matchMinField.getText());
        int showMax = Integer.parseInt(showMaxField.getText());

        rankingTableView.setItems(rankWinnerAndLoser(isSanma, isTonnan, playerName, matchMin, showMax));
    }

    public void clear(ActionEvent actionEvent) {
        sanmaRadioButton.setSelected(true);
        tonnanRadioButton.setSelected(true);
        filterField.clear();
        matchMinField.setText(String.valueOf(DEFAULT_MATCH_MIN));
        showMaxField.setText(String.valueOf(DEFAULT_SHOW_MAX));
    }

    public void clearFilterField(ActionEvent actionEvent) {
        filterField.clear();
        filterField.requestFocus();
    }

    private ObservableList<Donation> rankWinnerAndLoser(boolean isSanma, boolean isTonnan, String playerName,
                                                        int matchMin, int showMax) {
        List<String[]> list = Main.databaseService.findWinnerAndLoser(isSanma, isTonnan, playerName);

        Map<String, Integer> sumMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();
        for (String[] winnerAndLoser : list) {
            String loserAndWinnerString = winnerAndLoser[1] + " " + winnerAndLoser[0];
            String winnerAndLoserString = winnerAndLoser[0] + " " + winnerAndLoser[1];
            if (sumMap.containsKey(winnerAndLoserString)) {
                sumMap.merge(winnerAndLoserString, 1, Integer::sum);
            } else {
                sumMap.merge(loserAndWinnerString, 1, Integer::sum);
                countMap.merge(loserAndWinnerString, 1, Integer::sum);
            }
        }

        List<Pair<String, Double>> candidates = new ArrayList<>();
        Set<String> keys = sumMap.keySet();
        for (String key : keys) {
            int sum = sumMap.get(key);
            if (sum < matchMin) continue;

            int count = countMap.get(key);
            if (count == sum - count) continue;

            String tmpKey;
            double percent;
            if (count > sum - count) {
                tmpKey = key + " " + count + "/" + sum;
                percent = (double) count / sum;
            } else {
                String[] split = key.split(" ");
                tmpKey = split[1] + " " + split[0] + " " + (sum - count) + "/" + sum;
                percent = (double) (sum - count) / sum;
            }
            candidates.add(new Pair<>(tmpKey, percent));
        }

        candidates.sort((c1, c2) -> Double.compare(c2.getValue(), c1.getValue()));

        ObservableList<Donation> donations = FXCollections.observableArrayList();
        for (int i = 0; i < showMax && i < candidates.size(); i++) {
            String[] splits = candidates.get(i).getKey().split(" ");
            donations.add(new Donation(splits[0], splits[1], splits[2]));
        }

        return donations;
    }

    private boolean isNotSmallNumberString(String s) {
        if (s.length() > 5) return true;

        Pattern pattern = Pattern.compile("\\d+");
        return !pattern.matcher(s).find();
    }

    class Donation {
        String from;
        String to;
        String value;

        Donation(String from, String to, String value) {
            this.from = from;
            this.to = to;
            this.value = value;
        }
    }
}
