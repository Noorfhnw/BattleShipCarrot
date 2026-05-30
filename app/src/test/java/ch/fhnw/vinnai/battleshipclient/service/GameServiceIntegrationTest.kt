package ch.fhnw.vinnai.battleshipclient.service

import ch.fhnw.vinnai.battleshipclient.view.PlacedShip
import ch.fhnw.vinnai.battleshipclient.view.ShipType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("SpellCheckingInspection")
class GameServiceIntegrationTest {

    @Test
    fun `joinGame posts ships and maps game status response`() {
        MockWebServer().use { server ->
            // Simple fake backend answer for the join request
            server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(
                        """
                        {
                          "x": 4,
                          "y": 7,
                          "gameover": false
                        }
                        """.trimIndent()
                    )
            )

            val ships = listOf(PlacedShip(ShipType.Carrier, x = 0, y = 0, isHorizontal = true))
            val baseUrl = server.url("/").toString().removeSuffix("/")
            val result = GameService().joinGame(baseUrl, "Noor", "1111", ships)

            when (result) {
                is NetworkResult.Success -> {
                    assertEquals(BoardShot(4, 7), result.value.enemyShot)
                    assertFalse(result.value.gameOver)
                }
                is NetworkResult.Error -> error("Expected success, got ${result.message}")
            }
        }
    }

    @Test
    fun `fire posts target and maps sunk ships response`() {
        MockWebServer().use { server ->
            // Same thing here, but for firing at the enemy board
            server.enqueue(
                MockResponse()
                    .setHeader("Content-Type", "application/json")
                    .setBody(
                        """
                        {
                          "hit": true,
                          "shipsSunk": ["Destroyer", "PatrolBoat"],
                          "gameover": true
                        }
                        """.trimIndent()
                    )
            )
            val baseUrl = server.url("/").toString().removeSuffix("/")
            val result = GameService().fire(baseUrl, "Noor", "2222", 6, 2)

            when (result) {
                is NetworkResult.Success -> {
                    assertTrue(result.value.hit)
                    assertEquals(listOf("Bad Bunny", "Lil Hop"), result.value.sunkEnemyShips)
                    assertTrue(result.value.gameOver)
                }
                is NetworkResult.Error -> error("Expected success, got ${result.message}")
            }
        }
    }
}
