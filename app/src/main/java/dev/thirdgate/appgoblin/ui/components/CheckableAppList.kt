    package dev.thirdgate.appgoblin.ui.components

    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.runtime.saveable.rememberSaveable
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.ImageBitmap
    import androidx.compose.ui.res.imageResource
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import dev.thirdgate.appgoblin.R
    import dev.thirdgate.appgoblin.data.model.AppInfo
    @Composable
    fun CheckableAppList(
        apps: List<AppInfo>,
        apiError: String?,
        onSendSelected: (List<AppInfo>) -> Unit,
        modifier: Modifier = Modifier,
        initialSelectedApps: Set<AppInfo> = emptySet() // NEW PARAMETER
    ) {
        var selectedApps by rememberSaveable { mutableStateOf(initialSelectedApps.toList()) } // Convert Set to List
        var searchQuery by remember { mutableStateOf("") }
        var appFilter by remember { mutableStateOf(AppFilter.USER_ONLY) }

        val filteredApps = apps.filter { app ->
            // Search filter
            val matchesSearch = app.name.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            // System app filter
            val matchesFilter = when (appFilter) {
                AppFilter.ALL -> true
                AppFilter.USER_ONLY -> !app.isSystemApp
                AppFilter.SYSTEM_ONLY -> app.isSystemApp
            }

            matchesSearch && matchesFilter
        }

        val allSelected = selectedApps.size == filteredApps.size && filteredApps.isNotEmpty()

        Column(modifier = modifier.fillMaxSize()) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name or package") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // App Filter Section
            AppFilterSection(
                currentFilter = appFilter,
                onFilterChange = { appFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Analyze Button
            Button(
                onClick = { onSendSelected(selectedApps.toList()) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                enabled = selectedApps.isNotEmpty()
            ) {
                Text("Analyze Selected Apps")
            }

            // API Error Handling
            apiError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Selected: ${selectedApps.size}/${filteredApps.size}", style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { isChecked ->
                            selectedApps = if (isChecked) filteredApps.toList() else emptyList()
                        }
                    )
                    Text("Select All", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // App List
            LazyColumn(Modifier.fillMaxSize()) {
                items(filteredApps) { app ->
                    AppListItem(
                        app = app,
                        isChecked = selectedApps.contains(app),
                        onCheckedChange = { isChecked ->
                            selectedApps = selectedApps.toMutableList().apply {
                                if (isChecked) add(app) else remove(app)
                            }
                        }
                    )
                }
            }
        }
    }

    enum class AppFilter(val displayName: String) {
        ALL("All Apps"),
        USER_ONLY("User Apps"),
        SYSTEM_ONLY("System Apps")
    }


    @Composable
    fun AppFilterSection(
        currentFilter: AppFilter,
        onFilterChange: (AppFilter) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Filter Apps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppFilter.values().forEach { filter ->
                        Button(
                            onClick = { onFilterChange(filter) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentFilter == filter) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                },
                                contentColor = if (currentFilter == filter) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = filter.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun AppListItem(app: AppInfo, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onCheckedChange(!isChecked) }, // Make the entire row clickable
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )

            Image(
                bitmap = app.appIcon ?: ImageBitmap.imageResource(R.drawable.ic_placeholder),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Column(Modifier.padding(start = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(app.name, style = MaterialTheme.typography.bodyLarge)
                    if (app.isSystemApp) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SYSTEM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
            }
        }
    }