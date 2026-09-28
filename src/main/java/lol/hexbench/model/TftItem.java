package lol.hexbench.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TftItem {
    private final String id;
    private final String name;
    private final String iconPath;
    private final String imageFile;
    private final ItemCategory category;
    private final List<String> componentIds;
    private final String description;
    private final Map<String, Double> stats;
    private final Map<String, Double> effects;
    private final String descriptionSourceId;

    public TftItem(String id, String name, String iconPath, String imageFile, ItemCategory category, List<String> componentIds, String description) {
        this(id, name, iconPath, imageFile, category, componentIds, description, Map.of(), Map.of(), "");
    }

    public TftItem(String id, String name, String iconPath, String imageFile, ItemCategory category, List<String> componentIds, String description, Map<String, Double> stats, Map<String, Double> effects, String descriptionSourceId) {
        this.id = id;
        this.name = name;
        this.iconPath = iconPath;
        this.imageFile = imageFile;
        this.category = category;
        this.componentIds = componentIds;
        this.description = description;
        this.stats = Collections.unmodifiableMap(new LinkedHashMap<>(stats));
        this.effects = Collections.unmodifiableMap(new LinkedHashMap<>(effects));
        this.descriptionSourceId = descriptionSourceId;
    }

    public String getId() { return this.id; }
    public String getName() { return this.name; }
    public String getIconPath() { return this.iconPath; }
    public String getImageFile() { return this.imageFile; }
    public ItemCategory getCategory() { return this.category; }
    public List<String> getComponentIds() { return this.componentIds; }
    public String getDescription() { return this.description; }
    public Map<String, Double> getStats() { return this.stats; }
    public Map<String, Double> getEffects() { return this.effects; }
    public String getDescriptionSourceId() { return this.descriptionSourceId; }
}
