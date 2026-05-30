package ch.fhnw.vinnai.battleshipclient.view

import org.junit.Assert.assertEquals
import org.junit.Test

class PlacedShipTest {

    @Test
    fun `occupiedCells returns horizontal coordinates in order`() {
        val ship = PlacedShip(
            type = ShipType.Destroyer,
            x = 2,
            y = 4,
            isHorizontal = true
        )

        assertEquals(listOf(2 to 4, 3 to 4, 4 to 4), ship.occupiedCells())
    }

    @Test
    fun `occupiedCells returns vertical coordinates in order`() {
        val ship = PlacedShip(
            type = ShipType.PatrolBoat,
            x = 7,
            y = 1,
            isHorizontal = false
        )

        assertEquals(listOf(7 to 1, 7 to 2), ship.occupiedCells())
    }
}
