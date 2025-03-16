package dev.thirdgate.appgoblin.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import dev.thirdgate.appgoblin.R
import dev.thirdgate.appgoblin.data.model.ApiRequest
import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
import dev.thirdgate.appgoblin.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AppRepository(private val context: Context) {

    private val packageManager = context.packageManager

    // Regular method to get installed apps
    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        // Create a default placeholder bitmap for apps where icon loading fails
        val placeholderBitmap = try {
            val placeholderDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_placeholder, context.theme)
            drawableToBitmap(placeholderDrawable)?.asImageBitmap()
        } catch (e: Exception) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.LTGRAY)
            }.asImageBitmap()
        }

        installedApps
            .filterNot { it.packageName.startsWith("com.android") || it.packageName.startsWith("android.") }
            .map { app ->
                val appName = app.loadLabel(packageManager).toString()
                val packageName = app.packageName

                // Load app icon
                val appIcon = try {
                    val icon = packageManager.getApplicationIcon(packageName)
                    drawableToBitmap(icon)?.asImageBitmap()
                } catch (e: Exception) {
                    placeholderBitmap
                }

                AppInfo(name = appName, packageName = packageName, appIcon = appIcon)
            }
            .sortedByDescending { it.packageName }
    }

    // Simplified helper function to convert any drawable to a bitmap
    private fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) return null

        // If it's already a BitmapDrawable, just return its bitmap
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        // For all other drawable types
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
    suspend fun analyzeApps(selectedApps: List<AppInfo>): AppAnalysisResult {
        return withContext(Dispatchers.IO) {
            try {
                val packageNames = selectedApps.map { it.packageName }
                val requestBody = ApiRequest(store_ids = packageNames)

                // Single JSON parser instance
                val jsonParser = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }

                val jsonBody = jsonParser.encodeToString(ApiRequest.serializer(), requestBody)

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://appgoblin.info/api/public/sdks/apps")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string() ?: throw Exception("Empty response")

                    if (!response.isSuccessful) {
                        throw Exception("API error: ${response.code} - $responseBody")
                    }

                    return@use jsonParser.decodeFromString<AppAnalysisResult>(responseBody)
                }
            } catch (e: Exception) {
                Log.e("AppGoblin", "Error analyzing apps: ${e.message}")
                return@withContext AppAnalysisResult()
            }
        }
    }

}