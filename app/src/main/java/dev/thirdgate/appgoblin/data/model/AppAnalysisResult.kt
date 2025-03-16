package dev.thirdgate.appgoblin.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SdkByStoreId(
    val category_slug: String,
    val company_name: String,
    val company_domain: String
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
    val company_name: String,
    val company_domain: String,
    val count: Int,
    val apps: List<StoreAppInfo>
)

@Serializable
data class AppAnalysisResult(
    val sdks_by_store_id: Map<String, List<SdkByStoreId>>,
    val company_categories: List<String> = CompanyCategory.ALL_CATEGORIES,
    val sdks_by_company_category: Map<CompanyCategory, List<SdkByCompanyCategory>>,
    val failed_store_ids: List<String>,
    val success_store_ids: List<String>
)
