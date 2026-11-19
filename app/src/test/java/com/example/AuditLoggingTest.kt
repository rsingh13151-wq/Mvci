package com.example

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.core.AuditSanitizer
import com.example.model.AuditLogEntry
import com.example.model.WorkspaceConfig
import com.example.ui.screens.AuditLogExplorerSection
import com.example.ui.screens.SystemSettingsScreen
import com.example.ui.theme.MvciControlTheme
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuditLoggingTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun auditSanitizer_redactsSecretsAndFormatsSettingsDiff() {
        val previous = mapOf(
            "adminUsername" to "Rishu@2050",
            "adminPasswordHash" to "001ea4e661e22e4d7ac2a2c46c6a5989d263afad3ac79672a184ec24dd1149d8",
            "adminPasswordSalt" to "mvci_v1_salt_2050",
            "maintenanceMode" to false,
            "languageCode" to "en"
        )
        val updates = mapOf<String, Any>(
            "adminUsername" to "Rishu@2099",
            "adminPasswordHash" to "9999deadbeef",
            "adminPasswordSalt" to "new_salt_888",
            "maintenanceMode" to true
        )

        val (prevDiff, nextDiff) = AuditSanitizer.buildSettingsDiff(previous, updates)

        assertTrue(prevDiff.contains("adminUsername=Rishu@2050"))
        assertTrue(prevDiff.contains("adminPasswordHash=[REDACTED]"))
        assertTrue(prevDiff.contains("adminPasswordSalt=[REDACTED]"))
        assertTrue(prevDiff.contains("maintenanceMode=false"))
        assertFalse(prevDiff.contains("001ea4e"))
        assertFalse(prevDiff.contains("mvci_v1_salt_2050"))

        assertTrue(nextDiff.contains("adminUsername=Rishu@2099"))
        assertTrue(nextDiff.contains("adminPasswordHash=[REDACTED]"))
        assertTrue(nextDiff.contains("adminPasswordSalt=[REDACTED]"))
        assertTrue(nextDiff.contains("maintenanceMode=true"))
        assertFalse(nextDiff.contains("9999deadbeef"))
        assertFalse(nextDiff.contains("new_salt_888"))

        val freeSanitized = AuditSanitizer.sanitizeFreeText("user=Rishu otp=482910 token=abc123xyz")
        assertTrue(freeSanitized.contains("otp=[REDACTED]"))
        assertTrue(freeSanitized.contains("token=[REDACTED]"))
        assertFalse(freeSanitized.contains("482910"))
        assertFalse(freeSanitized.contains("abc123xyz"))
    }

    @Test
    fun auditSanitizer_classifiesCategoryAndSeverity() {
        assertEquals("SECURITY", AuditSanitizer.classifyCategory("OTP_PASSWORD_RESET", "System Settings"))
        assertEquals("CRITICAL", AuditSanitizer.classifySeverity("OTP_PASSWORD_RESET"))

        assertEquals("SECURITY", AuditSanitizer.classifyCategory("SUPER_ADMIN_LOGIN_SUCCESS", "Security and Permissions"))
        assertEquals("INFO", AuditSanitizer.classifySeverity("SUPER_ADMIN_LOGIN_SUCCESS"))

        assertEquals("SECURITY", AuditSanitizer.classifyCategory("SUPER_ADMIN_CONSOLE_LOCKED", "Security and Permissions"))
        assertEquals("WARNING", AuditSanitizer.classifySeverity("SUPER_ADMIN_CONSOLE_LOCKED"))

        assertEquals("SETTINGS", AuditSanitizer.classifyCategory("CRITICAL_SYSTEM_SETTING", "System Settings"))
        assertEquals("WARNING", AuditSanitizer.classifySeverity("CRITICAL_SYSTEM_SETTING"))

        assertEquals("CONNECTOR", AuditSanitizer.classifyCategory("SYNC_PROBE_FAIL", "Universal Website Connector"))
        assertEquals("CRITICAL", AuditSanitizer.classifySeverity("SYNC_PROBE_FAIL"))
    }

    @Test
    fun auditLogExplorerSection_filtersByCategorySeveritySearchAndExports() {
        val sampleLogs = listOf(
            AuditLogEntry(
                id = "aud_1",
                actorEmail = "rsingh13151@gmail.com",
                actorUsername = "Rishu@2050",
                actorRole = "SUPER_ADMIN",
                action = "OTP_PASSWORD_RESET",
                moduleName = "Security and Permissions",
                targetId = "uid_1",
                category = "SECURITY",
                severity = "CRITICAL",
                previousVal = "adminPasswordHash=[REDACTED]",
                newVal = "adminPasswordHash=[REDACTED]",
                deviceInfo = "Pixel",
                createdAt = Timestamp.now()
            ),
            AuditLogEntry(
                id = "aud_2",
                actorEmail = "rsingh13151@gmail.com",
                actorUsername = "Rishu@2050",
                actorRole = "SUPER_ADMIN",
                action = "SET_LANGUAGE",
                moduleName = "System Settings",
                targetId = "uid_1",
                category = "SETTINGS",
                severity = "WARNING",
                previousVal = "languageCode=en",
                newVal = "languageCode=hi",
                deviceInfo = "Pixel",
                createdAt = Timestamp.now()
            )
        )
        var exportClicked = false

        composeRule.setContent {
            MvciControlTheme {
                AuditLogExplorerSection(
                    auditLogs = sampleLogs,
                    subPageTitle = "Audit Logs",
                    onExportLogs = { exportClicked = true }
                )
            }
        }

        // Initially both entries exist when scrolled into view
        composeRule.onNodeWithTag("audit_logs_list").performScrollToNode(hasTestTag("audit_log_card_aud_1"))
        composeRule.onNodeWithTag("audit_log_card_aud_1").assertExists()
        composeRule.onNodeWithTag("audit_logs_list").performScrollToNode(hasTestTag("audit_log_card_aud_2"))
        composeRule.onNodeWithTag("audit_log_card_aud_2").assertExists()

        // Scroll back to header and filter to SETTINGS only
        composeRule.onNodeWithTag("audit_logs_list").performScrollToIndex(0)
        composeRule.onNodeWithTag("audit_filter_SETTINGS").performScrollTo().performClick()
        composeRule.onAllNodesWithTag("audit_log_card_aud_1").assertCountEquals(0)
        composeRule.onNodeWithTag("audit_logs_list").performScrollToNode(hasTestTag("audit_log_card_aud_2"))
        composeRule.onNodeWithTag("audit_log_card_aud_2").assertExists()

        // Filter to CRITICAL only
        composeRule.onNodeWithTag("audit_logs_list").performScrollToIndex(0)
        composeRule.onNodeWithTag("audit_filter_CRITICAL").performScrollTo().performClick()
        composeRule.onNodeWithTag("audit_logs_list").performScrollToNode(hasTestTag("audit_log_card_aud_1"))
        composeRule.onNodeWithTag("audit_log_card_aud_1").assertExists()
        composeRule.onAllNodesWithTag("audit_log_card_aud_2").assertCountEquals(0)

        // Reset to ALL and search for languageCode
        composeRule.onNodeWithTag("audit_logs_list").performScrollToIndex(0)
        composeRule.onNodeWithTag("audit_filter_ALL").performScrollTo().performClick()
        composeRule.onNodeWithTag("audit_search_input").performTextInput("languageCode")
        composeRule.onAllNodesWithTag("audit_log_card_aud_1").assertCountEquals(0)
        composeRule.onNodeWithTag("audit_logs_list").performScrollToNode(hasTestTag("audit_log_card_aud_2"))
        composeRule.onNodeWithTag("audit_log_card_aud_2").assertExists()

        // Scroll back to top and trigger export
        composeRule.onNodeWithTag("audit_logs_list").performScrollToIndex(0)
        composeRule.onNodeWithTag("audit_export_button").performClick()
        assertTrue(exportClicked)
    }

    @Test
    fun systemSettingsScreen_systemLogsTab_rendersAuditExplorer() {
        val logEntry = AuditLogEntry(
            id = "aud_sys_1",
            actorUsername = "Rishu@2050",
            actorRole = "SUPER_ADMIN",
            action = "CRITICAL_SYSTEM_SETTING",
            moduleName = "System Settings",
            targetId = "ws_1",
            category = "SETTINGS",
            severity = "WARNING",
            previousVal = "maintenanceMode=false",
            newVal = "maintenanceMode=true",
            createdAt = Timestamp.now()
        )

        composeRule.setContent {
            MvciControlTheme {
                SystemSettingsScreen(
                    subPage = "System Logs",
                    onSubPageChange = {},
                    config = WorkspaceConfig(),
                    auditLogs = listOf(logEntry),
                    onUpdateWorkspace = { _, _, _ -> },
                    onUpdateSuperAdminCreds = { _, _, _ -> },
                    onResetSuperAdminCreds = {}
                )
            }
        }

        composeRule.onNodeWithText("Immutable Security Audit Ledger").assertIsDisplayed()
        composeRule.onNodeWithTag("audit_logs_list").performScrollToNode(hasTestTag("audit_log_card_aud_sys_1"))
        composeRule.onNodeWithTag("audit_log_card_aud_sys_1").assertExists()
    }
}
