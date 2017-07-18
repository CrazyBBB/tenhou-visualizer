package tenhouvisualizer.domain.model;

import java.util.ArrayList;

public class Kyoku {
    public String summary;
    public ArrayList<MahjongScene> scenes;

    public Kyoku(String summary, ArrayList<MahjongScene> scenes) {
        this.summary = summary;
        this.scenes = scenes;
    }
}
