package com.vidi.droidsudoku.ui.sound

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Lightweight placeholder sound effects using [ToneGenerator] beeps, standing in for real
 * audio assets. To swap in real sound later: drop .ogg/.mp3 files into res/raw/, replace the
 * ToneGenerator.startTone calls below with a SoundPool-backed player, keep the same method
 * names so call sites don't need to change.
 */
class SoundFx {
    private var enabled = true
    private val toneGen: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 60)
    } catch (e: RuntimeException) {
        null
    }

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    fun digitEnter() {
        if (enabled) toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
    }

    fun conflict() {
        if (enabled) toneGen?.startTone(ToneGenerator.TONE_CDMA_PIP, 90)
    }

    fun hint() {
        if (enabled) toneGen?.startTone(ToneGenerator.TONE_PROP_ACK, 60)
    }

    fun win() {
        if (enabled) toneGen?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
    }

    fun release() {
        toneGen?.release()
    }
}
