package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.EmailOtpCoordinator
import com.example.core.ErpGlobalSearch
import com.example.core.SuperAdminCredentialManager
import com.example.core.SuperAdminCredentialStore
import com.example.data.AiGenerationState
import com.example.data.AiToolsRepository
import com.example.data.MvciRepository
import com.example.data.WebsiteConnectorProbe
import com.example.model.AuditLogEntry
import com.example.model.ContentItem
import com.example.model.CourseRecord
import com.example.model.ExamRecord
import com.example.model.FranchiseRecord
import com.example.model.GlobalSearchMatch
import com.example.model.MenuModule
import com.example.model.PaymentRecord
import com.example.model.RoleDefinition
import com.example.model.StaffRecord
import com.example.model.StudentRecord
import com.example.model.SystemNotification
import com.example.model.WebsiteConnector
import com.example.model.WebsiteProbeResult
import com.example.model.WorkflowRule
import com.example.model.WorkspaceConfig
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MvciViewModel(
    private val repository: MvciRepository,
    private val probe: WebsiteConnectorProbe,
    private val aiRepository: AiToolsRepository,
    val currentUserId: String,
    private val credentialStore: SuperAdminCredentialStore? = null,
    private val otpCoordinator: EmailOtpCoordinator? = null
) : ViewModel() {

    private val _activeModule = MutableStateFlow(MenuModule.DASHBOARD)
    val activeModule: StateFlow<MenuModule> = _activeModule.asStateFlow()

    private val _activeSubPage = MutableStateFlow(MenuModule.DASHBOARD.subMenus.first())
    val activeSubPage: StateFlow<String> = _activeSubPage.asStateFlow()

    private val navHistory = ArrayDeque<Pair<MenuModule, String>>()

    private val _canNavigateBack = MutableStateFlow(false)
    val canNavigateBack: StateFlow<Boolean> = _canNavigateBack.asStateFlow()

    private val _dateFilter = MutableStateFlow("ALL")
    val dateFilter: StateFlow<String> = _dateFilter.asStateFlow()

    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _probeResult = MutableStateFlow<WebsiteProbeResult?>(null)
    val probeResult: StateFlow<WebsiteProbeResult?> = _probeResult.asStateFlow()

    private val _isProbing = MutableStateFlow(false)
    val isProbing: StateFlow<Boolean> = _isProbing.asStateFlow()

    private val _aiState = MutableStateFlow<AiGenerationState>(AiGenerationState.Idle)
    val aiState: StateFlow<AiGenerationState> = _aiState.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _errorBanner = MutableStateFlow<String?>(null)
    val errorBanner: StateFlow<String?> = _errorBanner.asStateFlow()

    private val _isSuperAdminUnlocked = MutableStateFlow(false)
    val isSuperAdminUnlocked: StateFlow<Boolean> = _isSuperAdminUnlocked.asStateFlow()

    private var aiJob: Job? = null

    val workspaceConfig: StateFlow<UiState<WorkspaceConfig?>> =
        repository.observeWorkspaceConfig(currentUserId)
            .toUiState("workspace")

    val studentsState: StateFlow<UiState<List<StudentRecord>>> =
        repository.observeStudents(currentUserId).toUiState("students")

    val franchisesState: StateFlow<UiState<List<FranchiseRecord>>> =
        repository.observeFranchises(currentUserId).toUiState("franchises")

    val staffState: StateFlow<UiState<List<StaffRecord>>> =
        repository.observeStaff(currentUserId).toUiState("staff")

    val coursesState: StateFlow<UiState<List<CourseRecord>>> =
        repository.observeCourses(currentUserId).toUiState("courses")

    val connectorsState: StateFlow<UiState<List<WebsiteConnector>>> =
        repository.observeConnectors(currentUserId).toUiState("connectors")

    val rolesState: StateFlow<UiState<List<RoleDefinition>>> =
        repository.observeRoles(currentUserId).toUiState("roles")

    val automationsState: StateFlow<UiState<List<WorkflowRule>>> =
        repository.observeAutomations(currentUserId).toUiState("automations")

    val paymentsState: StateFlow<UiState<List<PaymentRecord>>> =
        repository.observePayments(currentUserId).toUiState("payments")

    val examsState: StateFlow<UiState<List<ExamRecord>>> =
        repository.observeExams(currentUserId).toUiState("exams")

    val contentsState: StateFlow<UiState<List<ContentItem>>> =
        repository.observeContents(currentUserId).toUiState("contents")

    val auditLogsState: StateFlow<UiState<List<AuditLogEntry>>> =
        repository.observeAuditLogs(currentUserId).toUiState("audit_logs")

    val notificationsState: StateFlow<UiState<List<SystemNotification>>> =
        repository.observeNotifications(currentUserId).toUiState("notifications")

    init {
        repository.activeActorUsername = currentActorUsername()
        viewModelScope.launch {
            repository.bootstrapWorkspaceIfNeeded(currentUserId)
                .onFailure { err ->
                    _errorBanner.value = err.localizedMessage ?: "Failed to initialize workspace"
                }
        }
        viewModelScope.launch {
            workspaceConfig.collect { state ->
                val cfg = (state as? UiState.Success)?.data ?: return@collect
                repository.activeActorUsername = SuperAdminCredentialManager.resolveUsername(cfg)
                if (credentialStore?.hasPendingCloudSync() == true) {
                    val syncPayload = credentialStore.pendingSyncUpdateMap()
                    val res = repository.updateWorkspaceSettings(
                        uid = currentUserId,
                        updates = syncPayload,
                        auditAction = "OTP_PASSWORD_RESET_SYNC",
                        actorUsername = credentialStore.activeUsername()
                    )
                    if (res.isSuccess) {
                        credentialStore.markCloudSynced()
                    }
                } else {
                    credentialStore?.mirrorFromWorkspaceConfig(cfg)
                }
            }
        }
    }

    private fun currentActorUsername(): String {
        val cfg = (workspaceConfig.value as? UiState.Success)?.data
        return credentialStore?.activeUsername()?.takeIf { it.isNotBlank() }
            ?: SuperAdminCredentialManager.resolveUsername(cfg)
    }

    private fun <T> Flow<T>.toUiState(label: String): StateFlow<UiState<T>> =
        map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val root = firestoreError ?: error
                Log.w(TAG, "Error observing $label", root)
                emit(UiState.Error(root.message ?: "Failed to load $label"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = UiState.Loading
            )

    fun navigateTo(module: MenuModule, subPage: String = module.subMenus.first()) {
        val current = _activeModule.value to _activeSubPage.value
        if (current.first != module || current.second != subPage) {
            navHistory.addLast(current)
            if (navHistory.size > 30) navHistory.removeFirst()
            _activeModule.value = module
            _activeSubPage.value = subPage
            _canNavigateBack.value = navHistory.isNotEmpty()
        }
    }

    fun navigateBack(): Boolean {
        val prev = navHistory.removeLastOrNull() ?: return false
        _activeModule.value = prev.first
        _activeSubPage.value = prev.second
        _canNavigateBack.value = navHistory.isNotEmpty()
        return true
    }

    fun setDateFilter(filter: String) {
        _dateFilter.value = filter
    }

    fun setGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
    }

    fun clearBanners() {
        _statusBanner.value = null
        _errorBanner.value = null
    }

    fun postNotice(message: String) {
        _statusBanner.value = message
    }

    fun buildSearchResults(query: String): List<GlobalSearchMatch> = ErpGlobalSearch.search(
        query = query,
        config = (workspaceConfig.value as? UiState.Success)?.data,
        students = (studentsState.value as? UiState.Success)?.data.orEmpty(),
        staff = (staffState.value as? UiState.Success)?.data.orEmpty(),
        auditLogs = (auditLogsState.value as? UiState.Success)?.data.orEmpty(),
        franchises = (franchisesState.value as? UiState.Success)?.data.orEmpty(),
        courses = (coursesState.value as? UiState.Success)?.data.orEmpty(),
        connectors = (connectorsState.value as? UiState.Success)?.data.orEmpty(),
        limit = 24
    )

    fun maskSecret(raw: String): String = probe.maskSecret(raw)

    fun runWebsiteProbe(
        url: String,
        bearerToken: String = "",
        headerName: String = "",
        headerVal: String = ""
    ) {
        viewModelScope.launch {
            _isProbing.value = true
            val result = probe.probeUrl(url, bearerToken, headerName, headerVal)
            _probeResult.value = result
            _isProbing.value = false
        }
    }

    fun testAndSyncConnector(connector: WebsiteConnector, syncDeltaRecords: Long = 0L) {
        viewModelScope.launch {
            _isProbing.value = true
            val target = connector.apiBaseUrl.ifBlank { connector.url }
            val result = probe.probeUrl(target)
            _probeResult.value = result
            _isProbing.value = false
            val health = if (result.reachable) "HTTP ${result.httpCode} (${result.tlsProtocol})" else "OFFLINE / ERR"
            val imported = if (result.reachable && syncDeltaRecords > 0) syncDeltaRecords else 0L
            val failed = if (!result.reachable && syncDeltaRecords > 0) 1L else 0L
            handleResult(
                repository.updateConnectorProbe(
                    uid = currentUserId,
                    connectorId = connector.id,
                    reachable = result.reachable,
                    healthStatus = health,
                    latencyMs = result.latencyMs,
                    note = result.message,
                    importedDelta = imported,
                    updatedDelta = if (result.reachable && syncDeltaRecords > 0) 2L else 0L,
                    failedDelta = failed,
                    currentImported = connector.recordsImported,
                    currentUpdated = connector.recordsUpdated,
                    currentFailed = connector.failedRecords
                ),
                successMsg = if (result.reachable) "${connector.name}: ${result.message}" else null
            )
            if (!result.reachable) {
                _errorBanner.value = "${connector.name}: ${result.message}"
            }
        }
    }

    fun createOrUpdateConnector(connector: WebsiteConnector, existingId: String? = null) {
        viewModelScope.launch {
            handleResult(repository.saveConnector(currentUserId, connector, existingId), "Connector saved")
        }
    }

    fun setConnectorStatus(connectorId: String, status: String, note: String) {
        viewModelScope.launch {
            handleResult(repository.setConnectorStatus(currentUserId, connectorId, status, note), "Connector marked $status")
        }
    }

    fun deleteConnector(connectorId: String) {
        viewModelScope.launch {
            handleResult(repository.deleteConnector(currentUserId, connectorId), "Connector deleted")
        }
    }

    fun saveStudent(student: StudentRecord, existingId: String? = null) {
        viewModelScope.launch {
            handleResult(repository.saveStudent(currentUserId, student, existingId), "Student record saved")
        }
    }

    fun updateStudentStatus(studentId: String, status: String, triggerAbsentAlert: Boolean = false) {
        viewModelScope.launch {
            handleResult(repository.updateStudentStatus(currentUserId, studentId, status, triggerAbsentAlert), "Student updated: $status")
        }
    }

    fun markAttendance(student: StudentRecord, present: Boolean) {
        viewModelScope.launch {
            handleResult(
                repository.markStudentAttendance(currentUserId, student, present),
                if (present) "${student.name} marked Present" else "${student.name} marked Absent (Parent Alert sent)"
            )
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            handleResult(repository.deleteStudent(currentUserId, studentId), "Student deleted")
        }
    }

    fun saveFranchise(franchise: FranchiseRecord, existingId: String? = null) {
        viewModelScope.launch {
            handleResult(repository.saveFranchise(currentUserId, franchise, existingId), "Franchise saved")
        }
    }

    fun setFranchiseStatus(franchiseId: String, status: String) {
        viewModelScope.launch {
            handleResult(repository.setFranchiseStatus(currentUserId, franchiseId, status), "Franchise marked $status")
        }
    }

    fun deleteFranchise(franchiseId: String) {
        viewModelScope.launch {
            handleResult(repository.deleteFranchise(currentUserId, franchiseId), "Franchise removed")
        }
    }

    fun saveStaff(staff: StaffRecord, existingId: String? = null) {
        viewModelScope.launch {
            handleResult(repository.saveStaff(currentUserId, staff, existingId), "User account saved")
        }
    }

    fun setStaffStatus(staffId: String, status: String) {
        viewModelScope.launch {
            handleResult(repository.setStaffStatus(currentUserId, staffId, status), "Account updated: $status")
        }
    }

    fun deleteStaff(staffId: String) {
        viewModelScope.launch {
            handleResult(repository.deleteStaff(currentUserId, staffId), "User removed")
        }
    }

    fun saveCourse(course: CourseRecord, existingId: String? = null) {
        viewModelScope.launch {
            handleResult(repository.saveCourse(currentUserId, course, existingId), "Course saved")
        }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            handleResult(repository.deleteCourse(currentUserId, courseId), "Course deleted")
        }
    }

    fun saveRole(role: RoleDefinition, existingId: String? = null) {
        viewModelScope.launch {
            handleResult(repository.saveRole(currentUserId, role, existingId), "Role permissions updated")
        }
    }

    fun duplicateRole(role: RoleDefinition) {
        viewModelScope.launch {
            val copy = role.copy(id = "", name = "${role.name} Copy", assignedCount = 0L)
            handleResult(repository.saveRole(currentUserId, copy, null), "Role duplicated")
        }
    }

    fun deleteRole(roleId: String) {
        viewModelScope.launch {
            handleResult(repository.deleteRole(currentUserId, roleId), "Role deleted")
        }
    }

    fun saveAutomation(rule: WorkflowRule, existingId: String? = null) {
        viewModelScope.launch {
            handleResult(repository.saveAutomation(currentUserId, rule, existingId), "Workflow rule saved")
        }
    }

    fun triggerAutomation(rule: WorkflowRule) {
        viewModelScope.launch {
            handleResult(repository.triggerAutomationRun(currentUserId, rule), "Triggered ${rule.title}")
        }
    }

    fun savePayment(payment: PaymentRecord, existingId: String? = null) {
        viewModelScope.launch {
            handleResult(repository.savePayment(currentUserId, payment, existingId), "Payment ledger updated")
        }
    }

    fun saveExam(exam: ExamRecord, existingId: String? = null) {
        viewModelScope.launch {
            handleResult(repository.saveExam(currentUserId, exam, existingId), "Examination updated")
        }
    }

    fun saveContent(item: ContentItem) {
        viewModelScope.launch {
            handleResult(repository.saveContent(currentUserId, item), "Saved ${item.title}")
        }
    }

    fun deleteContent(contentId: String) {
        viewModelScope.launch {
            handleResult(repository.deleteContent(currentUserId, contentId), "Item deleted")
        }
    }

    fun markNotificationRead(notifId: String) {
        viewModelScope.launch {
            repository.markNotificationRead(currentUserId, notifId)
        }
    }

    fun updateWorkspaceSettings(updates: Map<String, Any>, auditAction: String, successMsg: String) {
        viewModelScope.launch {
            handleResult(
                repository.updateWorkspaceSettings(
                    uid = currentUserId,
                    updates = updates,
                    auditAction = auditAction,
                    actorUsername = currentActorUsername()
                ),
                successMsg
            )
        }
    }

    fun recordLoginUnlocked(username: String = currentActorUsername()) {
        val cleanUser = username.ifBlank { currentActorUsername() }
        repository.activeActorUsername = cleanUser
        _isSuperAdminUnlocked.value = true
        viewModelScope.launch {
            repository.recordSecurityAuditEvent(
                uid = currentUserId,
                action = "SUPER_ADMIN_LOGIN_SUCCESS",
                targetId = currentUserId,
                previousVal = "LOCKED",
                newVal = "Unlocked MVCI Super Admin Control Center ($cleanUser)",
                moduleName = "Security and Permissions",
                category = "SECURITY",
                severity = "INFO",
                actorUsername = cleanUser
            )
        }
    }

    fun unlockSuperAdmin(username: String, password: String): Boolean {
        val currentConfig = (workspaceConfig.value as? UiState.Success)?.data
        val ok = if (credentialStore != null && currentConfig == null) {
            credentialStore.verifyLogin(username, password)
        } else {
            SuperAdminCredentialManager.verifyCredentials(username, password, currentConfig)
        }
        if (ok) {
            val resolved = SuperAdminCredentialManager.resolveUsername(currentConfig)
            recordLoginUnlocked(resolved)
            _statusBanner.value = "Super Admin unlocked ($resolved)"
        }
        return ok
    }

    fun lockSuperAdmin() {
        val actor = currentActorUsername()
        _isSuperAdminUnlocked.value = false
        viewModelScope.launch {
            repository.recordSecurityAuditEvent(
                uid = currentUserId,
                action = "SUPER_ADMIN_CONSOLE_LOCKED",
                targetId = currentUserId,
                previousVal = "UNLOCKED",
                newVal = "Super Admin locked active console session ($actor)",
                moduleName = "Security and Permissions",
                category = "SECURITY",
                severity = "WARNING",
                actorUsername = actor
            )
        }
    }

    fun updateSuperAdminCredentials(
        currentPassword: String,
        newUsername: String,
        newPassword: String
    ) {
        val currentConfig = (workspaceConfig.value as? UiState.Success)?.data
        val currentValid = SuperAdminCredentialManager.verifyPasswordOnly(currentPassword, currentConfig) ||
            (credentialStore?.verifyPassword(currentPassword) == true)
        if (!currentValid) {
            _errorBanner.value = "Current password is incorrect."
            return
        }
        val cleanUser = newUsername.trim()
        if (cleanUser.length < 3 || cleanUser.length > 80) {
            _errorBanner.value = "Username must be between 3 and 80 characters."
            return
        }
        if (newPassword.length < 6) {
            _errorBanner.value = "New password must be at least 6 characters."
            return
        }
        val payload = SuperAdminCredentialManager.createCredentialUpdateMap(cleanUser, newPassword)
        repository.activeActorUsername = cleanUser
        credentialStore?.saveCredentialsLocal(
            username = cleanUser,
            salt = payload["adminPasswordSalt"] as String,
            hash = payload["adminPasswordHash"] as String,
            recoveryEmail = SuperAdminCredentialManager.resolveRecoveryEmail(currentConfig),
            pendingCloudSync = false
        )
        updateWorkspaceSettings(
            updates = payload,
            auditAction = "UPDATE_SUPER_ADMIN_CREDENTIALS",
            successMsg = "Super Admin username & password updated"
        )
    }

    fun updateRecoverySettings(recoveryEmail: String, otpWebhookUrl: String) {
        val cleanEmail = recoveryEmail.trim()
        val cleanWebhook = otpWebhookUrl.trim()
        if (!cleanEmail.contains("@") || cleanEmail.length < 5 || cleanEmail.length > 120) {
            _errorBanner.value = "Enter a valid recovery email address."
            return
        }
        if (cleanWebhook.isNotEmpty() && !cleanWebhook.startsWith("https://", ignoreCase = true)) {
            _errorBanner.value = "OTP Webhook URL must start with https://"
            return
        }
        credentialStore?.let { store ->
            store.saveCredentialsLocal(
                username = store.activeUsername(),
                salt = store.activeSalt(),
                hash = store.activePasswordHash(),
                recoveryEmail = cleanEmail,
                pendingCloudSync = false
            )
        }
        updateWorkspaceSettings(
            updates = mapOf(
                "recoveryEmail" to cleanEmail,
                "otpWebhookUrl" to cleanWebhook
            ),
            auditAction = "UPDATE_OTP_RECOVERY_SETTINGS",
            successMsg = "Recovery email & OTP settings saved"
        )
    }

    suspend fun requestPasswordResetOtp(enteredEmail: String): Result<String> {
        val coordinator = otpCoordinator
            ?: return Result.failure(IllegalStateException("Email OTP coordinator unavailable."))
        val cfg = (workspaceConfig.value as? UiState.Success)?.data
        val res = coordinator.requestOtpForRegisteredEmail(
            enteredEmail = enteredEmail,
            instituteName = cfg?.instituteName ?: "Maa Vaishno Computer Institute",
            webhookOverride = cfg?.otpWebhookUrl ?: credentialStore?.otpWebhookUrl().orEmpty()
        )
        res.onSuccess {
            repository.recordSecurityAuditEvent(
                uid = currentUserId,
                action = "OTP_RESET_REQUESTED",
                targetId = currentUserId,
                previousVal = "",
                newVal = "Email OTP requested for ${enteredEmail.trim()}",
                moduleName = "Security and Permissions",
                category = "SECURITY",
                severity = "WARNING",
                actorUsername = currentActorUsername()
            )
        }
        return res
    }

    suspend fun verifyOtpAndResetPassword(otpCode: String, newPassword: String): Result<String> {
        val coordinator = otpCoordinator
            ?: return Result.failure(IllegalStateException("Email OTP coordinator unavailable."))
        val result = coordinator.verifyOtpAndResetPassword(otpCode, newPassword)
        result.onSuccess { updates ->
            val syncRes = repository.updateWorkspaceSettings(
                uid = currentUserId,
                updates = updates,
                auditAction = "OTP_PASSWORD_RESET",
                actorUsername = currentActorUsername()
            )
            if (syncRes.isSuccess) {
                credentialStore?.markCloudSynced()
            }
            _statusBanner.value = "Password reset verified via OTP on ${credentialStore?.recoveryEmail() ?: SuperAdminCredentialManager.DEFAULT_RECOVERY_EMAIL}"
        }
        return result.map { "Password reset verified via Email OTP!" }
    }

    fun resetSuperAdminCredentials() {
        val defaults = SuperAdminCredentialManager.defaultCredentialUpdateMap()
        val defaultUser = defaults["adminUsername"] as String
        repository.activeActorUsername = defaultUser
        credentialStore?.saveCredentialsLocal(
            username = defaultUser,
            salt = defaults["adminPasswordSalt"] as String,
            hash = defaults["adminPasswordHash"] as String,
            recoveryEmail = defaults["recoveryEmail"] as String,
            pendingCloudSync = false
        )
        updateWorkspaceSettings(
            updates = defaults,
            auditAction = "RESET_SUPER_ADMIN_CREDENTIALS",
            successMsg = "Super Admin credentials reset in Super Admin Settings"
        )
    }

    fun runAiTool(toolName: String, prompt: String, languageCode: String) {
        if (prompt.isBlank()) {
            _errorBanner.value = "Enter a topic or question for Gemini AI."
            return
        }
        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            aiRepository.streamMvciAiTask(toolName, prompt, languageCode).collect { state ->
                _aiState.value = state
            }
        }
    }

    fun buildCsvExport(): String {
        val sb = StringBuilder()
        sb.appendLine("SECTION,ID_OR_CODE,NAME_OR_TITLE,DETAIL,STATUS,AMOUNT_OR_METRIC")
        (studentsState.value as? UiState.Success)?.data?.forEach {
            sb.appendLine("STUDENT,\"${it.rollNo}\",\"${it.name}\",\"${it.courseName} (${it.branchName})\",${it.status},${it.paidFee}/${it.totalFee}")
        }
        (franchisesState.value as? UiState.Success)?.data?.forEach {
            sb.appendLine("FRANCHISE,\"${it.code}\",\"${it.name}\",\"${it.city} - ${it.ownerName}\",${it.status},${it.revenueTotal}")
        }
        (coursesState.value as? UiState.Success)?.data?.forEach {
            sb.appendLine("COURSE,\"${it.code}\",\"${it.title}\",\"${it.category}\",${it.status},${it.feeAmount}")
        }
        (paymentsState.value as? UiState.Success)?.data?.forEach {
            sb.appendLine("PAYMENT,\"${it.receiptNo}\",\"${it.studentName}\",\"${it.category} (${it.paymentMode})\",${it.status},${it.amount}")
        }
        (connectorsState.value as? UiState.Success)?.data?.forEach {
            sb.appendLine("CONNECTOR,\"${it.integrationMethod}\",\"${it.name}\",\"${it.url}\",${it.status},${it.recordsImported}")
        }
        (auditLogsState.value as? UiState.Success)?.data?.forEach {
            val actor = it.actorUsername.ifBlank { it.actorEmail }.ifBlank { it.actorRole }
            val safeDiff = "${it.previousVal} -> ${it.newVal}".replace("\"", "'")
            sb.appendLine("AUDIT_LOG,\"${it.id}\",\"${it.action}\",\"${it.moduleName} (${it.category}) | $actor | $safeDiff\",${it.severity},${it.createdAt?.toDate()?.time ?: 0L}")
        }
        return sb.toString()
    }

    private fun handleResult(result: Result<Unit>, successMsg: String?) {
        result.onSuccess {
            if (successMsg != null) _statusBanner.value = successMsg
        }.onFailure { err ->
            _errorBanner.value = err.localizedMessage ?: "Operation failed"
        }
    }

    private companion object {
        const val TAG = "MvciViewModel"
    }
}
