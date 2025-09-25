package dev.thirdgate.appgoblin.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
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

    private val iconCache = LruCache<String, ImageBitmap>(100) // Cache up to 100 icons

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        // Get all launcher activities directly
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val launcherApps = packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { resolveInfo ->
                try {
                    packageManager.getApplicationInfo(resolveInfo.activityInfo.packageName, 0)
                } catch (e: Exception) {
                    null
                }
            }
            .distinctBy { it.packageName }

        val placeholderBitmap = try {
            val placeholderDrawable = ResourcesCompat.getDrawable(
                context.resources,
                R.drawable.ic_placeholder,
                context.theme
            )
            drawableToBitmap(placeholderDrawable, 48, 48)?.asImageBitmap()
        } catch (e: Exception) {
            Log.w("AppGoblin", "Failed to load placeholder: ${e.message}")
            Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.LTGRAY)
            }.asImageBitmap()
        }

        launcherApps
            .map { app ->
                async {
                    val appName = app.loadLabel(packageManager).toString()
                    val packageName = app.packageName

                    // Determine if this is a system app
                    val isSystemApp = determineIfSystemApp(app)

                    val appIcon = getIconFromCache(packageName) ?: try {
                        val icon = packageManager.getApplicationIcon(packageName)
                        val bitmap = drawableToBitmap(icon, 48, 48)?.asImageBitmap()
                        bitmap?.let { iconCache.put(packageName, it) }
                        bitmap ?: placeholderBitmap
                    } catch (e: Exception) {
                        Log.w("AppGoblin", "Failed to load icon for $packageName: ${e.message}")
                        placeholderBitmap
                    }

                    AppInfo(
                        name = appName,
                        packageName = packageName,
                        isSystemApp = isSystemApp,
                        appIcon = appIcon
                    )
                }
            }
            .awaitAll()
            .sortedBy { it.name }
    }

    private fun determineIfSystemApp(app: ApplicationInfo): Boolean {
        // Check if it's a system app (not user-installed)
        val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        // Check if it's an updated system app (these are often considered user apps)
        val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

        // Check if it has a system package name
        val hasSystemPackageName = isSystemPackageName(app.packageName)

        // An app is considered a system app if:
        // - It has the system flag AND it's not an updated system app
        // - OR it has a known system package name
        return (isSystemApp && !isUpdatedSystemApp) || hasSystemPackageName
    }

    private fun isSystemPackageName(packageName: String): Boolean {
        val systemPrefixes = setOf(
            "com.android",
            "android.",
            "com.google.android",
            "com.samsung",
            "com.sec.android",
            "com.lge",
            "com.htc",
            "com.sonymobile",
            "com.miui",
            "com.xiaomi",
            "com.huawei",
            "com.oneplus",
            "com.oppo",
            "com.vivo",
            "com.qualcomm",
            "com.mediatek"
        )

        return systemPrefixes.any { packageName.startsWith(it) }
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
//                    .url("https://appgoblin.info/api/public/sdks/apps")
                    .url("http://localhost:8000/api/public/sdks/apps")
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

    suspend fun requestSDKScan(packageNames: List<String>): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = ApiRequest(store_ids = packageNames)

                // Use the existing JSON parser from the analyzeApps function
                val jsonParser = Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }

                val jsonBody = jsonParser.encodeToString(ApiRequest.serializer(), requestBody)

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://appgoblin.info/api/public/sdks/apps/requestSDKScan")
                    .post(jsonBody.toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    return@use response.isSuccessful
                }
            } catch (e: Exception) {
                Log.e("AppGoblin", "Error requesting SDK scan: ${e.message}")
                return@withContext false
            }
        }
    }
}