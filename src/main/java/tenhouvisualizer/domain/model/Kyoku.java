package tenhouvisualizer.domain.model;

import java.util.ArrayList;

public class Kyoku {
    public String summary;
    public ArrayList<Scene> scenes;

    public Kyoku(String summary, ArrayList<Scene> scenes) {
        this.summary = summary;
        this.scenes = scenes;
    }
}
