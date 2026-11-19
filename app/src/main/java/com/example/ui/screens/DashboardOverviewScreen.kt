package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.ErpGlobalSearch
import com.example.model.AuditLogEntry
import com.example.model.ContentItem
import com.example.model.CourseRecord
import com.example.model.ExamRecord
import com.example.model.FranchiseRecord
import com.example.model.MenuModule
import com.example.model.PaymentRecord
import com.example.model.StaffRecord
import com.example.model.StudentRecord
import com.example.model.WebsiteConnector
import com.example.model.WebsiteProbeResult
import com.example.model.WorkspaceConfig
import com.example.ui.components.ErpGlobalSearchBar
import com.example.ui.components.KpiMetricTile
import com.example.ui.components.QuickAccessActionCard
import com.example.ui.components.StatusPill
import com.example.ui.components.SubPageTabsStrip
import com.example.ui.components.bi
import com.example.ui.components.formatTimestamp
import com.example.ui.theme.MvciAmberAccent
import com.example.ui.theme.MvciCyanAccent
import com.example.ui.theme.MvciEmerald
import com.example.ui.theme.MvciIndigoPrimary
import com.example.ui.theme.MvciRoseError

private data class DashboardKpiSpec(
    val labelEn: String,
    val labelHi: String,
    val value: String,
    val icon: ImageVector,
    val accent: Color,
    val hint: String,
    val targetModule: MenuModule,
    val targetSubPage: String
)

