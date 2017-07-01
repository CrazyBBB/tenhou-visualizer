package tenhouvisualizer;

import javafx.scene.control.TreeView;

public class MjlogTreeControl extends TreeView<Mjlog> {
    public MjlogTreeControl() {
        this.setShowRoot(false);
    }

    public void showMjlogContent(MjlogFile mjlogFile) {
        MjlogTreeItem root = new MjlogTreeItem();
        MjlogTreeItem child1 = new MjlogTreeItem(new Mjlog("aaa"));
        MjlogTreeItem child1child1 = new MjlogTreeItem(new Mjlog("sss"));
        MjlogTreeItem child1child2 = new MjlogTreeItem(new Mjlog("ttt"));
        MjlogTreeItem child1child3 = new MjlogTreeItem(new Mjlog("uuu"));
        tenhouvisualizer.Scene scene1 = new tenhouvisualizer.Scene(
                true,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                0,
                null
        );
        MjlogTreeItem child1child4 = new MjlogTreeItem(new Mjlog("mmm", scene1));
        child1.getChildren().addAll(child1child1, child1child2, child1child3, child1child4);
        MjlogTreeItem child2 = new MjlogTreeItem(new Mjlog("bbb"));
        MjlogTreeItem child2child1 = new MjlogTreeItem(new Mjlog("xxx"));
        child2.getChildren().add(child2child1);
        root.getChildren().add(child1);
        root.getChildren().add(child2);
        this.setRoot(root);
    }
}
