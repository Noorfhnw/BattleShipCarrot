package ch.fhnw.vinnai.battleshipclient.viewmodel

import ch.fhnw.vinnai.battleshipclient.DEFAULT_BASE_URL
import ch.fhnw.vinnai.battleshipclient.view.PlacedShip

data class BattleshipUiState(
    val pingResult: Boolean? = null,
    val statusMessage: String = "",
    val serverBaseUrl: String = DEFAULT_BASE_URL,
    val isMyTurn: Boolean = false,
    val gameOver: Boolean = false,
    val hasJoined: Boolean = false,
    val isJoining: Boolean = false,
    val didIWin: Boolean? = null,
    val sunkEnemyShips: List<String> = emptyList(),
    val joinedGameId: String = "",
    val placedShips: List<PlacedShip> = emptyList(),
    val currentShipIndex: Int = 0,
    val isHorizontal: Boolean = true,
    val placementErrorMessage: String = ""
)
