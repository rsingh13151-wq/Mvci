package com.example.data

import android.os.Build
import com.example.core.AuditSanitizer
import com.example.model.AuditLogEntry
import com.example.model.ContentItem
import com.example.model.CourseRecord
import com.example.model.ExamRecord
import com.example.model.FranchiseRecord
import com.example.model.PaymentRecord
import com.example.model.RoleDefinition
import com.example.model.StaffRecord
import com.example.model.StudentRecord
import com.example.model.SystemNotification
import com.example.model.WebsiteConnector
import com.example.model.WorkflowRule
import com.example.model.WorkspaceConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class MvciRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    @Volatile
    var activeActorUsername: String = "Rishu@2050"

    private fun workspaceDoc(uid: String) = db.collection("workspaces").document(uid)
    private fun subCol(uid: String, name: String) = workspaceDoc(uid).collection(name)

    private fun requireActorUid(expectedUid: String): String {
        val current = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google.")
        check(current == expectedUid) { "Session user changed; aborting cross-user operation." }
        return current
    }

    private fun <T> Flow<List<T>>.withFirestoreLogging(uid: String, path: String): Flow<List<T>> =
        catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val toLog = firestoreError ?: (error as? Exception)
            if (toLog != null && auth.currentUser?.uid == uid) {
                handleFirestoreError(toLog, OperationType.LIST, path)
            }
            throw error
        }

    fun observeWorkspaceConfig(uid: String): Flow<WorkspaceConfig?> =
        workspaceDoc(uid).snapshots()
            .map { snap ->
                if (!snap.exists()) null
                else snap.toObject(WorkspaceConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val toLog = firestoreError ?: (error as? Exception)
                if (toLog != null && auth.currentUser?.uid == uid) {
                    handleFirestoreError(toLog, OperationType.GET, "workspaces/$uid")
                }
                throw error
            }

    fun observeStudents(uid: String): Flow<List<StudentRecord>> =
        subCol(uid, "students")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(StudentRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/students")

    fun observeFranchises(uid: String): Flow<List<FranchiseRecord>> =
        subCol(uid, "franchises")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(FranchiseRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/franchises")

    fun observeStaff(uid: String): Flow<List<StaffRecord>> =
        subCol(uid, "staff")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(StaffRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/staff")

    fun observeCourses(uid: String): Flow<List<CourseRecord>> =
        subCol(uid, "courses")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(CourseRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/courses")

    fun observeConnectors(uid: String): Flow<List<WebsiteConnector>> =
        subCol(uid, "connectors")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(WebsiteConnector::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/connectors")

    fun observeRoles(uid: String): Flow<List<RoleDefinition>> =
        subCol(uid, "roles")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(RoleDefinition::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/roles")

    fun observeAutomations(uid: String): Flow<List<WorkflowRule>> =
        subCol(uid, "automations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(WorkflowRule::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/automations")

    fun observePayments(uid: String): Flow<List<PaymentRecord>> =
        subCol(uid, "payments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(PaymentRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/payments")

    fun observeExams(uid: String): Flow<List<ExamRecord>> =
        subCol(uid, "exams")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(ExamRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/exams")

    fun observeContents(uid: String): Flow<List<ContentItem>> =
        subCol(uid, "contents")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { it.toObjects(ContentItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/contents")

    fun observeAuditLogs(uid: String): Flow<List<AuditLogEntry>> =
        subCol(uid, "audit_logs")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(200)
            .snapshots()
            .map { it.toObjects(AuditLogEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/audit_logs")

    fun observeNotifications(uid: String): Flow<List<SystemNotification>> =
        subCol(uid, "notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .snapshots()
            .map { it.toObjects(SystemNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
            .withFirestoreLogging(uid, "workspaces/$uid/notifications")

    suspend fun bootstrapWorkspaceIfNeeded(uid: String): Result<Unit> = runMutation(OperationType.WRITE, "workspaces/$uid") {
        requireActorUid(uid)
        val docRef = workspaceDoc(uid)
        val existing = docRef.get().await()
        if (existing.exists()) return@runMutation

        val batch = db.batch()
        batch.set(
            docRef,
            mapOf(
                "userId" to uid,
                "instituteName" to "Maa Vaishno Computer Institute",
                "websiteUrl" to "https://www.mvci.online",
                "directorName" to "Rishu Singh",
                "mysqlHost" to "mysql.mvci.online",
                "mysqlDbName" to "mvci_central_erp",
                "hostingProvider" to "GoDaddy Compatible",
                "languageCode" to "en",
                "darkTheme" to true,
                "maintenanceMode" to false,
                "autoBackupSchedule" to "DAILY_02_00",
                "otpRequired" to true,
                "adminUsername" to "Rishu@2050",
                "adminPasswordSalt" to "mvci_v1_salt_2050",
                "adminPasswordHash" to "001ea4e661e22e4d7ac2a2c46c6a5989d263afad3ac79672a184ec24dd1149d8",
                "recoveryEmail" to "rsingh13151@gmail.com",
                "otpWebhookUrl" to "",
                "disabledModules" to emptyList<String>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        // Primary MVCI website connector
        batch.set(
            subCol(uid, "connectors").document("conn_mvci_main"),
            mapOf(
                "userId" to uid,
                "name" to "MVCI Official Portal",
                "url" to "https://www.mvci.online",
                "siteType" to "Primary Institute Website",
                "description" to "Existing MVCI Website + LMS + ERP centralized endpoint",
                "integrationMethod" to "REST API",
                "apiBaseUrl" to "https://www.mvci.online/api/v1",
                "maskedApiKey" to "mvci_••••live",
                "authMethod" to "Secure API Token",
                "webhookUrl" to "https://www.mvci.online/webhooks/mvci-sync",
                "syncDirection" to "TWO_WAY",
                "syncFrequency" to "AUTOMATIC",
                "conflictPolicy" to "MERGE_APPROVED",
                "status" to "CONNECTED",
                "healthStatus" to "PENDING_PROBE",
                "lastLatencyMs" to 0L,
                "recordsImported" to 0L,
                "recordsUpdated" to 0L,
                "failedRecords" to 0L,
                "fieldMappings" to "student_name->name,roll_number->rollNo,course_code->courseName,fee_paid->paidFee",
                "lastSyncNote" to "Ready for live probe & sync",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        )

        // Seed standard MVCI courses & roles & 9 workflow automations
        val seedCourses = listOf(
            Triple("ADCA-01", "ADCA (Advance Diploma in Computer Applications)", "Diploma"),
            Triple("DCA-02", "DCA (Diploma in Computer Applications)", "Diploma"),
            Triple("CCC-03", "NIELIT CCC Certification", "Certification"),
            Triple("TALLY-04", "Tally Prime with GST Accounting", "Accounting"),
            Triple("OLEV-05", "NIELIT O-Level IT Foundation", "Government")
        )
        seedCourses.forEachIndexed { idx, (code, title, cat) ->
            batch.set(
                subCol(uid, "courses").document("course_${idx + 1}"),
                mapOf(
                    "userId" to uid,
                    "code" to code,
                    "title" to title,
                    "category" to cat,
                    "durationMonths" to if (idx == 0 || idx == 4) 12L else 6L,
                    "feeAmount" to if (idx == 0) 14500L else if (idx == 4) 18000L else 7500L,
                    "activeBatches" to 2L,
                    "syllabusProgressPct" to 72L,
                    "facultyName" to "Rishu Singh",
                    "batchTiming" to "08:00 AM - 10:00 AM",
                    "status" to "ACTIVE",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        val seedRoles = listOf(
            "Branch Manager" to "VIEW,ADD,EDIT,APPROVE,EXPORT",
            "Academic Manager" to "VIEW,ADD,EDIT,APPROVE,DOWNLOAD",
            "Examination Controller" to "VIEW,ADD,EDIT,APPROVE,EXPORT,DOWNLOAD",
            "Accountant" to "VIEW,ADD,EDIT,APPROVE,EXPORT",
            "Admission Officer" to "VIEW,ADD,EDIT",
            "Library Manager" to "VIEW,ADD,EDIT,DOWNLOAD",
            "Parent Support Manager" to "VIEW,EDIT",
            "Franchise Manager" to "VIEW,ADD,EDIT,APPROVE,EXPORT"
        )
        seedRoles.forEachIndexed { idx, (rName, mask) ->
            batch.set(
                subCol(uid, "roles").document("role_${idx + 1}"),
                mapOf(
                    "userId" to uid,
                    "name" to rName,
                    "description" to "Enterprise custom role for $rName",
                    "branchScope" to if (rName.contains("Franchise") || rName.contains("Branch")) "Assigned Branch" else "All Branches",
                    "permissionsMask" to mask,
                    "moduleScope" to "Students,Academic,Attendance,Finance",
                    "customRole" to true,
                    "assignedCount" to 1L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        val seedRules = listOf(
            Triple("Absent Parent Alert", "STUDENT_ABSENT", "Notify approved parent via App & SMS"),
            Triple("Fee Due Reminder", "FEE_DUE", "Send automated payment reminder & receipt link"),
            Triple("Result Publication Alert", "RESULT_PUBLISHED", "Notify student and parent with marksheet link"),
            Triple("New Admission Alert", "ADMISSION_SUBMITTED", "Notify Admin & Branch Manager for approval"),
            Triple("Syllabus Progress Sync", "CHAPTER_COMPLETED", "Recalculate course & batch completion %"),
            Triple("Certificate Generation", "CERTIFICATE_APPROVED", "Issue verified MVCI digital certificate"),
            Triple("Franchise Login Provisioning", "FRANCHISE_APPROVED", "Create authorized franchise admin portal login"),
            Triple("Backup Failure Escalation", "BACKUP_FAILED", "Alert Super Admin Director immediately"),
            Triple("API Connector Health Alert", "API_CONNECTION_FAILED", "Create critical system notification")
        )
        seedRules.forEachIndexed { idx, (title, trigger, action) ->
            batch.set(
                subCol(uid, "automations").document("wf_${idx + 1}"),
                mapOf(
                    "userId" to uid,
                    "title" to title,
                    "triggerEvent" to trigger,
                    "actionSummary" to action,
                    "enabled" to true,
                    "retryLimit" to 3L,
                    "preventDuplicates" to true,
                    "executionCount" to 0L,
                    "failureCount" to 0L,
                    "lastRunStatus" to "ARMED",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }

        batch.set(
            subCol(uid, "audit_logs").document("log_init_${System.currentTimeMillis()}"),
            mapOf(
                "userId" to uid,
                "actorEmail" to (auth.currentUser?.email ?: "").take(120),
                "actorUsername" to activeActorUsername.take(80),
                "actorRole" to "SUPER_ADMIN",
                "action" to "WORKSPACE_INITIALIZED",
                "moduleName" to "System",
                "targetId" to uid,
                "category" to "SYSTEM",
                "severity" to "INFO",
                "previousVal" to "NONE",
                "newVal" to "MVCI Control Center Active",
                "deviceInfo" to "${Build.MANUFACTURER} ${Build.MODEL}".take(160),
                "createdAt" to FieldValue.serverTimestamp()
            )
        )

        batch.commit().await()
    }

    suspend fun updateWorkspaceSettings(
        uid: String,
        updates: Map<String, Any>,
        auditAction: String,
        actorUsername: String = activeActorUsername
    ): Result<Unit> =
        runMutation(OperationType.UPDATE, "workspaces/$uid") {
            requireActorUid(uid)
            val existingData = try {
                workspaceDoc(uid).get().await().data
            } catch (_: Exception) {
                null
            }
            val (prevDiff, nextDiff) = AuditSanitizer.buildSettingsDiff(existingData, updates)
            val resolvedModule = if (
                auditAction.contains("OTP", ignoreCase = true) ||
                auditAction.contains("PASSWORD", ignoreCase = true) ||
                auditAction.contains("CREDENTIAL", ignoreCase = true) ||
                auditAction.contains("RECOVERY", ignoreCase = true)
            ) {
                "Security and Permissions"
            } else {
                "System Settings"
            }
            val payload = updates.toMutableMap()
            payload["updatedAt"] = FieldValue.serverTimestamp()
            workspaceDoc(uid).update(payload).await()
            writeAudit(
                uid = uid,
                action = auditAction,
                moduleName = resolvedModule,
                targetId = uid,
                previousVal = prevDiff,
                newVal = nextDiff,
                actorUsername = actorUsername
            )
        }

    suspend fun saveStudent(uid: String, student: StudentRecord, existingId: String? = null): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "st_${System.currentTimeMillis()}"
        return runMutation(OperationType.WRITE, "workspaces/$uid/students/$docId") {
            requireActorUid(uid)
            val ref = subCol(uid, "students").document(docId)
            if (existingId.isNullOrBlank()) {
                val payload = mapOf(
                    "userId" to uid,
                    "name" to student.name.trim().take(100),
                    "rollNo" to student.rollNo.trim().take(40),
                    "email" to student.email.trim().take(120),
                    "phone" to student.phone.trim().take(30),
                    "courseName" to student.courseName.trim().take(100),
                    "batchName" to student.batchName.trim().take(80),
                    "branchName" to student.branchName.trim().take(100),
                    "parentName" to student.parentName.trim().take(100),
                    "parentPhone" to student.parentPhone.trim().take(30),
                    "parentLinked" to student.parentLinked,
                    "status" to student.status,
                    "totalFee" to student.totalFee.coerceAtLeast(0L),
                    "paidFee" to student.paidFee.coerceAtLeast(0L),
                    "attendancePct" to student.attendancePct.coerceIn(0L, 100L),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.set(payload).await()
                writeAudit(uid, "CREATE_STUDENT", "Student Management", docId, "", student.name)
                if (student.status == "PENDING") {
                    createNotification(uid, "New Admission Submitted", "${student.name} applied for ${student.courseName}", "WORKFLOW", "INFO")
                }
            } else {
                val payload = mapOf(
                    "name" to student.name.trim().take(100),
                    "rollNo" to student.rollNo.trim().take(40),
                    "email" to student.email.trim().take(120),
                    "phone" to student.phone.trim().take(30),
                    "courseName" to student.courseName.trim().take(100),
                    "batchName" to student.batchName.trim().take(80),
                    "branchName" to student.branchName.trim().take(100),
                    "parentName" to student.parentName.trim().take(100),
                    "parentPhone" to student.parentPhone.trim().take(30),
                    "parentLinked" to student.parentLinked,
                    "status" to student.status,
                    "totalFee" to student.totalFee.coerceAtLeast(0L),
                    "paidFee" to student.paidFee.coerceAtLeast(0L),
                    "attendancePct" to student.attendancePct.coerceIn(0L, 100L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                ref.update(payload).await()
                writeAudit(uid, "UPDATE_STUDENT", "Student Management", docId, "", "${student.name} (${student.status})")
            }
        }
    }

    suspend fun updateStudentStatus(uid: String, studentId: String, newStatus: String, markAbsentAlert: Boolean = false): Result<Unit> =
        runMutation(OperationType.UPDATE, "workspaces/$uid/students/$studentId") {
            requireActorUid(uid)
            subCol(uid, "students").document(studentId).update(
                mapOf("status" to newStatus, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            writeAudit(uid, "STUDENT_STATUS_$newStatus", "Student Management", studentId, "", newStatus)
            if (markAbsentAlert) {
                createNotification(uid, "Absent Parent Alert Triggered", "Student $studentId marked absent; parent alert dispatched.", "PARENT_ALERT", "WARNING")
            }
        }

    suspend fun markStudentAttendance(uid: String, student: StudentRecord, present: Boolean): Result<Unit> =
        runMutation(OperationType.UPDATE, "workspaces/$uid/students/${student.id}") {
            requireActorUid(uid)
            val updatedPct = if (present) (student.attendancePct + 1L).coerceAtMost(100L) else (student.attendancePct - 2L).coerceAtLeast(0L)
            subCol(uid, "students").document(student.id).update(
                mapOf("attendancePct" to updatedPct, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            writeAudit(uid, if (present) "MARK_PRESENT" else "MARK_ABSENT", "Attendance Management", student.id, "${student.attendancePct}%", "$updatedPct%")
            if (!present && student.parentLinked) {
                createNotification(
                    uid,
                    "Parent Absent Alert: ${student.name}",
                    "Workflow triggered for ${student.parentName.ifBlank { "Parent" }} (${student.batchName}).",
                    "PARENT_ALERT",
                    "WARNING"
                )
            }
        }

    suspend fun deleteStudent(uid: String, studentId: String): Result<Unit> =
        runMutation(OperationType.DELETE, "workspaces/$uid/students/$studentId") {
            requireActorUid(uid)
            subCol(uid, "students").document(studentId).delete().await()
            writeAudit(uid, "DELETE_STUDENT", "Student Management", studentId, "EXISTED", "DELETED")
        }

    suspend fun saveFranchise(uid: String, franchise: FranchiseRecord, existingId: String? = null): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "fr_${System.currentTimeMillis()}"
        return runMutation(OperationType.WRITE, "workspaces/$uid/franchises/$docId") {
            requireActorUid(uid)
            val ref = subCol(uid, "franchises").document(docId)
            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "code" to franchise.code.trim().take(30),
                        "name" to franchise.name.trim().take(120),
                        "city" to franchise.city.trim().take(80),
                        "ownerName" to franchise.ownerName.trim().take(80),
                        "phone" to franchise.phone.trim().take(30),
                        "email" to franchise.email.trim().take(120),
                        "status" to franchise.status,
                        "studentCount" to franchise.studentCount.coerceAtLeast(0L),
                        "revenueTotal" to franchise.revenueTotal.coerceAtLeast(0L),
                        "commissionPct" to franchise.commissionPct.coerceIn(0L, 100L),
                        "loginProvisioned" to (franchise.status == "ACTIVE"),
                        "brandingTitle" to franchise.brandingTitle.ifBlank { franchise.name }.take(120),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "CREATE_FRANCHISE", "Franchise Management", docId, "", franchise.name)
            } else {
                ref.update(
                    mapOf(
                        "code" to franchise.code.trim().take(30),
                        "name" to franchise.name.trim().take(120),
                        "city" to franchise.city.trim().take(80),
                        "ownerName" to franchise.ownerName.trim().take(80),
                        "phone" to franchise.phone.trim().take(30),
                        "email" to franchise.email.trim().take(120),
                        "status" to franchise.status,
                        "studentCount" to franchise.studentCount.coerceAtLeast(0L),
                        "revenueTotal" to franchise.revenueTotal.coerceAtLeast(0L),
                        "commissionPct" to franchise.commissionPct.coerceIn(0L, 100L),
                        "loginProvisioned" to franchise.loginProvisioned,
                        "brandingTitle" to franchise.brandingTitle.take(120),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "UPDATE_FRANCHISE", "Franchise Management", docId, "", franchise.name)
            }
        }
    }

    suspend fun setFranchiseStatus(uid: String, franchiseId: String, status: String): Result<Unit> =
        runMutation(OperationType.UPDATE, "workspaces/$uid/franchises/$franchiseId") {
            requireActorUid(uid)
            val loginOk = status == "ACTIVE"
            subCol(uid, "franchises").document(franchiseId).update(
                mapOf(
                    "status" to status,
                    "loginProvisioned" to loginOk,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAudit(uid, "FRANCHISE_STATUS_$status", "Franchise Management", franchiseId, "", status)
            if (status == "ACTIVE") {
                createNotification(uid, "Franchise Approved & Login Provisioned", "Franchise $franchiseId portal credentials activated.", "WORKFLOW", "INFO")
            }
        }

    suspend fun deleteFranchise(uid: String, franchiseId: String): Result<Unit> =
        runMutation(OperationType.DELETE, "workspaces/$uid/franchises/$franchiseId") {
            requireActorUid(uid)
            subCol(uid, "franchises").document(franchiseId).delete().await()
            writeAudit(uid, "DELETE_FRANCHISE", "Franchise Management", franchiseId, "EXISTED", "DELETED")
        }

    suspend fun saveStaff(uid: String, staff: StaffRecord, existingId: String? = null): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "usr_${System.currentTimeMillis()}"
        return runMutation(OperationType.WRITE, "workspaces/$uid/staff/$docId") {
            requireActorUid(uid)
            val ref = subCol(uid, "staff").document(docId)
            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "name" to staff.name.trim().take(100),
                        "roleType" to staff.roleType,
                        "customRoleName" to staff.customRoleName.trim().take(80),
                        "branchName" to staff.branchName.trim().take(100),
                        "email" to staff.email.trim().take(120),
                        "phone" to staff.phone.trim().take(30),
                        "department" to staff.department.trim().take(80),
                        "status" to staff.status,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "CREATE_USER_${staff.roleType}", "User Management", docId, "", staff.name)
            } else {
                ref.update(
                    mapOf(
                        "name" to staff.name.trim().take(100),
                        "roleType" to staff.roleType,
                        "customRoleName" to staff.customRoleName.trim().take(80),
                        "branchName" to staff.branchName.trim().take(100),
                        "email" to staff.email.trim().take(120),
                        "phone" to staff.phone.trim().take(30),
                        "department" to staff.department.trim().take(80),
                        "status" to staff.status,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "UPDATE_USER", "User Management", docId, "", "${staff.name} (${staff.status})")
            }
        }
    }

    suspend fun setStaffStatus(uid: String, staffId: String, status: String): Result<Unit> =
        runMutation(OperationType.UPDATE, "workspaces/$uid/staff/$staffId") {
            requireActorUid(uid)
            subCol(uid, "staff").document(staffId).update(
                mapOf("status" to status, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
            writeAudit(uid, "USER_STATUS_$status", "User Management", staffId, "", status)
        }

    suspend fun deleteStaff(uid: String, staffId: String): Result<Unit> =
        runMutation(OperationType.DELETE, "workspaces/$uid/staff/$staffId") {
            requireActorUid(uid)
            subCol(uid, "staff").document(staffId).delete().await()
            writeAudit(uid, "DELETE_USER", "User Management", staffId, "EXISTED", "DELETED")
        }

    suspend fun saveCourse(uid: String, course: CourseRecord, existingId: String? = null): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "crs_${System.currentTimeMillis()}"
        return runMutation(OperationType.WRITE, "workspaces/$uid/courses/$docId") {
            requireActorUid(uid)
            val ref = subCol(uid, "courses").document(docId)
            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "code" to course.code.trim().take(30),
                        "title" to course.title.trim().take(120),
                        "category" to course.category.trim().take(80),
                        "durationMonths" to course.durationMonths.coerceIn(1L, 120L),
                        "feeAmount" to course.feeAmount.coerceAtLeast(0L),
                        "activeBatches" to course.activeBatches.coerceAtLeast(0L),
                        "syllabusProgressPct" to course.syllabusProgressPct.coerceIn(0L, 100L),
                        "facultyName" to course.facultyName.trim().take(100),
                        "batchTiming" to course.batchTiming.trim().take(80),
                        "status" to course.status,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "CREATE_COURSE", "Academic Management", docId, "", course.title)
            } else {
                ref.update(
                    mapOf(
                        "code" to course.code.trim().take(30),
                        "title" to course.title.trim().take(120),
                        "category" to course.category.trim().take(80),
                        "durationMonths" to course.durationMonths.coerceIn(1L, 120L),
                        "feeAmount" to course.feeAmount.coerceAtLeast(0L),
                        "activeBatches" to course.activeBatches.coerceAtLeast(0L),
                        "syllabusProgressPct" to course.syllabusProgressPct.coerceIn(0L, 100L),
                        "facultyName" to course.facultyName.trim().take(100),
                        "batchTiming" to course.batchTiming.trim().take(80),
                        "status" to course.status,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "UPDATE_COURSE", "Academic Management", docId, "", course.title)
            }
        }
    }

    suspend fun deleteCourse(uid: String, courseId: String): Result<Unit> =
        runMutation(OperationType.DELETE, "workspaces/$uid/courses/$courseId") {
            requireActorUid(uid)
            subCol(uid, "courses").document(courseId).delete().await()
            writeAudit(uid, "DELETE_COURSE", "Academic Management", courseId, "EXISTED", "DELETED")
        }

    suspend fun saveConnector(uid: String, connector: WebsiteConnector, existingId: String? = null): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "conn_${System.currentTimeMillis()}"
        return runMutation(OperationType.WRITE, "workspaces/$uid/connectors/$docId") {
            requireActorUid(uid)
            val ref = subCol(uid, "connectors").document(docId)
            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "name" to connector.name.trim().take(120),
                        "url" to connector.url.trim().take(300),
                        "siteType" to connector.siteType.trim().take(60),
                        "description" to connector.description.trim().take(400),
                        "integrationMethod" to connector.integrationMethod.trim().take(60),
                        "apiBaseUrl" to connector.apiBaseUrl.trim().take(300),
                        "maskedApiKey" to connector.maskedApiKey.trim().take(120),
                        "authMethod" to connector.authMethod.trim().take(60),
                        "webhookUrl" to connector.webhookUrl.trim().take(300),
                        "syncDirection" to connector.syncDirection,
                        "syncFrequency" to connector.syncFrequency,
                        "conflictPolicy" to connector.conflictPolicy,
                        "status" to connector.status,
                        "healthStatus" to connector.healthStatus.take(40),
                        "lastLatencyMs" to connector.lastLatencyMs.coerceAtLeast(0L),
                        "recordsImported" to connector.recordsImported.coerceAtLeast(0L),
                        "recordsUpdated" to connector.recordsUpdated.coerceAtLeast(0L),
                        "failedRecords" to connector.failedRecords.coerceAtLeast(0L),
                        "fieldMappings" to connector.fieldMappings.take(600),
                        "lastSyncNote" to connector.lastSyncNote.take(300),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "CONNECT_WEBSITE", "Universal Website Connector", docId, "", connector.url)
            } else {
                ref.update(
                    mapOf(
                        "name" to connector.name.trim().take(120),
                        "url" to connector.url.trim().take(300),
                        "siteType" to connector.siteType.trim().take(60),
                        "description" to connector.description.trim().take(400),
                        "integrationMethod" to connector.integrationMethod.trim().take(60),
                        "apiBaseUrl" to connector.apiBaseUrl.trim().take(300),
                        "maskedApiKey" to connector.maskedApiKey.trim().take(120),
                        "authMethod" to connector.authMethod.trim().take(60),
                        "webhookUrl" to connector.webhookUrl.trim().take(300),
                        "syncDirection" to connector.syncDirection,
                        "syncFrequency" to connector.syncFrequency,
                        "conflictPolicy" to connector.conflictPolicy,
                        "status" to connector.status,
                        "healthStatus" to connector.healthStatus.take(40),
                        "lastLatencyMs" to connector.lastLatencyMs.coerceAtLeast(0L),
                        "recordsImported" to connector.recordsImported.coerceAtLeast(0L),
                        "recordsUpdated" to connector.recordsUpdated.coerceAtLeast(0L),
                        "failedRecords" to connector.failedRecords.coerceAtLeast(0L),
                        "fieldMappings" to connector.fieldMappings.take(600),
                        "lastSyncNote" to connector.lastSyncNote.take(300),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "UPDATE_CONNECTOR", "Universal Website Connector", docId, "", connector.status)
            }
        }
    }

    suspend fun updateConnectorProbe(
        uid: String,
        connectorId: String,
        reachable: Boolean,
        healthStatus: String,
        latencyMs: Long,
        note: String,
        importedDelta: Long = 0L,
        updatedDelta: Long = 0L,
        failedDelta: Long = 0L,
        currentImported: Long = 0L,
        currentUpdated: Long = 0L,
        currentFailed: Long = 0L
    ): Result<Unit> = runMutation(OperationType.UPDATE, "workspaces/$uid/connectors/$connectorId") {
        requireActorUid(uid)
        val newStatus = if (reachable) "CONNECTED" else "ERROR"
        subCol(uid, "connectors").document(connectorId).update(
            mapOf(
                "status" to newStatus,
                "healthStatus" to healthStatus.take(40),
                "lastLatencyMs" to latencyMs.coerceAtLeast(0L),
                "recordsImported" to (currentImported + importedDelta).coerceAtLeast(0L),
                "recordsUpdated" to (currentUpdated + updatedDelta).coerceAtLeast(0L),
                "failedRecords" to (currentFailed + failedDelta).coerceAtLeast(0L),
                "lastSyncNote" to note.take(300),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
        writeAudit(uid, if (reachable) "SYNC_PROBE_OK" else "SYNC_PROBE_FAIL", "Universal Website Connector", connectorId, "", note)
        if (!reachable) {
            createNotification(uid, "API Connection Failed", "Connector $connectorId failed health check: $note", "INTEGRATION", "CRITICAL")
        }
    }

    suspend fun setConnectorStatus(uid: String, connectorId: String, status: String, note: String): Result<Unit> =
        runMutation(OperationType.UPDATE, "workspaces/$uid/connectors/$connectorId") {
            requireActorUid(uid)
            subCol(uid, "connectors").document(connectorId).update(
                mapOf(
                    "status" to status,
                    "lastSyncNote" to note.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAudit(uid, "CONNECTOR_$status", "Universal Website Connector", connectorId, "", note)
        }

    suspend fun deleteConnector(uid: String, connectorId: String): Result<Unit> =
        runMutation(OperationType.DELETE, "workspaces/$uid/connectors/$connectorId") {
            requireActorUid(uid)
            subCol(uid, "connectors").document(connectorId).delete().await()
            writeAudit(uid, "DELETE_CONNECTOR", "Universal Website Connector", connectorId, "EXISTED", "DELETED")
        }

    suspend fun saveRole(uid: String, role: RoleDefinition, existingId: String? = null): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "role_${System.currentTimeMillis()}"
        return runMutation(OperationType.WRITE, "workspaces/$uid/roles/$docId") {
            requireActorUid(uid)
            val ref = subCol(uid, "roles").document(docId)
            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "name" to role.name.trim().take(80),
                        "description" to role.description.trim().take(240),
                        "branchScope" to role.branchScope.trim().take(100),
                        "permissionsMask" to role.permissionsMask.trim().take(120),
                        "moduleScope" to role.moduleScope.trim().take(200),
                        "customRole" to role.customRole,
                        "assignedCount" to role.assignedCount.coerceAtLeast(0L),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "CREATE_ROLE", "Security and Permissions", docId, "", role.name)
            } else {
                ref.update(
                    mapOf(
                        "name" to role.name.trim().take(80),
                        "description" to role.description.trim().take(240),
                        "branchScope" to role.branchScope.trim().take(100),
                        "permissionsMask" to role.permissionsMask.trim().take(120),
                        "moduleScope" to role.moduleScope.trim().take(200),
                        "customRole" to role.customRole,
                        "assignedCount" to role.assignedCount.coerceAtLeast(0L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "UPDATE_ROLE_PERMISSIONS", "Security and Permissions", docId, "", role.permissionsMask)
            }
        }
    }

    suspend fun deleteRole(uid: String, roleId: String): Result<Unit> =
        runMutation(OperationType.DELETE, "workspaces/$uid/roles/$roleId") {
            requireActorUid(uid)
            subCol(uid, "roles").document(roleId).delete().await()
            writeAudit(uid, "DELETE_ROLE", "Security and Permissions", roleId, "EXISTED", "DELETED")
        }

    suspend fun saveAutomation(uid: String, rule: WorkflowRule, existingId: String? = null): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "wf_${System.currentTimeMillis()}"
        return runMutation(OperationType.WRITE, "workspaces/$uid/automations/$docId") {
            requireActorUid(uid)
            val ref = subCol(uid, "automations").document(docId)
            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "title" to rule.title.trim().take(120),
                        "triggerEvent" to rule.triggerEvent.trim().take(120),
                        "actionSummary" to rule.actionSummary.trim().take(240),
                        "enabled" to rule.enabled,
                        "retryLimit" to rule.retryLimit.coerceIn(0L, 10L),
                        "preventDuplicates" to rule.preventDuplicates,
                        "executionCount" to rule.executionCount.coerceAtLeast(0L),
                        "failureCount" to rule.failureCount.coerceAtLeast(0L),
                        "lastRunStatus" to rule.lastRunStatus.take(60),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "CREATE_WORKFLOW", "Workflow Automation", docId, "", rule.title)
            } else {
                ref.update(
                    mapOf(
                        "title" to rule.title.trim().take(120),
                        "triggerEvent" to rule.triggerEvent.trim().take(120),
                        "actionSummary" to rule.actionSummary.trim().take(240),
                        "enabled" to rule.enabled,
                        "retryLimit" to rule.retryLimit.coerceIn(0L, 10L),
                        "preventDuplicates" to rule.preventDuplicates,
                        "executionCount" to rule.executionCount.coerceAtLeast(0L),
                        "failureCount" to rule.failureCount.coerceAtLeast(0L),
                        "lastRunStatus" to rule.lastRunStatus.take(60),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "UPDATE_WORKFLOW", "Workflow Automation", docId, "", "${rule.title} (${if (rule.enabled) "ON" else "OFF"})")
            }
        }
    }

    suspend fun triggerAutomationRun(uid: String, rule: WorkflowRule): Result<Unit> =
        runMutation(OperationType.UPDATE, "workspaces/$uid/automations/${rule.id}") {
            requireActorUid(uid)
            val nextCount = rule.executionCount + 1L
            subCol(uid, "automations").document(rule.id).update(
                mapOf(
                    "executionCount" to nextCount,
                    "lastRunStatus" to "EXECUTED_OK",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAudit(uid, "EXECUTE_WORKFLOW", "Workflow Automation", rule.id, "", rule.triggerEvent)
            createNotification(uid, "Workflow Executed: ${rule.title}", rule.actionSummary, "WORKFLOW", "INFO")
        }

    suspend fun savePayment(uid: String, payment: PaymentRecord, existingId: String? = null): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "pay_${System.currentTimeMillis()}"
        return runMutation(OperationType.WRITE, "workspaces/$uid/payments/$docId") {
            requireActorUid(uid)
            val ref = subCol(uid, "payments").document(docId)
            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "studentName" to payment.studentName.trim().take(100),
                        "branchName" to payment.branchName.trim().take(100),
                        "category" to payment.category,
                        "amount" to payment.amount.coerceAtLeast(0L),
                        "paymentMode" to payment.paymentMode.trim().take(40),
                        "receiptNo" to payment.receiptNo.trim().take(60),
                        "status" to payment.status,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "RECORD_PAYMENT", "Finance and Accounting", docId, "", "${payment.receiptNo} INR ${payment.amount}")
            } else {
                ref.update(
                    mapOf(
                        "status" to payment.status,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "UPDATE_PAYMENT_STATUS", "Finance and Accounting", docId, "", payment.status)
            }
        }
    }

    suspend fun saveExam(uid: String, exam: ExamRecord, existingId: String? = null): Result<Unit> {
        val docId = existingId?.takeIf { it.isNotBlank() } ?: "exm_${System.currentTimeMillis()}"
        return runMutation(OperationType.WRITE, "workspaces/$uid/exams/$docId") {
            requireActorUid(uid)
            val ref = subCol(uid, "exams").document(docId)
            if (existingId.isNullOrBlank()) {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "title" to exam.title.trim().take(120),
                        "courseName" to exam.courseName.trim().take(100),
                        "examType" to exam.examType,
                        "scheduledDate" to exam.scheduledDate.trim().take(40),
                        "questionCount" to exam.questionCount.coerceAtLeast(0L),
                        "certificatesIssued" to exam.certificatesIssued.coerceAtLeast(0L),
                        "status" to exam.status,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "CREATE_EXAM", "Examination Management", docId, "", exam.title)
            } else {
                ref.update(
                    mapOf(
                        "status" to exam.status,
                        "certificatesIssued" to exam.certificatesIssued.coerceAtLeast(0L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                writeAudit(uid, "UPDATE_EXAM", "Examination Management", docId, "", "${exam.title} (${exam.status})")
                if (exam.status == "PUBLISHED") {
                    createNotification(uid, "Results Published: ${exam.title}", "Automated student & parent result alerts triggered.", "WORKFLOW", "INFO")
                }
            }
        }
    }

    suspend fun saveContent(uid: String, item: ContentItem): Result<Unit> {
        val docId = "cnt_${System.currentTimeMillis()}"
        return runMutation(OperationType.CREATE, "workspaces/$uid/contents/$docId") {
            requireActorUid(uid)
            subCol(uid, "contents").document(docId).set(
                mapOf(
                    "userId" to uid,
                    "title" to item.title.trim().take(140),
                    "moduleCategory" to item.moduleCategory,
                    "description" to item.description.trim().take(500),
                    "urlOrMeta" to item.urlOrMeta.trim().take(300),
                    "downloadable" to item.downloadable,
                    "status" to item.status,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            writeAudit(uid, "PUBLISH_${item.moduleCategory}", item.moduleCategory, docId, "", item.title)
        }
    }

    suspend fun deleteContent(uid: String, contentId: String): Result<Unit> =
        runMutation(OperationType.DELETE, "workspaces/$uid/contents/$contentId") {
            requireActorUid(uid)
            subCol(uid, "contents").document(contentId).delete().await()
            writeAudit(uid, "DELETE_CONTENT", "Content Management", contentId, "EXISTED", "DELETED")
        }

    suspend fun markNotificationRead(uid: String, notifId: String): Result<Unit> =
        runMutation(OperationType.UPDATE, "workspaces/$uid/notifications/$notifId") {
            requireActorUid(uid)
            subCol(uid, "notifications").document(notifId).update(
                mapOf("unread" to false, "updatedAt" to FieldValue.serverTimestamp())
            ).await()
        }

    suspend fun recordSecurityAuditEvent(
        uid: String,
        action: String,
        targetId: String = uid,
        previousVal: String = "",
        newVal: String = "",
        moduleName: String = "Security and Permissions",
        category: String = "SECURITY",
        severity: String? = null,
        actorUsername: String = activeActorUsername
    ): Result<Unit> = runMutation(OperationType.CREATE, "workspaces/$uid/audit_logs") {
        requireActorUid(uid)
        writeAudit(
            uid = uid,
            action = action,
            moduleName = moduleName,
            targetId = targetId.ifBlank { uid },
            previousVal = previousVal,
            newVal = newVal,
            category = category,
            severity = severity,
            actorUsername = actorUsername
        )
    }

    private suspend fun writeAudit(
        uid: String,
        action: String,
        moduleName: String,
        targetId: String,
        previousVal: String,
        newVal: String,
        category: String? = null,
        severity: String? = null,
        actorUsername: String = activeActorUsername
    ) {
        val docId = "aud_${System.currentTimeMillis()}_${(100..999).random()}"
        val resolvedCategory = category ?: AuditSanitizer.classifyCategory(action, moduleName)
        val resolvedSeverity = severity ?: AuditSanitizer.classifySeverity(action)
        val cleanPrev = AuditSanitizer.sanitizeFreeText(previousVal, 300)
        val cleanNew = AuditSanitizer.sanitizeFreeText(newVal, 300)
        subCol(uid, "audit_logs").document(docId).set(
            mapOf(
                "userId" to uid,
                "actorEmail" to (auth.currentUser?.email ?: "").take(120),
                "actorUsername" to actorUsername.ifBlank { "Rishu@2050" }.take(80),
                "actorRole" to "SUPER_ADMIN",
                "action" to action.take(120),
                "moduleName" to moduleName.take(80),
                "targetId" to targetId.ifBlank { uid }.take(128),
                "category" to resolvedCategory,
                "severity" to resolvedSeverity,
                "previousVal" to cleanPrev,
                "newVal" to cleanNew,
                "deviceInfo" to "${Build.MANUFACTURER} ${Build.MODEL}".take(160),
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    private suspend fun createNotification(
        uid: String,
        title: String,
        message: String,
        channel: String,
        severity: String
    ) {
        val docId = "ntf_${System.currentTimeMillis()}_${(100..999).random()}"
        subCol(uid, "notifications").document(docId).set(
            mapOf(
                "userId" to uid,
                "title" to title.take(120),
                "message" to message.take(400),
                "channel" to channel,
                "severity" to severity,
                "unread" to true,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }

    private inline fun runMutation(
        op: OperationType,
        path: String,
        block: () -> Unit
    ): Result<Unit> = try {
        block()
        Result.success(Unit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        handleFirestoreError(e, op, path)
        Result.failure(e)
    }
}
