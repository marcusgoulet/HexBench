package lol.hexbench.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lol.hexbench.model.ItemCategory;
import lol.hexbench.model.TftItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataDragonItemLoader {
    private static final String ITEM_DATA_PATH = "/data/ddragon/16.19.1/tft-item.json";
    private static final String CURRENT_SET_PREFIX = "TFTSet18/Set18_Items/";

    public List<TftItem> loadItems() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream inputStream = getClass().getResourceAsStream(ITEM_DATA_PATH)) {
            if (inputStream == null) {
                throw new IOException("Could not find item data: " + ITEM_DATA_PATH);
            }

            JsonNode root = mapper.readTree(inputStream);
            JsonNode data = root.get("data");

            List<TftItem> items = new ArrayList<>();

            Iterator<Map.Entry<String, JsonNode>> fields = data.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();

                String key = entry.getKey();

                if (!key.startsWith(CURRENT_SET_PREFIX)) {
                    continue;
                }

                JsonNode itemNode = entry.getValue();

                String id = itemNode.get("id").asText();
                String name = itemNode.get("name").asText();
                String imageFile = itemNode.get("image").get("full").asText();
                ItemCategory category = classify(id, name);

                items.add(new TftItem(id, name, imageFile, category, List.of(), ""));
            }

            return items;
        }
    }

    private ItemCategory classify(String id, String name) {
        if (id.startsWith("DA_Component_")) {
            return ItemCategory.COMPONENT;
        }

        if (id.startsWith("DA_Artifact_")) {
            return ItemCategory.ARTIFACT;
        }

        if (id.startsWith("DA_Consumable_")) {
            return ItemCategory.CONSUMABLE;
        }

        if (id.contains("Emblem")) {
            return ItemCategory.EMBLEM;
        }

        if (id.contains("Radiant") || name.startsWith("Radiant")) {
            return ItemCategory.RADIANT;
        }

        return ItemCategory.COMPLETED;
    }
}