@Composable
fun DashboardOverviewScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    onNavigate: (MenuModule, String) -> Unit,
    dateFilter: String,
    onDateFilterChange: (String) -> Unit,
    config: WorkspaceConfig?,
    students: List<StudentRecord>,
    franchises: List<FranchiseRecord>,
    staff: List<StaffRecord>,
    courses: List<CourseRecord>,
    payments: List<PaymentRecord>,
    exams: List<ExamRecord>,
    connectors: List<WebsiteConnector>,
    contents: List<ContentItem>,
    auditLogs: List<AuditLogEntry>,
    probeResult: WebsiteProbeResult?,
    isProbing: Boolean,
    onProbeMainWebsite: () -> Unit,
    onOpenFocusedAdmin: () -> Unit = {},
    isHindi: Boolean
) {
    var dashboardSearchQuery by rememberSaveable { mutableStateOf("") }
    val searchMatches = remember(
        dashboardSearchQuery,
        config,
        students,
        staff,
        auditLogs,
        franchises,
        courses,
        connectors
    ) {
        ErpGlobalSearch.search(
            query = dashboardSearchQuery,
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

    Column(modifier = Modifier.fillMaxSize()) {
        ErpGlobalSearchBar(
            query = dashboardSearchQuery,
            onQueryChange = { dashboardSearchQuery = it },
            matches = searchMatches,
            onSelectMatch = { match ->
                dashboardSearchQuery = ""
                onNavigate(match.module, match.subPage)
            },
            placeholder = bi(
                "Search user records, audit logs, ERP settings...",
                "उपयोगकर्ता रिकॉर्ड, ऑडिट लॉग, सेटिंग्स खोजें...",
                isHindi
            ),
            searchInputTag = "dashboard_global_search_input",
            resultsTag = "dashboard_global_search_results",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
        )
        SubPageTabsStrip(
            subPages = MenuModule.DASHBOARD.subMenus,
            selectedSubPage = subPage,
            onSelect = onSubPageChange
        )

        when (subPage) {
            "Overview" -> OverviewContent(
                dateFilter = dateFilter,
                onDateFilterChange = onDateFilterChange,
                onNavigate = onNavigate,
                config = config,
                students = students,
                franchises = franchises,
                staff = staff,
                courses = courses,
                payments = payments,
                exams = exams,
                connectors = connectors,
                contents = contents,
                probeResult = probeResult,
                isProbing = isProbing,
                onProbeMainWebsite = onProbeMainWebsite,
                onOpenFocusedAdmin = onOpenFocusedAdmin,
                isHindi = isHindi
            )
            "Analytics", "Institute Performance" -> AnalyticsPerformanceContent(
                students = students,
                courses = courses,
                franchises = franchises,
                payments = payments,
                isHindi = isHindi
            )
            "Branch Performance" -> BranchPerformanceContent(franchises = franchises, students = students, onNavigate = onNavigate)
            "Financial Overview" -> FinancialOverviewContent(payments = payments, students = students, onNavigate = onNavigate)
            "Recent Activities" -> RecentActivitiesContent(auditLogs = auditLogs)
            else -> OverviewContent(
                dateFilter, onDateFilterChange, onNavigate, config, students, franchises, staff, courses, payments, exams, connectors, contents, probeResult, isProbing, onProbeMainWebsite, onOpenFocusedAdmin, isHindi
            )
        }
    }
}

@Composable
private fun OverviewContent(
    dateFilter: String,
    onDateFilterChange: (String) -> Unit,
    onNavigate: (MenuModule, String) -> Unit,
    config: WorkspaceConfig?,
    students: List<StudentRecord>,
    franchises: List<FranchiseRecord>,
    staff: List<StaffRecord>,
    courses: List<CourseRecord>,
    payments: List<PaymentRecord>,
    exams: List<ExamRecord>,
    connectors: List<WebsiteConnector>,
    contents: List<ContentItem>,
    probeResult: WebsiteProbeResult?,
    isProbing: Boolean,
    onProbeMainWebsite: () -> Unit,
    onOpenFocusedAdmin: () -> Unit,
    isHindi: Boolean
) {
    val activeStudents = students.count { it.status == "ACTIVE" }
    val inactiveStudents = students.count { it.status == "INACTIVE" }
    val totalParents = staff.count { it.roleType == "PARENT" } + students.count { it.parentLinked }
    val totalFaculty = staff.count { it.roleType == "FACULTY" }
    val totalAdmins = staff.count { it.roleType == "ADMIN" || it.roleType == "SUPER_ADMIN" || it.roleType == "FRANCHISE_ADMIN" } + 1
    val activeFranchises = franchises.count { it.status == "ACTIVE" }
    val activeBatches = courses.sumOf { it.activeBatches }.toInt()
    val todayAdmissions = students.count { it.status == "PENDING" }
    val avgAttendance = if (students.isEmpty()) 0 else (students.sumOf { it.attendancePct } / students.size).toInt()
    val verifiedPayments = payments.filter { it.status == "VERIFIED" && it.category != "REFUND" && it.category != "EXPENSE" }
    val totalFeeCollected = verifiedPayments.sumOf { it.amount }
    val todayFee = verifiedPayments.take(3).sumOf { it.amount }
    val pendingFees = students.sumOf { (it.totalFee - it.paidFee).coerceAtLeast(0L) }
    val publishedResults = exams.count { it.status == "PUBLISHED" }
    val certsIssued = exams.sumOf { it.certificatesIssued }
    val connectedSites = connectors.count { it.status == "CONNECTED" }
    val failedConnectors = connectors.count { it.status == "ERROR" }
    val backupsCount = contents.count { it.moduleCategory == "BACKUP" }
    val runtime = Runtime.getRuntime()
    val usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxMb = runtime.maxMemory() / (1024 * 1024)

    val kpis = listOf(
        DashboardKpiSpec("Total Students", "कुल छात्र", "${students.size}", Icons.Default.Groups, MvciIndigoPrimary, "Manage", MenuModule.STUDENT_MGMT, "All Students"),
        DashboardKpiSpec("Active Students", "सक्रिय छात्र", "$activeStudents", Icons.Default.CheckCircle, MvciEmerald, "Profiles", MenuModule.STUDENT_MGMT, "Student Profiles"),
        DashboardKpiSpec("Inactive Students", "निष्क्रिय छात्र", "$inactiveStudents", Icons.Default.PersonOff, MvciRoseError, "Status", MenuModule.STUDENT_MGMT, "Student Status"),
        DashboardKpiSpec("Total Parents", "कुल अभिभावक", "$totalParents", Icons.Default.FamilyRestroom, MvciCyanAccent, "Portal", MenuModule.PARENT_PORTAL, "Parent Accounts"),
        DashboardKpiSpec("Total Faculty", "कुल शिक्षक", "$totalFaculty", Icons.Default.School, MvciAmberAccent, "Staff", MenuModule.USER_MGMT, "Faculty"),
        DashboardKpiSpec("Total Admins", "कुल एडमिन", "$totalAdmins", Icons.Default.AdminPanelSettings, MvciIndigoPrimary, "Roles", MenuModule.USER_MGMT, "Admin Users"),
        DashboardKpiSpec("Total Franchises", "कुल फ्रेंचाइजी", "${franchises.size}", Icons.Default.Business, MvciCyanAccent, "Branches", MenuModule.FRANCHISE_MGMT, "Approve Franchise"),
        DashboardKpiSpec("Active Franchises", "सक्रिय फ्रेंचाइजी", "$activeFranchises", Icons.Default.Verified, MvciEmerald, "Active", MenuModule.FRANCHISE_MGMT, "Franchise Reports"),
        DashboardKpiSpec("Total Courses", "कुल पाठ्यक्रम", "${courses.size}", Icons.Default.MenuBook, MvciAmberAccent, "Syllabus", MenuModule.ACADEMIC_MGMT, "Courses"),
        DashboardKpiSpec("Active Batches", "सक्रिय बैच", "$activeBatches", Icons.Default.Class, MvciIndigoPrimary, "Schedule", MenuModule.ACADEMIC_MGMT, "Batches"),
        DashboardKpiSpec("Today's Admissions", "आज के प्रवेश", "$todayAdmissions", Icons.Default.PendingActions, MvciAmberAccent, "Approve", MenuModule.STUDENT_MGMT, "Admission Approval"),
        DashboardKpiSpec("Today's Attendance", "आज की उपस्थिति", "$avgAttendance%", Icons.Default.CalendarToday, MvciEmerald, "Mark", MenuModule.ATTENDANCE_MGMT, "Daily Attendance"),
        DashboardKpiSpec("Today's Fee Collection", "आज का शुल्क", "INR $todayFee", Icons.Default.Payments, MvciEmerald, "Collect", MenuModule.FINANCE_ACCOUNTING, "Student Payments"),
        DashboardKpiSpec("Total Fee Collection", "कुल शुल्क संग्रह", "INR $totalFeeCollected", Icons.Default.AccountBalanceWallet, MvciIndigoPrimary, "Ledger", MenuModule.FINANCE_ACCOUNTING, "Financial Reports"),
        DashboardKpiSpec("Pending Fees", "बकाया शुल्क", "INR $pendingFees", Icons.Default.Assessment, MvciRoseError, "Dues", MenuModule.FINANCE_ACCOUNTING, "Pending Fees"),
        DashboardKpiSpec("Total Examinations", "कुल परीक्षाएं", "${exams.size}", Icons.Default.Quiz, MvciCyanAccent, "Exams", MenuModule.EXAM_MGMT, "Exam Scheduling"),
        DashboardKpiSpec("Published Results", "प्रकाशित परिणाम", "$publishedResults", Icons.Default.AssignmentTurnedIn, MvciEmerald, "Publish", MenuModule.EXAM_MGMT, "Result Publication"),
        DashboardKpiSpec("Certificates Issued", "जारी प्रमाणपत्र", "$certsIssued", Icons.Default.Badge, MvciAmberAccent, "Certs", MenuModule.EXAM_MGMT, "Certificate Settings"),
        DashboardKpiSpec("Website Visitors", "वेबसाइट विज़िटर", if (probeResult?.reachable == true) "Live Track" else "Probe Req.", Icons.Default.Public, MvciCyanAccent, "Site", MenuModule.WEBSITE_MGMT, "Existing Website Settings"),
        DashboardKpiSpec("Connected Websites", "कनेक्टेड वेबसाइट", "$connectedSites/${connectors.size}", Icons.Default.Language, MvciIndigoPrimary, "Connect", MenuModule.WEBSITE_CONNECTOR, "Connected Websites"),
        DashboardKpiSpec("System Health", "सिस्टम स्वास्थ्य", if (failedConnectors == 0) "Healthy" else "$failedConnectors Alert", Icons.Default.HealthAndSafety, if (failedConnectors == 0) MvciEmerald else MvciRoseError, "Monitor", MenuModule.ADVANCED_SETTINGS, "System Diagnostics"),
        DashboardKpiSpec("Database Status", "डेटाबेस स्थिति", "Cloud + MySQL", Icons.Default.CloudDone, MvciEmerald, "DB", MenuModule.DATABASE_BACKUP, "MySQL Database Status"),
        DashboardKpiSpec("Storage Usage", "स्टोरेज उपयोग", "${usedMb}/${maxMb} MB", Icons.Default.Storage, MvciAmberAccent, "Storage", MenuModule.DATABASE_BACKUP, "Storage Management"),
        DashboardKpiSpec("Backup Status", "बैकअप स्थिति", if (backupsCount > 0) "$backupsCount Snapshots" else "Ready", Icons.Default.Backup, MvciCyanAccent, "Backup", MenuModule.DATABASE_BACKUP, "Database Backup")
    )

    val lmsMaterialsCount = contents.count { it.moduleCategory.startsWith("LMS_") }
    val avgSyllabusPct = if (courses.isEmpty()) 0 else (courses.sumOf { it.syllabusProgressPct } / courses.size).toInt()
    val totalFeeScope = (totalFeeCollected + pendingFees).coerceAtLeast(1L)
    val feeCollectionPct = ((totalFeeCollected * 100L) / totalFeeScope).toInt().coerceIn(0, 100)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_overview_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_mvci_hero_banner),
                        contentDescription = "MVCI Ecosystem Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xE60A0F1E), Color(0x990A0F1E))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = config?.instituteName ?: "Maa Vaishno Computer Institute",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = "Director: ${config?.directorName ?: "Rishu Singh"} • ${config?.websiteUrl ?: "https://www.mvci.online"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MvciAmberAccent
                                )
                            }
                            StatusPill(
                                text = when {
                                    isProbing -> "PROBING"
                                    probeResult?.reachable == true -> "ONLINE ${probeResult.latencyMs}ms"
                                    probeResult != null -> "UNREACHABLE"
                                    else -> "LIVE"
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onProbeMainWebsite,
                                enabled = !isProbing,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (isProbing) "Checking mvci.online..." else "Verify mvci.online Live")
                            }
                            Button(
                                onClick = { onNavigate(MenuModule.WEBSITE_CONNECTOR, "Connect New Website") },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(bi("Connect Website", "वेबसाइट जोड़ें", isHindi))
                            }
                            Button(
                                onClick = onOpenFocusedAdmin,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Admin Drawer")
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    bi("Period:", "अवधि:", isHindi),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                listOf("TODAY" to "Today", "7D" to "7 Days", "30D" to "30 Days", "ALL" to "All Time").forEach { (key, label) ->
                    FilterChip(
                        selected = dateFilter == key,
                        onClick = { onDateFilterChange(key) },
                        label = { Text(label) }
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_summary_stats_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = bi("Executive Summary Statistics", "कार्यकारी सारांश आंकड़े", isHindi),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryStatisticProgressCard(
                        title = bi("Students & Attendance", "छात्र और उपस्थिति", isHindi),
                        primaryValue = "$activeStudents / ${students.size}",
                        caption = "$avgAttendance% avg attendance",
                        progress = (avgAttendance / 100f).coerceIn(0f, 1f),
                        accent = MvciIndigoPrimary,
                        icon = Icons.Default.Groups,
                        onClick = { onNavigate(MenuModule.STUDENT_MGMT, "All Students") },
                        modifier = Modifier.weight(1f),
                        testTagName = "summary_card_students"
                    )
                    SummaryStatisticProgressCard(
                        title = bi("LMS Courses & Library", "एलएमएस पाठ्यक्रम", isHindi),
                        primaryValue = "${courses.size} / $lmsMaterialsCount",
                        caption = "$avgSyllabusPct% syllabus • $lmsMaterialsCount files",
                        progress = (avgSyllabusPct / 100f).coerceIn(0f, 1f),
                        accent = MvciCyanAccent,
                        icon = Icons.AutoMirrored.Filled.LibraryBooks,
                        onClick = { onNavigate(MenuModule.LMS_LIBRARY, "Study Materials") },
                        modifier = Modifier.weight(1f),
                        testTagName = "summary_card_lms"
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryStatisticProgressCard(
                        title = bi("Fee Collections", "शुल्क संग्रहण", isHindi),
                        primaryValue = "INR $totalFeeCollected",
                        caption = "Pending: INR $pendingFees",
                        progress = (feeCollectionPct / 100f).coerceIn(0f, 1f),
                        accent = MvciEmerald,
                        icon = Icons.Default.AccountBalanceWallet,
                        onClick = { onNavigate(MenuModule.FINANCE_ACCOUNTING, "Financial Reports") },
                        modifier = Modifier.weight(1f),
                        testTagName = "summary_card_finance"
                    )
                    SummaryStatisticProgressCard(
                        title = bi("Portals & Server", "पोर्टल और सर्वर", isHindi),
                        primaryValue = "$connectedSites / ${connectors.size}",
                        caption = if (failedConnectors == 0) "${franchises.size} branches • Healthy" else "$failedConnectors connector alerts",
                        progress = if (connectors.isEmpty()) 1f else (connectedSites.toFloat() / connectors.size.toFloat()).coerceIn(0f, 1f),
                        accent = if (failedConnectors == 0) MvciAmberAccent else MvciRoseError,
                        icon = Icons.Default.HealthAndSafety,
                        onClick = { onNavigate(MenuModule.WEBSITE_CONNECTOR, "Connected Websites") },
                        modifier = Modifier.weight(1f),
                        testTagName = "summary_card_servers"
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_quick_actions_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = bi("Quick Access Actions", "त्वरित कार्य", isHindi),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAccessActionCard(
                        title = bi("Add Student", "छात्र जोड़ें", isHindi),
                        subtitle = "New Admission",
                        icon = Icons.Default.PersonAdd,
                        accent = MvciIndigoPrimary,
                        onClick = { onNavigate(MenuModule.STUDENT_MGMT, "New Admissions") },
                        modifier = Modifier.weight(1f),
                        testTagName = "dashboard_quick_add_student"
                    )
                    QuickAccessActionCard(
                        title = bi("Publish LMS", "एलएमएस सामग्री", isHindi),
                        subtitle = "PDF, Video, Book",
                        icon = Icons.AutoMirrored.Filled.LibraryBooks,
                        accent = MvciCyanAccent,
                        onClick = { onNavigate(MenuModule.LMS_LIBRARY, "Study Materials") },
                        modifier = Modifier.weight(1f),
                        testTagName = "dashboard_quick_publish_lms"
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAccessActionCard(
                        title = bi("Collect Fee", "शुल्क जमा", isHindi),
                        subtitle = "ERP Receipts",
                        icon = Icons.Default.Payments,
                        accent = MvciEmerald,
                        onClick = { onNavigate(MenuModule.FINANCE_ACCOUNTING, "Student Payments") },
                        modifier = Modifier.weight(1f),
                        testTagName = "dashboard_quick_collect_fee"
                    )
                    QuickAccessActionCard(
                        title = bi("Mark Attendance", "उपस्थिति दर्ज", isHindi),
                        subtitle = "Daily Roll Call",
                        icon = Icons.Default.CalendarToday,
                        accent = MvciAmberAccent,
                        onClick = { onNavigate(MenuModule.ATTENDANCE_MGMT, "Daily Attendance") },
                        modifier = Modifier.weight(1f),
                        testTagName = "dashboard_quick_attendance"
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAccessActionCard(
                        title = bi("Security Audit", "ऑडिट लॉग", isHindi),
                        subtitle = "Immutable Trail",
                        icon = Icons.Default.Security,
                        accent = MvciRoseError,
                        onClick = { onNavigate(MenuModule.SECURITY_PERMISSIONS, "Audit Logs") },
                        modifier = Modifier.weight(1f),
                        testTagName = "dashboard_quick_audit"
                    )
                    QuickAccessActionCard(
                        title = bi("Admin Drawer", "एडमिन ड्रॉअर", isHindi),
                        subtitle = "Focused Workspace",
                        icon = Icons.Default.Dashboard,
                        accent = MvciIndigoPrimary,
                        onClick = onOpenFocusedAdmin,
                        modifier = Modifier.weight(1f),
                        testTagName = "dashboard_quick_admin_drawer"
                    )
                }
            }
        }

        item {
            Text(
                text = bi("All Ecosystem Metrics (24 Live KPIs)", "सभी इकोसिस्टम मेट्रिक्स", isHindi),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        val pairs = kpis.chunked(2)
        items(pairs) { rowKpis ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowKpis.forEach { kpi ->
                    KpiMetricTile(
                        label = bi(kpi.labelEn, kpi.labelHi, isHindi),
                        value = kpi.value,
                        icon = kpi.icon,
                        accent = kpi.accent,
                        hint = kpi.hint,
                        onClick = { onNavigate(kpi.targetModule, kpi.targetSubPage) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowKpis.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item {
            RevenueAndEnrollmentChartCard(
                courses = courses,
                payments = payments,
                isHindi = isHindi
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RevenueAndEnrollmentChartCard(
    courses: List<CourseRecord>,
    payments: List<PaymentRecord>,
    isHindi: Boolean
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = bi("Course Fee & Syllabus Analytics", "पाठ्यक्रम और शुल्क विश्लेषण", isHindi),
                style = MaterialTheme.typography.titleMedium
            )
            if (courses.isEmpty()) {
                Text(
                    "Add courses or payments to render live institute analytics.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val maxFee = (courses.maxOfOrNull { it.feeAmount } ?: 1L).coerceAtLeast(1L)
                val barColor = MvciIndigoPrimary
                val accentColor = MvciAmberAccent
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val count = courses.size.coerceAtMost(6)
                    val slotW = size.width / count.toFloat()
                    val barW = slotW * 0.45f
                    for (i in 0 until count) {
                        val course = courses[i]
                        val ratio = (course.feeAmount.toFloat() / maxFee.toFloat()).coerceIn(0.1f, 1f)
                        val barH = size.height * 0.85f * ratio
                        val left = i * slotW + (slotW - barW) / 2f
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, size.height - barH),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                        val progH = size.height * 0.85f * (course.syllabusProgressPct / 100f).coerceIn(0.05f, 1f)
                        drawRoundRect(
                            color = accentColor,
                            topLeft = Offset(left + barW * 0.6f, size.height - progH),
                            size = Size(barW * 0.35f, progH),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Blue: Course Fee", style = MaterialTheme.typography.labelMedium, color = MvciIndigoPrimary)
                    Text("Amber: Syllabus Progress", style = MaterialTheme.typography.labelMedium, color = MvciAmberAccent)
                    Text("Verified Payments: ${payments.count { it.status == "VERIFIED" }}", style = MaterialTheme.typography.labelMedium, color = MvciEmerald)
                }
            }
        }
    }
}

@Composable
private fun AnalyticsPerformanceContent(
    students: List<StudentRecord>,
    courses: List<CourseRecord>,
    franchises: List<FranchiseRecord>,
    payments: List<PaymentRecord>,
    isHindi: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            RevenueAndEnrollmentChartCard(courses = courses, payments = payments, isHindi = isHindi)
        }
        items(courses) { course ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(course.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        StatusPill(course.status)
                    }
                    Text("${course.code} • ${course.category} • INR ${course.feeAmount}", style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(
                        progress = { (course.syllabusProgressPct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Text("Syllabus ${course.syllabusProgressPct}% • Enrolled: ${students.count { it.courseName == course.title || it.courseName == course.code }} • Franchises: ${franchises.size}", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun BranchPerformanceContent(
    franchises: List<FranchiseRecord>,
    students: List<StudentRecord>,
    onNavigate: (MenuModule, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (franchises.isEmpty()) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("No Franchise Branches Added Yet", style = MaterialTheme.typography.titleMedium)
                        Text("Main Campus has ${students.size} students. Add branches in Franchise Management.", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { onNavigate(MenuModule.FRANCHISE_MGMT, "Add Franchise") }) {
                            Text("Open Franchise Management")
                        }
                    }
                }
            }
        }
        items(franchises) { fr ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${fr.name} (${fr.code})", style = MaterialTheme.typography.titleMedium)
                        StatusPill(fr.status)
                    }
                    Text("${fr.city} • Owner: ${fr.ownerName}", style = MaterialTheme.typography.bodySmall)
                    Text("Students: ${fr.studentCount} • Revenue: INR ${fr.revenueTotal} • Commission: ${fr.commissionPct}%", style = MaterialTheme.typography.labelMedium, color = MvciEmerald)
                }
            }
        }
    }
}

@Composable
private fun FinancialOverviewContent(
    payments: List<PaymentRecord>,
    students: List<StudentRecord>,
    onNavigate: (MenuModule, String) -> Unit
) {
    val income = payments.filter { it.status == "VERIFIED" && it.category != "REFUND" && it.category != "EXPENSE" }.sumOf { it.amount }
    val expenses = payments.filter { it.category == "EXPENSE" || it.category == "REFUND" }.sumOf { it.amount }
    val dues = students.sumOf { (it.totalFee - it.paidFee).coerceAtLeast(0L) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Consolidated Financial Snapshot", style = MaterialTheme.typography.titleMedium)
                    Text("Verified Income: INR $income", style = MaterialTheme.typography.titleLarge, color = MvciEmerald)
                    Text("Expenses & Refunds: INR $expenses • Pending Student Dues: INR $dues", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { onNavigate(MenuModule.FINANCE_ACCOUNTING, "Student Payments") }) {
                        Text("Open Finance & Accounting")
                    }
                }
            }
        }
        items(payments) { pay ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("${pay.studentName} • ${pay.category}", style = MaterialTheme.typography.titleMedium)
                        Text("${pay.receiptNo} • ${pay.paymentMode} • ${pay.branchName}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("INR ${pay.amount}", style = MaterialTheme.typography.titleMedium)
                        StatusPill(pay.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStatisticProgressCard(
    title: String,
    primaryValue: String,
    caption: String,
    progress: Float,
    accent: Color,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTagName: String
) {
    Card(
        onClick = onClick,
        modifier = modifier.testTag(testTagName),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Icon(icon, contentDescription = title, tint = accent, modifier = Modifier.size(18.dp))
            }
            Text(
                text = primaryValue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { progress },
                color = accent,
                trackColor = accent.copy(alpha = 0.16f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
        }
    }
}

@Composable
private fun RecentActivitiesContent(auditLogs: List<AuditLogEntry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(auditLogs) { log ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(log.action, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusPill(log.severity.ifBlank { "INFO" })
                            StatusPill(log.category.ifBlank { log.actorRole })
                        }
                    }
                    Text("${log.moduleName} • Target: ${log.targetId}", style = MaterialTheme.typography.bodySmall)
                    if (log.previousVal.isNotBlank() && log.previousVal != "NONE" && log.previousVal != "UNSPECIFIED") {
                        Text("Previous: ${log.previousVal}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (log.newVal.isNotBlank()) {
                        Text("Change: ${log.newVal}", style = MaterialTheme.typography.labelMedium, color = MvciCyanAccent)
                    }
                    val actor = log.actorUsername.ifBlank { log.actorEmail }.ifBlank { log.actorRole }
                    Text("$actor • ${log.deviceInfo} • ${formatTimestamp(log.createdAt?.toDate()?.time)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
