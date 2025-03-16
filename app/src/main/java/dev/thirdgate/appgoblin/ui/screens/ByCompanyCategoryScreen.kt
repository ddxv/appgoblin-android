package dev.thirdgate.appgoblin.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.thirdgate.appgoblin.R
import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
import dev.thirdgate.appgoblin.data.model.AppInfo
import dev.thirdgate.appgoblin.data.model.CompanyCategory
import dev.thirdgate.appgoblin.data.model.SdkByCompanyCategory
import dev.thirdgate.appgoblin.data.model.StoreAppInfo

@Composable
fun ByCompanyCategoryScreen(results: AppAnalysisResult, navController: NavHostController, installedApps: List<AppInfo>) {
    // Extract categories
    val categories = results.sdks_by_company_category
    val numIdsSuccessful = results.success_store_ids.count()
    val numIdsFailed = results.failed_store_ids.count()

    Scaffold(
        topBar = {
            Column() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Analysis Results",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Back")
                    }

                }
                Text("Successfully Analyzed Apps: $numIdsSuccessful", style = MaterialTheme.typography.bodyMedium)
                Text("Apps Failed to Analyze: $numIdsFailed", style = MaterialTheme.typography.bodyMedium)

            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxHeight()
        ) {
            categories.forEach { (categoryName, companies) ->
                item {
                    CategoryCard(categoryName, companies, numIdsSuccessful, installedApps)
                }
            }
        }
    }
}

@Composable
fun CategoryCard(categoryName: CompanyCategory, companies: List<SdkByCompanyCategory>, numIdsSuccessful: Int, installedApps: List<AppInfo>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = categoryName.prettyName,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        companies.forEach { company ->
            CompanyCard(company, numIdsSuccessful, installedApps)
        }
    }
}

@Composable
fun CompanyCard(company: SdkByCompanyCategory, numIdsSuccessful: Int, installedApps: List<AppInfo>) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val currentContext = rememberUpdatedState(context)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = company.company_name,
                style = MaterialTheme.typography.titleMedium
            )
            if (company.percent_open_source > 0.7) {
                Text(
                    text = "Open Source",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Domain: ${company.company_domain}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://appgoblin.info/companies/${company.company_domain}?ref=appgoblin_android"))
                    currentContext.value.startActivity(intent)
                }
            )






            Text(
                text = "Apps: ${company.count}/${numIdsSuccessful}",
                style = MaterialTheme.typography.bodySmall
            )


            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(if (expanded) "Hide Apps" else "Matched Apps")
            }
            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp) // Constrain the height if needed
                ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(company.apps) { app ->

                        val appIcon = installedApps.find { it.packageName == app.store_id }?.appIcon ?: ImageBitmap.imageResource(R.drawable.ic_placeholder)

                        AppItem(app, appIcon)
                    }
                }
                }
            }
        }
    }
}

@Composable
fun AppItem(app: StoreAppInfo, appIcon: ImageBitmap) {
    val context = LocalContext.current
    val currentContext = rememberUpdatedState(context)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                bitmap = appIcon,
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f) // Text takes available space
            ) {
                Text(
                    text = app.app_name,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Store ID: ${app.store_id}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://appgoblin.info/apps/${app.store_id}?ref=appgoblin_android")
                        )
                        currentContext.value.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View details in browser",
                        tint = MaterialTheme.colorScheme.primary
                    )

                }
                Text(
                    text = "Explore App SDKs & Details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}



