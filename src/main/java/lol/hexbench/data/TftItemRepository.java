package lol.hexbench.data;

import lol.hexbench.model.TftItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TftItemRepository {
    private final List<TftItem> items;
    private final Map<String, TftItem> itemsById;

    public TftItemRepository(List<TftItem> items) {
        this.items = items;
        this.itemsById = new HashMap<>();

        for (TftItem item : items) {
            this.itemsById.put(item.getId(), item);
        }
    }

    public List<TftItem> findAll() { return this.items; }
    public TftItem findById(String id) { return this.itemsById.get(id); }

    public List<TftItem> findItemsBuiltFrom(String componentId) {
        List<TftItem> matchingItems = new ArrayList<>();

        for (TftItem item : items) {
            if (item.getComponentIds().contains(componentId)) {
                matchingItems.add(item);
            }
        }

        return matchingItems;
    }

    public List<TftItem> findRecipeComponents(TftItem item) {
        List<TftItem> components = new ArrayList<>();

        for (String componentId : item.getComponentIds()) {
            TftItem component = findById(componentId);

            if (component != null) {
                components.add(component);
            }
        }

        return components;
    }

}
