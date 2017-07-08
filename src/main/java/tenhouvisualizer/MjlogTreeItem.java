package tenhouvisualizer;

import javafx.scene.control.TreeItem;

public class MjlogTreeItem extends TreeItem<Mjlog> {
    public MjlogTreeItem(){
        this(null);
    }

    public MjlogTreeItem(Mjlog mjlog){
        super(mjlog);
    }

    @Override
    public boolean isLeaf(){
        Mjlog mjlog = getValue();
        return mjlog != null && mjlog.getScene() != null;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
