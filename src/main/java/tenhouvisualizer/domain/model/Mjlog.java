package tenhouvisualizer.domain.model;

public class Mjlog {
    private String name;
    private Scene scene;
    private int idx;

    public Mjlog(String name, Scene scene, int idx) {
        this.name = name;
        this.scene = scene;
        this.idx = idx;
    }

    public Mjlog(String name) {
        this.name = name;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    @Override
    public String toString() {
        return getName();
    }
}
