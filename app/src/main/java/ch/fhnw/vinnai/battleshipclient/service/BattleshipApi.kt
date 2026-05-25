package ch.fhnw.vinnai.battleshipclient.service

import ch.fhnw.vinnai.battleshipclient.view.PlacedShip
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface NetworkResult<out T> {
    data class Success<T>(val value: T) : NetworkResult<T>
    data class Error(val message: String) : NetworkResult<Nothing>
}

class BattleshipApi {
    fun ping(baseUrl: String): Boolean {
        return try {
            val connection = apiUrl(baseUrl, "/ping").openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 100000
            connection.readTimeout = 100000
            connection.responseCode == 200
        } catch (_: Exception) {
            false
        }
    }

    fun joinGame(
        baseUrl: String,
        player: String,
        gameKey: String,
        ships: List<PlacedShip>
    ): NetworkResult<JSONObject> {
        val body = JSONObject().apply {
            put("player", player)
            put("gamekey", gameKey)
            put("ships", ships.toJson())
        }
        return postJson(baseUrl, "/game/join", body)
    }

    fun fire(baseUrl: String, player: String, gameKey: String, x: Int, y: Int): NetworkResult<JSONObject> {
        val body = JSONObject().apply {
            put("player", player)
            put("gamekey", gameKey)
            put("x", x)
            put("y", y)
        }
        return postJson(baseUrl, "/game/fire", body)
    }

    fun waitForEnemyFire(baseUrl: String, player: String, gameKey: String): NetworkResult<JSONObject> {
        val body = JSONObject().apply {
            put("player", player)
            put("gamekey", gameKey)
        }
        return postJson(baseUrl, "/game/enemyFire", body)
    }

    private fun postJson(baseUrl: String, path: String, body: JSONObject): NetworkResult<JSONObject> {
        return try {
            val connection = apiUrl(baseUrl, path).openConnection() as HttpURLConnection
            connection.setupJsonPost()
            connection.writeJson(body)

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                if (json.has("Error")) {
                    NetworkResult.Error("Error: ${json.getString("Error")}")
                } else {
                    NetworkResult.Success(json)
                }
            } else {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                NetworkResult.Error("Error: ${error ?: "Unexpected response ${connection.responseCode}"}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Error: ${e.message}")
        }
    }

    private fun apiUrl(baseUrl: String, path: String) = URL("$baseUrl$path")

    private fun HttpURLConnection.setupJsonPost() {
        requestMethod = "POST"
        doOutput = true
        connectTimeout = 100000
        readTimeout = 600000
        setRequestProperty("Content-Type", "application/json")
    }

    private fun HttpURLConnection.writeJson(body: JSONObject) {
        outputStream.bufferedWriter().use { it.write(body.toString()) }
    }

    private fun List<PlacedShip>.toJson() = org.json.JSONArray().apply {
        for (ship in this@toJson) {
            put(JSONObject().apply {
                put("ship", ship.type.name)
                put("x", ship.x)
                put("y", ship.y)
                put("orientation", if (ship.isHorizontal) "horizontal" else "vertical")
            })
        }
    }
}
