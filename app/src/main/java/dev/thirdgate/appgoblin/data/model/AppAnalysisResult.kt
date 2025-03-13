package dev.thirdgate.appgoblin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AppAnalysisResult(
    val store_id: String,
    val category_slug: String,
    val company_domain: String,
    val company_name: String
)
