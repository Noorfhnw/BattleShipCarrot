package ch.fhnw.vinnai.battleshipclient.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.fhnw.vinnai.battleshipclient.service.GameService
import ch.fhnw.vinnai.battleshipclient.service.GameStatus
import ch.fhnw.vinnai.battleshipclient.service.NetworkResult
import ch.fhnw.vinnai.battleshipclient.view.CellState
import ch.fhnw.vinnai.battleshipclient.view.PlacedShip
import ch.fhnw.vinnai.battleshipclient.view.ShipType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

class BattleshipViewModel(
    private val gameService: GameService = GameService()
) : ViewModel() {
    companion object {
        const val MIN_JOIN_NAME_LENGTH = 3
    }

    enum class SoundEffect {
        CARROT_EAT,
        WIN,
        LOSE
    }

    var uiState by mutableStateOf(BattleshipUiState())
        private set

    var myBoard = Array(10) { Array(10) { mutableStateOf(CellState.EMPTY) } }
    var opponentBoard = Array(10) { Array(10) { mutableStateOf(CellState.EMPTY) } }

    private var playerName = ""
    private var gameKey = ""

    val joinedGameId: String
        get() = gameKey

    // Ship placement state
    private val shipOrder = ShipType.entries          // Carrier → PatrolBoat
    var placementBoard = Array(10) { Array(10) { mutableStateOf(CellState.EMPTY) } }
    var pendingSoundEffect by mutableStateOf<SoundEffect?>(null)
    var soundEventVersion by mutableIntStateOf(0)
    val currentShipType: ShipType?
        get() = shipOrder.getOrNull(uiState.currentShipIndex)

    val allShipsPlaced: Boolean
        get() = uiState.currentShipIndex >= shipOrder.size

    private inline fun updateUiState(update: BattleshipUiState.() -> BattleshipUiState) {
        uiState = uiState.update()
    }

    fun toggleOrientation() {
        updateUiState { copy(isHorizontal = !isHorizontal) }
    }

    fun placeShipAt(x: Int, y: Int) {
        val type = currentShipType ?: return
        val ship = PlacedShip(type, x, y, uiState.isHorizontal)

        // Bounds check
        val cells = ship.occupiedCells()
        if (cells.any { (cx, cy) -> cx !in 0..9 || cy !in 0..9 }) {
            updateUiState { copy(placementErrorMessage = "${type.displayName} doesn't fit there!") }
            return
        }

        // Overlap check
        val occupied = uiState.placedShips.flatMap { it.occupiedCells() }.toSet()
        if (cells.any { it in occupied }) {
            updateUiState { copy(placementErrorMessage = "Overlaps with another ship!") }
            return
        }

        // Place it
        cells.forEach { (cx, cy) -> placementBoard[cy][cx].value = CellState.SHIP }
        updateUiState {
            copy(
                placedShips = placedShips + ship,
                currentShipIndex = currentShipIndex + 1,
                placementErrorMessage = ""
            )
        }
    }

    fun undoLastShip() {
        if (uiState.isJoining || uiState.hasJoined || uiState.placedShips.isEmpty()) return
        val last = uiState.placedShips.last()
        last.occupiedCells().forEach { (cx, cy) -> placementBoard[cy][cx].value = CellState.EMPTY }
        updateUiState {
            copy(
                placedShips = placedShips.dropLast(1),
                currentShipIndex = currentShipIndex - 1,
                placementErrorMessage = ""
            )
        }
    }

    fun resetPlacement() {
        if (uiState.isJoining || uiState.hasJoined) return
        updateUiState {
            copy(
                placedShips = emptyList(),
                currentShipIndex = 0,
                placementErrorMessage = ""
            )
        }
        for (row in placementBoard) for (cell in row) cell.value = CellState.EMPTY
    }

    fun ping() {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { gameService.ping(uiState.serverBaseUrl) }
            updateUiState { copy(pingResult = result) }
        }
    }

    fun updateServerBaseUrl(rawValue: String): Boolean {
        val normalized = normalizeBaseUrl(rawValue) ?: run {
            updateUiState {
                copy(
                    statusMessage = "Invalid server URL",
                    pingResult = false
                )
            }
            return false
        }

        updateUiState {
            copy(
                serverBaseUrl = normalized,
                pingResult = null,
                statusMessage = if (statusMessage == "Invalid server URL") "" else statusMessage
            )
        }
        return true
    }

    fun joinGame(player: String, key: String) {
        if (uiState.isJoining) return
        val normalizedPlayer = player.trim()
        val normalizedGameKey = key.trim()
        val validationMessage = validateJoinInputs(normalizedPlayer, normalizedGameKey)
        if (validationMessage != null) {
            updateUiState { copy(statusMessage = validationMessage) }
            return
        }

        playerName = normalizedPlayer
        gameKey = normalizedGameKey
        pendingSoundEffect = null
        updateUiState {
            copy(
                isJoining = true,
                gameOver = false,
                didIWin = null,
                isMyTurn = false,
                isFiringShot = false,
                sunkEnemyShips = emptyList(),
                statusMessage = "Waiting for opponent…",
                joinedGameId = normalizedGameKey
            )
        }
        copyPlacementBoardToDefenseBoard()

        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) {
                gameService.joinGame(
                    uiState.serverBaseUrl,
                    normalizedPlayer,
                    normalizedGameKey,
                    uiState.placedShips
                )
            }) {
                is NetworkResult.Success -> {
                    applyGameStatus(result.value)
                    updateUiState {
                        copy(
                            statusMessage = "Joined game",
                            hasJoined = true
                        )
                    }
                    if (!uiState.isMyTurn && !uiState.gameOver) {
                        waitForEnemyFire()
                    }
                }
                is NetworkResult.Error -> {
                    updateUiState {
                        copy(
                            statusMessage = result.message,
                            isJoining = false
                        )
                    }
                }
            }
        }
    }

    fun fire(x: Int, y: Int) {
        if (!uiState.isMyTurn || uiState.gameOver || uiState.isFiringShot) return
        // Prevent firing on an already-shot cell
        if (opponentBoard[y][x].value != CellState.EMPTY) return

        updateUiState { copy(isFiringShot = true) }
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) {
                gameService.fire(uiState.serverBaseUrl, playerName, gameKey, x, y)
            }) {
                is NetworkResult.Success -> {
                    opponentBoard[y][x].value = if (result.value.hit) CellState.HIT else CellState.MISS
                    if (result.value.hit) {
                        emitSoundEffect(SoundEffect.CARROT_EAT)
                    }
                    if (result.value.gameOver) {
                        emitSoundEffect(SoundEffect.WIN)
                        updateUiState {
                            copy(
                                sunkEnemyShips = result.value.sunkEnemyShips,
                                gameOver = true,
                                didIWin = true,
                                isFiringShot = false,
                                statusMessage = "Shot fired"
                            )
                        }
                    } else {
                        updateUiState {
                            copy(
                                sunkEnemyShips = result.value.sunkEnemyShips,
                                isMyTurn = false,
                                isFiringShot = false,
                                statusMessage = "Shot fired"
                            )
                        }
                        waitForEnemyFire()
                    }
                }
                is NetworkResult.Error -> {
                    updateUiState {
                        copy(
                            isFiringShot = false,
                            statusMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun waitForEnemyFire() {
        viewModelScope.launch {
            when (val result = withContext(Dispatchers.IO) {
                gameService.waitForEnemyFire(uiState.serverBaseUrl, playerName, gameKey)
            }) {
                is NetworkResult.Success -> {
                    applyGameStatus(result.value)
                    updateUiState {
                        copy(
                            isFiringShot = false,
                            statusMessage = "Opponent moved"
                        )
                    }
                }
                is NetworkResult.Error -> {
                    updateUiState {
                        copy(
                            isFiringShot = false,
                            statusMessage = result.message
                        )
                    }
                }
            }
        }
    }

    private fun applyGameStatus(gameStatus: GameStatus) {
        val enemyShot = gameStatus.enemyShot
        if (enemyShot != null) {
            val wasShip = myBoard[enemyShot.y][enemyShot.x].value == CellState.SHIP
            myBoard[enemyShot.y][enemyShot.x].value = if (wasShip) CellState.HIT else CellState.MISS
        }
        if (gameStatus.gameOver) {
            if (uiState.didIWin == null) {
                emitSoundEffect(SoundEffect.LOSE)
                updateUiState {
                    copy(
                        gameOver = true,
                        didIWin = false,
                        isFiringShot = false
                    )
                }
            } else {
                updateUiState { copy(gameOver = true, isFiringShot = false) }
            }
        } else {
            updateUiState { copy(gameOver = false, isMyTurn = true, isFiringShot = false) }
        }
    }

    private fun copyPlacementBoardToDefenseBoard() {
        for (row in 0 until 10) {
            for (col in 0 until 10) {
                myBoard[row][col].value = placementBoard[row][col].value
            }
        }
    }

    fun consumePendingSoundEffect() {
        pendingSoundEffect = null
    }

    private fun emitSoundEffect(effect: SoundEffect) {
        pendingSoundEffect = effect
        soundEventVersion++
    }

    private fun validateJoinInputs(player: String, key: String): String? = when {
        player.length < MIN_JOIN_NAME_LENGTH && key.length < MIN_JOIN_NAME_LENGTH ->
            "Player name and game name must be at least 3 characters"
        player.length < MIN_JOIN_NAME_LENGTH ->
            "Player name must be at least 3 characters"
        key.length < MIN_JOIN_NAME_LENGTH ->
            "Game name must be at least 3 characters"
        else -> null
    }

    private fun normalizeBaseUrl(rawValue: String): String? {
        val trimmed = rawValue.trim().trimEnd('/')
        if (trimmed.isBlank()) return null

        return try {
            val parsed = URL(trimmed)
            if (parsed.protocol !in listOf("http", "https") || parsed.host.isBlank()) {
                null
            } else {
                trimmed
            }
        } catch (_: Exception) {
            null
        }
    }
}
