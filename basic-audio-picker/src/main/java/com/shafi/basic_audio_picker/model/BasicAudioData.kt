package com.shafi.basic_audio_picker.model


import java.io.Serializable

data class BasicAudioData(
    val name: String,
    val path: String,
    val uri: String
): Serializable

