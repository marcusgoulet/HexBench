# HexBench

HexBench is a JavaFX desktop app for exploring Teamfight Tactics data. The current focus is a small item tracker foundation: browsing item categories, inspecting item details, and understanding recipe/build relationships.

The project is intentionally scoped around Items first. Champions, Traits, and Builds are planned areas, but they are not active features yet.

## What You Can Do

- Browse TFT items by category.
- View item names, icons, categories, descriptions, stats, recipes, and build targets.
- Click item rows to select them and update the detail panel.
- Click recipe components and build-target rows from the detail panel.
- Use a dark desktop layout with a sidebar and item inspector panel.

## Current Scope

HexBench currently focuses on TFT items:

- Components
- Completed items
- Artifacts
- Radiant items
- Emblems

The app is still early and intentionally modest. It is meant to become a useful TFT reference tool one piece at a time.

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
