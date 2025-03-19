package dev.thirdgate.appgoblin.data.repository

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import dev.thirdgate.appgoblin.R
import dev.thirdgate.appgoblin.data.model.ApiRequest
import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
import dev.thirdgate.appgoblin.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class AppRepository(private val context: Context) {

    private val packageManager = context.packageManager

    // Simple memory cache for app icons
    private val iconCache = LruCache<String, ImageBitmap>(100) // Cache up to 100 icons

//    // Regular method to get installed apps
//    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
//        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
//
//        // Create a default placeholder bitmap
//        val placeholderBitmap = try {
//            val placeholderDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_placeholder, context.theme)
//            drawableToBitmap(placeholderDrawable, 48, 48)?.asImageBitmap()
//        } catch (e: Exception) {
//            Log.w("AppGoblin", "Failed to load placeholder: ${e.message}")
//            Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
//                eraseColor(Color.LTGRAY)
//            }.asImageBitmap()
//        }
//
//        installedApps
//            .filterNot { it.packageName.startsWith("com.android") || it.packageName.startsWith("android.") }
//            .map { app ->
//                val appName = app.loadLabel(packageManager).toString()
//                val packageName = app.packageName
//
//                // Check cache first, then load if needed
//                val appIcon = getIconFromCache(packageName) ?: try {
//                    val icon = packageManager.getApplicationIcon(packageName)
//                    val bitmap = drawableToBitmap(icon, 48, 48)?.asImageBitmap()
//                    bitmap?.let { iconCache.put(packageName, it) }
//                    bitmap ?: placeholderBitmap
//                } catch (e: Exception) {
//                    Log.w("AppGoblin", "Failed to load icon for $packageName: ${e.message}")
//                    placeholderBitmap
//                }
//
//                AppInfo(name = appName, packageName = packageName, appIcon = appIcon)
//            }
//            .sortedBy { it.name }
//    }

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val placeholderBitmap = try {
            val placeholderDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_placeholder, context.theme)
            drawableToBitmap(placeholderDrawable, 48, 48)?.asImageBitmap()
        } catch (e: Exception) {
            Log.w("AppGoblin", "Failed to load placeholder: ${e.message}")
            Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.LTGRAY)
            }.asImageBitmap()
        }

        installedApps
            .filterNot { it.packageName.startsWith("com.android") || it.packageName.startsWith("android.") }
            .map { app ->
                async {
                    val appName = app.loadLabel(packageManager).toString()
                    val packageName = app.packageName

                    val appIcon = getIconFromCache(packageName) ?: try {
                        val icon = packageManager.getApplicationIcon(packageName)
                        val bitmap = drawableToBitmap(icon, 48, 48)?.asImageBitmap()
                        bitmap?.let { iconCache.put(packageName, it) }
                        bitmap ?: placeholderBitmap
                    } catch (e: Exception) {
                        Log.w("AppGoblin", "Failed to load icon for $packageName: ${e.message}")
                        placeholderBitmap
                    }

                    AppInfo(name = appName, packageName = packageName, appIcon = appIcon)
                }
            }
            .awaitAll() // Run tasks in parallel
            .sortedBy { it.name }
    }


    // Get icon from cache
    private fun getIconFromCache(packageName: String): ImageBitmap? {
        return iconCache.get(packageName)
    }

    // Helper function to convert drawable to bitmap with specific dimensions
    private fun drawableToBitmap(drawable: Drawable?, width: Int = 48, height: Int = 48): Bitmap? {
        if (drawable == null) return null

        // If it's already a BitmapDrawable, scale the bitmap appropriately
        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                if (drawable.bitmap.width == width && drawable.bitmap.height == height) {
                    return drawable.bitmap
                }
                return Bitmap.createScaledBitmap(drawable.bitmap, width, height, true)
            }
        }

        // For all other drawable types
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