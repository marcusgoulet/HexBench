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
- The top app header was removed; `HexBench` now lives in the sidebar.
- The sidebar is focused on `Items` and acts as the current page indicator.
- Champions, Traits, and Builds are left as TBD comments.
- The Items view opens automatically after startup.
- Item category controls are custom full-width tab buttons above the item workspace.
- Those item tabs span both the scrollable item list and the selected-item detail panel.
- The selected-item panel shows name, category, cleaned description text, and recipe/builds-into data.
- The selected-item detail panel has its own vertical scroll pane for long content.
- Component build targets now show the completed item plus its recipe, with the selected component first and the secondary component second.
- Item rows now show Data Dragon PNG icons when a local image file exists.
- Description cleanup currently handles simple markup such as line breaks, tags, and icon tokens.
- Completed/radiant/artifact item descriptions are enriched from older/generic CommunityDragon records when the current `DA_...` item record has no description.
- Simple description placeholders such as `@ManaPercIncrease*100@` are resolved from the enriched `effects` map.

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
│           ├── img/
│           │   └── tft-item/
│           │       └── *.png
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
- Data Dragon is still useful as a current Set 18 allowlist because current item entries live under `TFTSet18/Set18_Items/`.
- Data Dragon also gives convenient `.png` image filenames through `image.full`.
- Data Dragon item PNGs are extracted locally from `dragontail-16.19.1.tgz` into `src/main/resources/data/ddragon/16.19.1/img/tft-item/`.

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

The `composition` field gives recipe component IDs.

Current source-of-truth decision:

- CommunityDragon is the primary content source for items.
- Data Dragon is used as a current-set allowlist and PNG image filename fallback.
- CommunityDragon provides `icon` paths, but those point to `.tex` assets, so they are stored separately from Data Dragon image filenames.
- Current `DA_...` CommunityDragon item records often have recipe/icon data but no `desc` or `effects`.
- The loader tries to enrich completed, radiant, and artifact items from described fallback records with matching normalized names, such as `DA_AdaptiveHelm` -> `TFT_Item_AdaptiveHelm`.
- Components are enriched through exact fallback ID mappings, such as `DA_Component_BFSword` -> `TFT_Item_BFSword`; Spatula and Frying Pan intentionally remain statless.
- Flat item stats are normalized from known fallback `effects` keys such as `AD`, `AP`, `AS`, `Armor`, `MagicResist`, `Health`, `Mana`, `ManaRegen`, `CritChance`, and `StatOmnivamp`.
- `AD`, `AP`, `AS`, `CritChance`, and `StatOmnivamp` are displayed as percent-style values.
- Emblems and consumables are intentionally skipped by the fallback enrichment pass for now because old same-name records can carry set-specific behavior that may be wrong.

## Model Decisions

`TftItem` currently represents one item with:

```text
id
name
iconPath
imageFile
category
componentIds
description
stats
effects
descriptionSourceId
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

1. Refine the current Items tracker view.
   - Keep it modest.
   - Tune spacing and visual hierarchy in the selected-item panel now that icons and recipes are visible.
   - Item images are now in the first modest pass; avoid turning the item grid into a dense visual redesign until behavior settles.

2. Improve item selection behavior.
   - Clicking a component shows completed items that use it.
   - Clicking a completed item shows its component recipe.
   - The detail panel should stay under roughly 45% of the item tab width.
   - The detail panel now scrolls independently for long selected-item content.
   - Selected item visual state in the item grid has not been added yet.
   - Component build targets now include the secondary component needed to create each built item.

3. Resolve item description placeholders.
   - Simple CommunityDragon placeholders such as `@AttackSpeed*100@` are now resolved from `effects`.
   - Runtime/computed placeholders such as `@TFTUnitProperty.item:...@` are not resolved yet.
   - Fallback source matching should be audited before treating all displayed values as authoritative.

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
