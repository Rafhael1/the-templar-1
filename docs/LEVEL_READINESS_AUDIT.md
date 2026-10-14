# Readiness for additional levels

This document records the baseline before the level infrastructure refactor. The implemented workflow is documented in `ADICIONAR_FASES.md`.

Audit date: 2026-10-13. Reviewed the current working tree, including the existing local refactor of `GameAssets.java`; that refactor was preserved.

## Verdict

The project has a suitable foundation for more levels, but adding a TMX file does not currently make another playable level available. It is a working single-level game, with reusable simulation code and several assumptions about its original content embedded in application and presentation code.

Keep the existing modules and packages. A small level definition, explicit progression, and stronger map validation are sufficient for the next step. An ECS, plugin architecture, general campaign framework, or persistence system is unnecessary for adding a second level.

## Scope and verification

Reviewed both Gradle modules; all production Java classes; the test suites; the TMX and Tiled project; localization catalogs; asset-generation scripts; launch and packaging configuration; and project documentation.

| Check | Result | What it establishes |
|---|---|---|
| `./gradlew test build` | Passed | Compilation, checks, JARs and development distributions build on this Mac. Initial test result was cached. |
| `./gradlew :core:test --rerun-tasks` | Passed: 91 tests, zero failures/errors/skips | Fresh execution of the existing simulation, loader, input, localization and scenery tests. |
| `./gradlew :lwjgl3:smokeTest` | Passed | Real desktop/OpenGL execution: menus, intro, controls, audio initialization, pause, resize, full route to victory, arena, terrain pixels, deaths, language and preference restoration. |
| Screenshot inspection | Arena and middle scenery transition inspected | These two current-level captures appeared coherent; this was not a manual review of every screenshot. |
| `./gradlew :lwjgl3:performanceTest` | 1920×1080; 300 measured frames; median 1.071 ms, p95 1.314 ms, max 2.243 ms | Current-level simulation/render cost on this machine, including GPU completion and excluding vsync waiting. Does not establish performance of denser future levels. |
| Temporary Java probes against compiled production classes | Confirmed validation gaps below | Used temporary files outside the repository; no gameplay changes or permanent test additions. |

Native `appImage` packaging was not rebuilt or launched. Windows/Linux execution, fresh dependency downloads, manual playtesting, and hypothetical future maps were not verified. No second level exists to exercise transitions.

## Findings, in implementation order

### 1. High: no selectable level or progression path

Sources: `presentation/GameAssets.java:29,77,135`, `screen/PlayScreen.java:35,69`, `screen/MenuScreen.java:63`, `CrusaderGame.java:47`.

`GameAssets.MAP` is the single path `maps/exiles-road.tmx`. The asset manager loads this map once, `map()` returns it, and both gameplay and menu geometry load the same constant. After victory, `PlayScreen` calls `game.startGame()`, constructing the same level again. There is no level identity, ordered level list, selected level, or next-level transition.

Adding another TMX only includes a resource in the build. Changing only the simulation map would also leave the renderer drawing the old map.

Minimal change: define an immutable `LevelDefinition` with a stable ID, TMX path and presentation metadata; pass the selected definition through game, play screen and renderer. Use one loading operation for the selected map's rendered and simulated representations. Keep a small explicit ordered list and current index in `CrusaderGame`; distinguish next level from final campaign victory. Retain a deliberate first-level menu backdrop.

### 2. High: accepted placements can make a level impossible

Sources: `level/LevelData.java:27-45`, `gameplay/GameWorld.java:117-131`.

Validation requires exactly one guardian but does not require it to be inside the arena or clear of terrain and gates. Spawns, checkpoint, exit, pickups and solids also lack comprehensive placement checks. Exit placement beyond the gate and a clear arena are assertions about the committed map in `LevelLoaderTest`, not checks applied to every loaded map.

