package ch.fhnw.vinnai.battleshipclient.viewmodel

import ch.fhnw.vinnai.battleshipclient.view.CellState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleshipViewModelTest {

    @Test
    fun `placeShipAt marks cells and advances to next ship`() {
        val viewModel = BattleshipViewModel()

        viewModel.placeShipAt(0, 0)

        assertEquals(1, viewModel.uiState.currentShipIndex)
        assertEquals(CellState.SHIP, viewModel.placementBoard[0][0].value)
        assertEquals(CellState.SHIP, viewModel.placementBoard[0][4].value)
    }

    @Test
    fun `placeShipAt rejects out of bounds placement`() {
        val viewModel = BattleshipViewModel()

        viewModel.placeShipAt(6, 0)

        assertEquals(0, viewModel.uiState.currentShipIndex)
        assertEquals("Big Chungus doesn't fit there!", viewModel.uiState.placementErrorMessage)
    }

    @Test
    fun `placeShipAt rejects overlap with existing ship`() {
        val viewModel = BattleshipViewModel()
        viewModel.placeShipAt(0, 0)
        viewModel.placeShipAt(0, 0)

        assertEquals(1, viewModel.uiState.currentShipIndex)
        assertEquals("Overlaps with another ship!", viewModel.uiState.placementErrorMessage)
    }

    @Test
    fun `undoLastShip removes ship cells and rewinds placement progress`() {
        val viewModel = BattleshipViewModel()
        viewModel.placeShipAt(0, 0)

        viewModel.undoLastShip()

        assertEquals(0, viewModel.uiState.currentShipIndex)
        assertEquals(CellState.EMPTY, viewModel.placementBoard[0][0].value)
    }

    @Test
    fun `resetPlacement clears board and placement state`() {
        val viewModel = BattleshipViewModel()
        viewModel.placeShipAt(0, 0)
        viewModel.placeShipAt(0, 1)

        viewModel.resetPlacement()

        assertEquals(0, viewModel.uiState.currentShipIndex)
        assertTrue(viewModel.uiState.placedShips.isEmpty())
        assertEquals(CellState.EMPTY, viewModel.placementBoard[0][0].value)
        assertEquals(CellState.EMPTY, viewModel.placementBoard[1][0].value)
    }

    @Test
    fun `toggleOrientation flips horizontal flag`() {
        val viewModel = BattleshipViewModel()

        viewModel.toggleOrientation()

        assertFalse(viewModel.uiState.isHorizontal)
    }

    @Test
    fun `updateServerBaseUrl normalizes valid url and resets ping result`() {
        val viewModel = BattleshipViewModel()
        viewModel.updateServerBaseUrl("not-a-url")

        val updated = viewModel.updateServerBaseUrl(" https://example.com/ ")

        assertTrue(updated)
        assertEquals("https://example.com", viewModel.uiState.serverBaseUrl)
        assertNull(viewModel.uiState.pingResult)
    }

    @Test
    fun `updateServerBaseUrl rejects invalid url`() {
        val viewModel = BattleshipViewModel()

        val updated = viewModel.updateServerBaseUrl("example.com")

        assertFalse(updated)
        assertEquals("Invalid server URL", viewModel.uiState.statusMessage)
        assertEquals(false, viewModel.uiState.pingResult)
    }
}
