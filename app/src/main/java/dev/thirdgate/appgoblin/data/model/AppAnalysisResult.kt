package dev.thirdgate.appgoblin.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SdkByStoreId(
    val category_slug: String,
    val company_name: String,
    val company_domain: String,
    val percent_open_source: Double
    )

@Serializable
enum class CompanyCategory(val slug: String, val prettyName: String) {
    @SerialName("ad-networks") AD_NETWORKS("ad-networks", "Ad Networks"),
    @SerialName("development-tools") DEVELOPMENT_TOOLS("development-tools", "Development Tools"),
    @SerialName("ad-attribution") AD_ATTRIBUTION("ad-attribution", "Ad Attribution"),
    @SerialName("product-analytics") PRODUCT_ANALYTICS("product-analytics", "Product Analytics"),
    @SerialName("business-tools") BUSINESS_TOOLS("business-tools", "Business Tools");

    companion object {
        val ALL_CATEGORIES = values().map { it.slug }
        val PRETTY_NAMES = values().associateBy({ it.slug }, { it.prettyName })
    }
}


@Serializable
data class StoreAppInfo(
    val store: String,
    val store_id: String,
    val app_name: String
)

@Serializable
data class SdkByCompanyCategory(
    val company_name: String = "None",
    val company_domain: String = "None",
    val company_logo_url: String = "None",
    val count: Int = 0,
    val percent_open_source: Double = 0.0,
    val apps: List<StoreAppInfo> = emptyList()
)

@Serializable
data class AppAnalysisResult(
    val sdks_by_store_id: Map<String, List<SdkByStoreId>> = emptyMap(),
    val company_categories: List<String> = CompanyCategory.ALL_CATEGORIES,
    val sdks_by_company_category: Map<CompanyCategory, List<SdkByCompanyCategory>> = emptyMap(),
    val failed_store_ids: List<String> = emptyList(),
    val success_store_ids: List<String> = emptyList()
)
