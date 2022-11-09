package org.snd.settings

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val fontSize: Float = 13f,
    val emoteSize: Float = 120f,
    val timestampFormat: String = "HH:mm:ss",
    val historySize: Int = 1000,
    val currentChannel: String? = null,
    val accountName: String? = null
)