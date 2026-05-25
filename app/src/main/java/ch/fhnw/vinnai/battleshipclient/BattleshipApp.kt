package ch.fhnw.vinnai.battleshipclient

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.fhnw.vinnai.battleshipclient.view.GameScreen
import ch.fhnw.vinnai.battleshipclient.view.ShipPlacementScreen
import ch.fhnw.vinnai.battleshipclient.viewmodel.BattleshipViewModel
import ch.fhnw.vinnai.battleshipclient.R

private val BAR_PADDING = 8.dp
private val FIELD_PADDING = 4.dp
private val START_BUTTON_BASE = Color(0xFFCC6C54)
private val START_BUTTON_SHADOW = Color(0xFF8A4736)
private val START_BUTTON_HIGHLIGHT = Color(0xFFFFD8CC)
private val START_BUTTON_WIDTH = 180.dp
private val START_BUTTON_HEIGHT = 88.dp
private val START_BUTTON_TEXT_SIZE = 20.sp

@Composable
fun BattleshipApp(
    viewModel: BattleshipViewModel,
    onTryFindCarrot: () -> Unit = {},
    onEnemyShipHit: () -> Unit = {},
    onGameWon: () -> Unit = {},
    onGameLost: () -> Unit = {}
) {
    var showWelcome by rememberSaveable { mutableStateOf(true) }
    val uiState = viewModel.uiState

    LaunchedEffect(viewModel.soundEventVersion) {
        when (viewModel.pendingSoundEffect) {
            BattleshipViewModel.SoundEffect.CARROT_EAT -> onEnemyShipHit()
            BattleshipViewModel.SoundEffect.WIN -> onGameWon()
            BattleshipViewModel.SoundEffect.LOSE -> onGameLost()
            null -> Unit
        }
        viewModel.consumePendingSoundEffect()
    }

    if (showWelcome) {
        WelcomeScreen(onStartClick = { showWelcome = false })
        return
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val showConnectionControls = viewModel.allShipsPlaced && !uiState.hasJoined
            if (showConnectionControls) {
                ServerBar(viewModel)
                JoinBar(viewModel)
            }

            if (uiState.hasJoined) {
                GameScreen(
                    viewModel = viewModel,
                    onTryFindCarrot = onTryFindCarrot
                )
            } else {
                ShipPlacementScreen(viewModel)
            }
        }
    }
}

@Composable
private fun WelcomeScreen(onStartClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.wooden_sign_in_sunlit_forest),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // title is manually positioned on the wood
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = 420.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                GameTitle()
            }
        }

        //  start button below wood on the start screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 95.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .width(START_BUTTON_WIDTH)
                    .height(START_BUTTON_HEIGHT),
                shape = RoundedCornerShape(50),
                border = BorderStroke(2.dp, START_BUTTON_HIGHLIGHT),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 2.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = START_BUTTON_BASE,
                    disabledContainerColor = START_BUTTON_SHADOW
                )
            ) {
                Text("Start", fontSize = START_BUTTON_TEXT_SIZE, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GameTitle() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BubbleText("Battleship")
        Spacer(modifier = Modifier.height(12.dp))
        BubbleText("Carrot")
    }
}

@Composable
fun BubbleText(text: String) {
    val fontSize = 74.sp

    Box(contentAlignment = Alignment.Center) {

        val outlineColor = Color.White

        listOf(
            Offset(-3f, -3f), Offset(3f, -3f),
            Offset(-3f, 3f),  Offset(3f, 3f),
            Offset(-4f, 0f),  Offset(4f, 0f),
            Offset(0f, -4f),  Offset(0f, 4f)
        ).forEach {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = outlineColor,
                modifier = Modifier.offset(it.x.dp, it.y.dp)
            )
        }

        // shadow
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFEF6C00),
            modifier = Modifier.offset(4.dp, 4.dp)
        )

        //  MAIN FILL (bubble gradient)
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            style = TextStyle(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFFFFF176), // top highlight
                        Color(0xFFFFA726),
                        Color(0xFF5D2E00)  // bottom depth
                    )
                )
            )
        )
    }
}

@Composable
private fun ServerBar(viewModel: BattleshipViewModel) {
    val uiState = viewModel.uiState
    var serverAddressInput by rememberSaveable { mutableStateOf(uiState.serverBaseUrl) }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(BAR_PADDING)
    ) {
        TextField(
            value = serverAddressInput,
            onValueChange = { serverAddressInput = it },
            label = { Text("Server") },
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                if (viewModel.updateServerBaseUrl(serverAddressInput)) {
                    serverAddressInput = viewModel.uiState.serverBaseUrl
                    viewModel.ping()
                }
            },
            modifier = Modifier.padding(start = FIELD_PADDING)
        ) {
            Text("Ping")
        }
        PingStatusIndicator(uiState.pingResult)
    }
}

@Composable
private fun JoinBar(viewModel: BattleshipViewModel) {
    val uiState = viewModel.uiState
    var userName by rememberSaveable { mutableStateOf("") }
    var gameKey by rememberSaveable { mutableStateOf("") }
    val canJoin = uiState.pingResult == true && viewModel.allShipsPlaced && !uiState.isJoining
    val statusMessage = uiState.statusMessage

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(BAR_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            JoinInputField(
                value = userName,
                onValueChange = { userName = it },
                label = "User"
            )
            JoinInputField(
                value = gameKey,
                onValueChange = { gameKey = it },
                label = "Game Key"
            )
        }
        Button(
            enabled = canJoin,
            onClick = { viewModel.joinGame(userName, gameKey) },
            modifier = Modifier.padding(top = BAR_PADDING)
        ) {
            Text("Join Game")
        }
        if (statusMessage.isNotEmpty()) {
            Text(statusMessage, modifier = Modifier.padding(top = FIELD_PADDING))
        }
    }
}

@Composable
private fun PingStatusIndicator(pingResult: Boolean?) {
    Text(
        text = when (pingResult) {
            true -> "✅"
            false -> "❌"
            null -> "?"
        }
    )
}

@Composable
private fun RowScope.JoinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .weight(1f)
            .padding(FIELD_PADDING)
    )
}
