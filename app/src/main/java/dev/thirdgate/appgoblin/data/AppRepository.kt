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

    suspend fun getInstalledUserApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        // Alternative faster approach: Get all launcher activities directly
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
            .filter { app ->
                // Still apply system package filtering
                val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                val isUpdatedSystemApp =
                    (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                val isSystemPackage = isSystemPackageName(app.packageName)

                (isUserApp || isUpdatedSystemApp) && !isSystemPackage
            }

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
            .awaitAll()
            .sortedBy { it.name }
    }

    private fun isUserInstalledApp(app: ApplicationInfo): Boolean {
        // Method 1: Check if app was installed by user (not system)
        val isUserApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0

        // Method 2: Check if it's an updated system app that should be considered user-installed
        val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

        // Method 3: Additional package name filters for common system apps
        val isSystemPackage = isSystemPackageName(app.packageName)

        // Method 4: Check if the app has a launcher activity (user-facing apps)
        val hasLauncherActivity = hasLauncherActivity(app.packageName)

        // An app is considered user-installed if:
        // - It's not a system app OR it's an updated system app
        // - AND it's not a known system package
        // - AND it has a launcher activity (optional, but helps filter out background services)
        return (isUserApp || isUpdatedSystemApp) && !isSystemPackage && hasLauncherActivity
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

    private fun hasLauncherActivity(packageName: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                `package` = packageName
            }
            val activities =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            activities.isNotEmpty()
        } catch (e: Exception) {
            Log.w("AppGoblin", "Failed to check launcher activity for $packageName: ${e.message}")
            false
        }
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