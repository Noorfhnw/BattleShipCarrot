package ch.fhnw.vinnai.battleshipclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import ch.fhnw.vinnai.battleshipclient.audio.SoundManager
import ch.fhnw.vinnai.battleshipclient.ui.theme.BattleshipCarrotTheme
import ch.fhnw.vinnai.battleshipclient.viewmodel.BattleshipViewModel

const val DEFAULT_BASE_URL = "http://brad-home.ch:50003"

class MainActivity : ComponentActivity() {
    private val viewModel: BattleshipViewModel by viewModels()
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)
        enableEdgeToEdge()
        setContent {
            BattleshipCarrotTheme {
                BattleshipApp(
                    viewModel = viewModel,
                    onTryFindCarrot = { soundManager.playDig() },
                    onEnemyShipHit = { soundManager.playCarrotEat() },
                    onGameWon = { soundManager.playWin() },
                    onGameLost = { soundManager.playLose() }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        soundManager.resume()
    }

    override fun onStop() {
        super.onStop()
        soundManager.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}
