package tenhouvisualizer;

public class Mjlog {
    private String name;
    private Scene scene;

    public Mjlog(String name, Scene scene) {
        this.name = name;
        this.scene = scene;
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

    @Override
    public String toString() {
        return getName();
    }
}
