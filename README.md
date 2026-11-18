# Chess — a Chess.com-style Android app, built 1:1 from my own screenshots

A dark-theme chess app for Android (Java, no frameworks) that replicates the Chess.com mobile UI **pixel-by-pixel from real screenshots**. Every image, card, avatar, icon and hero art in the app is cropped directly from the actual Chess.com screenshots in [`screenshots/`](screenshots/) — nothing is AI-generated.

## What's inside

- **Chess rules engine** — [`chesslib`](https://github.com/bhlangonijr/chesslib) (via JitPack), the popular GitHub chess library: move generation, legality, castling, en-passant, promotion, check/mate/draw detection.
- **Bot opponent** — custom minimax with alpha-beta pruning on top of chesslib (`BotPlayer.java`), with personas:
  - `codon201100 (1472)` — depth 3
  - `Marko (1123)` — depth 2, more random
  - `David (847)` — coach mode, gives tips during the game
- **Pieces** — the classic cburnett set from the lichess repository, rasterized to 256px PNG with resvg-js (`tools/raster.js`).
- **App icon** — cropped straight from the provided icon screenshot.

## Screens (all matching the screenshots)

| Screen | Files |
|---|---|
| Login (Play as Guest) | `LoginScreen.java` |
| Home — 3 states (new user / after Learn / after a game) | `HomeScreen.java` |
| New Game (10 min selector, expandable list) | `NewGameScreen.java` |
| Live game (Searching…, clocks, move list, resign/flip, result) | `GameScreen.java`, `BoardView.java` |
| Learn lesson path | `LearnScreen.java` |
| Watch (Events Today + News) | `WatchScreen.java` |
| More menu | `MoreScreen.java` |
| Premium paywall (Diamond/Platinum/Gold/Friends & Family, AZN prices) | `PremiumScreen.java` |
| Friends | `FriendsScreen.java` |
| Puzzles (mate-in-1 solver, hand-verified solutions) | `PuzzlesScreen.java` |

## Build

```bash
# requires JDK 17 + Android SDK (API 35) + Gradle 8.9
gradle assembleDebug
# output: app/build/outputs/apk/debug/app-debug.apk
```

`local.properties` (sdk.dir) is machine-specific and not committed — create it if needed.

## Art pipeline (`tools/`)

- `crops.py` — crops every card/icon/avatar/hero from `screenshots/` into `assets_crops/` (coordinates calibrated with grid overlays)
- `raster.js` — cburnett SVG → PNG pieces + green logo pawn (resvg-js)
- `icon.py` / `grid.py` — launcher icon processing / crop calibration

## Screenshots used

![login](screenshots/Screenshot%202026-11-18%20071700.png)
![home](screenshots/Screenshot%202026-11-18%20071929.png)
![game](screenshots/Screenshot%202026-11-18%20072146.png)
