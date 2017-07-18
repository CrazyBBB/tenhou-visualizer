package tenhouvisualizer.domain.model;

public class Mjlog {
    private String name;
    private MahjongScene scene;
    private int idx;

    public Mjlog(String name, MahjongScene scene, int idx) {
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

    public MahjongScene getScene() {
        return scene;
    }

    public void setScene(MahjongScene scene) {
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
