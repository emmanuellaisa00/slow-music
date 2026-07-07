package com.slowmusic.app.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import com.slowmusic.app.domain.model.EqualizerSettings
import com.slowmusic.app.util.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class EqualizerManager @Inject constructor() {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var audioSessionId: Int = 0

    val presets: List<String>
        get() = equalizer?.let { eq ->
            List(eq.numberOfPresets.toInt()) { index -> eq.getPresetName(index.toShort()) }
        } ?: DEFAULT_PRESETS

    fun attachToAudioSession(sessionId: Int) {
        if (sessionId == 0 || sessionId == audioSessionId) return
        release()
        audioSessionId = sessionId
        runCatching {
            equalizer = Equalizer(0, sessionId).apply { enabled = false }
            bassBoost = BassBoost(0, sessionId).apply { enabled = false }
            virtualizer = Virtualizer(0, sessionId).apply { enabled = false }
            loudnessEnhancer = LoudnessEnhancer(sessionId).apply { enabled = false }
        }.onFailure { Logger.e("Equalizer", "Failed to attach equalizer", it) }
    }

    fun apply(settings: EqualizerSettings) {
        val eq = equalizer ?: return
        runCatching {
            eq.enabled = settings.enabled
            bassBoost?.enabled = settings.enabled
            virtualizer?.enabled = settings.enabled
            loudnessEnhancer?.enabled = settings.enabled

            if (!settings.enabled) return

            if (settings.preset in 0 until eq.numberOfPresets) {
                eq.usePreset(settings.preset.toShort())
            }

            val range = eq.bandLevelRange
            val min = range[0].toInt()
            val max = range[1].toInt()
            settings.bandLevels.forEach { (band, levelPercent) ->
                if (band in 0 until eq.numberOfBands) {
                    val clamped = levelPercent.coerceIn(0, 100)
                    val millibels = min + ((max - min) * (clamped / 100f)).roundToInt()
                    eq.setBandLevel(band.toShort(), millibels.toShort())
                }
            }
        }.onFailure { Logger.e("Equalizer", "Failed to apply settings", it) }
    }

    fun release() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { loudnessEnhancer?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        audioSessionId = 0
    }

    companion object {
        val DEFAULT_PRESETS = listOf("Normal", "Classical", "Dance", "Flat", "Folk", "Heavy Metal", "Hip Hop", "Jazz", "Pop", "Rock")
    }
}
