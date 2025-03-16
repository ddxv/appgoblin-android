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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.AccountBox
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.openattribution.sdk.OpenAttribution
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

    val context = LocalContext.current

    OpenAttribution.trackEvent(context, "by_company_category_screen")


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
            Text("Successfully Analyzed Apps: $numIdsSuccessful/${numIdsSuccessful + numIdsFailed}", style = MaterialTheme.typography.bodyMedium)
            Text("Apps Failed to Analyze: $numIdsFailed/${numIdsSuccessful + numIdsFailed}", style = MaterialTheme.typography.bodyMedium)

        LazyColumn(
            modifier = Modifier
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
fun CategoryCard(
    categoryName: CompanyCategory,
    companies: List<SdkByCompanyCategory>,
    numIdsSuccessful: Int,
    installedApps: List<AppInfo>
) {
    var isExpanded by remember { mutableStateOf(false) } // Track expansion state

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded } // Toggle expansion
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${categoryName.prettyName} (${companies.size})", // Append count
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand/Collapse"
                )
            }

            // Show the companies list only if expanded
            if (isExpanded) {
                companies.forEach { company ->
                    CompanyCard(company, numIdsSuccessful, installedApps)
                }
            }
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
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header section with company name and open source chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = company.company_name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (company.percent_open_source > 0.7) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ThumbUp,
                                contentDescription = "Open Source",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Open Source",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info section with domain and apps count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBox,
                            contentDescription = "Domain",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = company.company_domain,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://appgoblin.info/companies/${company.company_domain}?ref=appgoblin_android")
                                )
                                currentContext.value.startActivity(intent)
                            },
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Apps",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${company.count}/${numIdsSuccessful}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Apps section with toggle button
            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) "Hide Apps" else "Show Apps"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (expanded) "Hide Apps" else "Matched Apps")
            }


                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .padding(top = 8.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(company.apps) { app ->
                            val appIcon = installedApps.find { it.packageName == app.store_id }?.appIcon
                                ?: ImageBitmap.imageResource(R.drawable.ic_placeholder)

                            AppItem(app, appIcon)
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



