package lol.hexbench.model;

import java.util.List;

public class TftItem {
    private final String id;
    private final String name;
    private final String imageFile;
    private final ItemCategory category;
    private final List<String> componentIds;
    private final String description;

    public TftItem(String id, String name, String imageFile, ItemCategory category, List<String> componentIds, String description) {
        this.id = id;
        this.name = name;
        this.imageFile = imageFile;
        this.category = category;
        this.componentIds = componentIds;
        this.description = description;
    }

    public String getId() { return this.id; }
    public String getName() { return this.name; }
    public String getImageFile() { return this.imageFile; }
    public ItemCategory getCategory() { return this.category; }
    public List<String> getComponentIds() { return this.componentIds; }
    public String getDescription() { return this.description; }
}
