# 🔢 DroidSudoku — Sudoku for Android

> A native Jetpack Compose Sudoku app for Android — uniquely-solvable puzzles, pencil-mark notes, limited hints and a local leaderboard.

"DroidSudoku" is a from-scratch Kotlin implementation built around a single guarantee: **every generated puzzle has exactly one solution**. There's no puzzle database — each game is freshly generated on-device by filling a full grid and carving it back down while checking uniqueness at every step.

## 📦 What's Inside

- 🎚️ Three difficulty levels — **Easy** (40–45 clues), **Medium** (32–36 clues) and **Hard** (26–30 clues)
- ✅ Uniquely-solvable puzzles — clues are removed one at a time, keeping each removal only if the puzzle still solves to exactly one grid
- ✏️ Pencil-mark notes mode — jot down candidate digits in a cell instead of committing a final value
- 🚫 Live conflict detection — placing a digit that already exists in the same row, column or 3x3 box highlights every conflicting cell
- 💡 Limited hints per game (3/4/5 by difficulty) that reveal the correct value for the selected cell — or a random incorrect one if nothing's selected — and ↩️ unlimited undo
- 🎬 Smooth Compose animations on digit entry and hint reveal
- 💾 Autosaves mid-game, with a "Continue Game" option from the main menu
- 🏆 A local best-time / fewest-hints leaderboard per difficulty, stored on-device (no backend), shown after a win
- 🔊 Sound feedback on digit entry, conflicts and winning (see [Sound](#-sound) below)
- 📖 An in-app "How to Play" guide covering the rule, given cells, conflicts, notes and the hint/undo tools
- 🇵🇹 🇬🇧 One-click language toggle between European Portuguese and English, remembered between visits

## 🛠️ Tech Stack

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?style=flat&logo=android&logoColor=white)
![Material3](https://img.shields.io/badge/Material%203-757575?style=flat&logo=materialdesign&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=flat&logo=gradle&logoColor=white)

## 🏗️ Project Structure

```
DroidSudoku/
├── build.gradle.kts / settings.gradle.kts / gradle.properties
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/                          # Theme, strings, adaptive launcher icon
│       │   └── java/com/vidi/droidsudoku/
│       │       ├── MainActivity.kt            # Entry point, screen router
│       │       ├── data/
│       │       │   └── SudokuCell.kt           # Cell model + difficulty clue/hint tiers
│       │       ├── engine/
│       │       │   ├── SudokuGenerator.kt       # Randomized full-grid fill + hole digging
│       │       │   ├── SudokuSolver.kt           # MRV backtracking solver, capped solution counter, conflict scan
│       │       │   ├── GameEngine.kt              # Board state, dispatch results, undo stack, hints
│       │       │   ├── SaveStore.kt                # Mid-game autosave/restore (SharedPreferences)
│       │       │   └── LeaderboardStore.kt          # Local best time/hints per difficulty (SharedPreferences)
│       │       ├── i18n/Localization.kt            # PT/EN strings and language persistence
│       │       └── ui/
│       │           ├── theme/Theme.kt               # ividi.dev-matched color tokens
│       │           ├── sound/SoundFx.kt               # Placeholder ToneGenerator-based sound feedback
│       │           └── screens/
│       │               ├── SplashScreen.kt              # Animated intro with developer credit
│       │               ├── MainMenuScreen.kt             # Menu, difficulty picker, language toggle
│       │               ├── HowToPlayScreen.kt             # Rules guide with a visual grid diagram
│       │               ├── GameScreen.kt                   # Board screen, stats, actions, modals
│       │               ├── SudokuGridView.kt                # 9x9 grid rendering, cell/box borders, highlighting
│       │               ├── NumberPad.kt                      # Digit entry pad + erase
│       │               ├── Modals.kt                          # Win / confirm modals + leaderboard block
│       │               ├── Buttons.kt                          # Shared button styles
│       │               └── BackgroundGlow.kt                    # Shared theme components
│       └── test/java/com/vidi/droidsudoku/            # JUnit unit tests (generator, solver, engine)
├── LICENSE
└── README.md
```

## ⚙️ Game Mechanics

### Generating a uniquely-solvable puzzle
```
1. Fill an empty 9x9 grid completely via randomized backtracking
   (shuffled digit order at each cell, row/col/box legality check)

2. Dig holes: shuffle cell order, then for each cell —
     - tentatively clear it
     - re-solve the puzzle with a capped counter (stop early at 2 solutions)
     - keep the removal only if the count is still exactly 1
     - otherwise put the digit back
   stop once the difficulty's minimum clue count is reached

3. The solver used for both digging and hints is a single MRV
   (minimum-remaining-values) backtracker: always branch on the
   emptiest cell first, so contradictions surface almost immediately
```

### Hints & conflicts
```
Hint: reveals solution[selectedCell] if that cell is empty or wrong;
      otherwise picks a random cell that still needs fixing. Capped
      per game by difficulty (Easy 3 / Medium 4 / Hard 5).

Conflicts: recomputed from scratch after every move — for each row,
      column and 3x3 box, any digit appearing more than once flags
      every cell holding it. Win = grid full AND zero conflicts.
```

## 🚀 How to Run

```bash
# 1. Clone the repository
git clone https://github.com/VidiPT89/DroidSudoku.git
cd DroidSudoku

# 2. Build and install a debug APK on a connected device/emulator
./gradlew installDebug

# ...or open the project in Android Studio and run it from there.

# Run the JUnit unit tests (generator uniqueness, solver conflicts, engine logic)
./gradlew test
```

## 🔊 Sound

Digit entry, conflict and win feedback use `android.media.ToneGenerator` — this is an explicit **placeholder**, since the repository ships no custom audio assets under `res/raw/`. See the comment at the top of `ui/sound/SoundFx.kt` for how to swap in real sound files later (drop `.ogg`/`.mp3` files into `res/raw/` and replace the `ToneGenerator.startTone` calls with a small `SoundPool`).

## 📝 Notes

- Given (fixed) cells are set once at generation time and can never be edited or cleared, only the cells you fill in yourself
- Correcting a wrong entry counts as a valid hint target, not just filling in a blank cell
- Undo restores both the previous value and the previous notes for a cell, and un-spends a hint if the move it's undoing was one
- Language, in-progress games and the local leaderboard are all stored locally via `SharedPreferences`, so they persist between visits
- This is an independent Kotlin codebase, part of a small triplet of from-scratch Sudoku implementations across Android, iOS/macOS and the web — no code is shared between them

---

Developed by **David Arsénio Martins** — *"Vidi"*
