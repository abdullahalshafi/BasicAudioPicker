package com.shafi.basic_audio_picker.util

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.shafi.basic_audio_picker.activity.AudioUtilActivity
import com.shafi.basic_audio_picker.model.AudioUtilConfig

/**
 * Created by Shafi on 17/07/2023.
 */
class AudioUtilHelper(
    private var context: Context,
    private var intentLauncher: ActivityResultLauncher<Intent>
) {

    private var config: AudioUtilConfig = AudioUtilConfig()

    companion object {
        const val PACKAGE_NAME = "package_name"

        fun create(
            context: Context,
            intentLauncher: ActivityResultLauncher<Intent>,
            audioUtil: AudioUtilHelper.() -> Unit
        ): AudioUtilHelper {
            return AudioUtilHelper(context, intentLauncher).apply(audioUtil)
        }
    }

    fun recordAudio() {
        config.isRecord = true
        config.isAudioPick = false
    }

    fun pickAudio() {
        config.isRecord = false
        config.isAudioPick = true
    }

    fun start() {

        if (!config.isRecord && !config.isAudioPick) {
            throw java.lang.Exception("You must specify either record or audio pick!")
        }

        val intent = Intent(context, AudioUtilActivity::class.java).apply {
            putExtra(AudioUtilConfig::class.java.simpleName, config)
            putExtra(PACKAGE_NAME, context.packageName)
        }
        intentLauncher.launch(intent)
    }
}