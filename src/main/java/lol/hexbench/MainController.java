package lol.hexbench;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import lol.hexbench.model.ItemCategory;
import lol.hexbench.model.TftItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainController {
    @FXML
    private VBox contentArea;

    private AppContext appContext;

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
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

        Label titleLabel = new Label("Items");
        titleLabel.getStyleClass().add("content-title");

        Label summaryLabel = new Label("Loaded " + items.size() + " items, including " + countRecipes(items) + " recipes.");
        summaryLabel.getStyleClass().add("content-subtitle");

        TabPane tabPane = new TabPane();
        tabPane.getStyleClass().add("items-tab-pane");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().add(createAllTab(items));
        tabPane.getTabs().add(createCoreTab(items));
        tabPane.getTabs().add(createItemTab("Artifacts", sortForDisplay(filterByCategory(items, ItemCategory.ARTIFACT))));
        tabPane.getTabs().add(createItemTab("Radiant", sortForDisplay(filterByCategory(items, ItemCategory.RADIANT))));
        tabPane.getTabs().add(createItemTab("Emblems", sortForDisplay(filterByCategory(items, ItemCategory.EMBLEM))));

        contentArea.getChildren().addAll(titleLabel, summaryLabel, tabPane);
    }

    private Tab createItemTab(String title, List<TftItem> items) {
        TilePane itemGrid = createItemGrid(items);
        VBox detailPanel = createDetailPanel();

        ScrollPane scrollPane = new ScrollPane(itemGrid);
        scrollPane.getStyleClass().add("item-scroll-pane");
        scrollPane.setFitToWidth(true);

        SplitPane splitPane = createItemSplitPane(scrollPane, detailPanel);
        wireItemSelection(itemGrid, detailPanel);

        Tab tab = new Tab(title);
        tab.setContent(splitPane);

        return tab;
    }

    private Tab createAllTab(List<TftItem> items) {
        VBox content = new VBox(12);
        VBox detailPanel = createDetailPanel();

        addItemSection(content, "Components", items, ItemCategory.COMPONENT);
        addItemSection(content, "Completed Items", items, ItemCategory.COMPLETED);
        addItemSection(content, "Artifacts", items, ItemCategory.ARTIFACT);
        addItemSection(content, "Radiant Items", items, ItemCategory.RADIANT);
        addItemSection(content, "Emblems", items, ItemCategory.EMBLEM);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("item-scroll-pane");
        scrollPane.setFitToWidth(true);

        SplitPane splitPane = createItemSplitPane(scrollPane, detailPanel);
        wireItemSelection(content, detailPanel);

        Tab tab = new Tab("All");
        tab.setContent(splitPane);

        return tab;
    }

    private Tab createCoreTab(List<TftItem> items) {
        VBox content = new VBox(12);
        VBox detailPanel = createDetailPanel();

        addItemSection(content, "Components", items, ItemCategory.COMPONENT);
        addItemSection(content, "Completed Items", items, ItemCategory.COMPLETED);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("item-scroll-pane");
        scrollPane.setFitToWidth(true);

        SplitPane splitPane = createItemSplitPane(scrollPane, detailPanel);
        wireItemSelection(content, detailPanel);

        Tab tab = new Tab("Core");
        tab.setContent(splitPane);

        return tab;
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
        itemGrid.setPrefTileWidth(150);
        itemGrid.setPrefTileHeight(34);

        for (TftItem item : items) {
            Label itemLabel = new Label(item.getName());
            itemLabel.getStyleClass().add("item-name");
            itemLabel.setUserData(item);
            itemLabel.setMaxWidth(Double.MAX_VALUE);
            itemGrid.getChildren().add(itemLabel);
        }

        return itemGrid;
    }

    private SplitPane createItemSplitPane(ScrollPane itemContent, VBox detailPanel) {
        SplitPane splitPane = new SplitPane();
        splitPane.getStyleClass().add("item-split-pane");
        splitPane.getItems().addAll(itemContent, detailPanel);
        splitPane.setDividerPositions(0.68);

        return splitPane;
    }

    private VBox createDetailPanel() {
        VBox detailPanel = new VBox(8);
        detailPanel.getStyleClass().add("item-detail-panel");
        detailPanel.setPrefWidth(340);
        detailPanel.setMaxWidth(420);

        Label titleLabel = new Label("Select an item");
        titleLabel.getStyleClass().add("detail-title");

        Label subtitleLabel = new Label("Recipe and build options will appear here.");
        subtitleLabel.getStyleClass().add("detail-text");
        subtitleLabel.setWrapText(true);

        detailPanel.getChildren().addAll(titleLabel, subtitleLabel);

        return detailPanel;
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
            if (child instanceof Label itemLabel && itemLabel.getUserData() instanceof TftItem item) {
                itemLabel.setOnMouseClicked(event -> showItemDetail(item, detailPanel));
            }
        }
    }

    private void showItemDetail(TftItem item, VBox detailPanel) {
        detailPanel.getChildren().clear();

        Label titleLabel = new Label(item.getName());
        titleLabel.getStyleClass().add("detail-title");

        Label categoryLabel = new Label(item.getCategory().toString());
        categoryLabel.getStyleClass().add("detail-meta");

        detailPanel.getChildren().addAll(titleLabel, categoryLabel);

        if (item.getCategory() == ItemCategory.COMPONENT) {
            addBuildsIntoDetail(item, detailPanel);
        } else if (!item.getComponentIds().isEmpty()) {
            addRecipeDetail(item, detailPanel);
        } else {
            Label noRecipeLabel = new Label("No standard recipe.");
            noRecipeLabel.getStyleClass().add("detail-text");
            detailPanel.getChildren().add(noRecipeLabel);
        }
    }

    private void addBuildsIntoDetail(TftItem component, VBox detailPanel) {
        Label sectionTitle = new Label("Builds Into");
        sectionTitle.getStyleClass().add("detail-section-title");
        detailPanel.getChildren().add(sectionTitle);

        List<TftItem> builtItems = sortForDisplay(appContext.getItemRepository().findItemsBuiltFrom(component.getId()));

        if (builtItems.isEmpty()) {
            Label emptyLabel = new Label("No known completed items.");
            emptyLabel.getStyleClass().add("detail-text");
            detailPanel.getChildren().add(emptyLabel);
            return;
        }

        for (TftItem builtItem : builtItems) {
            Label builtItemLabel = new Label(builtItem.getName());
            builtItemLabel.getStyleClass().add("detail-text");
            detailPanel.getChildren().add(builtItemLabel);
        }
    }

    private void addRecipeDetail(TftItem item, VBox detailPanel) {
        Label sectionTitle = new Label("Recipe");
        sectionTitle.getStyleClass().add("detail-section-title");
        detailPanel.getChildren().add(sectionTitle);

        List<TftItem> components = appContext.getItemRepository().findRecipeComponents(item);

        if (components.isEmpty()) {
            Label emptyLabel = new Label("Recipe data is unavailable.");
            emptyLabel.getStyleClass().add("detail-text");
            detailPanel.getChildren().add(emptyLabel);
            return;
        }

        String recipeText = components.stream()
                .map(TftItem::getName)
                .reduce((first, second) -> first + " + " + second)
                .orElse("");

        Label recipeLabel = new Label(recipeText);
        recipeLabel.getStyleClass().add("detail-text");
        recipeLabel.setWrapText(true);
        detailPanel.getChildren().add(recipeLabel);
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

    private int countRecipes(List<TftItem> items) {
        int count = 0;

        for (TftItem item : items) {
            if (!item.getComponentIds().isEmpty()) {
                count++;
            }
        }

        return count;
    }
}
