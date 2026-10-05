# HexBench

HexBench is a JavaFX desktop app for exploring Teamfight Tactics data. The current focus is a small item tracker foundation: browsing item categories, inspecting item details, and understanding recipe/build relationships.

The project is intentionally scoped around Items first. Champions, Traits, and Builds are planned areas, but they are not active features yet.

## Current Features

- Browse TFT items by category.
- View item names, icons, categories, descriptions, stats, recipes, and build targets.
- Click item rows to select them and update the detail panel.
- Click recipe components and build-target rows from the detail panel.
- Display local Data Dragon PNG item icons when available.
- Load item data from local Riot data resources.
- Use a dark JavaFX desktop layout with a sidebar and item inspector panel.

## Tech Stack

- Java 25
- JavaFX 21
- Maven
- Jackson Databind
- JUnit 5

## Project Layout

```text
src/main/java/lol/hexbench/
├── AppContext.java
├── HexBenchApplication.java
├── Launcher.java
├── MainController.java
├── data/
│   ├── CommunityDragonItemLoader.java
│   ├── DataDragonItemLoader.java
│   └── TftItemRepository.java
└── model/
    ├── ItemCategory.java
    └── TftItem.java
```

```text
src/main/resources/
├── data/
│   ├── communitydragon/
│   └── ddragon/
└── lol/hexbench/
    ├── main-view.fxml
    └── style.css
```

## Running the App

Use the Maven wrapper:

```bash
./mvnw javafx:run
```

On Windows:

```bash
mvnw.cmd javafx:run
```

## Running Tests

```bash
./mvnw test
```

## Data Notes

HexBench currently stores local data under `src/main/resources/data`.

CommunityDragon is the primary content source for item recipes, descriptions, effects, and related metadata. Data Dragon is used as a current-set allowlist and as a source for local PNG image filenames.

Raw downloaded data files should be treated as source data and should not be manually edited.

## Development Focus

The current development focus is a small, understandable item tracker foundation. Recent work has focused on JavaFX UI behavior and visual structure:

- Selected item state.
- Clickable recipe/build relationships.
- A clearer item detail inspector.
- Better detail-panel grouping and stat layout.

The goal is not to build the entire TFT ecosystem at once. Keep changes modest, inspectable, and useful.

## Project Notes

See `PROJECT_NOTES.md` for working notes, data-source decisions, UI direction, and next likely steps.
