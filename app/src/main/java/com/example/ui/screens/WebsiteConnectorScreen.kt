package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.model.AuditLogEntry
import com.example.model.MenuModule
import com.example.model.WebsiteConnector
import com.example.model.WebsiteProbeResult
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmptyModuleState
import com.example.ui.components.KpiMetricTile
import com.example.ui.components.StatusPill
import com.example.ui.components.SubPageTabsStrip
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.MvciAmberAccent
import com.example.ui.theme.MvciCyanAccent
import com.example.ui.theme.MvciEmerald
import com.example.ui.theme.MvciIndigoPrimary
import com.example.ui.theme.MvciRoseError

private val INTEGRATION_METHODS = listOf(
    "REST API",
    "Secure API Token",
    "OAuth 2.0",
    "Webhooks",
    "Database Migration/Import",
    "CSV Import",
    "Excel Import",
    "JSON Import",
    "XML Import",
    "Authorized Custom Connector"
)

private val AUTH_METHODS = listOf("Secure API Token", "Bearer OAuth 2.0", "API Key + Secret", "Webhook HMAC", "Manual File Import")
private val SYNC_SCOPES = listOf("Students", "Admissions", "Courses", "Faculty", "Attendance", "Results", "Certificates", "Fees", "Documents", "Notifications")

@Composable
fun WebsiteConnectorScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    connectors: List<WebsiteConnector>,
    auditLogs: List<AuditLogEntry>,
    probeResult: WebsiteProbeResult?,
    isProbing: Boolean,
    maskSecret: (String) -> String,
    onProbeUrl: (url: String, bearer: String, headerName: String, headerVal: String) -> Unit,
    onSaveConnector: (WebsiteConnector, String?) -> Unit,
    onTestAndSyncConnector: (WebsiteConnector, Long) -> Unit,
    onSetConnectorStatus: (String, String, String) -> Unit,
    onDeleteConnector: (String) -> Unit,
    onExportConnectorCsv: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(
            subPages = MenuModule.WEBSITE_CONNECTOR.subMenus,
            selectedSubPage = subPage,
            onSelect = onSubPageChange
        )

        when (subPage) {
            "Connect New Website" -> ConnectWebsiteWizard(
                probeResult = probeResult,
                isProbing = isProbing,
                maskSecret = maskSecret,
                onProbeUrl = onProbeUrl,
                onSaveConnector = { conn ->
                    onSaveConnector(conn, null)
                    onSubPageChange("Connected Websites")
                }
            )
            "Integration Health" -> IntegrationHealthDashboard(
                connectors = connectors,
                onTestAndSyncConnector = onTestAndSyncConnector,
                onNavigateConnect = { onSubPageChange("Connect New Website") }
            )
            "Connection Logs" -> ConnectionLogsView(auditLogs = auditLogs)
            "Import Website Data", "Export Website Data" -> ImportExportConnectorView(
                connectors = connectors,
                onTestAndSyncConnector = onTestAndSyncConnector,
                onExportCsv = onExportConnectorCsv
            )
            else -> ConnectedWebsitesList(
                subPage = subPage,
                connectors = connectors,
                onTestAndSync = onTestAndSyncConnector,
                onSetStatus = onSetConnectorStatus,
                onDelete = onDeleteConnector,
                onAddNew = { onSubPageChange("Connect New Website") }
            )
        }
    }
}

