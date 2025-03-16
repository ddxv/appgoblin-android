package dev.thirdgate.appgoblin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.thirdgate.appgoblin.data.model.AppAnalysisResult
import dev.thirdgate.appgoblin.data.model.CompanyCategory
import dev.thirdgate.appgoblin.data.model.SdkByCompanyCategory
import dev.thirdgate.appgoblin.data.model.StoreAppInfo

@Composable
fun ByCompanyCategoryScreen(results: AppAnalysisResult, navController: NavHostController) {
    // Extract categories
    val categories = results.sdks_by_company_category

    Scaffold(
        topBar = {
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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxHeight()
        ) {
            categories.forEach { (categoryName, companies) ->
                item {
                    CategoryCard(categoryName, companies)
                }
            }
        }
    }
}

@Composable
fun CategoryCard(categoryName: CompanyCategory, companies: List<SdkByCompanyCategory>) {
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
            CompanyCard(company)
        }
    }
}

@Composable
fun CompanyCard(company: SdkByCompanyCategory) {
    var expanded by remember { mutableStateOf(false) }

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
            Text(
                text = "Domain: ${company.company_domain}",
                style = MaterialTheme.typography.bodySmall
            )

            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(if (expanded) "Hide Apps" else "Show Apps")
            }

            if (expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp) // Constrain the height if needed
                ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(company.apps) { app ->
                        AppItem(app)
                    }
                }
                }
            }
        }
    }
}

@Composable
fun AppItem(app: StoreAppInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = app.app_name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Store: ${app.store}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Store ID: ${app.store_id}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

