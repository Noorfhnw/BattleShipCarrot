# Battleship Carrot

**Author:** Noor Vinnai  
**Course:** 26FS Mobile Applications with Android

This is my student project submission for the Android course. The project is an Android client for a multiplayer Battleship game, themed around rabbits and carrots. The course was around Easter, that's why I went with the playful rabbit/carrot theme instead of the classic naval one.

Battleship Carrot is an Android client for a multiplayer Battleship game built with **Kotlin** and **Jetpack Compose**. The project keeps the classic Battleship rules, but swaps the naval theme for a rabbit-and-carrot theme: ships become named carrots, the UI uses playful graphics, and the game includes themed sound effects for attacks, hits, wins, and losses.

The App was developed in Android Studio Panda (2025.3.4), and needs `minSdk = 27`, targets `targetSdk = 36`.

The client handles:

- the full ship placement flow
- validation for illegal placements
- server connection and join requests
- turn-based firing against the enemy board
- receiving enemy shots from the backend
- end-of-game feedback with themed audio

## Running the project

The application can be run on an Android emulator or a physical device, after the gradle project is imported in Android Studio.

Current default DEFAULT_BASE_URL is set to "http://brad-home.ch:50003", but it can be changed in-game.

## Architecture and structure

<img src="./images/architecture.png" alt="Architecture diagram"  />

(diagram made in excalidraw)


The app follows an **MVVM-style structure**:

- **Compose UI** renders the screens and forwards user actions
- **ViewModel** owns the game state and game rules
- **Service/API layer** talks to the backend and maps JSON into app-friendly models
- **Audio layer** handles background music and sound effects

This split keeps the screen composables relatively focused on presentation, while the gameplay logic stays  in `BattleshipViewModel`.

## Main code areas

`*` = `app/src/main/java/ch/fhnw/vinnai/` 

| Path                                                                                 | Responsibility |
|--------------------------------------------------------------------------------------| --- |
| `*/MainActivity.kt`                                                                  | Android entry point, creates the ViewModel, wires the app to audio, and forwards lifecycle events to the `SoundManager` |
| `*/battleshipclient/BattleshipApp.kt`                                                | Root Compose flow; switches between welcome, placement, and game UI |
| `*/battleshipclient/viewmodel/BattleshipViewModel.kt`                                | Core game logic: placement validation, join flow, turn flow, firing, enemy-fire handling, and UI state updates |
| `app/src/main/java/ch/fhnw/vinnai/battleshipclient/viewmodel/BattleshipUiState.kt`   | Immutable UI-facing state model used by Compose |
| `app/src/main/java/ch/fhnw/vinnai/battleshipclient/view/ShipPlacementScreen.kt`      | Placement UI and controls |
| `app/src/main/java/ch/fhnw/vinnai/battleshipclient/view/GameScreen.kt`               | Match UI, boards, status, and result banner |
| `app/src/main/java/ch/fhnw/vinnai/battleshipclient/service/BattleshipApi.kt`         | Low-level HTTP communication using `HttpURLConnection` |
| `app/src/main/java/ch/fhnw/vinnai/battleshipclient/service/GameService.kt`           | Maps raw JSON responses into objects like `GameStatus` and `FireResult` |
| `app/src/main/java/ch/fhnw/vinnai/battleshipclient/audio/SoundManager.kt`            | Background music and sound effect playback |

## User interface and flow

### 1. Welcome screen

The first screen is the app start screen, which has a single Start button:

<img src="./images/main-screen.png" width="200" />

### 2. Ship placement screen

After starting, the player enters the placement phase. This is where the carrot "fleet" is planted into a **10x10 board**.

The placement screen shows:

- the current carrot/ship that must be placed next
- its required length
- the current orientation
- the board with row labels **A-J** and column labels **0-9**
- action buttons for **Horizontal/Vertical**, **Undo**, and **Reset**

Placement is validated before a ship is accepted. The app blocks placements that:

- go outside the board
- overlap with already placed ships

If a placement is invalid, an error message is shown.

<img src="./images/placement-screen.png" alt="Ship placement screen" width="250" />

### 3. Connection and join flow

Once all ships are placed, the app unlocks the connection controls at the top of the screen. The player can then:

1. enter or change the backend server address
2. ping the server to check whether it is reachable
3. enter a player name and game key, minimum length of 3 characters is validated for both fields in the client
4. join a match

