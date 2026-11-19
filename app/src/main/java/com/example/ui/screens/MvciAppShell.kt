package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.MenuModule
import com.example.ui.components.StatusPill
import com.example.ui.components.bi
import com.example.ui.theme.MvciAmberAccent
import com.example.ui.theme.MvciControlTheme
import com.example.ui.theme.MvciEmerald
import com.example.viewmodel.MvciViewModel
import com.example.viewmodel.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MvciAppShell(
    viewModel: MvciViewModel,
    onLockConsole: () -> Unit = { viewModel.lockSuperAdmin() }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val activeModule by viewModel.activeModule.collectAsStateWithLifecycle()
    val activeSubPage by viewModel.activeSubPage.collectAsStateWithLifecycle()
    val canGoBack by viewModel.canNavigateBack.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val probeResult by viewModel.probeResult.collectAsStateWithLifecycle()
    val isProbing by viewModel.isProbing.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val errorBanner by viewModel.errorBanner.collectAsStateWithLifecycle()

    val workspaceUi by viewModel.workspaceConfig.collectAsStateWithLifecycle()
    val studentsUi by viewModel.studentsState.collectAsStateWithLifecycle()
    val franchisesUi by viewModel.franchisesState.collectAsStateWithLifecycle()
    val staffUi by viewModel.staffState.collectAsStateWithLifecycle()
    val coursesUi by viewModel.coursesState.collectAsStateWithLifecycle()
    val connectorsUi by viewModel.connectorsState.collectAsStateWithLifecycle()
    val rolesUi by viewModel.rolesState.collectAsStateWithLifecycle()
    val automationsUi by viewModel.automationsState.collectAsStateWithLifecycle()
    val paymentsUi by viewModel.paymentsState.collectAsStateWithLifecycle()
    val examsUi by viewModel.examsState.collectAsStateWithLifecycle()
    val contentsUi by viewModel.contentsState.collectAsStateWithLifecycle()
    val auditLogsUi by viewModel.auditLogsState.collectAsStateWithLifecycle()
    val notificationsUi by viewModel.notificationsState.collectAsStateWithLifecycle()

    val config = (workspaceUi as? UiState.Success)?.data
    val darkTheme = config?.darkTheme ?: true
    val isHindi = config?.languageCode == "hi"
    val students = (studentsUi as? UiState.Success)?.data.orEmpty()
    val franchises = (franchisesUi as? UiState.Success)?.data.orEmpty()
    val staff = (staffUi as? UiState.Success)?.data.orEmpty()
    val courses = (coursesUi as? UiState.Success)?.data.orEmpty()
    val connectors = (connectorsUi as? UiState.Success)?.data.orEmpty()
    val roles = (rolesUi as? UiState.Success)?.data.orEmpty()
    val automations = (automationsUi as? UiState.Success)?.data.orEmpty()
    val payments = (paymentsUi as? UiState.Success)?.data.orEmpty()
    val exams = (examsUi as? UiState.Success)?.data.orEmpty()
    val contents = (contentsUi as? UiState.Success)?.data.orEmpty()
    val auditLogs = (auditLogsUi as? UiState.Success)?.data.orEmpty()
    val notifications = (notificationsUi as? UiState.Success)?.data.orEmpty()

    val unreadCount = notifications.count { it.unread }
    val snackbarHostState = remember { SnackbarHostState() }

    var showGlobalSearch by remember { mutableStateOf(false) }
    var showQuickActions by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showQuickSettings by remember { mutableStateOf(false) }
    var showHelpSupport by remember { mutableStateOf(false) }
    var sidebarCollapsed by remember { mutableStateOf(false) }
    var focusedAdminMode by remember { mutableStateOf(false) }

    BackHandler(enabled = focusedAdminMode || canGoBack) {
        if (focusedAdminMode) {
            focusedAdminMode = false
        } else {
            viewModel.navigateBack()
        }
    }

    LaunchedEffect(statusBanner, errorBanner) {
        val msg = errorBanner ?: statusBanner
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanners()
        }
    }

    val shareExportCsv: () -> Unit = {
        val csv = viewModel.buildCsvExport()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "MVCI Super Admin Export Report")
            putExtra(Intent.EXTRA_TEXT, csv)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Export MVCI Report"))
        } catch (_: Exception) {
            Toast.makeText(context, "Prepared CSV (${csv.length} bytes)", Toast.LENGTH_SHORT).show()
        }
    }

    MvciControlTheme(darkTheme = darkTheme) {
        if (showGlobalSearch) {
            GlobalSearchDialog(
                viewModel = viewModel,
                onSelect = { match ->
                    viewModel.navigateTo(match.module, match.subPage)
                    showGlobalSearch = false
                },
                onDismiss = { showGlobalSearch = false }
            )
        }

        if (showQuickActions) {
            QuickActionsDialog(
                onOpenAdminDashboard = {
                    showQuickActions = false
                    focusedAdminMode = true
                },
                onAction = { mod, sub ->
                    focusedAdminMode = false
                    viewModel.navigateTo(mod, sub)
                    showQuickActions = false
                },
                onDismiss = { showQuickActions = false }
            )
        }

        if (showNotifications) {
            NotificationsDialog(
                notifications = notifications,
                onMarkRead = { viewModel.markNotificationRead(it) },
                onDismiss = { showNotifications = false }
            )
        }

        if (showQuickSettings) {
            QuickSettingsDialog(
                darkTheme = darkTheme,
                isHindi = isHindi,
                onToggleDark = { d -> viewModel.updateWorkspaceSettings(mapOf("darkTheme" to d), "QUICK_SET_THEME", "Theme updated") },
                onToggleHindi = { hi -> viewModel.updateWorkspaceSettings(mapOf("languageCode" to if (hi) "hi" else "en"), "QUICK_SET_LANG", "Language updated") },
                onOpenSuperAdminReset = {
                    showQuickSettings = false
                    focusedAdminMode = false
                    viewModel.navigateTo(MenuModule.SYSTEM_SETTINGS, "General Settings")
                },
                onLockSuperAdmin = {
                    showQuickSettings = false
                    focusedAdminMode = false
                    onLockConsole()
                },
                onOpenHelp = { showQuickSettings = false; showHelpSupport = true },
                onDismiss = { showQuickSettings = false }
            )
        }

        if (showHelpSupport) {
            AlertDialog(
                onDismissRequest = { showHelpSupport = false },
                title = { Text("MVCI Help & Support") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Maa Vaishno Computer Institute", style = MaterialTheme.typography.titleMedium)
                        Text("Director: Rishu Singh")
                        Text("Portal: https://www.mvci.online")
                        Text("21 dedicated modules, Universal Website Connector, RBAC Permission Builder, and Gemini AI Tools are active and synced.")
                    }
                },
                confirmButton = { TextButton(onClick = { showHelpSupport = false }) { Text("Close") } }
            )
        }

        if (focusedAdminMode) {
            AdminDashboard(
                config = config,
                courses = courses,
                contents = contents,
                students = students,
                connectors = connectors,
                payments = payments,
                staff = staff,
                franchises = franchises,
                auditLogs = auditLogs,
                probeResult = probeResult,
                isProbing = isProbing,
                onSaveContent = { viewModel.saveContent(it) },
                onDeleteContent = { viewModel.deleteContent(it) },
                onSaveCourse = { c, id -> viewModel.saveCourse(c, id) },
                onSaveStudent = { s, id -> viewModel.saveStudent(s, id) },
                onUpdateStudentStatus = { id, st -> viewModel.updateStudentStatus(id, st) },
                onDeleteStudent = { viewModel.deleteStudent(it) },
                onProbeUrl = { viewModel.runWebsiteProbe(it) },
                onTestAndSyncConnector = { c, d -> viewModel.testAndSyncConnector(c, d) },
                onToggleMaintenanceMode = { m ->
                    viewModel.updateWorkspaceSettings(mapOf("maintenanceMode" to m), "TOGGLE_MAINTENANCE_MODE", "Maintenance mode updated")
                },
                onCreateBackupSnapshot = { viewModel.saveContent(it) },
                onOpenFullControlCenter = { focusedAdminMode = false },
                onNavigateToErpModule = { mod, sub ->
                    focusedAdminMode = false
                    viewModel.navigateTo(mod, sub)
                }
            )
        } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 840.dp
            val drawerState = rememberDrawerState(DrawerValue.Closed)

            val mainScaffold: @Composable () -> Unit = {
                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                if (isWide) {
                                    IconButton(
                                        onClick = { sidebarCollapsed = !sidebarCollapsed },
                                        modifier = Modifier.testTag("toggle_sidebar_collapse")
                                    ) {
                                        Icon(
                                            if (sidebarCollapsed) Icons.Default.ChevronRight else Icons.Default.ChevronLeft,
                                            contentDescription = "Toggle Sidebar"
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        modifier = Modifier.testTag("open_sidebar_drawer")
                                    ) {
                                        Icon(Icons.Default.Menu, contentDescription = "Open Menu")
                                    }
                                }
                            },
                            title = {
                                Column {
                                    Text(
                                        text = bi(activeModule.titleEn, activeModule.titleHi, isHindi),
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "MVCI › ${activeModule.titleEn} › $activeSubPage",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { showGlobalSearch = true }, modifier = Modifier.testTag("global_search_btn")) {
                                    Icon(Icons.Default.Search, contentDescription = "Global Search")
                                }
                                IconButton(onClick = { showQuickActions = true }, modifier = Modifier.testTag("quick_actions_btn")) {
                                    Icon(Icons.Default.Bolt, contentDescription = "Quick Actions", tint = MvciAmberAccent)
                                }
                                IconButton(onClick = { showNotifications = true }, modifier = Modifier.testTag("notifications_btn")) {
                                    BadgedBox(badge = { if (unreadCount > 0) Badge { Text("$unreadCount") } }) {
                                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                                    }
                                }
                                IconButton(onClick = { showQuickSettings = true }, modifier = Modifier.testTag("quick_settings_btn")) {
                                    Icon(Icons.Default.Tune, contentDescription = "Quick Settings & Profile")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (workspaceUi is UiState.Loading && config == null) {
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator()
                                Text("Initializing MVCI Command Center...")
                            }
                        } else {
                            when (activeModule) {
                                MenuModule.DASHBOARD -> DashboardOverviewScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.DASHBOARD, it) },
                                    onNavigate = { m, s -> viewModel.navigateTo(m, s) },
                                    dateFilter = dateFilter,
                                    onDateFilterChange = { viewModel.setDateFilter(it) },
                                    config = config,
                                    students = students,
                                    franchises = franchises,
                                    staff = staff,
                                    courses = courses,
                                    payments = payments,
                                    exams = exams,
                                    connectors = connectors,
                                    contents = contents,
                                    auditLogs = auditLogs,
                                    probeResult = probeResult,
                                    isProbing = isProbing,
                                    onProbeMainWebsite = { viewModel.runWebsiteProbe(config?.websiteUrl ?: "https://www.mvci.online") },
                                    onOpenFocusedAdmin = { focusedAdminMode = true },
                                    isHindi = isHindi
                                )
                                MenuModule.WEBSITE_MGMT -> WebsiteManagementScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.WEBSITE_MGMT, it) },
                                    config = config,
                                    contents = contents,
                                    probeResult = probeResult,
                                    isProbing = isProbing,
                                    onProbeWebsite = { viewModel.runWebsiteProbe(it) },
                                    onUpdateWorkspace = { u, a, m -> viewModel.updateWorkspaceSettings(u, a, m) },
                                    onSaveContent = { viewModel.saveContent(it) },
                                    onDeleteContent = { viewModel.deleteContent(it) }
                                )
                                MenuModule.WEBSITE_CONNECTOR -> WebsiteConnectorScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.WEBSITE_CONNECTOR, it) },
                                    connectors = connectors,
                                    auditLogs = auditLogs,
                                    probeResult = probeResult,
                                    isProbing = isProbing,
                                    maskSecret = { viewModel.maskSecret(it) },
                                    onProbeUrl = { u, b, hn, hv -> viewModel.runWebsiteProbe(u, b, hn, hv) },
                                    onSaveConnector = { c, id -> viewModel.createOrUpdateConnector(c, id) },
                                    onTestAndSyncConnector = { c, d -> viewModel.testAndSyncConnector(c, d) },
                                    onSetConnectorStatus = { id, st, note -> viewModel.setConnectorStatus(id, st, note) },
                                    onDeleteConnector = { viewModel.deleteConnector(it) },
                                    onExportConnectorCsv = shareExportCsv
                                )
                                MenuModule.INSTITUTE_MGMT -> InstituteManagementScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.INSTITUTE_MGMT, it) },
                                    config = config,
                                    franchises = franchises,
                                    onUpdateWorkspace = { u, a, m -> viewModel.updateWorkspaceSettings(u, a, m) }
                                )
                                MenuModule.FRANCHISE_MGMT -> FranchiseManagementScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.FRANCHISE_MGMT, it) },
                                    franchises = franchises,
                                    onSaveFranchise = { f, id -> viewModel.saveFranchise(f, id) },
                                    onSetStatus = { id, st -> viewModel.setFranchiseStatus(id, st) },
                                    onDeleteFranchise = { viewModel.deleteFranchise(it) }
                                )
                                MenuModule.USER_MGMT -> UserManagementScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.USER_MGMT, it) },
                                    staff = staff,
                                    roles = roles,
                                    onSaveStaff = { s, id -> viewModel.saveStaff(s, id) },
                                    onSetStaffStatus = { id, st -> viewModel.setStaffStatus(id, st) },
                                    onDeleteStaff = { viewModel.deleteStaff(it) }
                                )
                                MenuModule.ACADEMIC_MGMT -> AcademicManagementScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.ACADEMIC_MGMT, it) },
                                    courses = courses,
                                    onSaveCourse = { c, id -> viewModel.saveCourse(c, id) },
                                    onDeleteCourse = { viewModel.deleteCourse(it) }
                                )
                                MenuModule.STUDENT_MGMT -> StudentManagementScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.STUDENT_MGMT, it) },
                                    students = students,
                                    courses = courses,
                                    onSaveStudent = { s, id -> viewModel.saveStudent(s, id) },
                                    onUpdateStatus = { id, st -> viewModel.updateStudentStatus(id, st) },
                                    onDeleteStudent = { viewModel.deleteStudent(it) }
                                )
                                MenuModule.PARENT_PORTAL -> ParentPortalScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.PARENT_PORTAL, it) },
                                    students = students,
                                    onSaveStudent = { s, id -> viewModel.saveStudent(s, id) }
                                )
                                MenuModule.ATTENDANCE_MGMT -> AttendanceManagementScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.ATTENDANCE_MGMT, it) },
                                    students = students,
                                    onMarkAttendance = { s, p -> viewModel.markAttendance(s, p) }
                                )
                                MenuModule.EXAM_MGMT -> ExaminationManagementScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.EXAM_MGMT, it) },
                                    exams = exams,
                                    courses = courses,
                                    onSaveExam = { e, id -> viewModel.saveExam(e, id) }
                                )
                                MenuModule.LMS_LIBRARY -> LmsLibraryScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.LMS_LIBRARY, it) },
                                    contents = contents,
                                    onSaveContent = { viewModel.saveContent(it) },
                                    onDeleteContent = { viewModel.deleteContent(it) }
                                )
                                MenuModule.FINANCE_ACCOUNTING -> FinanceAccountingScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.FINANCE_ACCOUNTING, it) },
                                    payments = payments,
                                    students = students,
                                    onSavePayment = { p, id -> viewModel.savePayment(p, id) }
                                )
                                MenuModule.COMMUNICATION_CENTER, MenuModule.MOBILE_APP_MGMT -> CommunicationAndMobileAppScreen(
                                    module = activeModule,
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(activeModule, it) },
                                    contents = contents,
                                    onSaveContent = { viewModel.saveContent(it) },
                                    onDeleteContent = { viewModel.deleteContent(it) }
                                )
                                MenuModule.AI_TOOLS_MGMT -> AiToolsManagementScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.AI_TOOLS_MGMT, it) },
                                    aiState = aiState,
                                    isHindi = isHindi,
                                    onRunAi = { t, p, l -> viewModel.runAiTool(t, p, l) }
                                )
                                MenuModule.REPORTS_ANALYTICS -> ReportsAnalyticsScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.REPORTS_ANALYTICS, it) },
                                    students = students,
                                    franchises = franchises,
                                    courses = courses,
                                    payments = payments,
                                    connectors = connectors,
                                    onExportCsv = shareExportCsv
                                )
                                MenuModule.SECURITY_PERMISSIONS -> SecurityPermissionsScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.SECURITY_PERMISSIONS, it) },
                                    roles = roles,
                                    auditLogs = auditLogs,
                                    config = config,
                                    onSaveRole = { r, id -> viewModel.saveRole(r, id) },
                                    onDuplicateRole = { viewModel.duplicateRole(it) },
                                    onDeleteRole = { viewModel.deleteRole(it) },
                                    onUpdateWorkspace = { u, a, m -> viewModel.updateWorkspaceSettings(u, a, m) },
                                    onUpdateSuperAdminCreds = viewModel::updateSuperAdminCredentials,
                                    onResetSuperAdminCreds = viewModel::resetSuperAdminCredentials,
                                    onUpdateRecoverySettings = viewModel::updateRecoverySettings,
                                    onRequestOtp = { viewModel.requestPasswordResetOtp(it) },
                                    onVerifyOtpReset = { code, pass -> viewModel.verifyOtpAndResetPassword(code, pass) },
                                    onExportLogs = shareExportCsv
                                )
                                MenuModule.DATABASE_BACKUP -> DatabaseBackupScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.DATABASE_BACKUP, it) },
                                    config = config,
                                    contents = contents,
                                    onUpdateWorkspace = { u, a, m -> viewModel.updateWorkspaceSettings(u, a, m) },
                                    onCreateBackup = { viewModel.saveContent(it) },
                                    onExportJsonBackup = shareExportCsv
                                )
                                MenuModule.SYSTEM_SETTINGS -> SystemSettingsScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.SYSTEM_SETTINGS, it) },
                                    config = config,
                                    auditLogs = auditLogs,
                                    onExportLogs = shareExportCsv,
                                    onUpdateWorkspace = { u, a, m -> viewModel.updateWorkspaceSettings(u, a, m) },
                                    onUpdateSuperAdminCreds = viewModel::updateSuperAdminCredentials,
                                    onResetSuperAdminCreds = viewModel::resetSuperAdminCredentials,
                                    onUpdateRecoverySettings = viewModel::updateRecoverySettings,
                                    onRequestOtp = { viewModel.requestPasswordResetOtp(it) },
                                    onVerifyOtpReset = { code, pass -> viewModel.verifyOtpAndResetPassword(code, pass) }
                                )
                                MenuModule.ADVANCED_SETTINGS -> AdvancedSettingsScreen(
                                    subPage = activeSubPage,
                                    onSubPageChange = { viewModel.navigateTo(MenuModule.ADVANCED_SETTINGS, it) },
                                    config = config,
                                    automations = automations,
                                    connectors = connectors,
                                    probeResult = probeResult,
                                    isProbing = isProbing,
                                    onProbeMainSite = { viewModel.runWebsiteProbe(config?.websiteUrl ?: "https://www.mvci.online") },
                                    onSaveAutomation = { r, id -> viewModel.saveAutomation(r, id) },
                                    onTriggerAutomation = { viewModel.triggerAutomation(it) },
                                    onUpdateWorkspace = { u, a, m -> viewModel.updateWorkspaceSettings(u, a, m) }
                                )
                            }
                        }
                    }
                }
            }

            if (isWide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Surface(
                        modifier = Modifier
                            .width(if (sidebarCollapsed) 76.dp else 280.dp)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        SidebarContent(
                            collapsed = sidebarCollapsed,
                            activeModule = activeModule,
                            isHindi = isHindi,
                            onSelectModule = { mod -> viewModel.navigateTo(mod, mod.subMenus.first()) },
                            onOpenHelp = { showHelpSupport = true }
                        )
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        mainScaffold()
                    }
                }
            } else {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
                            SidebarContent(
                                collapsed = false,
                                activeModule = activeModule,
                                isHindi = isHindi,
                                onSelectModule = { mod ->
                                    viewModel.navigateTo(mod, mod.subMenus.first())
                                    scope.launch { drawerState.close() }
                                },
                                onOpenHelp = {
                                    scope.launch { drawerState.close() }
                                    showHelpSupport = true
                                }
                            )
                        }
                    },
                    content = mainScaffold
                )
            }
        }
        }
    }
}

