package com.example.core

import com.example.model.AuditLogEntry
import com.example.model.CourseRecord
import com.example.model.FranchiseRecord
import com.example.model.GlobalSearchMatch
import com.example.model.MenuModule
import com.example.model.StaffRecord
import com.example.model.StudentRecord
import com.example.model.WebsiteConnector
import com.example.model.WorkspaceConfig

object ErpGlobalSearch {

    private data class ErpSettingEntry(
        val id: String,
        val title: String,
        val keywords: String,
        val subtitle: String,
        val module: MenuModule,
        val subPage: String
    )

    private val STATIC_SETTINGS = listOf(
        ErpSettingEntry(
            id = "set_super_admin_creds",
            title = "Super Admin Login ID & Password Reset",
            keywords = "super admin password username credentials login reset rishu@2050",
            subtitle = "System Settings • Update or reset Super Admin login",
            module = MenuModule.SYSTEM_SETTINGS,
            subPage = "General Settings"
        ),
        ErpSettingEntry(
            id = "set_otp_recovery",
            title = "Email OTP Recovery & Webhook",
            keywords = "otp email forgot password recovery webhook rsingh13151@gmail.com 2fa",
            subtitle = "Security & Settings • Recovery email & OTP settings",
            module = MenuModule.SECURITY_PERMISSIONS,
            subPage = "OTP Settings"
        ),
        ErpSettingEntry(
            id = "set_language",
            title = "Language Settings (English / हिन्दी)",
            keywords = "language hindi english locale translation",
            subtitle = "System Settings • Switch UI language",
            module = MenuModule.SYSTEM_SETTINGS,
            subPage = "Language Settings"
        ),
        ErpSettingEntry(
            id = "set_theme",
            title = "Theme Settings (Dark / Light Mode)",
            keywords = "theme dark light mode appearance ui",
            subtitle = "System Settings • Control center theme",
            module = MenuModule.SYSTEM_SETTINGS,
            subPage = "Theme Settings"
        ),
        ErpSettingEntry(
            id = "set_maintenance",
            title = "Global Maintenance Mode",
            keywords = "maintenance mode portal offline website downtime",
            subtitle = "System Settings • Portal availability switch",
            module = MenuModule.SYSTEM_SETTINGS,
            subPage = "Maintenance Mode"
        ),
        ErpSettingEntry(
            id = "set_mysql_db",
            title = "MySQL Database & Backup Schedule",
            keywords = "mysql database godaddy host backup restore snapshot storage",
            subtitle = "Database & Backup • Centralized MySQL bridge",
            module = MenuModule.DATABASE_BACKUP,
            subPage = "Database Configuration"
        ),
        ErpSettingEntry(
            id = "set_audit_logs",
            title = "Immutable Security Audit Logs",
            keywords = "audit log history security trail accountability",
            subtitle = "Security & Permissions • Search & export audit trail",
            module = MenuModule.SECURITY_PERMISSIONS,
            subPage = "Audit Logs"
        )
    )

    fun search(
        query: String,
        config: WorkspaceConfig? = null,
        students: List<StudentRecord> = emptyList(),
        staff: List<StaffRecord> = emptyList(),
        auditLogs: List<AuditLogEntry> = emptyList(),
        franchises: List<FranchiseRecord> = emptyList(),
        courses: List<CourseRecord> = emptyList(),
        connectors: List<WebsiteConnector> = emptyList(),
        limit: Int = 24
    ): List<GlobalSearchMatch> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val matches = mutableListOf<GlobalSearchMatch>()

        // 1. User Records: Students & Staff
        students.forEach { st ->
            if (st.name.lowercase().contains(q) ||
                st.rollNo.lowercase().contains(q) ||
                st.courseName.lowercase().contains(q) ||
                st.email.lowercase().contains(q) ||
                st.branchName.lowercase().contains(q)
            ) {
                matches.add(
                    GlobalSearchMatch(
                        id = "st_${st.id}",
                        title = st.name,
                        subtitle = "${st.rollNo} • ${st.courseName} (${st.branchName}) • ${st.status}",
                        module = MenuModule.STUDENT_MGMT,
                        subPage = "All Students",
                        category = "STUDENT"
                    )
                )
            }
        }

        staff.forEach { member ->
            if (member.name.lowercase().contains(q) ||
                member.email.lowercase().contains(q) ||
                member.roleType.lowercase().contains(q) ||
                member.branchName.lowercase().contains(q) ||
                member.department.lowercase().contains(q)
            ) {
                val targetSub = when (member.roleType) {
                    "SUPER_ADMIN" -> "Super Admin Users"
                    "ADMIN" -> "Admin Users"
                    "FRANCHISE_ADMIN" -> "Franchise Admins"
                    "FACULTY" -> "Faculty"
                    "PARENT" -> "Parents"
                    else -> "Admin Users"
                }
                matches.add(
                    GlobalSearchMatch(
                        id = "usr_${member.id}",
                        title = member.name,
                        subtitle = "${member.roleType} • ${member.email.ifBlank { member.branchName }} (${member.status})",
                        module = MenuModule.USER_MGMT,
                        subPage = targetSub,
                        category = "USER"
                    )
                )
            }
        }