If the request succeeds, the player enters the game screen. If it fails, the app stays on the current screen and shows the backend error message.

<img src="./images/joined-game-screen.png" alt="Joined game screen" width="250" />


### 4. Game screen

During the match, the player sees:

- the current game ID
- a status message that shows whether it is the player's turn, the app is firing, or it is waiting for the opponent
- a list of enemy ships already sunk (list of carrots chomped)
- the opponent board used for targeting
- the player's own board used as the defense board at the bottom

When it is the player's turn, they can tap a cell on the opponent board to fire. When it is not their turn, the app waits for the backend to report the enemy move.

When the game ends, the app displays a **You Won!** or **You Lost!** banner and plays the matching sound.

<img src="./images/gameplay.png" alt="Gameplay screen" width="250" />

Gameplay screen on Google Pixel 9 Pro XL


## Game rules implemented in the client

The client implements usage for all APIs of the server.  
Rules enforced:

- Board size is **10x10**
- Rows are labeled **A-J**
- Columns are labeled **0-9**
- Every ship must be placed before joining a game
- Ships cannot overlap
- Ships cannot extend outside the board
- The player can only fire when `isMyTurn` is true
- The player cannot fire twice at the same target cell
- The game ends when the backend reports game over

Each board cell is represented by one of four states:

| State | Meaning |
| --- | --- |
| `EMPTY` | Nothing placed or no shot taken yet |
| `SHIP` | A player ship occupies the cell |
| `HIT` | A shot hit a ship |
| `MISS` | A shot landed on an empty cell |

These states are used across placement, defense, and targeting boards.

## Carrot fleet

The project replaces classic ship names with carrot themed names:

| Ship in code | Name shown in app | Size |
| --- | --- | ---: |
| `Carrier` | Big Chungus | 5 |
| `Battleship` | Fat Carrot | 4 |
| `Destroyer` | Bad Bunny | 3 |
| `Submarine` | Digger | 3 |
| `PatrolBoat` | Lil Hop | 2 |


## State and flow

The ViewModel drives the app through three main phases:

1. **Placement** - the user places all ships on `placementBoard`
2. **Joined game** - the placement board is copied to `myBoard`, and the client joins a backend match
3. **Turn-based play** - the user attacks through `opponentBoard` while enemy shots are applied to `myBoard`

Important state is stored in `BattleshipUiState` includes:

- server and ping information
- current turn state
- join/loading status
- win/loss state
- sunk enemy ships
- placed ship list
- current ship index and orientation
- placement error messages

Alongside that UI state, the ViewModel also manages three 10x10 board structures:

- `placementBoard`
- `myBoard`
- `opponentBoard`

That separation is useful because the general UI state describes the phase of the game, while the board arrays hold the frequently changing cell values.

## Backend communication

The client expects a compatible Battleship server and communicates with these endpoints:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/ping` | Check whether the server is reachable |
| `POST` | `/game/join` | Join a match with player name, game key, and ship placement |
| `POST` | `/game/fire` | Fire at the opponent board |
| `POST` | `/game/enemyFire` | Wait for and receive the enemy move |

### Data sent by the client

When joining a game, the client sends:

- player name
- game key
- the full fleet with coordinates and orientation

When firing, the client sends:

- player name
- game key
- target `x`
- target `y`

### Data used by the client

The service layer converts backend JSON into:

- `GameStatus`
- `FireResult`
- `BoardShot`

This keeps raw JSON handling out of the UI layer.

## Audio

The app includes:

- looping background music
- a digging sound when the player attacks
- a carrot-eating sound when a hit lands
- separate win and lose sounds

Audio playback is managed by `SoundManager`, which is started, paused, and released together with the Android activity lifecycle.
The SoundManager implementation was created with the help of GitHub Copilot Plan mode, it helped me understand how the sound loop works, and also why the background music quality was crispy in the beginning.

## Tests

The repository includes a few unit and integration tests for:

- `PlacedShipTest`
- `BattleshipViewModelTest`
- `GameServiceIntegrationTest`

These cover placement behavior and service-layer/backend interaction logic.

```

## Credits

- Sound effects: Pixabay / freesound_community and floraphonic
- Audio edited with Audacity
- Welcome screen background created with ChatGPT
