package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.example.core.ErpGlobalSearch
import com.example.model.AuditLogEntry
import com.example.model.ContentItem
import com.example.model.CourseRecord
import com.example.model.MenuModule
import com.example.model.PaymentRecord
import com.example.model.StaffRecord
import com.example.model.StudentRecord
import com.example.model.WebsiteConnector
import com.example.model.WorkspaceConfig
import com.example.ui.screens.AdminDashboard
import com.example.ui.screens.AdminDashboardSection
import com.example.ui.screens.DashboardOverviewScreen
import com.example.ui.theme.MvciControlTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AdminDashboardTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun adminDashboard_navigationDrawer_switchesBetweenAllSections() {
        val sampleCourses = listOf(
            CourseRecord(id = "c1", code = "ADCA-01", title = "ADCA Diploma", category = "Diploma", syllabusProgressPct = 70L)
        )
        val sampleContents = listOf(
            ContentItem(id = "m1", title = "Tally Prime Notes PDF", moduleCategory = "LMS_PDF")
        )
        val sampleStudents = listOf(
            StudentRecord(id = "s1", name = "Aarav Sharma", rollNo = "MVCI-101", courseName = "ADCA Diploma", batchName = "Morning-08", branchName = "Main Campus", status = "ACTIVE", totalFee = 14500L, paidFee = 10000L, attendancePct = 92L)
        )
        val sampleConnectors = listOf(
            WebsiteConnector(id = "w1", name = "MVCI Portal", url = "https://www.mvci.online", status = "CONNECTED", healthStatus = "HTTP 200", lastLatencyMs = 85L)
        )

        composeRule.setContent {
            MvciControlTheme {
                AdminDashboard(
                    courses = sampleCourses,
                    contents = sampleContents,
                    students = sampleStudents,
                    connectors = sampleConnectors
                )
            }
        }

        // Default section: LMS Management
        composeRule.onNodeWithTag("admin_topbar_title").assertTextEquals("LMS Management")
        composeRule.onNodeWithTag("lms_lazy_list").performScrollToNode(hasText("ADCA Diploma"))
        composeRule.onNodeWithText("ADCA Diploma").assertIsDisplayed()
        composeRule.onNodeWithTag("lms_lazy_list").performScrollToNode(hasText("Tally Prime Notes PDF"))
        composeRule.onNodeWithText("Tally Prime Notes PDF").assertIsDisplayed()

        // Open drawer and switch to Admin Overview
        composeRule.onNodeWithTag("admin_drawer_toggle").performClick()
        composeRule.onNodeWithTag("drawer_item_overview").performClick()
        composeRule.onNodeWithTag("admin_topbar_title").assertTextEquals("Admin Overview")
        composeRule.onNodeWithTag("admin_overview_lazy_list").performScrollToNode(hasText("Summary Statistics"))
        composeRule.onNodeWithText("Summary Statistics").assertIsDisplayed()

        // Open drawer and switch to Student Records
        composeRule.onNodeWithTag("admin_drawer_toggle").performClick()
        composeRule.onNodeWithTag("drawer_item_student_records").performClick()
        composeRule.onNodeWithTag("admin_topbar_title").assertTextEquals("Student Records")
        composeRule.onNodeWithTag("students_lazy_list").performScrollToNode(hasText("Aarav Sharma"))
        composeRule.onNodeWithText("Aarav Sharma").assertIsDisplayed()

        // Open drawer and switch to Server Status Tools
        composeRule.onNodeWithTag("admin_drawer_toggle").performClick()
        composeRule.onNodeWithTag("drawer_item_server_status").performClick()
        composeRule.onNodeWithTag("admin_topbar_title").assertTextEquals("Server Status Tools")
        composeRule.onNodeWithTag("server_lazy_list").performScrollToNode(hasText("Live Server & Endpoint Diagnostic Probe"))
        composeRule.onNodeWithText("Live Server & Endpoint Diagnostic Probe").assertIsDisplayed()
        composeRule.onNodeWithTag("server_lazy_list").performScrollToNode(hasText("MVCI Portal"))
        composeRule.onNodeWithText("MVCI Portal").assertIsDisplayed()
    }

    @Test
    fun adminDashboard_overview_rendersSummaryCardsAndQuickActions() {
        var probed = false
        var backupCreated = false

        composeRule.setContent {
            MvciControlTheme {
                AdminDashboard(
                    initialSection = AdminDashboardSection.ERP_OVERVIEW,
                    students = listOf(
                        StudentRecord(id = "s1", name = "Priya Verma", rollNo = "MVCI-201", courseName = "DCA", batchName = "B1", branchName = "Main", status = "ACTIVE", attendancePct = 95L)
                    ),
                    courses = listOf(
                        CourseRecord(id = "c1", code = "DCA-01", title = "DCA", category = "Diploma", syllabusProgressPct = 80L)
                    ),
                    payments = listOf(
                        PaymentRecord(id = "p1", studentName = "Priya Verma", branchName = "Main", category = "COURSE_FEE", amount = 7500L, receiptNo = "R-01", status = "VERIFIED")
                    ),
                    connectors = listOf(
                        WebsiteConnector(id = "w1", name = "MVCI Portal", url = "https://www.mvci.online", status = "CONNECTED")
                    ),
                    onProbeUrl = { probed = true },
                    onCreateBackupSnapshot = { backupCreated = true }
                )
            }
        }

        composeRule.onNodeWithTag("admin_overview_lazy_list").performScrollToNode(hasText("INR 7500"))
        composeRule.onNodeWithText("INR 7500").assertIsDisplayed()

        composeRule.onNodeWithTag("admin_overview_lazy_list").performScrollToNode(hasTestTag("admin_quick_backup"))
        composeRule.onNodeWithTag("admin_quick_backup").performClick()
        assertTrue(backupCreated)

        composeRule.onNodeWithTag("admin_overview_lazy_list").performScrollToNode(hasTestTag("admin_quick_probe_server"))
        composeRule.onNodeWithTag("admin_quick_probe_server").performClick()
        assertTrue(probed)
    }

    @Test
    fun dashboardOverviewScreen_summaryStatsAndQuickActionsNavigateCorrectly() {
        var lastNav: Pair<MenuModule, String>? = null
        var openedAdminDrawer = false

        composeRule.setContent {
            MvciControlTheme {
                DashboardOverviewScreen(
                    subPage = "Overview",
                    onSubPageChange = {},
                    onNavigate = { mod, sub -> lastNav = mod to sub },
                    dateFilter = "ALL",
                    onDateFilterChange = {},
                    config = WorkspaceConfig(),
                    students = listOf(
                        StudentRecord(id = "s1", name = "Rohan", rollNo = "MVCI-01", courseName = "ADCA", batchName = "M1", branchName = "Main", status = "ACTIVE", totalFee = 10000L, paidFee = 6000L, attendancePct = 90L)
                    ),
                    franchises = emptyList(),
                    staff = emptyList(),
                    courses = listOf(
                        CourseRecord(id = "c1", code = "ADCA", title = "ADCA", category = "Diploma", syllabusProgressPct = 65L)
                    ),
                    payments = listOf(
                        PaymentRecord(id = "p1", studentName = "Rohan", branchName = "Main", category = "COURSE_FEE", amount = 6000L, receiptNo = "REC-1", status = "VERIFIED")
                    ),
                    exams = emptyList(),
                    connectors = listOf(
                        WebsiteConnector(id = "w1", name = "MVCI Portal", url = "https://www.mvci.online", status = "CONNECTED")
                    ),
                    contents = listOf(
                        ContentItem(id = "m1", title = "Syllabus PDF", moduleCategory = "LMS_PDF")
                    ),
                    auditLogs = emptyList(),
                    probeResult = null,
                    isProbing = false,
                    onProbeMainWebsite = {},
                    onOpenFocusedAdmin = { openedAdminDrawer = true },
                    isHindi = false
                )
            }
        }

        composeRule.onNodeWithTag("dashboard_overview_list").performScrollToNode(hasTestTag("summary_card_finance"))
        composeRule.onNodeWithTag("summary_card_finance").performClick()
        assertEquals(MenuModule.FINANCE_ACCOUNTING to "Financial Reports", lastNav)

        composeRule.onNodeWithTag("dashboard_overview_list").performScrollToNode(hasTestTag("dashboard_quick_audit"))
        composeRule.onNodeWithTag("dashboard_quick_audit").performClick()
        assertEquals(MenuModule.SECURITY_PERMISSIONS to "Audit Logs", lastNav)

        composeRule.onNodeWithTag("dashboard_overview_list").performScrollToNode(hasTestTag("dashboard_quick_admin_drawer"))
        composeRule.onNodeWithTag("dashboard_quick_admin_drawer").performClick()
        assertTrue(openedAdminDrawer)
    }

    @Test
    fun erpGlobalSearch_findsUserRecordsAuditLogsAndErpSettings() {
        val staff = listOf(
            StaffRecord(id = "u1", name = "Anjali Tiwari", email = "anjali@mvci.online", roleType = "FACULTY", branchName = "Lucknow", department = "CS")
        )
        val students = listOf(
            StudentRecord(id = "s1", name = "Vikram Yadav", rollNo = "MVCI-901", courseName = "ADCA", branchName = "Main")
        )
        val logs = listOf(
            AuditLogEntry(id = "a1", action = "OTP_PASSWORD_RESET", moduleName = "Security", actorUsername = "Rishu@2050", category = "SECURITY", severity = "CRITICAL", newVal = "Password updated")
        )
        val config = WorkspaceConfig(adminUsername = "Rishu@2050", recoveryEmail = "rsingh13151@gmail.com")

        val staffMatches = ErpGlobalSearch.search("anjali", config = config, staff = staff)
        assertTrue(staffMatches.any { it.category == "USER" && it.module == MenuModule.USER_MGMT })

        val studentMatches = ErpGlobalSearch.search("MVCI-901", config = config, students = students)
        assertTrue(studentMatches.any { it.category == "STUDENT" && it.title == "Vikram Yadav" })

        val auditMatches = ErpGlobalSearch.search("OTP_PASSWORD_RESET", config = config, auditLogs = logs)
        assertTrue(auditMatches.any { it.category == "AUDIT" && it.module == MenuModule.SECURITY_PERMISSIONS && it.subPage == "Audit Logs" })

        val settingMatches = ErpGlobalSearch.search("maintenance", config = config)
        assertTrue(settingMatches.any { it.category == "SETTING" && it.module == MenuModule.SYSTEM_SETTINGS })
    }

    @Test
    fun adminDashboard_topGlobalSearchBar_findsUsersAuditLogsAndSettings() {
        var navigatedErpTarget: Pair<MenuModule, String>? = null
        val staff = listOf(
            StaffRecord(id = "stf1", name = "Anjali Tiwari", email = "anjali@mvci.online", roleType = "FACULTY", branchName = "Main", status = "ACTIVE")
        )
        val students = listOf(
            StudentRecord(id = "stu1", name = "Karan Patel", rollNo = "MVCI-505", courseName = "O Level", branchName = "Main", status = "ACTIVE")
        )
        val logs = listOf(
            AuditLogEntry(id = "aud1", action = "TOGGLE_MAINTENANCE_MODE", moduleName = "System Settings", actorUsername = "Rishu@2050", category = "SETTINGS", severity = "WARNING", newVal = "Enabled")
        )

        composeRule.setContent {
            MvciControlTheme {
                AdminDashboard(
                    config = WorkspaceConfig(),
                    staff = staff,
                    students = students,
                    auditLogs = logs,
                    onNavigateToErpModule = { mod, sub -> navigatedErpTarget = mod to sub }
                )
            }
        }

        // 1. Find audit log & navigate via callback
        composeRule.onNodeWithTag("admin_global_search_input").performTextInput("TOGGLE_MAINTENANCE")
        composeRule.onNodeWithTag("admin_global_search_results").assertIsDisplayed()
        composeRule.onNodeWithTag("admin_global_search_results_item_aud_aud1").performClick()
        assertEquals(MenuModule.SECURITY_PERMISSIONS to "Audit Logs", navigatedErpTarget)

        // 2. Find user record & navigate via callback
        composeRule.onNodeWithTag("admin_global_search_input").performTextReplacement("Anjali")
        composeRule.onNodeWithTag("admin_global_search_results_item_usr_stf1").performClick()
        assertEquals(MenuModule.USER_MGMT to "Faculty", navigatedErpTarget)

        // 3. Find ERP setting & navigate via callback
        composeRule.onNodeWithTag("admin_global_search_input").performTextReplacement("Language")
        composeRule.onNodeWithTag("admin_global_search_results_item_set_language").performClick()
        assertEquals(MenuModule.SYSTEM_SETTINGS to "Language Settings", navigatedErpTarget)

        // 4. Find student & switch drawer section locally
        composeRule.onNodeWithTag("admin_global_search_input").performTextReplacement("Karan")
        composeRule.onNodeWithTag("admin_global_search_results_item_st_stu1").performClick()
        composeRule.onNodeWithTag("admin_topbar_title").assertTextEquals("Student Records")
    }

    @Test
    fun dashboardOverviewScreen_topGlobalSearchBar_navigatesToUsersAuditLogsAndSettings() {
        var lastNav: Pair<MenuModule, String>? = null
        val staff = listOf(
            StaffRecord(id = "u42", name = "Meera Verma", email = "meera@mvci.online", roleType = "ADMIN", branchName = "North", status = "ACTIVE")
        )
        val logs = listOf(
            AuditLogEntry(id = "log99", action = "RESET_SUPER_ADMIN_CREDENTIALS", moduleName = "Security", actorUsername = "Rishu@2050", category = "SECURITY", severity = "CRITICAL", newVal = "Reset")
        )

        composeRule.setContent {
            MvciControlTheme {
                DashboardOverviewScreen(
                    subPage = "Overview",
                    onSubPageChange = {},
                    onNavigate = { mod, sub -> lastNav = mod to sub },
                    dateFilter = "ALL",
                    onDateFilterChange = {},
                    config = WorkspaceConfig(),
                    students = emptyList(),
                    franchises = emptyList(),
                    staff = staff,
                    courses = emptyList(),
                    payments = emptyList(),
                    exams = emptyList(),
                    connectors = emptyList(),
                    contents = emptyList(),
                    auditLogs = logs,
                    probeResult = null,
                    isProbing = false,
                    onProbeMainWebsite = {},
                    isHindi = false
                )
            }
        }

        // 1. User record search
        composeRule.onNodeWithTag("dashboard_global_search_input").performTextInput("Meera")
        composeRule.onNodeWithTag("dashboard_global_search_results_item_usr_u42").performClick()
        assertEquals(MenuModule.USER_MGMT to "Admin Users", lastNav)

        // 2. Audit log search
        composeRule.onNodeWithTag("dashboard_global_search_input").performTextReplacement("RESET_SUPER_ADMIN")
        composeRule.onNodeWithTag("dashboard_global_search_results_item_aud_log99").performClick()
        assertEquals(MenuModule.SECURITY_PERMISSIONS to "Audit Logs", lastNav)

        // 3. ERP Setting search
        composeRule.onNodeWithTag("dashboard_global_search_input").performTextReplacement("MySQL")
        composeRule.onNodeWithTag("dashboard_global_search_results_item_set_mysql_db").performClick()
        assertEquals(MenuModule.DATABASE_BACKUP to "Database Configuration", lastNav)
    }
}
