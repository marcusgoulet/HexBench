# HexBench

HexBench is a JavaFX desktop app for exploring Teamfight Tactics data. The current focus is a small item tracker foundation: browsing item categories, inspecting item details, and understanding recipe/build relationships.

The project is intentionally scoped around Items first. Champions, Traits, and Builds are planned areas, but they are not active features yet.

## What You Can Do

- Browse TFT items by category.
- View item names, icons, categories, descriptions, stats, recipes, and build targets.
- Click item rows to select them and update the detail panel.
- Click recipe components and build-target rows from the detail panel.
- Use a dark desktop layout with a sidebar and item inspector panel.

Default Item Page
![HexBench Item Browser](img/Hexbench%20Item%20Page.png)

Item Selection
![Hexbench Item Selection](img/Hexbench%20Item%20Page%202.png)

## Current Scope

HexBench currently focuses on TFT items:

- Components
- Completed items
- Artifacts
- Radiant items
- Emblems

The app is still early and intentionally modest. It is meant to become a useful TFT reference tool one piece at a time.

## Tech Stack

- Java 25
- JavaFX 21
- Maven
- Jackson Databind
- JUnit 5

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

## Current Focus

The current focus is making the Items view easier to use and read:

- Clear item selection.
- Clickable recipe and build relationships.
- A readable item detail panel.
- Better grouping for stats, descriptions, recipes, and build targets.

## Status

HexBench is a work in progress. Expect rough edges while the item tracker foundation is being built.

## Disclaimer

HexBench isn't endorsed by Riot Games and doesn't reflect the views or opinions of Riot Games or anyone officially 
involved in producing or managing Riot Games properties.