@Composable
private fun ConnectWebsiteWizard(
    probeResult: WebsiteProbeResult?,
    isProbing: Boolean,
    maskSecret: (String) -> String,
    onProbeUrl: (String, String, String, String) -> Unit,
    onSaveConnector: (WebsiteConnector) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://") }
    var siteType by remember { mutableStateOf("Franchise Portal") }
    var description by remember { mutableStateOf("") }
    var integrationMethod by remember { mutableStateOf("REST API") }
    var apiBaseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var apiSecret by remember { mutableStateOf("") }
    var authMethod by remember { mutableStateOf("Secure API Token") }
    var webhookUrl by remember { mutableStateOf("") }
    var fieldMappings by remember { mutableStateOf("student_name->name,roll_number->rollNo,course_code->courseName,fee_paid->paidFee") }
    var syncDirection by remember { mutableStateOf("TWO_WAY") }
    var syncFrequency by remember { mutableStateOf("AUTOMATIC") }
    var conflictPolicy by remember { mutableStateOf("MERGE_APPROVED") }
    var selectedScopes by remember { mutableStateOf(setOf("Students", "Courses", "Fees")) }
    var ownershipVerified by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Universal Website Connector Wizard (Step $step of 4)", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Connect authorized websites, LMS portals, or franchise sites with live HTTPS/API verification. Credentials are encrypted/masked and never shown in plain text.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(1 to "1. Domain", 2 to "2. Auth & API", 3 to "3. Mapping", 4 to "4. Confirm").forEach { (s, lbl) ->
                            FilterChip(selected = step == s, onClick = { step = s }, label = { Text(lbl) })
                        }
                    }
                }
            }
        }

        if (step == 1) {
            item {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Website Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Website URL (https://...)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = siteType, onValueChange = { siteType = it }, label = { Text("Website Type") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Website Description") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onProbeUrl(url, "", "", "") },
                        enabled = !isProbing && url.length > 8
                    ) {
                        Icon(Icons.Default.Http, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isProbing) "Testing Reachability..." else "Verify Domain & SSL")
                    }
                    FilterChip(
                        selected = ownershipVerified,
                        onClick = { ownershipVerified = !ownershipVerified },
                        label = { Text("Authorized Ownership Confirmed") }
                    )
                }
                if (probeResult != null) {
                    Spacer(Modifier.height(10.dp))
                    ProbeResultBanner(probeResult)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { step = 2 },
                    enabled = name.isNotBlank() && url.isNotBlank() && ownershipVerified,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next: Configure API & Authentication")
                }
            }
        }

        if (step == 2) {
            item {
                Text("Select Integration Method", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    INTEGRATION_METHODS.forEach { m ->
                        FilterChip(selected = integrationMethod == m, onClick = { integrationMethod = m }, label = { Text(m) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("Authentication Method", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AUTH_METHODS.forEach { a ->
                        FilterChip(selected = authMethod == a, onClick = { authMethod = a }, label = { Text(a) })
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = apiBaseUrl, onValueChange = { apiBaseUrl = it }, label = { Text("API Base URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key / Token (masked on save)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiSecret,
                    onValueChange = { apiSecret = it },
                    label = { Text("API Secret (never stored in plain text)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = webhookUrl, onValueChange = { webhookUrl = it }, label = { Text("Webhook Callback URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val testTarget = apiBaseUrl.ifBlank { url }
                            onProbeUrl(testTarget, apiKey, "X-API-Key", apiKey)
                        },
                        enabled = !isProbing
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isProbing) "Testing Auth..." else "Test Authentication")
                    }
                    Button(
                        onClick = { step = 3 },
                        enabled = authMethod == "Manual File Import" || apiKey.isNotBlank() || apiBaseUrl.isNotBlank()
                    ) {
                        Text("Next: Field Mapping")
                    }
                }
                if (probeResult != null) {
                    Spacer(Modifier.height(10.dp))
                    ProbeResultBanner(probeResult)
                }
            }
        }

        if (step == 3) {
            item {
                Text("Data Sync Modules", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SYNC_SCOPES.forEach { sc ->
                        val active = sc in selectedScopes
                        FilterChip(
                            selected = active,
                            onClick = { selectedScopes = if (active) selectedScopes - sc else selectedScopes + sc },
                            label = { Text(sc) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = fieldMappings,
                    onValueChange = { fieldMappings = it },
                    label = { Text("Field Mapping (Website -> MVCI DB)") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text("Sync Direction & Conflict Policy", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ONE_WAY", "TWO_WAY").forEach { dir ->
                        FilterChip(selected = syncDirection == dir, onClick = { syncDirection = dir }, label = { Text(dir) })
                    }
                    listOf("MANUAL", "SCHEDULED", "AUTOMATIC").forEach { freq ->
                        FilterChip(selected = syncFrequency == freq, onClick = { syncFrequency = freq }, label = { Text(freq) })
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("SKIP_DUPLICATES", "MERGE_APPROVED", "SOURCE_WINS").forEach { pol ->
                        FilterChip(selected = conflictPolicy == pol, onClick = { conflictPolicy = pol }, label = { Text(pol) })
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(onClick = { step = 4 }, modifier = Modifier.fillMaxWidth()) {
                    Text("Next: Preview & Confirm Connection")
                }
            }
        }

        if (step == 4) {
            item {
                val requiresCreds = authMethod != "Manual File Import" && apiKey.isBlank()
                val verifiedReachable = probeResult?.reachable == true
                val finalStatus = when {
                    requiresCreds -> "PENDING"
                    verifiedReachable -> "CONNECTED"
                    else -> "PENDING"
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Integration Preview", style = MaterialTheme.typography.titleLarge)
                        Text("Website: $name ($url)")
                        Text("Method: $integrationMethod • Auth: $authMethod")
                        Text("Masked Key: ${maskSecret(apiKey).ifBlank { "Credentials Pending" }}", style = MaterialTheme.typography.labelSmall)
                        Text("Sync: $syncDirection • $syncFrequency • $conflictPolicy")
                        Text("Modules: ${selectedScopes.joinToString(", ")}")
                        Text(
                            if (verifiedReachable) "Reachability verified (HTTP ${probeResult?.httpCode} in ${probeResult?.latencyMs}ms)."
                            else "Domain probe not yet verified; connection will be saved as PENDING until credentials & probe succeed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (verifiedReachable) MvciEmerald else MvciAmberAccent
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        onSaveConnector(
                            WebsiteConnector(
                                name = name,
                                url = url,
                                siteType = siteType,
                                description = description.ifBlank { "Modules: ${selectedScopes.joinToString(",")}" },
                                integrationMethod = integrationMethod,
                                apiBaseUrl = apiBaseUrl.ifBlank { url },
                                maskedApiKey = maskSecret(apiKey),
                                authMethod = authMethod,
                                webhookUrl = webhookUrl,
                                syncDirection = syncDirection,
                                syncFrequency = syncFrequency,
                                conflictPolicy = conflictPolicy,
                                status = finalStatus,
                                healthStatus = if (verifiedReachable) "HTTP ${probeResult?.httpCode}" else "NEEDS_VERIFY",
                                lastLatencyMs = probeResult?.latencyMs ?: 0L,
                                fieldMappings = fieldMappings,
                                lastSyncNote = probeResult?.message ?: "Awaiting live endpoint verification"
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_website_connection")
                ) {
                    Icon(Icons.Default.AddLink, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm & Create Website Connection")
                }
            }
        }
    }
}

@Composable
private fun ProbeResultBanner(result: WebsiteProbeResult) {
    val accent = if (result.reachable) MvciEmerald else MvciRoseError
    Surface(
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (result.reachable) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = accent
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(result.message, style = MaterialTheme.typography.labelLarge, color = accent)
                Text(
                    "TLS: ${result.tlsProtocol} • SSL Valid: ${result.sslValid} • Server: ${result.serverHeader.ifBlank { "N/A" }}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun ConnectedWebsitesList(
    subPage: String,
    connectors: List<WebsiteConnector>,
    onTestAndSync: (WebsiteConnector, Long) -> Unit,
    onSetStatus: (String, String, String) -> Unit,
    onDelete: (String) -> Unit,
    onAddNew: () -> Unit
) {
    var deleteCandidate by remember { mutableStateOf<WebsiteConnector?>(null) }

    if (deleteCandidate != null) {
        ConfirmActionDialog(
            title = "Delete Website Connection?",
            message = "Remove ${deleteCandidate!!.name} (${deleteCandidate!!.url}) from MVCI Universal Website Connector?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                onDelete(deleteCandidate!!.id)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(subPage, style = MaterialTheme.typography.titleLarge)
                    Text("${connectors.size} configured connectors • Secrets masked", style = MaterialTheme.typography.bodySmall)
                }
                Button(onClick = onAddNew) {
                    Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Connect Site")
                }
            }
        }

        if (connectors.isEmpty()) {
            item {
                EmptyModuleState(
                    icon = Icons.Default.Language,
                    title = "No Connected Websites",
                    subtitle = "Use Connect New Website to verify and link external portals, REST APIs, or webhooks.",
                    actionLabel = "Connect New Website",
                    onAction = onAddNew
                )
            }
        }

        items(connectors, key = { it.id }) { conn ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(conn.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(conn.url, style = MaterialTheme.typography.labelSmall, color = MvciCyanAccent)
                        }
                        StatusPill(conn.status)
                    }
                    Text(
                        "${conn.integrationMethod} • ${conn.authMethod} • Key: ${conn.maskedApiKey.ifBlank { "None" }}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "Health: ${conn.healthStatus} (${conn.lastLatencyMs}ms) • Sync: ${conn.syncDirection}/${conn.syncFrequency}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "Imported: ${conn.recordsImported} • Updated: ${conn.recordsUpdated} • Failed: ${conn.failedRecords}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MvciEmerald
                    )
                    if (conn.lastSyncNote.isNotBlank()) {
                        Text(conn.lastSyncNote, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(onClick = { onTestAndSync(conn, 0L) }) {
                            Icon(Icons.Default.Http, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Test")
                        }
                        Button(onClick = { onTestAndSync(conn, 5L) }) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sync Now")
                        }
                        if (conn.status == "PAUSED") {
                            TextButton(onClick = { onSetStatus(conn.id, "CONNECTED", "Sync resumed by Super Admin") }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Resume")
                            }
                        } else {
                            TextButton(onClick = { onSetStatus(conn.id, "PAUSED", "Sync paused by Super Admin") }) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                Text("Pause")
                            }
                        }
                        TextButton(onClick = { onSetStatus(conn.id, "DISCONNECTED", "Access revoked by Super Admin") }) {
                            Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Disconnect")
                        }
                        IconButton(onClick = { deleteCandidate = conn }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Connector", tint = MvciRoseError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IntegrationHealthDashboard(
    connectors: List<WebsiteConnector>,
    onTestAndSyncConnector: (WebsiteConnector, Long) -> Unit,
    onNavigateConnect: () -> Unit
) {
    val active = connectors.count { it.status == "CONNECTED" }
    val failed = connectors.count { it.status == "ERROR" }
    val totalImported = connectors.sumOf { it.recordsImported }
    val totalErrors = connectors.sumOf { it.failedRecords }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiMetricTile("Connected Sites", "${connectors.size}", Icons.Default.Language, MvciIndigoPrimary, onClick = onNavigateConnect, modifier = Modifier.weight(1f))
                KpiMetricTile("Active Links", "$active", Icons.Default.CheckCircle, MvciEmerald, onClick = {}, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiMetricTile("Failed / Errors", "$failed ($totalErrors err)", Icons.Default.Error, MvciRoseError, onClick = {}, modifier = Modifier.weight(1f))
                KpiMetricTile("Imported Records", "$totalImported", Icons.Default.Sync, MvciCyanAccent, onClick = {}, modifier = Modifier.weight(1f))
            }
        }
        items(connectors) { c ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(c.name, style = MaterialTheme.typography.titleMedium)
                        Text("${c.healthStatus} • ${c.lastLatencyMs}ms", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { onTestAndSyncConnector(c, 0L) }) {
                        Text("Probe")
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionLogsView(auditLogs: List<AuditLogEntry>) {
    val connectorLogs = auditLogs.filter { it.moduleName.contains("Connector", ignoreCase = true) || it.action.contains("SYNC") || it.action.contains("CONNECT") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (connectorLogs.isEmpty()) {
            item {
                EmptyModuleState(
                    icon = Icons.Default.Sync,
                    title = "No Connector Logs Yet",
                    subtitle = "Test or sync a connected website to generate immutable integration audit logs."
                )
            }
        }
        items(connectorLogs) { log ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(log.action, style = MaterialTheme.typography.titleMedium)
                        Text(formatTimestamp(log.createdAt?.toDate()?.time), style = MaterialTheme.typography.labelSmall)
                    }
                    Text("${log.targetId} • ${log.newVal}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ImportExportConnectorView(
    connectors: List<WebsiteConnector>,
    onTestAndSyncConnector: (WebsiteConnector, Long) -> Unit,
    onExportCsv: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Data Import & Export Synchronization", style = MaterialTheme.typography.titleLarge)
                    Text("Run transaction-safe sync across connected portals or export MVCI records to CSV/Excel-compatible format.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = onExportCsv, modifier = Modifier.fillMaxWidth()) {
                        Text("Export MVCI Data to CSV / Excel")
                    }
                }
            }
        }
        items(connectors) { conn ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(conn.name, style = MaterialTheme.typography.titleMedium)
                        Text("Policy: ${conn.conflictPolicy} • ${conn.fieldMappings}", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { onTestAndSyncConnector(conn, 10L) }) {
                        Text("Import Batch")
                    }
                }
            }
        }
    }
}
