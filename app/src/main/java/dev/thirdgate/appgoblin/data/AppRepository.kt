package dev.thirdgate.appgoblin.data.repository

import android.content.pm.PackageManager
import dev.thirdgate.appgoblin.data.model.AppInfo
import dev.thirdgate.appgoblin.data.model.ApiRequest
import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log

class AppRepository(private val packageManager: PackageManager) {

    // Get installed applications
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        var installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        println("Installed Apps Count: ${installedApps.size}") // Debug Log
        installedApps.map { app ->
            val appName = app.loadLabel(packageManager).toString()
            val packageName = app.packageName
            AppInfo(name = appName, packageName = packageName)
        }
            .sortedByDescending { it.packageName }
    }

    // Send selected apps to API
    suspend fun analyzeApps(selectedApps: List<AppInfo>): AppAnalysisResult {
        return withContext(Dispatchers.IO) {
            try {
                val packageNames = selectedApps.map { it.packageName }
                val requestBody = ApiRequest(store_ids = packageNames)

                val json = Json { prettyPrint = true }
                val jsonBody = json.encodeToString(ApiRequest.serializer(), requestBody)

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://appgoblin.info/api/public/sdks/apps")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: throw Exception("Empty response")

                    if (!response.isSuccessful) {
                        throw Exception("API error: ${response.code}")
                    }

                    val jsonParser = Json { ignoreUnknownKeys = true }
                    jsonParser.decodeFromString<AppAnalysisResult>(responseBody)
                }
            } catch (e: Exception) {
                Log.e("AppGoblin", "Error analyzing apps: ${e.message}")
                throw e
            }
        }
    }
}