package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class WorkspaceConfig(
    @DocumentId val id: String = "",
    val userId: String = "",
    val instituteName: String = "Maa Vaishno Computer Institute",
    val websiteUrl: String = "https://www.mvci.online",
    val directorName: String = "Rishu Singh",
    val mysqlHost: String = "mysql.mvci.online",
    val mysqlDbName: String = "mvci_central_erp",
    val hostingProvider: String = "GoDaddy Shared/VPS Compatible",
    val languageCode: String = "en",
    val darkTheme: Boolean = true,
    val maintenanceMode: Boolean = false,
    val autoBackupSchedule: String = "DAILY_02_00",
    val otpRequired: Boolean = true,
    val adminUsername: String = "Rishu@2050",
    val adminPasswordSalt: String = "mvci_v1_salt_2050",
    val adminPasswordHash: String = "001ea4e661e22e4d7ac2a2c46c6a5989d263afad3ac79672a184ec24dd1149d8",
    val recoveryEmail: String = "rsingh13151@gmail.com",
    val otpWebhookUrl: String = "",
    val disabledModules: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StudentRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val rollNo: String = "",
    val email: String = "",
    val phone: String = "",
    val courseName: String = "",
    val batchName: String = "",
    val branchName: String = "",
    val parentName: String = "",
    val parentPhone: String = "",
    val parentLinked: Boolean = false,
    val status: String = "ACTIVE",
    val totalFee: Long = 0L,
    val paidFee: Long = 0L,
    val attendancePct: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class FranchiseRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val name: String = "",
    val city: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val status: String = "ACTIVE",
    val studentCount: Long = 0L,
    val revenueTotal: Long = 0L,
    val commissionPct: Long = 15L,
    val loginProvisioned: Boolean = false,
    val brandingTitle: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class StaffRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val roleType: String = "FACULTY",
    val customRoleName: String = "",
    val branchName: String = "",
    val email: String = "",
    val phone: String = "",
    val department: String = "",
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CourseRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val code: String = "",
    val title: String = "",
    val category: String = "",
    val durationMonths: Long = 12L,
    val feeAmount: Long = 0L,
    val activeBatches: Long = 1L,
    val syllabusProgressPct: Long = 0L,
    val facultyName: String = "",
    val batchTiming: String = "",
    val status: String = "ACTIVE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WebsiteConnector(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val url: String = "",
    val siteType: String = "Institute Portal",
    val description: String = "",
    val integrationMethod: String = "REST API",
    val apiBaseUrl: String = "",
    val maskedApiKey: String = "",
    val authMethod: String = "Secure API Token",
    val webhookUrl: String = "",
    val syncDirection: String = "TWO_WAY",
    val syncFrequency: String = "AUTOMATIC",
    val conflictPolicy: String = "MERGE_APPROVED",
    val status: String = "PENDING",
    val healthStatus: String = "UNVERIFIED",
    val lastLatencyMs: Long = 0L,
    val recordsImported: Long = 0L,
    val recordsUpdated: Long = 0L,
    val failedRecords: Long = 0L,
    val fieldMappings: String = "",
    val lastSyncNote: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class RoleDefinition(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val branchScope: String = "All Branches",
    val permissionsMask: String = "VIEW",
    val moduleScope: String = "Dashboard",
    val customRole: Boolean = true,
    val assignedCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class WorkflowRule(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val triggerEvent: String = "",
    val actionSummary: String = "",
    val enabled: Boolean = true,
    val retryLimit: Long = 3L,
    val preventDuplicates: Boolean = true,
    val executionCount: Long = 0L,
    val failureCount: Long = 0L,
    val lastRunStatus: String = "READY",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class PaymentRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentName: String = "",
    val branchName: String = "",
    val category: String = "COURSE_FEE",
    val amount: Long = 0L,
    val paymentMode: String = "UPI",
    val receiptNo: String = "",
    val status: String = "VERIFIED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ExamRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val courseName: String = "",
    val examType: String = "ONLINE",
    val scheduledDate: String = "",
    val questionCount: Long = 0L,
    val certificatesIssued: Long = 0L,
    val status: String = "SCHEDULED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class ContentItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val moduleCategory: String = "LMS_PDF",
    val description: String = "",
    val urlOrMeta: String = "",
    val downloadable: Boolean = true,
    val status: String = "PUBLISHED",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class AuditLogEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actorEmail: String = "",
    val actorUsername: String = "",
    val actorRole: String = "SUPER_ADMIN",
    val action: String = "",
    val moduleName: String = "",
    val targetId: String = "",
    val category: String = "SYSTEM",
    val severity: String = "INFO",
    val previousVal: String = "",
    val newVal: String = "",
    val deviceInfo: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class SystemNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val channel: String = "SYSTEM",
    val severity: String = "INFO",
    val unread: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class WebsiteProbeResult(
    val targetUrl: String,
    val reachable: Boolean,
    val httpCode: Int,
    val sslValid: Boolean,
    val tlsProtocol: String,
    val latencyMs: Long,
    val serverHeader: String,
    val contentType: String,
    val message: String,
    val checkedAtMillis: Long = System.currentTimeMillis()
)

data class GlobalSearchMatch(
    val id: String,
    val title: String,
    val subtitle: String,
    val module: MenuModule,
    val subPage: String,
    val category: String = "MENU"
)

enum class MenuModule(
    val number: Int,
    val titleEn: String,
    val titleHi: String,
    val subMenus: List<String>
) {
    DASHBOARD(
        1, "Dashboard", "डैशबोर्ड",
        listOf("Overview", "Analytics", "Institute Performance", "Branch Performance", "Financial Overview", "Recent Activities")
    ),
    WEBSITE_MGMT(
        2, "Website Management", "वेबसाइट प्रबंधन",
        listOf("Existing Website Settings", "Website Pages", "Homepage Builder", "Header and Footer", "Menus and Navigation", "Website Banners", "Gallery Management", "Notices and Announcements", "Contact Information", "SEO Settings", "Domain Settings", "Website Maintenance", "Website Backup")
    ),
    WEBSITE_CONNECTOR(
        3, "Universal Website Connector", "यूनिवर्सल वेबसाइट कनेक्टर",
        listOf("Connect New Website", "Connected Websites", "Website Integration Settings", "API Connections", "Webhook Management", "Data Synchronization", "Import Website Data", "Export Website Data", "Field Mapping", "Connection Logs", "Integration Health", "Disconnect Website")
    ),
    INSTITUTE_MGMT(
        4, "Institute Management", "संस्थान प्रबंधन",
        listOf("Institute Profile", "Institute Logo", "Branch Management", "Address Settings", "Contact Settings", "Director Profile", "Affiliation Details", "Certificates and Accreditations", "Institute Documents")
    ),
    FRANCHISE_MGMT(
        5, "Franchise Management", "फ्रेंचाइजी प्रबंधन",
        listOf("Add Franchise", "Edit Franchise", "Delete Franchise", "Approve Franchise", "Suspend Franchise", "Franchise Login", "Franchise Permissions", "Franchise Branding", "Franchise Fees", "Franchise Reports", "Franchise Data Management", "Franchise Activity Logs")
    ),
    USER_MGMT(
        6, "User Management", "उपयोगकर्ता प्रबंधन",
        listOf("Super Admin Users", "Admin Users", "Franchise Admins", "Faculty", "Students", "Parents", "User Registration", "User Approval", "User Suspension", "Password Management", "Login History", "User Permissions")
    ),
    ACADEMIC_MGMT(
        7, "Academic Management", "शैक्षणिक प्रबंधन",
        listOf("Courses", "Course Categories", "Subjects", "Syllabus", "Chapters", "Batches", "Batch Timings", "Faculty Allocation", "Academic Calendar", "Holidays", "Class Scheduling")
    ),
    STUDENT_MGMT(
        8, "Student Management", "छात्र प्रबंधन",
        listOf("All Students", "New Admissions", "Admission Approval", "Student Profiles", "Roll Number Management", "Student Documents", "Student ID Cards", "Student Promotion", "Student Transfer", "Student Status", "Student Reports")
    ),
    PARENT_PORTAL(
        9, "Parent Portal Management", "अभिभावक पोर्टल",
        listOf("Parent Accounts", "Parent Approval", "Student Linking", "Attendance Visibility", "Syllabus Visibility", "Result Visibility", "Fee Visibility", "Parent Notifications", "Parent Communication", "Parent Reports", "Parent Portal Branding", "Parent Portal Permissions")
    ),
    ATTENDANCE_MGMT(
        10, "Attendance Management", "उपस्थिति प्रबंधन",
        listOf("Daily Attendance", "Monthly Attendance", "Faculty Attendance", "Student Attendance", "Attendance Correction", "Attendance Reports", "Attendance Rules", "Parent Alerts")
    ),
    EXAM_MGMT(
        11, "Examination Management", "परीक्षा प्रबंधन",
        listOf("Online Examination", "Question Bank", "Exam Categories", "Exam Scheduling", "Admit Cards", "Internal Examinations", "Final Examinations", "Practical Examinations", "Marks Management", "Result Publication", "Marksheet Settings", "Certificate Settings")
    ),
    LMS_LIBRARY(
        12, "LMS and Digital Library", "एलएमएस और डिजिटल लाइब्रेरी",
        listOf("Study Materials", "PDF Management", "Video Lectures", "Recorded Classes", "Live Classes", "Digital Books", "Assignments", "Homework", "Student Submissions", "Content Permissions", "Download Permissions")
    ),
    FINANCE_ACCOUNTING(
        13, "Finance and Accounting", "वित्त और लेखा",
        listOf("Course Fees", "Admission Fees", "Student Payments", "Pending Fees", "Payment Verification", "Online Payment Gateway", "Payment Receipts", "Wallet Management", "Refund Management", "Expenses", "Income Reports", "Financial Reports")
    ),
    COMMUNICATION_CENTER(
        14, "Communication Center", "संचार केंद्र",
        listOf("Website Notifications", "SMS Settings", "WhatsApp Integration", "Email Settings", "Push Notifications", "Notification Templates", "Parent Messages", "Faculty Messages", "Support Tickets", "Announcements")
    ),
    MOBILE_APP_MGMT(
        15, "Mobile Application Management", "मोबाइल ऐप प्रबंधन",
        listOf("App Settings", "App Branding", "App Login", "App Permissions", "Push Notification Settings", "App Version Management", "App Users", "App Activity", "Maintenance Settings")
    ),
    AI_TOOLS_MGMT(
        16, "AI Tools Management", "एआई टूल्स प्रबंधन",
        listOf("AI Study Assistant", "AI Question Generator", "AI Test Generator", "AI Notes Generator", "AI PDF Tools", "AI Student Support", "AI Usage Limits", "AI API Configuration", "AI Usage Reports")
    ),
    REPORTS_ANALYTICS(
        17, "Reports and Analytics", "रिपोर्ट और विश्लेषण",
        listOf("Admission Reports", "Student Reports", "Franchise Reports", "Faculty Reports", "Attendance Reports", "Examination Reports", "Performance Reports", "Fee Reports", "Parent Activity Reports", "Website Reports", "System Reports")
    ),
    SECURITY_PERMISSIONS(
        18, "Security and Permissions", "सुरक्षा और अनुमतियाँ",
        listOf("Role Management", "Permission Builder", "Login Security", "OTP Settings", "Session Management", "Device Management", "IP Restrictions", "Audit Logs", "Login History", "Suspicious Activity", "Data Access Logs")
    ),
    DATABASE_BACKUP(
        19, "Database and Backup", "डेटाबेस और बैकअप",
        listOf("MySQL Database Status", "Database Configuration", "Database Backup", "Automatic Backup Schedule", "Restore Backup", "Database Health", "Storage Management", "File Management", "Migration History", "Data Integrity Reports")
    ),
    SYSTEM_SETTINGS(
        20, "System Settings", "सिस्टम सेटिंग्स",
        listOf("General Settings", "Language Settings", "Hindi/English Settings", "Time Zone", "Date Format", "Currency", "Branding", "Theme Settings", "Email Configuration", "API Configuration", "Maintenance Mode", "System Logs")
    ),
    ADVANCED_SETTINGS(
        21, "Advanced Settings", "उन्नत सेटिंग्स",
        listOf("Feature Activation", "Feature Deactivation", "Module Configuration", "Custom Fields", "Custom Forms", "Automation Rules", "Workflow Builder", "Integration Management", "System Diagnostics", "Performance Monitoring", "Implementation Report")
    )
}