A temporary probe kept the committed geometry, placed the guardian at `(600,64)`, and started the player inside the final arena. After 1,200 fixed steps, the world remained `PLAYING`, the arena was locked, and the guardian remained at x=600 in `PATROL`. The boss was unreachable behind the sealed entrance.

Minimal change: validate entity body bounds, guardian containment, spawn/checkpoint clearance, gate/exit ordering and the arena's supported geometry. Use the actual player and enemy dimensions through an explicit shared geometry contract rather than approximate test rectangles. Validate basic supported geometry at load time; route reachability still needs simulation/playtesting.

### 3. Medium: loader accepts unsupported or invalid inputs

Sources: `level/LevelLoader.java:13-29,36-42,59-61`, `level/LevelData.java:17-29`.

The loader checks orthogonal orientation and rectangular area shapes, but does not reject infinite maps or layer offsets. It only reads root object groups, so moving required layers into Tiled groups is unsupported. Spawn/checkpoint/enemy objects are not required to be unrotated points. Layer names are filtered, but repeated contract layers are not explicitly rejected.

Temporary probes established:

- Adding `offsetx="128" offsety="32"` to `collisions` produced exactly the same collision bounds: offsets were ignored.
- Changing only `infinite="0"` to `infinite="1"` was accepted by `LevelLoader`. This demonstrates a missing rejection, not support for real chunked infinite maps.
- `LevelData` accepted `NaN` width, a `NaN` spawn x, and an out-of-map spawn `(-500,900)`. These direct-construction probes establish missing model invariants; they do not assert that normal Tiled exports contain NaN.

Minimal change: make the supported Tiled subset explicit and reject unsupported features. Check finite dimensions/coordinates, positive tile/map dimensions, point shapes, unique required layers and offsets. Reject nested contract layers with a clear diagnostic. No alternative parsers or compatibility fallbacks are needed.

### 4. Medium: scenery and camera are tied to the original road

Sources: `presentation/SceneryTransition.java:5-17`, `presentation/WorldRenderer.java:57-68,83-85,165-170,205-249`, `screen/PlayScreen.java:97-100`.

Four regions are fixed at 2,240-pixel intervals with a 640-pixel blend; textures are `region-0` through `region-3`. Chapter announcements use the same region indexes. A shorter or longer level inherits these positions and labels regardless of its content.

The camera stays at y=270, covering the fixed 540-pixel viewport vertically. Tall levels can contain playable geometry outside the view. The x clamp assumes a world at least 960 pixels wide. Boss arenas have a fixed centered camera, but `LevelData` accepts arenas wider than one screen: a probe accepted width 8,704. The whole fight would not fit on screen.

Decorations assume a road floor at y=64; torches use fixed heights and spacing, flag colors change at x=4480, and corpse rendering has a minimum floor of 64. A level with a different floor elevation would have floating/buried decoration and incorrect corpse placement.

Minimal change: put region boundaries, textures, text keys and authored decoration placements in level metadata. For the next level, either explicitly enforce the existing horizontal camera envelope and arena width, or implement the required camera behavior if the design actually needs vertical travel. Derive corpse landing from level geometry without an assumed road floor.

### 5. Medium: UI and narrative are shared content, not level metadata

Sources: `screen/IntroScreen.java:16,42-54`, `screen/MenuScreen.java:123-157`, `presentation/Hud.java:86-88,157-174`, `assets/i18n/messages_{pt,en}.properties`.

The intro, menu chapter/story/level, boss name, objectives, region announcements and victory text all describe the existing journey. Localization itself is well organized and rejects missing translations, but there is no association between a level and its content keys. New levels would display the original route and guardian narrative unless code/catalog usage is changed.

Minimal change: reference level-specific keys from `LevelDefinition`, keeping both language catalogs and the existing fail-fast translation mechanism. Keep shared control labels shared. Update final victory versus level-completion text with progression.

### 6. Medium: coverage verifies one map, not every available level

