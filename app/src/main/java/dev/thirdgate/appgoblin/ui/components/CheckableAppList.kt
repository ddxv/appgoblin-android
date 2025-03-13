    package dev.thirdgate.appgoblin.ui.components

    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
    import dev.thirdgate.appgoblin.data.model.AppInfo

    @Composable
    fun CheckableAppList(
        apps: List<AppInfo>,
        apiError: String?,
        onSendSelected: (List<AppInfo>) -> Unit,
        modifier: Modifier = Modifier
    ) {
        var selectedApps by remember { mutableStateOf(mutableListOf<AppInfo>()) }
        Column(modifier = modifier.fillMaxSize()) {
            Button(
                onClick = {
                    onSendSelected(selectedApps)
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = selectedApps.isNotEmpty()
            ) {
                Text("Analyze Selected Apps")
            }

            if (apiError != null) {
                Text(apiError, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }

            LazyColumn(Modifier.fillMaxSize()) {
                items(apps) { app ->
                    AppListItem(
                        app = app,
                        isChecked = selectedApps.contains(app),
                        onCheckedChange = { isChecked ->
                            selectedApps = if (isChecked) {
                                selectedApps.toMutableList().apply { add(app) }
                            } else {
                                selectedApps.toMutableList().apply { remove(app) }
                            }                }
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
