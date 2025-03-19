    package dev.thirdgate.appgoblin.ui.components

    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.runtime.saveable.rememberSaveable
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
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
        val filteredApps = apps.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
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
//                            selectedApps = if (isChecked) filteredApps.toMutableSet() else mutableSetOf()
                            selectedApps = if (isChecked) apps.toMutableList() else emptyList()
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

    @Composable
    fun AppListItem(app: AppInfo, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
        Row(
            Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
            Column(Modifier.padding(start = 8.dp)) {
                Text(app.name, style = MaterialTheme.typography.bodyLarge)
                Text(app.packageName, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