Sources: `core/src/test/java/br/com/thecruzader/level/LevelLoaderTest.java`, `gameplay/GameWorldTest.java`, `lwjgl3/.../SmokeTest.java`, `PerformanceTest.java`.

The existing suite provides useful behavioral coverage, including a complete command-driven route. Content tests load `exiles-road.tmx` explicitly and assert original widths, object counts and positions. The smoke tour has fixed landmarks, y=64 route heuristics, original tileset pixels and original scenery boundaries. Several unit-test fixtures use oversized arenas, so they do not establish compatibility with the renderer's arena camera.

Minimal change: retain first-level content assertions, add shared contract checks parameterized over the registered levels, and add one end-to-end two-level journey test. Verify rendered map and collision data refer to the same selected level; completion advances exactly once; death reloads the current level; pickups/checkpoint/arena reset correctly across level transitions; final victory and new journey work; and level-specific translations/assets load from packaged classpath resources. Give each new map its own route/render evidence instead of assuming the existing tour covers it.

### 7. Low: authoring documentation and generation need clearer ownership

Sources: `README.md:81-97,125-127,178`, `tools/generate_assets.py:277-324`.

The primary object-contract table omits the now-required `arena`; it is only described later. Following the table alone creates a map rejected by the loader. The monolithic generator writes the original TMX and shared assets on execution, and has no level/output argument. The README accurately warns that it overwrites edited content; it should remain an explicit regeneration tool, not the workflow for adding levels.

Minimal change: add `arena` to the main contract table, document camera/layout constraints, and document one clear add-level workflow: author TMX, register the level definition, add localized metadata/assets, validate and play the map, then rebuild the desired distribution. If map generation is needed, separate that operation from shared sprite/audio generation and require an explicit output path. No backups are necessary.

## What is already suitable

- `core` and `lwjgl3` are correctly separated; resource source sets already package additional files under `assets`.
- Gameplay is independent of graphics, filesystem and audio; `GameWorld` consumes immutable `LevelData`.
- Movement, enemies, boss behavior, body collisions, melee and projectiles have distinct responsibilities.
- Existing enemy placements, solid rectangles and pickups are data-driven and can be reused without duplicating gameplay systems.
- Fixed simulation steps, retained input presses, interpolation and event consumption are established and tested.
- `Bounds` validates finite positive rectangles, lists are copied, and unknown entity types/missing mandatory entities fail early.
- Screens own/dispose their stages and renderers; shared GPU/audio assets have one owner. Preserve this ownership when adding level loading/unloading.
- Localization keys, toolchain, strict compiler warnings, dependency locks and wrapper checksum are established.

There is no evidence that directories need a broad reorganization. `WorldRenderer` contains original-level decoration rules that should move into authored content; its rendering responsibility itself is appropriate.

## Supported design limits to retain unless requirements change

The current contract requires one checkpoint, one guardian, one arena, one gate, one exit and at least one healing pickup. It supports rectangular solids, three fixed enemy kinds, solid platforms and enemies that step up at most 32 pixels without jumping. There is no disk save or campaign state beyond the current world. These are deliberate constraints, not defects; additional levels can use them.

All enemies are simulated and terrain is scanned repeatedly each step. Body separation can make up to N passes over N enemies. Current performance is healthy, but much denser maps need measurement before adding spatial indexing or other optimization.

## Smallest useful next implementation

1. Strengthen map/model validation and its tests, including the unreachable guardian reproduction.
2. Introduce `LevelDefinition` and a single selected-level loading path shared by simulation and rendering.
3. Move only necessary scenery/text/decor metadata into that definition and keep the horizontal level constraints explicit.
4. Add the second authored map and an explicit level order; implement completion, final victory and restart state ownership.
5. Validate every registered level, run its route/render checks, and rebuild the distribution users will launch.

Health/pickup/checkpoint carry-over is a gameplay decision that should be made explicitly when implementing progression. The simple initial policy is a new `GameWorld` per level, resetting level-local state; no persistence subsystem is required.
