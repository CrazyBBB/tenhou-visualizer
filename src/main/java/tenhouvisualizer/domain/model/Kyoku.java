package tenhouvisualizer.domain.model;

import java.util.List;

public class Kyoku {
    private String summary;
    private List<MahjongScene> mahjongScenes;

    public Kyoku(String summary, List<MahjongScene> mahjongScenes) {
        this.summary = summary;
        this.mahjongScenes = mahjongScenes;
    }

    public String getSummary() {
        return summary;
    }

    public List<MahjongScene> getMahjongScenes() {
        return mahjongScenes;
    }
}
