package tenhouvisualizer.app.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import tenhouvisualizer.domain.MahjongUtils;
import tenhouvisualizer.domain.model.HaiString;
import tenhouvisualizer.domain.model.MahjongScene;
import tenhouvisualizer.domain.model.Ukeire;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class UkeireTableView extends TableView<Ukeire> {
    public UkeireTableView() {
        super();

        TableColumn<Ukeire, String> candidateHaiStringColmun = new TableColumn<>("選");
        TableColumn<Ukeire, String> syantenColmun = new TableColumn<>("聴");
        TableColumn<Ukeire, String> infoColmun = new TableColumn<>("枚数");
        TableColumn<Ukeire, String> ukeireHaiStringsColmun = new TableColumn<>("受入牌");

        candidateHaiStringColmun.setCellValueFactory(e -> new SimpleStringProperty(HaiString.getHaiStringByIndex(e.getValue().getCandidateIndex())));
        syantenColmun.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getSyanten() == -1 ? "-" : String.valueOf(e.getValue().getSyanten())));
        infoColmun.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getSize() + "種" + e.getValue().getSum() + "牌"));
        ukeireHaiStringsColmun.setCellValueFactory(e -> new SimpleStringProperty(e.getValue().getUkeireIndices().stream().map(HaiString::getHaiStringByIndex).collect(Collectors.joining())));

        candidateHaiStringColmun.prefWidthProperty().bind(widthProperty().multiply(0.15));
        syantenColmun.prefWidthProperty().bind(widthProperty().multiply(0.13));
        infoColmun.prefWidthProperty().bind(widthProperty().multiply(0.3));
        ukeireHaiStringsColmun.prefWidthProperty().bind(widthProperty().multiply(0.41));

        getColumns().addAll(candidateHaiStringColmun, syantenColmun, infoColmun, ukeireHaiStringsColmun);
    }

    public void init() {
    }

    public void showUkeire(MahjongScene scene) {
        ObservableList<Ukeire> list = FXCollections.observableArrayList();
        int[] hais = new int[34];
        int naki = scene.naki.get(scene.heroPosition).size();
        Set<Integer> set = new TreeSet<>();
        for (int hai : scene.tehaiSets.get(scene.heroPosition)) {
            hais[hai / 4]++;
            set.add(hai / 4);
        }
        if (scene.tsumo[scene.heroPosition] != -1) {
            hais[scene.tsumo[scene.heroPosition] / 4]++;
            set.add(scene.tsumo[scene.heroPosition] / 4);
        }
        for (int candidate : set) {
            hais[candidate]--;
            int syanten = MahjongUtils.computeSyanten(hais, naki);
            hais[candidate]++;
            list.add(new Ukeire(candidate, new ArrayList<>(), 0, syanten));
        }
        list.sort((u1, u2) -> u1.getSyanten() != u2.getSyanten() ?
                u1.getSyanten() - u2.getSyanten() : u1.getSum() - u2.getSum());
        setItems(list);
    }
}