@Composable
private fun SidebarContent(
    collapsed: Boolean,
    activeModule: MenuModule,
    isHindi: Boolean,
    onSelectModule: (MenuModule) -> Unit,
    onOpenHelp: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_mvci_icon_fg),
                contentDescription = "MVCI Emblem",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            if (!collapsed) {
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("MVCI SUPER ADMIN", style = MaterialTheme.typography.labelLarge, color = MvciAmberAccent)
                    Text("Director: Rishu Singh", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        HorizontalDivider()
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(MenuModule.entries) { mod ->
                val selected = mod == activeModule
                NavigationDrawerItem(
                    selected = selected,
                    onClick = { onSelectModule(mod) },
                    icon = { Icon(iconForModule(mod), contentDescription = mod.titleEn, modifier = Modifier.size(20.dp)) },
                    label = {
                        if (!collapsed) {
                            Text(
                                "${mod.number}. ${bi(mod.titleEn, mod.titleHi, isHindi)}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("sidebar_item_${mod.number}")
                )
            }
        }
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenHelp)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help & Support", tint = MvciEmerald)
            if (!collapsed) {
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("System: Operational", style = MaterialTheme.typography.labelMedium, color = MvciEmerald)
                    Text("Help & Support", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

private fun iconForModule(mod: MenuModule): ImageVector = when (mod) {
    MenuModule.DASHBOARD -> Icons.Default.Dashboard
    MenuModule.WEBSITE_MGMT -> Icons.Default.Public
    MenuModule.WEBSITE_CONNECTOR -> Icons.Default.Language
    MenuModule.INSTITUTE_MGMT -> Icons.Default.AdminPanelSettings
    MenuModule.FRANCHISE_MGMT -> Icons.Default.Business
    MenuModule.USER_MGMT -> Icons.Default.Groups
    MenuModule.ACADEMIC_MGMT -> Icons.Default.MenuBook
    MenuModule.STUDENT_MGMT -> Icons.Default.School
    MenuModule.PARENT_PORTAL -> Icons.Default.FamilyRestroom
    MenuModule.ATTENDANCE_MGMT -> Icons.Default.CalendarToday
    MenuModule.EXAM_MGMT -> Icons.Default.Quiz
    MenuModule.LMS_LIBRARY -> Icons.AutoMirrored.Filled.LibraryBooks
    MenuModule.FINANCE_ACCOUNTING -> Icons.Default.AccountBalanceWallet
    MenuModule.COMMUNICATION_CENTER -> Icons.Default.SupportAgent
    MenuModule.MOBILE_APP_MGMT -> Icons.Default.PhoneAndroid
    MenuModule.AI_TOOLS_MGMT -> Icons.Default.AutoAwesome
    MenuModule.REPORTS_ANALYTICS -> Icons.Default.Assessment
    MenuModule.SECURITY_PERMISSIONS -> Icons.Default.Security
    MenuModule.DATABASE_BACKUP -> Icons.Default.Storage
    MenuModule.SYSTEM_SETTINGS -> Icons.Default.Settings
    MenuModule.ADVANCED_SETTINGS -> Icons.Default.Tune
}

@Composable
private fun GlobalSearchDialog(
    viewModel: MvciViewModel,
    onSelect: (com.example.model.GlobalSearchMatch) -> Unit,
    onDismiss: () -> Unit
) {
    val query by viewModel.globalSearchQuery.collectAsStateWithLifecycle()
    val results = remember(query) { viewModel.buildSearchResults(query) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Global MVCI Search") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.setGlobalSearchQuery(it) },
                    placeholder = { Text("Users, audit logs, ERP settings, courses, menus...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(modifier = Modifier.height(260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(results) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    StatusPill(item.category)
                                }
                                Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun QuickActionsDialog(
    onOpenAdminDashboard: () -> Unit,
    onAction: (MenuModule, String) -> Unit,
    onDismiss: () -> Unit
) {
    val actions = listOf(
        Triple("Add Student", MenuModule.STUDENT_MGMT, "New Admissions"),
        Triple("Add Franchise", MenuModule.FRANCHISE_MGMT, "Add Franchise"),
        Triple("Add Faculty", MenuModule.USER_MGMT, "Faculty"),
        Triple("Create Course", MenuModule.ACADEMIC_MGMT, "Courses"),
        Triple("Mark Attendance", MenuModule.ATTENDANCE_MGMT, "Daily Attendance"),
        Triple("Publish Result", MenuModule.EXAM_MGMT, "Result Publication"),
        Triple("Send Notice", MenuModule.COMMUNICATION_CENTER, "Announcements"),
        Triple("Connect Website", MenuModule.WEBSITE_CONNECTOR, "Connect New Website"),
        Triple("Create Backup", MenuModule.DATABASE_BACKUP, "Database Backup"),
        Triple("Super Admin Login & Reset", MenuModule.SYSTEM_SETTINGS, "General Settings")
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Super Admin Quick Actions") },
        text = {
            LazyColumn(modifier = Modifier.height(320.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    OutlinedButton(onClick = onOpenAdminDashboard, modifier = Modifier.fillMaxWidth().testTag("quick_open_admin_dashboard")) {
                        Text("Open AdminDashboard Drawer (LMS / Students / Server)")
                    }
                }
                items(actions) { (label, mod, sub) ->
                    OutlinedButton(onClick = { onAction(mod, sub) }, modifier = Modifier.fillMaxWidth()) {
                        Text(label)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun NotificationsDialog(
    notifications: List<com.example.model.SystemNotification>,
    onMarkRead: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("System & Workflow Notifications") },
        text = {
            LazyColumn(modifier = Modifier.height(280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (notifications.isEmpty()) {
                    item { Text("All systems clear. Workflow & integration alerts appear here.", style = MaterialTheme.typography.bodySmall) }
                }
                items(notifications, key = { it.id }) { n ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onMarkRead(n.id) }
                    ) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(n.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                StatusPill(if (n.unread) n.severity else "READ")
                            }
                            Text(n.message, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun QuickSettingsDialog(
    darkTheme: Boolean,
    isHindi: Boolean,
    onToggleDark: (Boolean) -> Unit,
    onToggleHindi: (Boolean) -> Unit,
    onOpenSuperAdminReset: () -> Unit,
    onLockSuperAdmin: () -> Unit,
    onOpenHelp: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Settings & Super Admin Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Maa Vaishno Computer Institute • Director: Rishu Singh", style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Dark Mode")
                    Switch(checked = darkTheme, onCheckedChange = onToggleDark)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Hindi Language (हिन्दी)")
                    Switch(checked = isHindi, onCheckedChange = onToggleHindi)
                }
                OutlinedButton(
                    onClick = onOpenSuperAdminReset,
                    modifier = Modifier.fillMaxWidth().testTag("quick_super_admin_reset_button")
                ) {
                    Text("Super Admin Login & Reset Settings")
                }
                OutlinedButton(
                    onClick = onLockSuperAdmin,
                    modifier = Modifier.fillMaxWidth().testTag("lock_super_admin_button")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Lock Super Admin Console")
                }
                OutlinedButton(onClick = onOpenHelp, modifier = Modifier.fillMaxWidth()) {
                    Text("Help & Support")
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } }
    )
}
