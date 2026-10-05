package lol.hexbench;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import lol.hexbench.model.ItemCategory;
import lol.hexbench.model.TftItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {
    private static final String ITEM_IMAGE_RESOURCE_PREFIX = "/data/ddragon/16.19.1/img/tft-item/";
    private static final double GRID_ICON_SIZE = 28;
    private static final double DETAIL_ICON_SIZE = 24;

    @FXML
    private VBox contentArea;

    private AppContext appContext;
    private HBox selectedItemRow;
    private final Map<String, HBox> visibleItemRowsById = new HashMap<>();

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
        showItems();
    }

    @FXML
    private void showItems() {
        List<TftItem> items = appContext.getItemRepository().findAll();
        showItemsView(items);
    }

    // TBD: Champion tracker implementation.
    // TBD: Trait tracker implementation.
    // TBD: Build planner implementation.

    private void showSection(String title, String subtitle) {
        contentArea.getChildren().clear();

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("content-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("content-subtitle");

        contentArea.getChildren().addAll(titleLabel, subtitleLabel);
    }

    private void showItemsView(List<TftItem> items) {
        contentArea.getChildren().clear();

        VBox detailPanel = createDetailPanel();
        ScrollPane scrollPane = new ScrollPane();
        VBox firstContent = createAllItemsContent(items);
        SplitPane splitPane = createItemSplitPane(scrollPane, detailPanel);
        HBox itemTabs = createItemTabBar(items, scrollPane, detailPanel);

        showItemContent(scrollPane, detailPanel, firstContent);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        contentArea.getChildren().addAll(itemTabs, splitPane);
    }

    private HBox createItemTabBar(List<TftItem> items, ScrollPane scrollPane, VBox detailPanel) {
        HBox tabBar = new HBox();
        tabBar.getStyleClass().add("item-tab-bar");

        ToggleGroup toggleGroup = new ToggleGroup();

        ToggleButton allButton = createItemTabButton("All", toggleGroup);
        ToggleButton coreButton = createItemTabButton("Core", toggleGroup);
        ToggleButton artifactButton = createItemTabButton("Artifacts", toggleGroup);
        ToggleButton radiantButton = createItemTabButton("Radiant", toggleGroup);
        ToggleButton emblemButton = createItemTabButton("Emblems", toggleGroup);

        allButton.setOnAction(event -> showItemContent(scrollPane, detailPanel, createAllItemsContent(items)));
        coreButton.setOnAction(event -> showItemContent(scrollPane, detailPanel, createCoreItemsContent(items)));
        artifactButton.setOnAction(event -> showItemContent(scrollPane, detailPanel, createSingleCategoryContent(items, ItemCategory.ARTIFACT)));
        radiantButton.setOnAction(event -> showItemContent(scrollPane, detailPanel, createSingleCategoryContent(items, ItemCategory.RADIANT)));
        emblemButton.setOnAction(event -> showItemContent(scrollPane, detailPanel, createSingleCategoryContent(items, ItemCategory.EMBLEM)));

        toggleGroup.selectedToggleProperty().addListener((observable, previousToggle, selectedToggle) -> {
            if (selectedToggle == null) {
                previousToggle.setSelected(true);
            }
        });

        allButton.setSelected(true);
        tabBar.getChildren().addAll(allButton, coreButton, artifactButton, radiantButton, emblemButton);

        return tabBar;
    }

    private ToggleButton createItemTabButton(String text, ToggleGroup toggleGroup) {
        ToggleButton button = new ToggleButton(text);
        button.getStyleClass().add("item-tab-button");
        button.setToggleGroup(toggleGroup);
        button.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(button, Priority.ALWAYS);

        return button;
    }

    private VBox createAllItemsContent(List<TftItem> items) {
        VBox content = new VBox(12);

        addItemSection(content, "Components", items, ItemCategory.COMPONENT);
        addItemSection(content, "Completed Items", items, ItemCategory.COMPLETED);
        addItemSection(content, "Artifacts", items, ItemCategory.ARTIFACT);
        addItemSection(content, "Radiant Items", items, ItemCategory.RADIANT);
        addItemSection(content, "Emblems", items, ItemCategory.EMBLEM);

        return content;
    }

    private VBox createCoreItemsContent(List<TftItem> items) {
        VBox content = new VBox(12);

        addItemSection(content, "Components", items, ItemCategory.COMPONENT);
        addItemSection(content, "Completed Items", items, ItemCategory.COMPLETED);

        return content;
    }

    private TilePane createSingleCategoryContent(List<TftItem> items, ItemCategory category) {
        return createItemGrid(sortForDisplay(filterByCategory(items, category)));
    }

    private void showItemContent(ScrollPane scrollPane, VBox detailPanel, javafx.scene.Node itemContent) {
        selectedItemRow = null;
        visibleItemRowsById.clear();
        resetDetailPanel(detailPanel);
        scrollPane.setContent(itemContent);
        if (!scrollPane.getStyleClass().contains("item-scroll-pane")) {
            scrollPane.getStyleClass().add("item-scroll-pane");
        }
        scrollPane.setFitToWidth(true);

        if (itemContent instanceof VBox content) {
            wireItemSelection(content, detailPanel);
        } else if (itemContent instanceof TilePane itemGrid) {
            wireItemSelection(itemGrid, detailPanel);
        }
    }

    private void addItemSection(VBox content, String title, List<TftItem> items, ItemCategory category) {
        List<TftItem> sectionItems = sortForDisplay(filterByCategory(items, category));

        if (sectionItems.isEmpty()) {
            return;
        }

        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("section-title");

        TilePane itemGrid = createItemGrid(sectionItems);

        content.getChildren().addAll(sectionTitle, itemGrid);
    }

    private TilePane createItemGrid(List<TftItem> items) {
        TilePane itemGrid = new TilePane();
        itemGrid.getStyleClass().add("item-grid");
        itemGrid.setHgap(8);
        itemGrid.setVgap(8);
        itemGrid.setPrefTileWidth(190);
        itemGrid.setPrefTileHeight(42);

        for (TftItem item : items) {
            HBox itemRow = createItemRow(item, GRID_ICON_SIZE, "item-name");
            itemRow.setMaxWidth(Double.MAX_VALUE);
            itemGrid.getChildren().add(itemRow);
        }

        return itemGrid;
    }

    private SplitPane createItemSplitPane(ScrollPane itemContent, VBox detailPanel) {
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("item-split-pane");
        splitPane.getItems().addAll(itemContent, createDetailScrollPane(detailPanel));
        splitPane.setDividerPositions(0.68);

        return splitPane;
    }

    private ScrollPane createDetailScrollPane(VBox detailPanel) {
        ScrollPane detailScrollPane = new ScrollPane(detailPanel);
        detailScrollPane.getStyleClass().add("detail-scroll-pane");
        detailScrollPane.setFitToWidth(true);
        detailScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return detailScrollPane;
    }

    private VBox createDetailPanel() {
        VBox detailPanel = new VBox(8);
        detailPanel.getStyleClass().add("item-detail-panel");
        detailPanel.setPrefWidth(340);
        detailPanel.setMaxWidth(420);

        resetDetailPanel(detailPanel);

        return detailPanel;
    }

    private void resetDetailPanel(VBox detailPanel) {
        detailPanel.getChildren().clear();

        Label titleLabel = new Label("Select an item");
        titleLabel.getStyleClass().add("detail-title");

        Label subtitleLabel = new Label("Recipe and build options will appear here.");
        subtitleLabel.getStyleClass().add("detail-text");
        subtitleLabel.setWrapText(true);

        detailPanel.getChildren().addAll(titleLabel, subtitleLabel);
    }

    private void wireItemSelection(VBox content, VBox detailPanel) {
        for (javafx.scene.Node child : content.getChildren()) {
            if (child instanceof TilePane itemGrid) {
                wireItemSelection(itemGrid, detailPanel);
            }
        }
    }

    private void wireItemSelection(TilePane itemGrid, VBox detailPanel) {
        for (javafx.scene.Node child : itemGrid.getChildren()) {
            if (child instanceof HBox itemRow && itemRow.getUserData() instanceof TftItem item) {
                visibleItemRowsById.put(item.getId(), itemRow);
                itemRow.setOnMouseClicked(event -> selectItem(item, detailPanel));
            }
        }
    }

    private void selectItem(TftItem item, VBox detailPanel) {
        selectItemRow(visibleItemRowsById.get(item.getId()));
        showItemDetail(item, detailPanel);
    }

    private void selectItemRow(HBox itemRow) {
        if (selectedItemRow != null) {
            selectedItemRow.getStyleClass().remove("selected-item-row");
        }

        selectedItemRow = itemRow;

        if (selectedItemRow != null && !selectedItemRow.getStyleClass().contains("selected-item-row")) {
            selectedItemRow.getStyleClass().add("selected-item-row");
        }
    }

    private void showItemDetail(TftItem item, VBox detailPanel) {
        detailPanel.getChildren().clear();

        detailPanel.getChildren().add(createDetailHeader(item));
        addStatsDetail(item, detailPanel);
        addDescriptionDetail(item, detailPanel);

        if (item.getCategory() == ItemCategory.COMPONENT) {
            addBuildsIntoDetail(item, detailPanel);
        } else if (!item.getComponentIds().isEmpty()) {
            addRecipeDetail(item, detailPanel);
        } else {
            VBox section = createDetailSection("Recipe");
            Label noRecipeLabel = new Label("No standard recipe.");
            noRecipeLabel.getStyleClass().add("detail-text");
            section.getChildren().add(noRecipeLabel);
            detailPanel.getChildren().add(section);
        }
    }

    private void addStatsDetail(TftItem item, VBox detailPanel) {
        if (item.getStats().isEmpty()) {
            return;
        }

        VBox section = createDetailSection("Stats");

        for (Map.Entry<String, Double> stat : item.getStats().entrySet()) {
            HBox statRow = new HBox(8);
            statRow.getStyleClass().add("stat-row");
            statRow.setMaxWidth(Double.MAX_VALUE);

            Label nameLabel = new Label(stat.getKey());
            nameLabel.getStyleClass().add("stat-name");
            nameLabel.setMinWidth(0);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            nameLabel.setWrapText(true);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            Label valueLabel = new Label(formatStatValue(stat.getKey(), stat.getValue()));
            valueLabel.getStyleClass().add("stat-value");
            valueLabel.setMinWidth(Region.USE_PREF_SIZE);

            statRow.getChildren().addAll(nameLabel, valueLabel);
            section.getChildren().add(statRow);
        }

        detailPanel.getChildren().add(section);
    }

    private void addDescriptionDetail(TftItem item, VBox detailPanel) {
        String description = cleanDescription(item.getDescription());

        if (description.isBlank()) {
            return;
        }

        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("detail-description");
        descriptionLabel.setWrapText(true);

        VBox section = createDetailSection("Description");
        section.getChildren().add(descriptionLabel);
        detailPanel.getChildren().add(section);
    }

    private void addBuildsIntoDetail(TftItem component, VBox detailPanel) {
        VBox section = createDetailSection("Builds Into");

        List<TftItem> builtItems = sortForDisplay(appContext.getItemRepository().findItemsBuiltFrom(component.getId()));

        if (builtItems.isEmpty()) {
            Label emptyLabel = new Label("No known completed items.");
            emptyLabel.getStyleClass().add("detail-text");
            section.getChildren().add(emptyLabel);
            detailPanel.getChildren().add(section);
            return;
        }

        for (TftItem builtItem : builtItems) {
            VBox builtItemRow = new VBox(4);
            builtItemRow.getStyleClass().add("builds-into-row");
            builtItemRow.getChildren().add(createClickableDetailItemRow(builtItem, "builds-into-parent-row", detailPanel));
            builtItemRow.getChildren().add(createRecipeRow(orderedRecipeComponents(builtItem, component), detailPanel));
            section.getChildren().add(builtItemRow);
        }

        detailPanel.getChildren().add(section);
    }

    private void addRecipeDetail(TftItem item, VBox detailPanel) {
        VBox section = createDetailSection("Recipe");

        List<TftItem> components = appContext.getItemRepository().findRecipeComponents(item);

        if (components.isEmpty()) {
            Label emptyLabel = new Label("Recipe data is unavailable.");
            emptyLabel.getStyleClass().add("detail-text");
            section.getChildren().add(emptyLabel);
            detailPanel.getChildren().add(section);
            return;
        }

        section.getChildren().add(createRecipeRow(components, detailPanel));
        detailPanel.getChildren().add(section);
    }

    private VBox createDetailSection(String title) {
        VBox section = new VBox(8);
        section.getStyleClass().add("detail-section");

        Label sectionTitle = new Label(title);
        sectionTitle.getStyleClass().add("detail-section-title");

        section.getChildren().add(sectionTitle);

        return section;
    }

    private HBox createDetailHeader(TftItem item) {
        HBox header = new HBox(10);
        header.getStyleClass().add("detail-header");

        ImageView iconView = createItemIconView(item, 42);
        if (iconView != null) {
            iconView.getStyleClass().add("detail-header-icon");
        }

        VBox titleStack = new VBox(2);

        Label titleLabel = new Label(item.getName());
        titleLabel.getStyleClass().add("detail-title");
        titleLabel.setWrapText(true);

        Label categoryLabel = new Label(item.getCategory().toString());
        categoryLabel.getStyleClass().add("detail-meta");

        titleStack.getChildren().addAll(titleLabel, categoryLabel);
        HBox.setHgrow(titleStack, Priority.ALWAYS);

        if (iconView == null) {
            header.getChildren().add(titleStack);
        } else {
            header.getChildren().addAll(iconView, titleStack);
        }

        return header;
    }

    private HBox createRecipeRow(List<TftItem> components, VBox detailPanel) {
        HBox recipeRow = new HBox(8);
        recipeRow.getStyleClass().add("recipe-row");

        for (int index = 0; index < components.size(); index++) {
            if (index > 0) {
                Label plusLabel = new Label("+");
                plusLabel.getStyleClass().add("detail-meta");
                recipeRow.getChildren().add(plusLabel);
            }

            recipeRow.getChildren().add(createClickableDetailItemRow(components.get(index), "detail-item-row", detailPanel));
        }

        return recipeRow;
    }

    private HBox createClickableDetailItemRow(TftItem item, String styleClass, VBox detailPanel) {
        HBox itemRow = createItemRow(item, DETAIL_ICON_SIZE, styleClass);
        itemRow.getStyleClass().add("clickable-detail-item-row");
        itemRow.setOnMouseClicked(event -> {
            selectItem(item, detailPanel);
            event.consume();
        });

        return itemRow;
    }

    private List<TftItem> orderedRecipeComponents(TftItem builtItem, TftItem primaryComponent) {
        List<TftItem> components = appContext.getItemRepository().findRecipeComponents(builtItem);
        List<TftItem> orderedComponents = new ArrayList<>();

        for (TftItem component : components) {
            if (component.getId().equals(primaryComponent.getId())) {
                orderedComponents.add(component);
            }
        }

        for (TftItem component : components) {
            if (!component.getId().equals(primaryComponent.getId())) {
                orderedComponents.add(component);
            }
        }

        return orderedComponents;
    }

    private HBox createItemRow(TftItem item, double iconSize, String styleClass) {
        HBox itemRow = new HBox(8);
        itemRow.getStyleClass().add(styleClass);
        itemRow.setUserData(item);
        itemRow.setMinHeight(iconSize + 8);

        ImageView iconView = createItemIconView(item, iconSize);
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("item-row-text");
        nameLabel.setWrapText(false);
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        if (iconView == null) {
            itemRow.getChildren().add(nameLabel);
        } else {
            itemRow.getChildren().addAll(iconView, nameLabel);
        }

        return itemRow;
    }

    private ImageView createItemIconView(TftItem item, double iconSize) {
        String imageFile = item.getImageFile();

        if (imageFile == null || imageFile.isBlank()) {
            return null;
        }

        String imagePath = ITEM_IMAGE_RESOURCE_PREFIX + imageFile;
        java.net.URL imageUrl = getClass().getResource(imagePath);

        if (imageUrl == null) {
            return null;
        }

        Image image = new Image(imageUrl.toExternalForm(), iconSize, iconSize, true, true);
        ImageView iconView = new ImageView(image);
        iconView.getStyleClass().add("item-icon");
        iconView.setFitWidth(iconSize);
        iconView.setFitHeight(iconSize);
        iconView.setPreserveRatio(true);

        return iconView;
    }

    private List<TftItem> filterByCategory(List<TftItem> items, ItemCategory category) {
        List<TftItem> filteredItems = new ArrayList<>();

        for (TftItem item : items) {
            if (item.getCategory() == category) {
                filteredItems.add(item);
            }
        }

        return filteredItems;
    }

    private List<TftItem> sortForDisplay(List<TftItem> items) {
        List<TftItem> sortedItems = new ArrayList<>(items);

        sortedItems.sort(
                Comparator.comparingInt((TftItem item) -> categorySortOrder(item.getCategory()))
                        .thenComparing(TftItem::getName)
        );

        return sortedItems;
    }

    private int categorySortOrder(ItemCategory category) {
        return switch (category) {
            case COMPONENT -> 0;
            case COMPLETED -> 1;
            case ARTIFACT -> 2;
            case RADIANT -> 3;
            case EMBLEM -> 4;
            case CONSUMABLE -> 5;
            case UNKNOWN -> 6;
        };
    }

    private String cleanDescription(String description) {
        if (description == null) {
            return "";
        }

        return description
                .replace("\\r\\n", "\n")
                .replace("\\n", "\n")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("%i:[^%]+%", "")
                .replaceAll("<[^>]+>", "")
                .replaceAll("[ \\t]+", " ")
                .replaceAll(" *\\n *", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String formatStatValue(String statName, double value) {
        double roundedValue = Math.round(value * 100.0) / 100.0;
        String suffix = statUsesPercent(statName) ? "%" : "";

        if (roundedValue == Math.rint(roundedValue)) {
            return (int) roundedValue + suffix;
        }

        return String.format(java.util.Locale.US, "%.2f%s", roundedValue, suffix);
    }

    private boolean statUsesPercent(String statName) {
        return statName.equals("Attack Damage")
                || statName.equals("Ability Power")
                || statName.equals("Attack Speed")
                || statName.equals("Crit Chance")
                || statName.equals("Omnivamp");
    }

}