        // 2. Audit Logs
        auditLogs.forEach { log ->
            if (log.action.lowercase().contains(q) ||
                log.moduleName.lowercase().contains(q) ||
                log.actorUsername.lowercase().contains(q) ||
                log.actorEmail.lowercase().contains(q) ||
                log.targetId.lowercase().contains(q) ||
                log.category.lowercase().contains(q) ||
                log.severity.lowercase().contains(q) ||
                log.previousVal.lowercase().contains(q) ||
                log.newVal.lowercase().contains(q)
            ) {
                val actor = log.actorUsername.ifBlank { log.actorEmail }.ifBlank { log.actorRole }
                val diffText = log.newVal.ifBlank { log.moduleName }
                matches.add(
                    GlobalSearchMatch(
                        id = "aud_${log.id}",
                        title = log.action,
                        subtitle = "${log.severity} • ${log.category} • $actor • $diffText",
                        module = MenuModule.SECURITY_PERMISSIONS,
                        subPage = "Audit Logs",
                        category = "AUDIT"
                    )
                )
            }
        }

        // 3. ERP Settings (live config + canonical setting shortcuts)
        if (config != null) {
            val liveConfigFields = listOf(
                Triple("Institute Profile: ${config.instituteName}", "institute director ${config.directorName} ${config.websiteUrl}", MenuModule.INSTITUTE_MGMT to "Institute Profile"),
                Triple("Active Admin User ID: ${config.adminUsername}", "admin username login ${config.adminUsername}", MenuModule.SYSTEM_SETTINGS to "General Settings"),
                Triple("Recovery Email: ${config.recoveryEmail}", "recovery email otp ${config.recoveryEmail}", MenuModule.SYSTEM_SETTINGS to "General Settings"),
                Triple("MySQL Host: ${config.mysqlHost} (${config.mysqlDbName})", "mysql database host ${config.mysqlHost} ${config.mysqlDbName}", MenuModule.DATABASE_BACKUP to "MySQL Database Status")
            )
            liveConfigFields.forEachIndexed { idx, (title, keys, target) ->
                if (title.lowercase().contains(q) || keys.lowercase().contains(q)) {
                    matches.add(
                        GlobalSearchMatch(
                            id = "cfg_$idx",
                            title = title,
                            subtitle = "${target.first.titleEn} • ${target.second}",
                            module = target.first,
                            subPage = target.second,
                            category = "SETTING"
                        )
                    )
                }
            }
        }

        STATIC_SETTINGS.forEach { setting ->
            if (setting.title.lowercase().contains(q) ||
                setting.keywords.contains(q) ||
                setting.subtitle.lowercase().contains(q)
            ) {
                matches.add(
                    GlobalSearchMatch(
                        id = setting.id,
                        title = setting.title,
                        subtitle = setting.subtitle,
                        module = setting.module,
                        subPage = setting.subPage,
                        category = "SETTING"
                    )
                )
            }
        }

        // 4. Franchises, Courses, Connectors, & Menus
        franchises.forEach { fr ->
            if (fr.name.lowercase().contains(q) || fr.city.lowercase().contains(q) || fr.code.lowercase().contains(q)) {
                matches.add(
                    GlobalSearchMatch(
                        id = "fr_${fr.id}",
                        title = fr.name,
                        subtitle = "${fr.code} • ${fr.city} (${fr.status})",
                        module = MenuModule.FRANCHISE_MGMT,
                        subPage = "Approve Franchise",
                        category = "FRANCHISE"
                    )
                )
            }
        }

        courses.forEach { cr ->
            if (cr.title.lowercase().contains(q) || cr.code.lowercase().contains(q) || cr.category.lowercase().contains(q)) {
                matches.add(
                    GlobalSearchMatch(
                        id = "crs_${cr.id}",
                        title = cr.title,
                        subtitle = "${cr.code} • INR ${cr.feeAmount}",
                        module = MenuModule.ACADEMIC_MGMT,
                        subPage = "Courses",
                        category = "COURSE"
                    )
                )
            }
        }

        connectors.forEach { cn ->
            if (cn.name.lowercase().contains(q) || cn.url.lowercase().contains(q)) {
                matches.add(
                    GlobalSearchMatch(
                        id = "cn_${cn.id}",
                        title = cn.name,
                        subtitle = "${cn.url} • ${cn.status}",
                        module = MenuModule.WEBSITE_CONNECTOR,
                        subPage = "Connected Websites",
                        category = "CONNECTOR"
                    )
                )
            }
        }

        MenuModule.entries.forEach { mod ->
            if (mod.titleEn.lowercase().contains(q) || mod.titleHi.contains(q)) {
                matches.add(
                    GlobalSearchMatch(
                        id = "mod_${mod.name}",
                        title = mod.titleEn,
                        subtitle = "Module ${mod.number} • ${mod.subMenus.first()}",
                        module = mod,
                        subPage = mod.subMenus.first(),
                        category = "MENU"
                    )
                )
            }
            mod.subMenus.forEach { sub ->
                if (sub.lowercase().contains(q)) {
                    matches.add(
                        GlobalSearchMatch(
                            id = "sub_${mod.name}_$sub",
                            title = sub,
                            subtitle = mod.titleEn,
                            module = mod,
                            subPage = sub,
                            category = if (mod == MenuModule.SYSTEM_SETTINGS || mod == MenuModule.SECURITY_PERMISSIONS) "SETTING" else "MENU"
                        )
                    )
                }
            }
        }

        return matches.distinctBy { it.id }.take(limit)
    }
}
