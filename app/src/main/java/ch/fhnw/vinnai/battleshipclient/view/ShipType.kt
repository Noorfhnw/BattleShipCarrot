package ch.fhnw.vinnai.battleshipclient.view

enum class ShipType(val size: Int, val displayName: String) {
    Carrier(5, "Big Chungus"),
    Battleship(4, "Fat Carrot"),
    Destroyer(3, "Bad Bunny"),
    Submarine(3, "Digger"),
    PatrolBoat(2, "Lil Hop");
}

data class PlacedShip(
    val type: ShipType,
    val x: Int,
    val y: Int,
    val isHorizontal: Boolean
) {
    fun occupiedCells(): List<Pair<Int, Int>> {
        return (0 until type.size).map { i ->
            if (isHorizontal) Pair(x + i, y) else Pair(x, y + i)
        }
    }
}
