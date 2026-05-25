package ch.fhnw.vinnai.battleshipclient.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.fhnw.vinnai.battleshipclient.viewmodel.BattleshipViewModel
import ch.fhnw.vinnai.battleshipclient.R

private const val GRID_SIZE = 10
private val ROW_LABELS = ('A'..'J').toList()
private val COLUMN_LABELS = (0..9).toList()
private typealias BoardState = Array<Array<androidx.compose.runtime.MutableState<CellState>>>

@Composable
fun GameScreen(
    viewModel: BattleshipViewModel,
    onTryFindCarrot: () -> Unit = {}
) {
    val uiState = viewModel.uiState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6D4C41))
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.rabbit),
                contentDescription = "Hungry Bunny",
                modifier = Modifier.size(60.dp).padding(end = 8.dp)
            )
            Text("Battleship Carrot", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Image(
                painter = painterResource(id = R.drawable.carrot),
                contentDescription = "yummy carrots",
                modifier = Modifier.size(60.dp).padding(start = 8.dp)
            )
        }

        if (uiState.joinedGameId.isNotBlank()) {
            Text(
                text = "Game ID: ${uiState.joinedGameId}",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Game Over banner
        if (uiState.gameOver) {
            val won = uiState.didIWin == true
            val bannerColor = if (won) Color(0xFF4CAF50) else Color(0xFFF44336)
            val bannerText = if (won) "You Won!" else "You Lost!"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(bannerColor, RoundedCornerShape(12.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = bannerText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Text(
            text = when {
                uiState.gameOver -> "Game Over"
                uiState.isMyTurn -> "Your Turn! (Attack Opponent's Board)"
                else -> "Waiting for Opponent..."
            },
            color = if (uiState.gameOver) Color.White else Color.Yellow,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.sunk_enemy_carrots),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        if (uiState.sunkEnemyShips.isEmpty()) {
            Text(text = stringResource(R.string.sunk_enemy_carrots_none), color = Color(0xFFFFF59D))
        } else {
            Text(text = uiState.sunkEnemyShips.joinToString("\n") { "- $it" }, color = Color(0xFFFFF59D))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Opponent's Board (Targeting)", color = Color.White)
        BoardView(grid = viewModel.opponentBoard) { row, col ->
            val isUntriedCell = viewModel.opponentBoard[row][col].value == CellState.EMPTY
            if (uiState.isMyTurn && !uiState.gameOver && isUntriedCell) {
                onTryFindCarrot()
            }
            viewModel.fire(col, row)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("My Board (Defense)", color = Color.White)
        BoardView(grid = viewModel.myBoard) { _, _ -> }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun BoardView(
    grid: BoardState,
    onCellClick: (Int, Int) -> Unit
) {
    Column {
        // Column labels (0–9)
        Row {
            Spacer(modifier = Modifier.size(36.dp)) // top-left empty
            COLUMN_LABELS.forEach { col ->
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(col.toString(), fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        //  Grid with row labels
        for (row in 0 until GRID_SIZE) {
            Row {
                // Row label (A–J)
                Box(
                    modifier = Modifier.size(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ROW_LABELS[row].toString(), fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Cells
                for (col in 0 until GRID_SIZE) {
                    GridCell(
                        state = grid[row][col].value,
                        onClick = { onCellClick(row, col) }
                    )
                }
            }
        }
    }
}
