package ch.fhnw.vinnai.battleshipclient.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.fhnw.vinnai.battleshipclient.R
import ch.fhnw.vinnai.battleshipclient.viewmodel.BattleshipViewModel

private const val GRID_SIZE = 10
private val ROW_LABELS = ('A'..'J').toList()
private val COLUMN_LABELS = (0..9).toList()
private val SCREEN_BACKGROUND = Color(0xFF6D4C41)
private val ORIENTATION_BUTTON_COLOR = Color(0xFF009900)
private val UNDO_BUTTON_COLOR = Color(0xFFFF9800)
private val RESET_BUTTON_COLOR = Color(0xFFF44336)
private val PRIMARY_TEXT_COLOR = Color.White
private val HIGHLIGHT_TEXT_COLOR = Color.Yellow
private val BOARD_LABEL_SIZE = 36.dp
private val CARROT_ICON_SIZE = 18.dp
private const val BUNNY_CONTENT_DESCRIPTION = "Hungry Bunny"
private const val CARROT_CONTENT_DESCRIPTION = "yummy carrot"

@Composable
fun ShipPlacementScreen(viewModel: BattleshipViewModel) {
    val uiState = viewModel.uiState
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SCREEN_BACKGROUND)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.rabbit),
                contentDescription = BUNNY_CONTENT_DESCRIPTION,
                modifier = Modifier.size(80.dp).padding(end = 8.dp)
            )
            Text(
                "Plant Your Carrots",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = PRIMARY_TEXT_COLOR
            )
            Image(
                painter = painterResource(id = R.drawable.carrot),
                contentDescription = CARROT_CONTENT_DESCRIPTION,
                modifier = Modifier.size(50.dp).padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Current ship info
        val currentShip = viewModel.currentShipType
        if (currentShip != null) {
            Text(
                text = "Place: ${currentShip.displayName} (${currentShip.size} cells)",
                color = HIGHLIGHT_TEXT_COLOR,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "Orientation: ${if (uiState.isHorizontal) "Horizontal ↔" else "Vertical ↕"}",
                color = PRIMARY_TEXT_COLOR
            )
        } else {
            val placementStatusText = if (uiState.isJoining || uiState.hasJoined) {
                "Waiting for opponent..."
            } else {
                "All carrots planted!\nReady to Join the Game"
            }
            Text(
                text = placementStatusText,
                color = if (uiState.isJoining || uiState.hasJoined) {
                    HIGHLIGHT_TEXT_COLOR
                } else {
                    Color.Green
                },
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // Error message
        if (uiState.placementErrorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = uiState.placementErrorMessage,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Control buttons
        val placementLocked = uiState.isJoining || uiState.hasJoined
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { viewModel.toggleOrientation() },
                enabled = currentShip != null,
                colors = ButtonDefaults.buttonColors(containerColor = ORIENTATION_BUTTON_COLOR)
            ) {
                Text(if (uiState.isHorizontal) "↔ Horizontal" else "↕ Vertical")
            }
            Button(
                onClick = { viewModel.undoLastShip() },
                enabled = uiState.placedShips.isNotEmpty() && !placementLocked,
                colors = ButtonDefaults.buttonColors(containerColor = UNDO_BUTTON_COLOR)
            ) {
                Text("Undo")
            }
            Button(
                onClick = { viewModel.resetPlacement() },
                enabled = uiState.placedShips.isNotEmpty() && !placementLocked,
                colors = ButtonDefaults.buttonColors(containerColor = RESET_BUTTON_COLOR)
            ) {
                Text("🔄 Reset")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Placement grid
        PlacementBoardView(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        // Ship list summary
        Text("Ships:", color = PRIMARY_TEXT_COLOR, fontWeight = FontWeight.Bold)
        ShipType.entries.forEachIndexed { index, type ->
            val placed = index < uiState.currentShipIndex
            val current = index == uiState.currentShipIndex
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = type.displayName,
                    color = if (current || placed) HIGHLIGHT_TEXT_COLOR else PRIMARY_TEXT_COLOR,
                    fontWeight = if (current) FontWeight.Bold else FontWeight.Normal
                )
                repeat(type.size) {
                    Image(
                        painter = painterResource(id = R.drawable.carrot),
                        contentDescription = CARROT_CONTENT_DESCRIPTION,
                        modifier = Modifier.size(CARROT_ICON_SIZE)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun PlacementBoardView(viewModel: BattleshipViewModel) {
    Column {
        // Column labels
        Row {
            Spacer(modifier = Modifier.size(BOARD_LABEL_SIZE))
            COLUMN_LABELS.forEach { col ->
                BoardAxisLabel(col.toString())
            }
        }

        // Grid rows
        for (row in 0 until GRID_SIZE) {
            Row {
                // Row label
                BoardAxisLabel(ROW_LABELS[row].toString())

                // Cells
                for (col in 0 until GRID_SIZE) {
                    GridCell(
                        state = viewModel.placementBoard[row][col].value,
                        onClick = { viewModel.placeShipAt(col, row) }  // x = col, y = row
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardAxisLabel(label: String) {
    Box(
        modifier = Modifier.size(BOARD_LABEL_SIZE),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = PRIMARY_TEXT_COLOR)
    }
}
