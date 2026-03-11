# UI System

<!-- Generated: 2026-03-12 | Files scanned: ~15 | Token estimate: ~500 -->

## Screen Architecture

### Screens
- `StartScreen` - Main menu
- `LevelSelectScreen` - Level selection
- `GameScreen` - Main game screen (1000+ lines)

### UI Manager (Phase 2)
- `GameUIManager` - Unified UI rendering & input
  - Scene2D Table-based layout
  - ButtonCallback interface for click handling
  - Layout params: deckX, deckY, shopX, shopY
  - Methods: `getCardAtDeckPosition()`, `getCardAtShopPosition()`

### View Management
- `ViewManagement` - Dual viewport system
  - `UI Viewport` - Screen coordinates for UI
  - `Game Viewport` - World coordinates for battlefield
  - `screenToUI()`, `screenToWorld()` conversions

## UI Areas

### Shop (Left Top)
- `CardShop` - Shop logic
- `CardPool` - Available cards
- Card rendering: `CardRenderer.render()`

### Deck (Right Top)
- `PlayerDeck` - Owned cards
- Upgrade indicators
- Click detection aligned with render coordinates

### Battlefield (Bottom)
- Tiled map rendering
- Character placement (drag & drop)
- Projectile visualization

### Header
- Level display
- Economy info (gold, interest)
- Synergy info
- Back button, Start Battle button

## Input Handling
- `GameInputHandler` - Mouse/touch input
- Drag events: `DragStartedEvent`, `DragMovedEvent`, `DroppedEvent`, `DragCancelledEvent`
- `GameEventSystem` - Event dispatch

## Key Files
- `screens/GameScreen.java` (1000+ lines) - Main game screen
- `ui/GameUIManager.java` (680 lines) - UI manager
- `input/GameInputHandler.java` - Input handling
- `event/GameEventSystem.java` - Event system
- `ui/CardRenderer.java` - Card rendering
