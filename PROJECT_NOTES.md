# HexBench Project Notes

## Purpose

HexBench is a JavaFX desktop app for exploring Teamfight Tactics data, starting with an item tracker.

The first real feature focus is Items:

- View TFT item components and completed items.
- Understand item build paths.
- Use Riot Data Dragon as the source for raw item names and image metadata.
- Keep app-specific logic, such as recipes and category handling, separate from raw Riot data.

Future areas are Champions, Traits, and Builds, but those are intentionally parked for now so the app does not turn into a half-built tab museum.

## Current App State

The project has been cleaned up from the default JavaFX starter app:

- Base package is now `lol.hexbench`.
- Main app class is `HexBenchApplication`.
- JavaFX entry helper is `Launcher`.
- Main controller is `MainController`.
- Main FXML view is `main-view.fxml`.
- App stylesheet is `style.css`.
- Window title is `HexBench`.
- Window opens at about 65% of the primary screen size.
- The UI currently uses a simple modern dark shell.
- The sidebar is focused on `Items`; Champions, Traits, and Builds are left as TBD comments.

The app is currently more foundation than feature. That is intentional.

## Current Source Layout

```text
src/main/java/lol/hexbench/
├── AppContext.java
├── HexBenchApplication.java
├── Launcher.java
├── MainController.java
├── data/
│   ├── DataDragonItemLoader.java
│   └── TftItemRepository.java
└── model/
    ├── ItemCategory.java
    └── TftItem.java
```

Resources:

```text
src/main/resources/
├── data/
│   ├── communitydragon/
│   │   └── 16.19.1/
│   │       └── tft-en_us.json
│   └── ddragon/
│       └── 16.19.1/
│           └── tft-item.json
└── lol/
    └── hexbench/
        ├── main-view.fxml
        └── style.css
```

## Data Dragon Notes

The current raw Data Dragon item file is:

```text
src/main/resources/data/ddragon/16.19.1/tft-item.json
```

This file should be treated as downloaded raw source data. Do not manually edit it.

Important discoveries:

- The file has around 1,188 item-like entries.
- It is not only current normal TFT items.
- It includes older sets, tutorial items, consumables, radiant items, artifacts, emblems, Double Up assist items, and other special data.
- Current Set 18 item entries appear under:

```text
TFTSet18/Set18_Items/
```

- Set 18 item objects only contain:

```text
id
name
image
```

- Data Dragon does not appear to provide item recipes/build paths in this file.

This means HexBench should parse names/images from Data Dragon, but maintain its own recipe/build-path data later.

Update: CommunityDragon is now also stored locally:

```text
src/main/resources/data/communitydragon/16.19.1/tft-en_us.json
```

CommunityDragon provides richer item fields, including:

```text
apiName
name
desc
icon
composition
effects
```

The `composition` field gives recipe component IDs. The app currently uses Data Dragon as the current-set filter/image source and CommunityDragon as the recipe/description enrichment source.

## Model Decisions

`TftItem` currently represents one item with:

```text
id
name
imageFile
category
```

`ItemCategory` is the app's controlled list of item buckets:

```text
COMPONENT
COMPLETED
RADIANT
ARTIFACT
EMBLEM
CONSUMABLE
UNKNOWN
```

The enum is not Riot data. It is HexBench's normalized category vocabulary.

Data Dragon item IDs such as `DA_Component_BFSword` should be classified into these categories by loader/importer logic, not by `TftItem` itself.

## Repository Decision

`TftItemRepository` stores:

```java
List<TftItem> items;
Map<String, TftItem> itemsById;
```

The list is useful for display. The map is useful for lookup by ID.

The repository currently builds the ID map from the item list in its constructor, which keeps the two structures consistent.

`AppContext` currently owns app-wide loaded data:

- It creates `DataDragonItemLoader`.
- It loads TFT items once during app startup.
- It creates `TftItemRepository`.
- `MainController` receives the `AppContext` after FXML loading.

Current data flow:

```text
HexBenchApplication
    -> AppContext
        -> CommunityDragonItemLoader
            -> Data Dragon tft-item.json for current Set 18 item IDs/images
            -> CommunityDragon tft-en_us.json for recipes/descriptions
        -> TftItemRepository
    -> MainController
        -> itemRepository.findAll()
```

## Current Dependency Direction

Jackson was added for JSON parsing:

```text
jackson-databind
```

The app uses Java modules, so `module-info.java` also needs Jackson listed with:

```java
requires com.fasterxml.jackson.databind;
```

If IntelliJ flags Jackson vulnerability warnings, prefer updating Jackson rather than ignoring the warning.

## Next Likely Steps

1. Start turning the Items summary into a real item tracker view.
   - Keep it modest.
   - First likely UI step: show components and completed items separately.
   - Avoid item images until the data/model flow is stable.

2. Add item selection behavior.
   - Clicking a component should eventually show completed items that use it.
   - Clicking a completed item should show its component recipe.
   - The detail panel should stay under roughly 45% of the item tab width.

## Guidance For A Future Agent

The user is learning Java, JavaFX, Maven, JSON parsing, packages, and architecture while building this. Do not take over the project unless explicitly asked.

Default mode should be advisory:

- Inspect files.
- Explain concepts.
- Review code.
- Suggest small next steps.
- Avoid large automatic rewrites.

Only edit files when the user explicitly asks for an implementation or modification.

Collaboration style:

- Be technically serious, but not sterile.
- Use dry humor and light sarcasm naturally.
- Aim for roughly 80% technical collaborator, 20% humor.
- Do not be condescending.
- Do not force jokes.
- Technical clarity always wins over being funny.

The current project goal is not "build the whole TFT stats app." The active goal is:

```text
Build a small, understandable item tracker foundation using Data Dragon item data.
```

Keep designs simple, explain tradeoffs, and avoid enterprise abstraction cosplay.
