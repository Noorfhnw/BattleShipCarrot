package ch.fhnw.vinnai.battleshipclient.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.util.Log
import ch.fhnw.vinnai.battleshipclient.R

/**
 * Note: the SoundManager implementation was created with the help of GitHub Copilot.
 */
class SoundManager(context: Context) {

    companion object {
        private const val TAG = "SoundManager"
        private const val ATTRIBUTION_TAG = "AudioPlayback"
        private const val BACKGROUND_VOLUME = 0.1f
        private const val EFFECT_VOLUME = 1f
    }

    private val attrContext: Context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.createAttributionContext(ATTRIBUTION_TAG)
    } else {
        context
    }

    private val audioAttrs: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private val musicPlayer: MediaPlayer? = try {
        MediaPlayer.create(attrContext, R.raw.garden)?.apply {
            isLooping = true
            setVolume(BACKGROUND_VOLUME, BACKGROUND_VOLUME)
            Log.d(TAG, "Background music loaded successfully")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create MediaPlayer for background music", e)
        null
    }

    private val winPlayer: MediaPlayer? = try {
        MediaPlayer.create(attrContext, R.raw.win_sound)?.apply {
            setVolume(EFFECT_VOLUME, EFFECT_VOLUME)
            Log.d(TAG, "Win sound loaded successfully")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create MediaPlayer for win sound", e)
        null
    }

    private val losePlayer: MediaPlayer? = try {
        MediaPlayer.create(attrContext, R.raw.lose_sound)?.apply {
            setVolume(EFFECT_VOLUME, EFFECT_VOLUME)
            Log.d(TAG, "Lose sound loaded successfully")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to create MediaPlayer for lose sound", e)
        null
    }

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(audioAttrs)
        .build()

    private var digSoundId: Int = 0
    private var carrotEatSoundId: Int = 0
    private var digLoaded = false
    private var carrotEatLoaded = false
    private var keepBackgroundMusicStopped = false

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                when (sampleId) {
                    digSoundId -> {
                        digLoaded = true
                    }
                    carrotEatSoundId -> {
                        carrotEatLoaded = true
                    }
                }
            } else {
                Log.e(TAG, "SoundPool load failed (id=$sampleId, status=$status)")
            }
        }
        digSoundId = soundPool.load(attrContext, R.raw.dig, 1)
        carrotEatSoundId = soundPool.load(attrContext, R.raw.carrot_eat, 1)
    }

    fun playDig() {
        if (digLoaded) {
            soundPool.play(digSoundId, EFFECT_VOLUME, EFFECT_VOLUME, 1, 0, 1f)
        } else {
            Log.w(TAG, "Dig sound not yet loaded, skipping playback")
        }
    }

    fun playCarrotEat() {
        if (carrotEatLoaded) {
            soundPool.play(carrotEatSoundId, EFFECT_VOLUME, EFFECT_VOLUME, 1, 0, 1f)
        } else {
            Log.w(TAG, "Carrot-eat sound not yet loaded, skipping playback")
        }
    }

    fun playWin() {
        keepBackgroundMusicStopped = true
        stopBackgroundMusic()
        try {
            winPlayer?.seekTo(0)
            winPlayer?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play win sound", e)
        }
    }

    fun playLose() {
        keepBackgroundMusicStopped = true
        stopBackgroundMusic()
        try {
            losePlayer?.seekTo(0)
            losePlayer?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play lose sound", e)
        }
    }

    fun resume() {
        try {
            if (!keepBackgroundMusicStopped) {
                musicPlayer?.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resume background music", e)
        }
    }

    fun pause() {
        stopBackgroundMusic()
    }

    private fun stopBackgroundMusic() {
        try {
            if (musicPlayer?.isPlaying == true) {
                musicPlayer.pause()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause background music", e)
        }
    }

    fun release() {
        for (player in listOf(musicPlayer, winPlayer, losePlayer)) {
            try { player?.stop(); player?.release() } catch (_: Exception) { }
        }
        try {
            soundPool.release()
            Log.d(TAG, "SoundPool released")
        } catch (_: Exception) { }
    }
}
