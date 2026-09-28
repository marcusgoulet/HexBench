package lol.hexbench.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lol.hexbench.model.ItemCategory;
import lol.hexbench.model.TftItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommunityDragonItemLoader {
    private static final String COMMUNITY_ITEM_DATA_PATH = "/data/communitydragon/16.19.1/tft-en_us.json";
    private static final String DATA_DRAGON_ITEM_DATA_PATH = "/data/ddragon/16.19.1/tft-item.json";
    private static final String CURRENT_SET_KEY_PREFIX = "TFTSet18/Set18_Items/";
    private static final Pattern DESCRIPTION_PLACEHOLDER = Pattern.compile("@([A-Za-z][A-Za-z0-9_]*)(\\*100)?@");

    public List<TftItem> loadItems() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> currentSetImageFiles = loadCurrentSetImageFiles(mapper);

        try (InputStream inputStream = getClass().getResourceAsStream(COMMUNITY_ITEM_DATA_PATH)) {
            if (inputStream == null) {
                throw new IOException("Could not find item data: " + COMMUNITY_ITEM_DATA_PATH);
            }

            JsonNode root = mapper.readTree(inputStream);
            JsonNode itemsNode = root.get("items");
            List<JsonNode> descriptionSourceItems = descriptionSourceItems(itemsNode);

            List<TftItem> items = new ArrayList<>();

            for (JsonNode itemNode : itemsNode) {
                if (!itemNode.isObject()) {
                    continue;
                }

                String id = textValue(itemNode, "apiName");

                if (!currentSetImageFiles.containsKey(id)) {
                    continue;
                }

                String name = textValue(itemNode, "name");
                String iconPath = textValue(itemNode, "icon");
                String imageFile = currentSetImageFiles.get(id);
                String description = textValue(itemNode, "desc");
                ItemCategory category = classify(id, name);
                List<String> componentIds = componentIds(itemNode);
                Map<String, Double> effects = effects(itemNode);
                String descriptionSourceId = "";

                JsonNode descriptionSourceItem = findDescriptionSource(id, name, category, descriptionSourceItems);

                if (descriptionSourceItem != null) {
                    if (description.isBlank() && category != ItemCategory.COMPONENT) {
                        description = textValue(descriptionSourceItem, "desc");
                    }

                    if (effects.isEmpty()) {
                        effects = effects(descriptionSourceItem);
                    }

                    descriptionSourceId = textValue(descriptionSourceItem, "apiName");
                }

                description = resolveDescriptionPlaceholders(description, effects);
                Map<String, Double> stats = stats(effects);

                items.add(new TftItem(id, name, iconPath, imageFile, category, componentIds, description, stats, effects, descriptionSourceId));
            }

            return items;
        }
    }

    private Map<String, String> loadCurrentSetImageFiles(ObjectMapper mapper) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(DATA_DRAGON_ITEM_DATA_PATH)) {
            if (inputStream == null) {
                throw new IOException("Could not find item data: " + DATA_DRAGON_ITEM_DATA_PATH);
            }

            JsonNode root = mapper.readTree(inputStream);
            JsonNode dataNode = root.get("data");
            Map<String, String> currentSetImageFiles = new HashMap<>();

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

                currentSetImageFiles.put(id, imageFile);
            });

            return currentSetImageFiles;
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

    private List<JsonNode> descriptionSourceItems(JsonNode itemsNode) {
        List<JsonNode> descriptionSourceItems = new ArrayList<>();

        for (JsonNode itemNode : itemsNode) {
            String description = textValue(itemNode, "desc");

            if (!description.isBlank()) {
                descriptionSourceItems.add(itemNode);
            }
        }

        return descriptionSourceItems;
    }

    private JsonNode findDescriptionSource(String id, String name, ItemCategory category, List<JsonNode> descriptionSourceItems) {
        if (category == ItemCategory.COMPONENT) {
            return findItemById(componentFallbackId(id), descriptionSourceItems);
        }

        if (category == ItemCategory.CONSUMABLE || category == ItemCategory.EMBLEM) {
            return null;
        }

        JsonNode bestSource = null;
        int bestScore = 0;
        String normalizedName = normalizeName(name);

        for (JsonNode sourceItem : descriptionSourceItems) {
            String sourceName = textValue(sourceItem, "name");

            if (!normalizeName(sourceName).equals(normalizedName)) {
                continue;
            }

            String sourceId = textValue(sourceItem, "apiName");
            int score = descriptionSourceScore(id, category, sourceId);

            if (score > bestScore) {
                bestScore = score;
                bestSource = sourceItem;
            }
        }

        return bestSource;
    }

    private JsonNode findItemById(String id, List<JsonNode> items) {
        if (id.isBlank()) {
            return null;
        }

        for (JsonNode item : items) {
            if (textValue(item, "apiName").equals(id)) {
                return item;
            }
        }

        return null;
    }

    private String componentFallbackId(String id) {
        return switch (id) {
            case "DA_Component_BFSword" -> "TFT_Item_BFSword";
            case "DA_Component_RecurveBow" -> "TFT_Item_RecurveBow";
            case "DA_Component_NeedlesslyLargeRod" -> "TFT_Item_NeedlesslyLargeRod";
            case "DA_Component_ChainVest" -> "TFT_Item_ChainVest";
            case "DA_Component_NegatronCloak" -> "TFT_Item_NegatronCloak";
            case "DA_Component_GiantsBelt" -> "TFT_Item_GiantsBelt";
            case "DA_Component_SparringGloves" -> "TFT_Item_SparringGloves";
            case "DA_Component_TearOfTheGoddess" -> "TFT_Item_TearOfTheGoddess";
            default -> "";
        };
    }

    private int descriptionSourceScore(String id, ItemCategory category, String sourceId) {
        if (category == ItemCategory.ARTIFACT && sourceId.startsWith("TFT_Item_Artifact_")) {
            return 50;
        }

        if (category == ItemCategory.RADIANT && sourceId.startsWith("TFT5_Item_") && sourceId.contains("Radiant")) {
            return 50;
        }

        if (category == ItemCategory.COMPLETED && sourceId.startsWith("TFT_Item_")) {
            return 50;
        }

        if (sourceId.startsWith("TFT_Item_")) {
            return 25;
        }

        if (sourceId.startsWith("TFT5_Item_")) {
            return 20;
        }

        if (sourceId.startsWith("TFT")) {
            return 10;
        }

        return id.equals(sourceId) ? 1 : 0;
    }

    private Map<String, Double> effects(JsonNode itemNode) {
        JsonNode effectsNode = itemNode.get("effects");
        Map<String, Double> effects = new HashMap<>();

        if (effectsNode == null || !effectsNode.isObject()) {
            return effects;
        }

        effectsNode.fields().forEachRemaining(entry -> {
            JsonNode valueNode = entry.getValue();

            if (valueNode.isNumber()) {
                effects.put(entry.getKey(), valueNode.asDouble());
            }
        });

        return effects;
    }

    private Map<String, Double> stats(Map<String, Double> effects) {
        Map<String, Double> stats = new LinkedHashMap<>();

        addPercentStat(stats, effects, "Attack Damage", "AD");
        addStat(stats, effects, "Ability Power", "AP");
        addStat(stats, effects, "Attack Speed", "AS");
        addStat(stats, effects, "Armor", "Armor");
        addStat(stats, effects, "Magic Resist", "MagicResist");
        addStat(stats, effects, "Health", "Health");
        addStat(stats, effects, "Mana", "Mana");
        addStat(stats, effects, "Mana Regen", "ManaRegen");
        addStat(stats, effects, "Crit Chance", "CritChance");
        addPercentStat(stats, effects, "Omnivamp", "StatOmnivamp");

        return stats;
    }

    private void addStat(Map<String, Double> stats, Map<String, Double> effects, String statName, String effectName) {
        Double value = effects.get(effectName);

        if (value != null) {
            stats.put(statName, value);
        }
    }

    private void addPercentStat(Map<String, Double> stats, Map<String, Double> effects, String statName, String effectName) {
        Double value = effects.get(effectName);

        if (value == null) {
            return;
        }

        if (Math.abs(value) <= 2) {
            value = value * 100;
        }

        stats.put(statName, roundStatValue(value));
    }

    private double roundStatValue(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String resolveDescriptionPlaceholders(String description, Map<String, Double> effects) {
        if (description.isBlank() || effects.isEmpty()) {
            return description;
        }

        Matcher matcher = DESCRIPTION_PLACEHOLDER.matcher(description);
        StringBuffer resolvedDescription = new StringBuffer();

        while (matcher.find()) {
            String effectName = matcher.group(1);
            Double effectValue = effects.get(effectName);

            if (effectValue == null) {
                continue;
            }

            boolean isPercentValue = matcher.group(2) != null;
            double displayValue = isPercentValue ? effectValue * 100 : effectValue;
            matcher.appendReplacement(resolvedDescription, Matcher.quoteReplacement(formatEffectValue(displayValue)));
        }

        matcher.appendTail(resolvedDescription);
        return resolvedDescription.toString();
    }

    private String formatEffectValue(double value) {
        double roundedValue = Math.round(value * 100.0) / 100.0;

        if (roundedValue == Math.rint(roundedValue)) {
            return String.valueOf((int) roundedValue);
        }

        if (roundedValue * 10 == Math.rint(roundedValue * 10)) {
            return String.format(Locale.US, "%.1f", roundedValue);
        }

        return String.format(Locale.US, "%.2f", roundedValue);
    }

    private String normalizeName(String name) {
        return name.toLowerCase()
                .replace("'", "")
                .replaceAll("[^a-z0-9]", "");
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
