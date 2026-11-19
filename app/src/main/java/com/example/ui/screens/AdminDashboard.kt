package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Http
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.ErpGlobalSearch
import com.example.model.AuditLogEntry
import com.example.model.ContentItem
import com.example.model.CourseRecord
import com.example.model.FranchiseRecord
import com.example.model.GlobalSearchMatch
import com.example.model.MenuModule
import com.example.model.PaymentRecord
import com.example.model.StaffRecord
import com.example.model.StudentRecord
import com.example.model.WebsiteConnector
import com.example.model.WebsiteProbeResult
import com.example.model.WorkspaceConfig
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmptyModuleState
import com.example.ui.components.ErpGlobalSearchBar
import com.example.ui.components.KpiMetricTile
import com.example.ui.components.QuickAccessActionCard
import com.example.ui.components.StatusPill
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.MvciAmberAccent
import com.example.ui.theme.MvciCyanAccent
import com.example.ui.theme.MvciEmerald
import com.example.ui.theme.MvciIndigoPrimary
import com.example.ui.theme.MvciRoseError
import kotlinx.coroutines.launch

enum class AdminDashboardSection(
    val title: String,
    val subtitle: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    ERP_OVERVIEW(
        title = "Admin Overview",
        subtitle = "Centralized summary stats & quick actions",
        selectedIcon = Icons.Default.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "drawer_item_overview"
    ),
    LMS_MANAGEMENT(
        title = "LMS Management",
        subtitle = "Courses, study materials & syllabus progress",
        selectedIcon = Icons.AutoMirrored.Filled.LibraryBooks,
        unselectedIcon = Icons.AutoMirrored.Outlined.LibraryBooks,
        testTag = "drawer_item_lms_management"
    ),
    STUDENT_RECORDS(
        title = "Student Records",
        subtitle = "Admissions, profiles, fees & attendance",
        selectedIcon = Icons.Default.School,
        unselectedIcon = Icons.Outlined.School,
        testTag = "drawer_item_student_records"
    ),
    SERVER_STATUS_TOOLS(
        title = "Server Status Tools",
        subtitle = "Live HTTP/TLS probes, runtime & backups",
        selectedIcon = Icons.Default.Dns,
        unselectedIcon = Icons.Outlined.Dns,
        testTag = "drawer_item_server_status"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    config: WorkspaceConfig? = null,
    courses: List<CourseRecord> = emptyList(),
    contents: List<ContentItem> = emptyList(),
    students: List<StudentRecord> = emptyList(),
    connectors: List<WebsiteConnector> = emptyList(),
    payments: List<PaymentRecord> = emptyList(),
    staff: List<StaffRecord> = emptyList(),
    franchises: List<FranchiseRecord> = emptyList(),
    auditLogs: List<AuditLogEntry> = emptyList(),
    probeResult: WebsiteProbeResult? = null,
    isProbing: Boolean = false,
    initialSection: AdminDashboardSection = AdminDashboardSection.LMS_MANAGEMENT,
    onSaveContent: (ContentItem) -> Unit = {},
    onDeleteContent: (String) -> Unit = {},
    onSaveCourse: (CourseRecord, String?) -> Unit = { _, _ -> },
    onSaveStudent: (StudentRecord, String?) -> Unit = { _, _ -> },
    onUpdateStudentStatus: (String, String) -> Unit = { _, _ -> },
    onDeleteStudent: (String) -> Unit = {},
    onProbeUrl: (String) -> Unit = {},
    onTestAndSyncConnector: (WebsiteConnector, Long) -> Unit = { _, _ -> },
    onToggleMaintenanceMode: (Boolean) -> Unit = {},
    onCreateBackupSnapshot: (ContentItem) -> Unit = {},
    onOpenFullControlCenter: (() -> Unit)? = null,
    onNavigateToErpModule: ((MenuModule, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedSection by rememberSaveable { mutableStateOf(initialSection) }
    var globalSearchQuery by rememberSaveable { mutableStateOf("") }
    var inspectedMatch by remember { mutableStateOf<GlobalSearchMatch?>(null) }
    var showAddLmsDialog by remember { mutableStateOf(false) }
    var showAddStudentDialog by remember { mutableStateOf(false) }

    val lmsMaterials = remember(contents) { contents.filter { it.moduleCategory.startsWith("LMS_") } }
    val errorConnectors = remember(connectors) { connectors.count { it.status == "ERROR" } }
    val globalSearchMatches = remember(
        globalSearchQuery,
        config,
        students,
        staff,
        auditLogs,
        franchises,
        courses,
        connectors
    ) {
        ErpGlobalSearch.search(
            query = globalSearchQuery,
            config = config,
            students = students,
            staff = staff,
            auditLogs = auditLogs,
            franchises = franchises,
            courses = courses,
            connectors = connectors,
            limit = 16
        )
    }

    BackHandler(enabled = drawerState.isOpen || selectedSection != AdminDashboardSection.LMS_MANAGEMENT) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else {
            selectedSection = AdminDashboardSection.LMS_MANAGEMENT
        }
    }

    if (inspectedMatch != null) {
        val match = inspectedMatch!!
        AlertDialog(
            onDismissRequest = { inspectedMatch = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusPill(match.category)
                    Text(match.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(match.subtitle, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "ERP Location: ${match.module.titleEn} › ${match.subPage}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MvciCyanAccent
                    )
                }
            },
            confirmButton = {
                if (onOpenFullControlCenter != null) {
                    Button(
                        onClick = {
                            inspectedMatch = null
                            onOpenFullControlCenter()
                        }
                    ) { Text("Open ERP Suite") }
                } else {
                    TextButton(onClick = { inspectedMatch = null }) { Text("Done") }
                }
            },
            dismissButton = {
                if (onOpenFullControlCenter != null) {
                    TextButton(onClick = { inspectedMatch = null }) { Text("Close") }
                }
            }
        )
    }

    if (showAddLmsDialog) {
        AddLmsMaterialDialog(
            onDismiss = { showAddLmsDialog = false },
            onSave = { item ->
                onSaveContent(item)
                showAddLmsDialog = false
            }
        )
    }

    if (showAddStudentDialog) {
        AddStudentRecordDialog(
            courses = courses,
            nextIndex = students.size + 1,
            onDismiss = { showAddStudentDialog = false },
            onSave = { record ->
                onSaveStudent(record, null)
                showAddStudentDialog = false
            }
        )
    }

    ModalNavigationDrawer(
        modifier = modifier.fillMaxSize(),
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                AdminDrawerHeader(config = config)
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AdminDashboardSection.entries.forEach { section ->
                        val selected = selectedSection == section
                        val badgeText = when (section) {
                            AdminDashboardSection.ERP_OVERVIEW -> "${courses.size + students.size}"
                            AdminDashboardSection.LMS_MANAGEMENT -> "${courses.size + lmsMaterials.size}"
                            AdminDashboardSection.STUDENT_RECORDS -> "${students.size}"
                            AdminDashboardSection.SERVER_STATUS_TOOLS -> if (errorConnectors == 0) "OK" else "$errorConnectors!"
                        }
                        NavigationDrawerItem(
                            selected = selected,
                            onClick = {
                                selectedSection = section
                                scope.launch { drawerState.close() }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) section.selectedIcon else section.unselectedIcon,
                                    contentDescription = section.title
                                )
                            },
                            label = {
                                Column {
                                    Text(
                                        text = section.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = section.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            badge = {
                                Badge(
                                    containerColor = if (section == AdminDashboardSection.SERVER_STATUS_TOOLS && errorConnectors > 0) {
                                        MvciRoseError
                                    } else {
                                        MaterialTheme.colorScheme.secondaryContainer
                                    }
                                ) {
                                    Text(badgeText)
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag(section.testTag)
                        )
                    }
                }
                if (onOpenFullControlCenter != null) {
                    HorizontalDivider()
                    NavigationDrawerItem(
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onOpenFullControlCenter()
                        },
                        icon = { Icon(Icons.Default.Apps, contentDescription = "Full Control Center") },
                        label = { Text("All 21 MVCI Modules") },
                        modifier = Modifier
                            .padding(12.dp)
                            .testTag("admin_open_full_shell")
                    )
                }
            }
        }
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = { scope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("admin_drawer_toggle")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open navigation drawer")
                        }
                    },
                    title = {
                        Column {
                            Text(
                                text = selectedSection.title,
                                style = MaterialTheme.typography.titleLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("admin_topbar_title")
                            )
                            Text(
                                text = "Admin Dashboard • ${config?.instituteName ?: "MVCI"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    actions = {
                        StatusPill(
                            text = if (errorConnectors == 0) "HEALTHY" else "$errorConnectors ALERT"
                        )
                        if (onOpenFullControlCenter != null) {
                            IconButton(
                                onClick = onOpenFullControlCenter,
                                modifier = Modifier.testTag("admin_top_full_shell")
                            ) {
                                Icon(Icons.Default.Apps, contentDescription = "Full 21-Module Suite")
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                when (selectedSection) {
                    AdminDashboardSection.ERP_OVERVIEW -> FloatingActionButton(
                        onClick = { showAddStudentDialog = true },
                        modifier = Modifier.testTag("admin_fab_add_student")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Student Record")
                    }
                    AdminDashboardSection.LMS_MANAGEMENT -> FloatingActionButton(
                        onClick = { showAddLmsDialog = true },
                        modifier = Modifier.testTag("admin_fab_add_lms")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add LMS Material")
                    }
                    AdminDashboardSection.STUDENT_RECORDS -> FloatingActionButton(
                        onClick = { showAddStudentDialog = true },
                        modifier = Modifier.testTag("admin_fab_add_student")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Student Record")
                    }
                    AdminDashboardSection.SERVER_STATUS_TOOLS -> FloatingActionButton(
                        onClick = { onProbeUrl(config?.websiteUrl ?: "https://www.mvci.online") },
                        modifier = Modifier.testTag("admin_fab_probe_server")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Run Live Server Probe")
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ErpGlobalSearchBar(
                    query = globalSearchQuery,
                    onQueryChange = { globalSearchQuery = it },
                    matches = globalSearchMatches,
                    onSelectMatch = { match ->
                        globalSearchQuery = ""
                        when (match.category) {
                            "STUDENT" -> selectedSection = AdminDashboardSection.STUDENT_RECORDS
                            "COURSE" -> selectedSection = AdminDashboardSection.LMS_MANAGEMENT
                            "CONNECTOR" -> selectedSection = AdminDashboardSection.SERVER_STATUS_TOOLS
                            else -> {
                                if (onNavigateToErpModule != null) {
                                    onNavigateToErpModule(match.module, match.subPage)
                                } else {
                                    inspectedMatch = match
                                }
                            }
                        }
                    },
                    placeholder = "Search users, audit logs, ERP settings...",
                    searchInputTag = "admin_global_search_input",
                    resultsTag = "admin_global_search_results",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Crossfade(
                        targetState = selectedSection,
                        animationSpec = tween(220),
                        label = "AdminDashboardSectionTransition"
                    ) { section ->
                    when (section) {
                        AdminDashboardSection.ERP_OVERVIEW -> ErpOverviewSection(
                            config = config,
                            courses = courses,
                            lmsMaterials = lmsMaterials,
                            students = students,
                            connectors = connectors,
                            payments = payments,
                            staff = staff,
                            franchises = franchises,
                            auditLogs = auditLogs,
                            onSelectSection = { selectedSection = it },
                            onAddLmsMaterial = { showAddLmsDialog = true },
                            onAddStudent = { showAddStudentDialog = true },
                            onRunProbe = { onProbeUrl(config?.websiteUrl ?: "https://www.mvci.online") },
                            onCreateSnapshot = {
                                onCreateBackupSnapshot(
                                    ContentItem(
                                        title = "MVCI Central Snapshot ${contents.count { it.moduleCategory == "BACKUP" } + 1}",
                                        moduleCategory = "BACKUP",
                                        description = "Centralized Admin Dashboard snapshot",
                                        urlOrMeta = "ERP + LMS Checkpoint",
                                        downloadable = true,
                                        status = "COMPLETED"
                                    )
                                )
                            },
                            onOpenFullControlCenter = onOpenFullControlCenter
                        )
                        AdminDashboardSection.LMS_MANAGEMENT -> LmsManagementSection(
                            courses = courses,
                            materials = lmsMaterials,
                            onAdvanceSyllabus = { course ->
                                val nextPct = (course.syllabusProgressPct + 10L).coerceAtMost(100L)
                                onSaveCourse(course.copy(syllabusProgressPct = nextPct), course.id)
                            },
                            onAddMaterial = { showAddLmsDialog = true },
                            onDeleteMaterial = onDeleteContent
                        )
                        AdminDashboardSection.STUDENT_RECORDS -> StudentRecordsSection(
                            students = students,
                            onAddStudent = { showAddStudentDialog = true },
                            onUpdateStatus = onUpdateStudentStatus,
                            onDeleteStudent = onDeleteStudent
                        )
                        AdminDashboardSection.SERVER_STATUS_TOOLS -> ServerStatusToolsSection(
                            config = config,
                            connectors = connectors,
                            backups = contents.filter { it.moduleCategory == "BACKUP" },
                            probeResult = probeResult,
                            isProbing = isProbing,
                            onProbeUrl = onProbeUrl,
                            onTestAndSyncConnector = onTestAndSyncConnector,
                            onToggleMaintenanceMode = onToggleMaintenanceMode,
                            onCreateBackupSnapshot = onCreateBackupSnapshot
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun ErpOverviewSection(
    config: WorkspaceConfig?,
    courses: List<CourseRecord>,
    lmsMaterials: List<ContentItem>,
    students: List<StudentRecord>,
    connectors: List<WebsiteConnector>,
    payments: List<PaymentRecord>,
    staff: List<StaffRecord>,
    franchises: List<FranchiseRecord>,
    auditLogs: List<AuditLogEntry>,
    onSelectSection: (AdminDashboardSection) -> Unit,
    onAddLmsMaterial: () -> Unit,
    onAddStudent: () -> Unit,
    onRunProbe: () -> Unit,
    onCreateSnapshot: () -> Unit,
    onOpenFullControlCenter: (() -> Unit)?
) {
    val activeStudents = students.count { it.status == "ACTIVE" }
    val avgAttendance = if (students.isEmpty()) 0 else (students.sumOf { it.attendancePct } / students.size).toInt()
    val avgSyllabus = if (courses.isEmpty()) 0 else (courses.sumOf { it.syllabusProgressPct } / courses.size).toInt()
    val verifiedRevenue = payments.filter { it.status == "VERIFIED" && it.category != "REFUND" && it.category != "EXPENSE" }.sumOf { it.amount }
    val connectedCount = connectors.count { it.status == "CONNECTED" }
    val errorCount = connectors.count { it.status == "ERROR" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_overview_lazy_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MvciIndigoPrimary.copy(alpha = 0.45f)),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = config?.instituteName ?: "Maa Vaishno Computer Institute",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Centralized ERP & LMS Hub • Director: ${config?.directorName ?: "Rishu Singh"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MvciAmberAccent
                            )
                        }
                        StatusPill(if (errorCount == 0) "OPERATIONAL" else "$errorCount ALERT")
                    }
                    Text(
                        text = "${franchises.size} Franchises • ${staff.size} Staff • ${auditLogs.size} Audit Events",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                text = "Summary Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiMetricTile(
                        label = "Students Enrolled",
                        value = "${students.size}",
                        icon = Icons.Default.School,
                        accent = MvciIndigoPrimary,
                        hint = "$activeStudents Active • $avgAttendance%",
                        onClick = { onSelectSection(AdminDashboardSection.STUDENT_RECORDS) },
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricTile(
                        label = "LMS Courses",
                        value = "${courses.size}",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        accent = MvciCyanAccent,
                        hint = "$avgSyllabus% Syllabus",
                        onClick = { onSelectSection(AdminDashboardSection.LMS_MANAGEMENT) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KpiMetricTile(
                        label = "ERP Fee Revenue",
                        value = "INR $verifiedRevenue",
                        icon = Icons.Default.AccountBalanceWallet,
                        accent = MvciEmerald,
                        hint = "${payments.size} Receipts",
                        onClick = { onOpenFullControlCenter?.invoke() },
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricTile(
                        label = "Server Connectors",
                        value = "$connectedCount/${connectors.size}",
                        icon = Icons.Default.Dns,
                        accent = if (errorCount == 0) MvciAmberAccent else MvciRoseError,
                        hint = "${lmsMaterials.size} LMS Files",
                        onClick = { onSelectSection(AdminDashboardSection.SERVER_STATUS_TOOLS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Text(
                text = "Quick Access Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAccessActionCard(
                        title = "Add Student",
                        subtitle = "Enroll in ERP",
                        icon = Icons.Default.PersonAdd,
                        accent = MvciIndigoPrimary,
                        onClick = onAddStudent,
                        modifier = Modifier.weight(1f),
                        testTagName = "admin_quick_add_student"
                    )
                    QuickAccessActionCard(
                        title = "Publish LMS",
                        subtitle = "Upload Material",
                        icon = Icons.AutoMirrored.Filled.LibraryBooks,
                        accent = MvciCyanAccent,
                        onClick = onAddLmsMaterial,
                        modifier = Modifier.weight(1f),
                        testTagName = "admin_quick_add_lms"
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAccessActionCard(
                        title = "Probe Server",
                        subtitle = "Check HTTPS/TLS",
                        icon = Icons.Default.HealthAndSafety,
                        accent = MvciEmerald,
                        onClick = {
                            onRunProbe()
                            onSelectSection(AdminDashboardSection.SERVER_STATUS_TOOLS)
                        },
                        modifier = Modifier.weight(1f),
                        testTagName = "admin_quick_probe_server"
                    )
                    QuickAccessActionCard(
                        title = "Create Backup",
                        subtitle = "Save Snapshot",
                        icon = Icons.Default.Backup,
                        accent = MvciAmberAccent,
                        onClick = onCreateSnapshot,
                        modifier = Modifier.weight(1f),
                        testTagName = "admin_quick_backup"
                    )
                }
                if (onOpenFullControlCenter != null) {
                    QuickAccessActionCard(
                        title = "Open Full 21-Module ERP Suite",
                        subtitle = "Finance, Franchises, Roles, Audit & Connector Settings",
                        icon = Icons.Default.Business,
                        accent = MvciIndigoPrimary,
                        onClick = onOpenFullControlCenter,
                        modifier = Modifier.fillMaxWidth(),
                        testTagName = "admin_quick_full_suite"
                    )
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun AdminDrawerHeader(config: WorkspaceConfig?) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_mvci_icon_fg),
                contentDescription = "MVCI Emblem",
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MVCI Admin Dashboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Director: ${config?.directorName ?: "Rishu Singh"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MvciAmberAccent
                )
                Text(
                    text = config?.websiteUrl ?: "https://www.mvci.online",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LmsManagementSection(
    courses: List<CourseRecord>,
    materials: List<ContentItem>,
    onAdvanceSyllabus: (CourseRecord) -> Unit,
    onAddMaterial: () -> Unit,
    onDeleteMaterial: (String) -> Unit
) {
    val avgSyllabus = if (courses.isEmpty()) 0 else (courses.sumOf { it.syllabusProgressPct } / courses.size).toInt()
    var filterCategory by remember { mutableStateOf("ALL") }
    val filteredMaterials = remember(materials, filterCategory) {
        if (filterCategory == "ALL") materials else materials.filter { it.moduleCategory == filterCategory }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("lms_lazy_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiMetricTile(
                    label = "Active Courses",
                    value = "${courses.size}",
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    accent = MvciIndigoPrimary,
                    hint = "$avgSyllabus% Avg",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                KpiMetricTile(
                    label = "Digital Resources",
                    value = "${materials.size}",
                    icon = Icons.AutoMirrored.Filled.LibraryBooks,
                    accent = MvciCyanAccent,
                    hint = "Publish",
                    onClick = onAddMaterial,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL", "LMS_PDF", "LMS_VIDEO", "LMS_BOOK", "LMS_ASSIGNMENT").forEach { cat ->
                    FilterChip(
                        selected = filterCategory == cat,
                        onClick = { filterCategory = cat },
                        label = { Text(if (cat == "ALL") "All Materials" else cat.removePrefix("LMS_")) }
                    )
                }
            }
        }

        if (courses.isNotEmpty()) {
            item {
                Text("Course Syllabus Progress", style = MaterialTheme.typography.titleMedium)
            }
            items(courses, key = { "crs_${it.id}" }) { course ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(course.title, style = MaterialTheme.typography.titleMedium)
                                Text("${course.code} • ${course.category} • Faculty: ${course.facultyName}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusPill("${course.syllabusProgressPct}%")
                        }
                        LinearProgressIndicator(
                            progress = { (course.syllabusProgressPct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Batches: ${course.activeBatches} (${course.batchTiming})", style = MaterialTheme.typography.labelSmall)
                            OutlinedButton(onClick = { onAdvanceSyllabus(course) }) {
                                Text("+10% Chapter")
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("LMS Content Repository", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onAddMaterial) {
                    Text("+ Upload Material")
                }
            }
        }

        if (filteredMaterials.isEmpty()) {
            item {
                EmptyModuleState(
                    icon = Icons.AutoMirrored.Filled.LibraryBooks,
                    title = "No LMS Materials Yet",
                    subtitle = "Publish PDFs, recorded lectures, digital books, or assignments for MVCI students.",
                    actionLabel = "Publish Material",
                    onAction = onAddMaterial
                )
            }
        }

        items(filteredMaterials, key = { "lms_${it.id}" }) { item ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${item.moduleCategory} • Download: ${if (item.downloadable) "Enabled" else "View-Only"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (item.urlOrMeta.isNotBlank()) {
                            Text(item.urlOrMeta, style = MaterialTheme.typography.labelSmall, color = MvciCyanAccent, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    IconButton(onClick = { onDeleteMaterial(item.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete LMS Item", tint = MvciRoseError)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun StudentRecordsSection(
    students: List<StudentRecord>,
    onAddStudent: () -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteStudent: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") }
    var idCardStudent by remember { mutableStateOf<StudentRecord?>(null) }
    var deleteCandidate by remember { mutableStateOf<StudentRecord?>(null) }

    if (idCardStudent != null) {
        val st = idCardStudent!!
        AlertDialog(
            onDismissRequest = { idCardStudent = null },
            title = { Text("MVCI Student Digital ID Card") },
            text = {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("MAA VAISHNO COMPUTER INSTITUTE", style = MaterialTheme.typography.labelMedium, color = MvciAmberAccent)
                        Text(st.name, style = MaterialTheme.typography.titleLarge)
                        Text("Roll No: ${st.rollNo}", style = MaterialTheme.typography.labelLarge)
                        Text("Course: ${st.courseName} • Batch: ${st.batchName}", style = MaterialTheme.typography.bodySmall)
                        Text("Branch: ${st.branchName} • Status: ${st.status}", style = MaterialTheme.typography.bodySmall)
                        Text("Attendance: ${st.attendancePct}% • Paid: INR ${st.paidFee}/${st.totalFee}", style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { idCardStudent = null }) { Text("Close") } }
        )
    }

    if (deleteCandidate != null) {
        ConfirmActionDialog(
            title = "Delete Student Record?",
            message = "Remove ${deleteCandidate!!.name} (${deleteCandidate!!.rollNo}) permanently?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteStudent(deleteCandidate!!.id)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null }
        )
    }

    val filtered = remember(students, searchQuery, statusFilter) {
        students.filter { st ->
            (statusFilter == "ALL" || st.status == statusFilter) &&
                (searchQuery.isBlank() ||
                    st.name.contains(searchQuery, ignoreCase = true) ||
                    st.rollNo.contains(searchQuery, ignoreCase = true) ||
                    st.courseName.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("students_lazy_list"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiMetricTile(
                    label = "Total Students",
                    value = "${students.size}",
                    icon = Icons.Default.School,
                    accent = MvciIndigoPrimary,
                    hint = "${students.count { it.status == "ACTIVE" }} Active",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                KpiMetricTile(
                    label = "Pending Review",
                    value = "${students.count { it.status == "PENDING" }}",
                    icon = Icons.Default.Badge,
                    accent = MvciAmberAccent,
                    hint = "Filter",
                    onClick = { statusFilter = "PENDING" },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Students") },
                placeholder = { Text("Search by student name, roll no, or course...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_student_search")
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ALL", "ACTIVE", "PENDING", "PROMOTED", "TRANSFERRED").forEach { st ->
                    FilterChip(selected = statusFilter == st, onClick = { statusFilter = st }, label = { Text(st) })
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyModuleState(
                    icon = Icons.Default.School,
                    title = "No Student Records Match",
                    subtitle = "Add a new student admission or clear filters to inspect records.",
                    actionLabel = "Add Student",
                    onAction = onAddStudent
                )
            }
        }

        items(filtered, key = { it.id }) { st ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(st.name, style = MaterialTheme.typography.titleMedium)
                            Text("${st.rollNo} • ${st.courseName} (${st.batchName})", style = MaterialTheme.typography.bodySmall)
                        }
                        StatusPill(st.status)
                    }
                    Text(
                        "Branch: ${st.branchName} • Fee: INR ${st.paidFee}/${st.totalFee} • Attendance: ${st.attendancePct}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MvciEmerald
                    )
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (st.status == "PENDING") {
                            Button(onClick = { onUpdateStatus(st.id, "ACTIVE") }) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Approve")
                            }
                        }
                        OutlinedButton(onClick = { idCardStudent = st }) {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("ID Card")
                        }
                        TextButton(onClick = { onUpdateStatus(st.id, "PROMOTED") }) { Text("Promote") }
                        IconButton(onClick = { deleteCandidate = st }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Student", tint = MvciRoseError)
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ServerStatusToolsSection(
    config: WorkspaceConfig?,
    connectors: List<WebsiteConnector>,
    backups: List<ContentItem>,
    probeResult: WebsiteProbeResult?,
    isProbing: Boolean,
    onProbeUrl: (String) -> Unit,
    onTestAndSyncConnector: (WebsiteConnector, Long) -> Unit,
    onToggleMaintenanceMode: (Boolean) -> Unit,
    onCreateBackupSnapshot: (ContentItem) -> Unit
) {
    val runtime = Runtime.getRuntime()
    val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxMb = runtime.maxMemory() / (1024 * 1024)
    val cores = runtime.availableProcessors()
    var probeUrlInput by remember(config) { mutableStateOf(config?.websiteUrl ?: "https://www.mvci.online") }
    var confirmMaintenance by remember { mutableStateOf<Boolean?>(null) }

    if (confirmMaintenance != null) {
        val next = confirmMaintenance!!
        ConfirmActionDialog(
            title = if (next) "Enable Maintenance Mode?" else "Disable Maintenance Mode?",
            message = "Update global MVCI maintenance status to $next and record an audit event.",
            confirmLabel = if (next) "Enable" else "Disable",
            onConfirm = {
                onToggleMaintenanceMode(next)
                confirmMaintenance = null
            },
            onDismiss = { confirmMaintenance = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("server_lazy_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiMetricTile(
                    label = "JVM Heap Usage",
                    value = "$usedMb/$maxMb MB",
                    icon = Icons.Default.Storage,
                    accent = MvciAmberAccent,
                    hint = "$cores Cores",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                KpiMetricTile(
                    label = "Endpoint Links",
                    value = "${connectors.count { it.status == "CONNECTED" }}/${connectors.size}",
                    icon = Icons.Default.CloudDone,
                    accent = MvciEmerald,
                    hint = "Active",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MvciEmerald)
                        Spacer(Modifier.width(8.dp))
                        Text("Live Server & Endpoint Diagnostic Probe", style = MaterialTheme.typography.titleMedium)
                    }
                    Text(
                        "MySQL Host: ${config?.mysqlHost ?: "mysql.mvci.online"} (${config?.mysqlDbName ?: "mvci_central_erp"}) • Hosting: ${config?.hostingProvider ?: "GoDaddy Compatible"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = probeUrlInput,
                        onValueChange = { probeUrlInput = it },
                        label = { Text("Target Server / Endpoint URL") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_probe_url_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onProbeUrl(probeUrlInput) },
                            enabled = !isProbing && probeUrlInput.isNotBlank(),
                            modifier = Modifier.testTag("server_probe_run_btn")
                        ) {
                            Icon(Icons.Default.Http, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isProbing) "Probing Endpoint..." else "Run Live HTTP/TLS Probe")
                        }
                        OutlinedButton(onClick = {
                            onCreateBackupSnapshot(
                                ContentItem(
                                    title = "MVCI Server Snapshot ${backups.size + 1}",
                                    moduleCategory = "BACKUP",
                                    description = "Cloud Firestore + MySQL (${config?.mysqlDbName ?: "mvci_central_erp"}) snapshot",
                                    urlOrMeta = "Triggered from Server Status Tools",
                                    downloadable = true,
                                    status = "COMPLETED"
                                )
                            )
                        }) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Snapshot")
                        }
                    }

                    if (probeResult != null) {
                        val tint = if (probeResult.reachable) MvciEmerald else MvciRoseError
                        Surface(
                            color = tint.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, tint.copy(alpha = 0.45f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (probeResult.reachable) Icons.Default.CheckCircle else Icons.Default.Error, contentDescription = null, tint = tint)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text("${probeResult.targetUrl} — ${probeResult.message}", style = MaterialTheme.typography.labelLarge, color = tint)
                                    Text(
                                        "HTTP ${probeResult.httpCode} • ${probeResult.tlsProtocol} • Latency: ${probeResult.latencyMs}ms • Server: ${probeResult.serverHeader.ifBlank { "N/A" }}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Portal Maintenance Mode", style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (config?.maintenanceMode == true) "Public portal currently shows maintenance notice" else "Public MVCI portals serving live traffic",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = config?.maintenanceMode ?: false,
                        onCheckedChange = { confirmMaintenance = it }
                    )
                }
            }
        }

        if (connectors.isNotEmpty()) {
            item {
                Text("Connected Website Servers (${connectors.size})", style = MaterialTheme.typography.titleMedium)
            }
            items(connectors, key = { "srv_${it.id}" }) { conn ->
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
                            Text("${conn.url} • ${conn.healthStatus} (${conn.lastLatencyMs}ms)", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(conn.status)
                            IconButton(onClick = { onTestAndSyncConnector(conn, 0L) }) {
                                Icon(Icons.Default.Sync, contentDescription = "Probe Connector", tint = MvciCyanAccent)
                            }
                        }
                    }
                }
            }
        }

        if (backups.isNotEmpty()) {
            item {
                Text("Recent Server Snapshots", style = MaterialTheme.typography.titleMedium)
            }
            items(backups.take(5), key = { "bkp_${it.id}" }) { bkp ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(bkp.title, style = MaterialTheme.typography.titleSmall)
                            Text("${bkp.description} • ${formatTimestamp(bkp.createdAt?.toDate()?.time)}", style = MaterialTheme.typography.labelSmall)
                        }
                        StatusPill(bkp.status)
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun AddLmsMaterialDialog(
    onDismiss: () -> Unit,
    onSave: (ContentItem) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("LMS_PDF") }
    var desc by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://www.mvci.online/library/") }
    var downloadable by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish LMS Material") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Material Title") })
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("LMS_PDF", "LMS_VIDEO", "LMS_BOOK", "LMS_ASSIGNMENT").forEach { cat ->
                        FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                    }
                }
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
                OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Resource URL / Path") })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Allow Student Download")
                    Switch(checked = downloadable, onCheckedChange = { downloadable = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ContentItem(
                            title = title,
                            moduleCategory = category,
                            description = desc,
                            urlOrMeta = url,
                            downloadable = downloadable,
                            status = "PUBLISHED"
                        )
                    )
                },
                enabled = title.isNotBlank()
            ) { Text("Publish") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddStudentRecordDialog(
    courses: List<CourseRecord>,
    nextIndex: Int,
    onDismiss: () -> Unit,
    onSave: (StudentRecord) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("MVCI-2026-${100 + nextIndex}") }
    var course by remember { mutableStateOf(courses.firstOrNull()?.title ?: "ADCA") }
    var batch by remember { mutableStateOf("Morning-08") }
    var branch by remember { mutableStateOf("Main Campus") }
    var totalFee by remember { mutableStateOf("14500") }
    var paidFee by remember { mutableStateOf("5000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Student Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Student Name") })
                OutlinedTextField(value = rollNo, onValueChange = { rollNo = it }, label = { Text("Roll Number") })
                OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Course") })
                OutlinedTextField(value = batch, onValueChange = { batch = it }, label = { Text("Batch") })
                OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("Branch") })
                OutlinedTextField(value = totalFee, onValueChange = { totalFee = it.filter { c -> c.isDigit() } }, label = { Text("Total Fee (INR)") })
                OutlinedTextField(value = paidFee, onValueChange = { paidFee = it.filter { c -> c.isDigit() } }, label = { Text("Paid Fee (INR)") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        StudentRecord(
                            name = name,
                            rollNo = rollNo,
                            courseName = course,
                            batchName = batch,
                            branchName = branch,
                            status = "ACTIVE",
                            totalFee = totalFee.toLongOrNull() ?: 0L,
                            paidFee = paidFee.toLongOrNull() ?: 0L,
                            attendancePct = 100L
                        )
                    )
                },
                enabled = name.isNotBlank() && rollNo.isNotBlank() && course.isNotBlank() && batch.isNotBlank() && branch.isNotBlank()
            ) { Text("Save Student") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
