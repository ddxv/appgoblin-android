package dev.thirdgate.appgoblin.data.model

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
    val name: String,
    val packageName: String,
    var isSelected: Boolean = false,
    val appIcon: ImageBitmap? = null
)