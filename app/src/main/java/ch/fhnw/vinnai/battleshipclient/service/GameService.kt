package ch.fhnw.vinnai.battleshipclient.service

import ch.fhnw.vinnai.battleshipclient.view.PlacedShip
import ch.fhnw.vinnai.battleshipclient.view.ShipType
import org.json.JSONArray
import org.json.JSONObject

data class BoardShot(val x: Int, val y: Int)

data class GameStatus(
    val enemyShot: BoardShot?,
    val gameOver: Boolean
)

data class FireResult(
    val hit: Boolean,
    val sunkEnemyShips: List<String>,
    val gameOver: Boolean
)

class GameService(private val api: BattleshipApi = BattleshipApi()) {
    fun ping(baseUrl: String): Boolean = api.ping(baseUrl)

    fun joinGame(
        baseUrl: String,
        player: String,
        gameKey: String,
        ships: List<PlacedShip>
    ): NetworkResult<GameStatus> {
        return when (val result = api.joinGame(baseUrl, player, gameKey, ships)) {
            is NetworkResult.Success -> NetworkResult.Success(result.value.toGameStatus())
            is NetworkResult.Error -> result
        }
    }

    fun fire(baseUrl: String, player: String, gameKey: String, x: Int, y: Int): NetworkResult<FireResult> {
        return when (val result = api.fire(baseUrl, player, gameKey, x, y)) {
            is NetworkResult.Success -> NetworkResult.Success(result.value.toFireResult())
            is NetworkResult.Error -> result
        }
    }

    fun waitForEnemyFire(baseUrl: String, player: String, gameKey: String): NetworkResult<GameStatus> {
        return when (val result = api.waitForEnemyFire(baseUrl, player, gameKey)) {
            is NetworkResult.Success -> NetworkResult.Success(result.value.toGameStatus())
            is NetworkResult.Error -> result
        }
    }

    private fun JSONObject.toGameStatus(): GameStatus {
        val enemyShot = if (has("x") && !isNull("x") && has("y") && !isNull("y")) {
            BoardShot(x = getInt("x"), y = getInt("y"))
        } else {
            null
        }
        return GameStatus(
            enemyShot = enemyShot,
            gameOver = optBoolean("gameover", false)
        )
    }

    private fun JSONObject.toFireResult(): FireResult {
        val sunkEnemyShips = optJSONArray("shipsSunk")?.toShipNameList().orEmpty()
        return FireResult(
            hit = getBoolean("hit"),
            sunkEnemyShips = sunkEnemyShips,
            gameOver = optBoolean("gameover", sunkEnemyShips.size >= ShipType.entries.size)
        )
    }

    private fun JSONArray.toShipNameList(): List<String> {
        val names = mutableListOf<String>()
        for (i in 0 until length()) {
            val rawName = optString(i).trim()
            if (rawName.isNotEmpty()) {
                names += rawName.toShipDisplayName()
            }
        }
        return names.distinct()
    }

    private fun String.toShipDisplayName(): String {
        val normalized = filter { it.isLetterOrDigit() }.lowercase()
        return ShipType.entries
            .firstOrNull { it.name.filter { ch -> ch.isLetterOrDigit() }.lowercase() == normalized }
            ?.displayName
            ?: this
    }
}
