package com.harvey.gamespc.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.harvey.gamespc.R

object SoundManager {

    private const val TAG = "SoundManager"
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private var isLoaded = false

    fun loadSounds(context: Context) {
        if (isLoaded) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                Log.d(TAG, "Sound loaded with ID: $sampleId")
            } else {
                Log.e(TAG, "Error loading sound with ID: $sampleId, status: $status")
            }
        }

        try {
            val soundId = soundPool?.load(context, R.raw.click_items, 1)
            if (soundId != null) {
                soundMap["click_items"] = soundId
            }
            isLoaded = true
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sound file. Make sure 'click_items.mp3' is in res/raw.", e)
        }
    }

    fun playSound(soundName: String) {
        if (!isLoaded) {
            Log.w(TAG, "Sounds not loaded yet.")
            return
        }
        val soundId = soundMap[soundName]
        if (soundId != null) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        } else {
            Log.w(TAG, "Sound not found: $soundName")
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        soundMap.clear()
        isLoaded = false
    }
}
