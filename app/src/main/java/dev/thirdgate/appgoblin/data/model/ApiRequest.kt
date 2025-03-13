package dev.thirdgate.appgoblin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiRequest(
    val store_ids: List<String>
)