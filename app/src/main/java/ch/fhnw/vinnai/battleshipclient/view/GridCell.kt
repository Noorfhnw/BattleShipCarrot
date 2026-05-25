package ch.fhnw.vinnai.battleshipclient.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ch.fhnw.vinnai.battleshipclient.R

@Composable
fun GridCell(state: CellState, onClick: () -> Unit) {
    val backgroundColor = if (state == CellState.HIT || state == CellState.MISS) Color(0xFF795548) else Color(0xFF8BC34A)
    val imageRes = when (state) {
        CellState.HIT -> R.drawable.carrot_eaten
        CellState.SHIP -> R.drawable.carrot
        else -> null
    }

    val emoji: String? = when (state) {
        CellState.EMPTY -> "🌱"
        CellState.MISS -> null
        CellState.HIT -> null
        CellState.SHIP -> null
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .padding(2.dp)
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = if (state == CellState.HIT) "Hit carrot" else "Carrot ship",
                modifier = Modifier.size(28.dp)
            )
        } else if (emoji != null) {
            Text(emoji)
        }
    }
}