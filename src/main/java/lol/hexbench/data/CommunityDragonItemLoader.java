package lol.hexbench.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lol.hexbench.model.ItemCategory;
import lol.hexbench.model.TftItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityDragonItemLoader {
    private static final String COMMUNITY_ITEM_DATA_PATH = "/data/communitydragon/16.19.1/tft-en_us.json";
    private static final String DATA_DRAGON_ITEM_DATA_PATH = "/data/ddragon/16.19.1/tft-item.json";
    private static final String CURRENT_SET_KEY_PREFIX = "TFTSet18/Set18_Items/";

    public List<TftItem> loadItems() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> currentItemImages = loadCurrentItemImages(mapper);

        try (InputStream inputStream = getClass().getResourceAsStream(COMMUNITY_ITEM_DATA_PATH)) {
            if (inputStream == null) {
                throw new IOException("Could not find item data: " + COMMUNITY_ITEM_DATA_PATH);
            }

            JsonNode root = mapper.readTree(inputStream);
            JsonNode itemsNode = root.get("items");

            List<TftItem> items = new ArrayList<>();

            for (JsonNode itemNode : itemsNode) {
                if (!itemNode.isObject()) {
                    continue;
                }

                String id = textValue(itemNode, "apiName");

                if (!currentItemImages.containsKey(id)) {
                    continue;
                }

                String name = textValue(itemNode, "name");
                String imageFile = currentItemImages.get(id);
                String description = textValue(itemNode, "desc");
                ItemCategory category = classify(id, name);
                List<String> componentIds = componentIds(itemNode);

                items.add(new TftItem(id, name, imageFile, category, componentIds, description));
            }

            return items;
        }
    }

    private Map<String, String> loadCurrentItemImages(ObjectMapper mapper) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(DATA_DRAGON_ITEM_DATA_PATH)) {
            if (inputStream == null) {
                throw new IOException("Could not find item data: " + DATA_DRAGON_ITEM_DATA_PATH);
            }

            JsonNode root = mapper.readTree(inputStream);
            JsonNode dataNode = root.get("data");
            Map<String, String> currentItemImages = new HashMap<>();

            dataNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();

                if (!key.startsWith(CURRENT_SET_KEY_PREFIX)) {
                    return;
                }

                JsonNode itemNode = entry.getValue();
                String id = textValue(itemNode, "id");
                String imageFile = "";

                JsonNode imageNode = itemNode.get("image");
                if (imageNode != null && imageNode.isObject()) {
                    imageFile = textValue(imageNode, "full");
                }

                currentItemImages.put(id, imageFile);
            });

            return currentItemImages;
        }
    }

    private List<String> componentIds(JsonNode itemNode) {
        List<String> componentIds = new ArrayList<>();
        JsonNode compositionNode = itemNode.get("composition");

        if (compositionNode == null || !compositionNode.isArray()) {
            return componentIds;
        }

        for (JsonNode componentNode : compositionNode) {
            if (componentNode.isTextual()) {
                componentIds.add(componentNode.asText());
            }
        }

        return componentIds;
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);

        if (valueNode == null || valueNode.isNull()) {
            return "";
        }

        return valueNode.asText();
    }

    private ItemCategory classify(String id, String name) {
        if (id.startsWith("DA_Component_")) {
            return ItemCategory.COMPONENT;
        }

        if (id.startsWith("DA_Artifact_") || id.startsWith("DA_Item_Artifact_")) {
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
