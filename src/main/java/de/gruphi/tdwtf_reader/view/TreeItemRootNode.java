package de.gruphi.tdwtf_reader.view;
import de.gruphi.tdwtf_reader.entities.InteractableItem;

public class TreeItemRootNode implements InteractableItem {
    private String desc;

    public TreeItemRootNode(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }
}
