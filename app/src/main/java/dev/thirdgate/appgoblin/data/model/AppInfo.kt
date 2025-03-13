package dev.thirdgate.appgoblin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppInfo(
    val name: String,
    val packageName: String,
    var isSelected: Boolean = false
)