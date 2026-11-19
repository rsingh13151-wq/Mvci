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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.model.CourseRecord
import com.example.model.FranchiseRecord
import com.example.model.MenuModule
import com.example.model.RoleDefinition
import com.example.model.StaffRecord
import com.example.model.StudentRecord
import com.example.model.WorkspaceConfig
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.EmptyModuleState
import com.example.ui.components.SearchAndActionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.SubPageTabsStrip
import com.example.ui.theme.MvciAmberAccent
import com.example.ui.theme.MvciCyanAccent
import com.example.ui.theme.MvciEmerald
import com.example.ui.theme.MvciRoseError

@Composable
fun InstituteManagementScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    config: WorkspaceConfig?,
    franchises: List<FranchiseRecord>,
    onUpdateWorkspace: (Map<String, Any>, String, String) -> Unit
) {
    var instName by remember(config) { mutableStateOf(config?.instituteName ?: "Maa Vaishno Computer Institute") }
    var directorName by remember(config) { mutableStateOf(config?.directorName ?: "Rishu Singh") }
    var websiteUrl by remember(config) { mutableStateOf(config?.websiteUrl ?: "https://www.mvci.online") }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.INSTITUTE_MGMT.subMenus, subPage, onSubPageChange)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(subPage, style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(value = instName, onValueChange = { instName = it }, label = { Text("Institute Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = directorName, onValueChange = { directorName = it }, label = { Text("Director Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = websiteUrl, onValueChange = { websiteUrl = it }, label = { Text("Official Website URL") }, modifier = Modifier.fillMaxWidth())
                        Button(
                            onClick = {
                                onUpdateWorkspace(
                                    mapOf(
                                        "instituteName" to instName.trim(),
                                        "directorName" to directorName.trim(),
                                        "websiteUrl" to websiteUrl.trim()
                                    ),
                                    "UPDATE_INSTITUTE_PROFILE",
                                    "Institute profile saved"
                                )
                            },
                            enabled = instName.isNotBlank() && directorName.isNotBlank() && websiteUrl.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Institute Configuration")
                        }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Active Branches (${franchises.size + 1})", style = MaterialTheme.typography.titleMedium)
                        Text("1. Main Headquarters Campus • Director: $directorName", style = MaterialTheme.typography.bodySmall)
                        franchises.forEachIndexed { i, fr ->
                            Text("${i + 2}. ${fr.name} (${fr.city}) — ${fr.status}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FranchiseManagementScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    franchises: List<FranchiseRecord>,
    onSaveFranchise: (FranchiseRecord, String?) -> Unit,
    onSetStatus: (String, String) -> Unit,
    onDeleteFranchise: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(subPage == "Add Franchise") }
    var editingFranchise by remember { mutableStateOf<FranchiseRecord?>(null) }
    var deleteCandidate by remember { mutableStateOf<FranchiseRecord?>(null) }

    if (showAddDialog || editingFranchise != null) {
        FranchiseFormDialog(
            initial = editingFranchise,
            onDismiss = {
                showAddDialog = false
                editingFranchise = null
            },
            onSave = { rec ->
                onSaveFranchise(rec, editingFranchise?.id)
                showAddDialog = false
                editingFranchise = null
            }
        )
    }

    if (deleteCandidate != null) {
        ConfirmActionDialog(
            title = "Delete Franchise?",
            message = "Delete ${deleteCandidate!!.name} (${deleteCandidate!!.code}) and revoke its branch portal access?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteFranchise(deleteCandidate!!.id)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null }
        )
    }

    val filtered = franchises.filter {
        query.isBlank() || it.name.contains(query, true) || it.city.contains(query, true) || it.code.contains(query, true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.FRANCHISE_MGMT.subMenus, subPage, onSubPageChange)
        SearchAndActionHeader(
            title = "Franchise Management",
            subtitle = "$subPage • Strict branch data isolation & portal provisioning",
            searchQuery = query,
            onSearchChange = { query = it },
            primaryActionLabel = "Add Franchise",
            primaryActionIcon = Icons.Default.Add,
            onPrimaryAction = { showAddDialog = true }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    EmptyModuleState(
                        icon = Icons.Default.Business,
                        title = "No Franchises Found",
                        subtitle = "Create a franchise branch to manage isolated logins, branding, fees, and reports.",
                        actionLabel = "Add Franchise",
                        onAction = { showAddDialog = true }
                    )
                }
            }
            items(filtered, key = { it.id }) { fr ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${fr.name} (${fr.code})", style = MaterialTheme.typography.titleMedium)
                                Text("${fr.city} • Owner: ${fr.ownerName}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusPill(fr.status)
                        }
                        Text(
                            "Students: ${fr.studentCount} • Revenue: INR ${fr.revenueTotal} • Commission: ${fr.commissionPct}% • Login: ${if (fr.loginProvisioned) "Provisioned" else "Locked"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MvciCyanAccent
                        )
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (fr.status != "ACTIVE") {
                                Button(onClick = { onSetStatus(fr.id, "ACTIVE") }) { Text("Approve & Provision Login") }
                            }
                            if (fr.status == "ACTIVE") {
                                OutlinedButton(onClick = { onSetStatus(fr.id, "SUSPENDED") }) { Text("Suspend") }
                            }
                            TextButton(onClick = { editingFranchise = fr }) { Text("Edit") }
                            IconButton(onClick = { deleteCandidate = fr }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Franchise", tint = MvciRoseError)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FranchiseFormDialog(
    initial: FranchiseRecord?,
    onDismiss: () -> Unit,
    onSave: (FranchiseRecord) -> Unit
) {
    var code by remember { mutableStateOf(initial?.code ?: "MVCI-BR01") }
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var city by remember { mutableStateOf(initial?.city ?: "") }
    var owner by remember { mutableStateOf(initial?.ownerName ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }
    var comm by remember { mutableStateOf((initial?.commissionPct ?: 15L).toString()) }
    var status by remember { mutableStateOf(initial?.status ?: "PENDING") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Franchise" else "Edit Franchise") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Branch Code") })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Franchise Name") })
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") })
                OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Owner Name") })
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                OutlinedTextField(value = comm, onValueChange = { comm = it.filter { c -> c.isDigit() } }, label = { Text("Commission %") })
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("PENDING", "ACTIVE", "SUSPENDED").forEach { st ->
                        FilterChip(selected = status == st, onClick = { status = st }, label = { Text(st) })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        FranchiseRecord(
                            code = code,
                            name = name,
                            city = city,
                            ownerName = owner,
                            phone = phone,
                            email = email,
                            status = status,
                            studentCount = initial?.studentCount ?: 0L,
                            revenueTotal = initial?.revenueTotal ?: 0L,
                            commissionPct = comm.toLongOrNull() ?: 15L,
                            loginProvisioned = status == "ACTIVE",
                            brandingTitle = name
                        )
                    )
                },
                enabled = code.isNotBlank() && name.isNotBlank() && city.isNotBlank() && owner.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun UserManagementScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    staff: List<StaffRecord>,
    roles: List<RoleDefinition>,
    onSaveStaff: (StaffRecord, String?) -> Unit,
    onSetStaffStatus: (String, String) -> Unit,
    onDeleteStaff: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var roleType by remember {
            mutableStateOf(
                when (subPage) {
                    "Super Admin Users" -> "SUPER_ADMIN"
                    "Admin Users" -> "ADMIN"
                    "Franchise Admins" -> "FRANCHISE_ADMIN"
                    "Parents" -> "PARENT"
                    else -> "FACULTY"
                }
            )
        }
        var customRole by remember { mutableStateOf(roles.firstOrNull()?.name ?: "Branch Manager") }
        var branch by remember { mutableStateOf("Main Campus") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Register Institute User") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") })
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("SUPER_ADMIN", "ADMIN", "FRANCHISE_ADMIN", "FACULTY", "PARENT").forEach { rt ->
                            FilterChip(selected = roleType == rt, onClick = { roleType = rt }, label = { Text(rt) })
                        }
                    }
                    OutlinedTextField(value = customRole, onValueChange = { customRole = it }, label = { Text("Assigned Custom Role") })
                    OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("Branch") })
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveStaff(
                            StaffRecord(
                                name = name,
                                roleType = roleType,
                                customRoleName = customRole,
                                branchName = branch,
                                email = email,
                                phone = phone,
                                department = "Academic",
                                status = "ACTIVE"
                            ),
                            null
                        )
                        showAdd = false
                    },
                    enabled = name.isNotBlank() && branch.isNotBlank()
                ) { Text("Create User") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    val filtered = staff.filter {
        (query.isBlank() || it.name.contains(query, true) || it.roleType.contains(query, true)) &&
            when (subPage) {
                "Faculty" -> it.roleType == "FACULTY"
                "Parents" -> it.roleType == "PARENT"
                "Franchise Admins" -> it.roleType == "FRANCHISE_ADMIN"
                "Admin Users" -> it.roleType == "ADMIN"
                "Super Admin Users" -> it.roleType == "SUPER_ADMIN"
                else -> true
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.USER_MGMT.subMenus, subPage, onSubPageChange)
        SearchAndActionHeader(
            title = "User Management",
            subtitle = "$subPage • Accounts, approvals, suspensions & role assignments",
            searchQuery = query,
            onSearchChange = { query = it },
            primaryActionLabel = "Add User",
            primaryActionIcon = Icons.Default.PersonAdd,
            onPrimaryAction = { showAdd = true }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    EmptyModuleState(
                        icon = Icons.Default.School,
                        title = "No Users in $subPage",
                        subtitle = "Add Admins, Franchise Admins, Faculty, or Parents with role-scoped permissions.",
                        actionLabel = "Add User",
                        onAction = { showAdd = true }
                    )
                }
            }
            items(filtered, key = { it.id }) { u ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(u.name, style = MaterialTheme.typography.titleMedium)
                                Text("${u.roleType} • ${u.customRoleName.ifBlank { u.department }} • ${u.branchName}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusPill(u.status)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (u.status != "ACTIVE") {
                                Button(onClick = { onSetStaffStatus(u.id, "ACTIVE") }) { Text("Approve") }
                            } else {
                                OutlinedButton(onClick = { onSetStaffStatus(u.id, "SUSPENDED") }) { Text("Suspend") }
                            }
                            IconButton(onClick = { onDeleteStaff(u.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete User", tint = MvciRoseError)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcademicManagementScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    courses: List<CourseRecord>,
    onSaveCourse: (CourseRecord, String?) -> Unit,
    onDeleteCourse: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<CourseRecord?>(null) }

    if (showAdd || editingCourse != null) {
        var code by remember { mutableStateOf(editingCourse?.code ?: "") }
        var title by remember { mutableStateOf(editingCourse?.title ?: "") }
        var category by remember { mutableStateOf(editingCourse?.category ?: "Diploma") }
        var duration by remember { mutableStateOf((editingCourse?.durationMonths ?: 12L).toString()) }
        var fee by remember { mutableStateOf((editingCourse?.feeAmount ?: 12000L).toString()) }
        var batches by remember { mutableStateOf((editingCourse?.activeBatches ?: 2L).toString()) }
        var syllabus by remember { mutableStateOf((editingCourse?.syllabusProgressPct ?: 50L).toString()) }
        var faculty by remember { mutableStateOf(editingCourse?.facultyName ?: "Rishu Singh") }
        var timing by remember { mutableStateOf(editingCourse?.batchTiming ?: "08:00 AM - 10:00 AM") }

        AlertDialog(
            onDismissRequest = { showAdd = false; editingCourse = null },
            title = { Text(if (editingCourse == null) "Create Course" else "Edit Course") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course Code") })
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Course Title") })
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                    OutlinedTextField(value = duration, onValueChange = { duration = it.filter { c -> c.isDigit() } }, label = { Text("Duration (Months)") })
                    OutlinedTextField(value = fee, onValueChange = { fee = it.filter { c -> c.isDigit() } }, label = { Text("Fee (INR)") })
                    OutlinedTextField(value = batches, onValueChange = { batches = it.filter { c -> c.isDigit() } }, label = { Text("Active Batches") })
                    OutlinedTextField(value = syllabus, onValueChange = { syllabus = it.filter { c -> c.isDigit() } }, label = { Text("Syllabus Progress %") })
                    OutlinedTextField(value = faculty, onValueChange = { faculty = it }, label = { Text("Allocated Faculty") })
                    OutlinedTextField(value = timing, onValueChange = { timing = it }, label = { Text("Batch Timing") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveCourse(
                            CourseRecord(
                                code = code,
                                title = title,
                                category = category,
                                durationMonths = (duration.toLongOrNull() ?: 6L).coerceIn(1L, 120L),
                                feeAmount = fee.toLongOrNull() ?: 0L,
                                activeBatches = batches.toLongOrNull() ?: 1L,
                                syllabusProgressPct = (syllabus.toLongOrNull() ?: 0L).coerceIn(0L, 100L),
                                facultyName = faculty,
                                batchTiming = timing,
                                status = "ACTIVE"
                            ),
                            editingCourse?.id
                        )
                        showAdd = false
                        editingCourse = null
                    },
                    enabled = code.isNotBlank() && title.isNotBlank() && category.isNotBlank()
                ) { Text("Save Course") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false; editingCourse = null }) { Text("Cancel") } }
        )
    }

    val filtered = courses.filter { query.isBlank() || it.title.contains(query, true) || it.code.contains(query, true) }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.ACADEMIC_MGMT.subMenus, subPage, onSubPageChange)
        SearchAndActionHeader(
            title = "Academic Management",
            subtitle = "$subPage • Courses, syllabus chapters, batches & faculty allocation",
            searchQuery = query,
            onSearchChange = { query = it },
            primaryActionLabel = "Create Course",
            primaryActionIcon = Icons.Default.MenuBook,
            onPrimaryAction = { showAdd = true }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.id }) { c ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(c.title, style = MaterialTheme.typography.titleMedium)
                                Text("${c.code} • ${c.category} • ${c.durationMonths} mo • INR ${c.feeAmount}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusPill(c.status)
                        }
                        Text("Batches: ${c.activeBatches} (${c.batchTiming}) • Faculty: ${c.facultyName}", style = MaterialTheme.typography.labelMedium)
                        LinearProgressIndicator(
                            progress = { (c.syllabusProgressPct / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                val nextPct = (c.syllabusProgressPct + 10L).coerceAtMost(100L)
                                onSaveCourse(c.copy(syllabusProgressPct = nextPct), c.id)
                            }) { Text("+10% Chapter Done") }
                            TextButton(onClick = { editingCourse = c }) { Text("Edit") }
                            IconButton(onClick = { onDeleteCourse(c.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Course", tint = MvciRoseError)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentManagementScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    students: List<StudentRecord>,
    courses: List<CourseRecord>,
    onSaveStudent: (StudentRecord, String?) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDeleteStudent: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showAdd by remember { mutableStateOf(subPage == "New Admissions") }
    var idCardStudent by remember { mutableStateOf<StudentRecord?>(null) }
    var deleteCandidate by remember { mutableStateOf<StudentRecord?>(null) }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var rollNo by remember { mutableStateOf("MVCI-2026-${100 + students.size + 1}") }
        var course by remember { mutableStateOf(courses.firstOrNull()?.title ?: "ADCA") }
        var batch by remember { mutableStateOf("Morning-08") }
        var branch by remember { mutableStateOf("Main Campus") }
        var phone by remember { mutableStateOf("") }
        var parentName by remember { mutableStateOf("") }
        var parentPhone by remember { mutableStateOf("") }
        var totalFee by remember { mutableStateOf("14500") }
        var paidFee by remember { mutableStateOf("5000") }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("New Student Admission") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Student Name") })
                    OutlinedTextField(value = rollNo, onValueChange = { rollNo = it }, label = { Text("Roll Number") })
                    OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Course") })
                    OutlinedTextField(value = batch, onValueChange = { batch = it }, label = { Text("Batch") })
                    OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("Branch") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Student Phone") })
                    OutlinedTextField(value = parentName, onValueChange = { parentName = it }, label = { Text("Parent Name") })
                    OutlinedTextField(value = parentPhone, onValueChange = { parentPhone = it }, label = { Text("Parent Phone") })
                    OutlinedTextField(value = totalFee, onValueChange = { totalFee = it.filter { c -> c.isDigit() } }, label = { Text("Total Fee (INR)") })
                    OutlinedTextField(value = paidFee, onValueChange = { paidFee = it.filter { c -> c.isDigit() } }, label = { Text("Paid Fee (INR)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveStudent(
                            StudentRecord(
                                name = name,
                                rollNo = rollNo,
                                phone = phone,
                                courseName = course,
                                batchName = batch,
                                branchName = branch,
                                parentName = parentName,
                                parentPhone = parentPhone,
                                parentLinked = parentName.isNotBlank(),
                                status = if (subPage == "New Admissions") "PENDING" else "ACTIVE",
                                totalFee = totalFee.toLongOrNull() ?: 0L,
                                paidFee = paidFee.toLongOrNull() ?: 0L,
                                attendancePct = 100L
                            ),
                            null
                        )
                        showAdd = false
                    },
                    enabled = name.isNotBlank() && rollNo.isNotBlank() && course.isNotBlank() && batch.isNotBlank() && branch.isNotBlank()
                ) { Text("Save Admission") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

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
                        Text("Course: ${st.courseName}", style = MaterialTheme.typography.bodySmall)
                        Text("Batch: ${st.batchName} • Branch: ${st.branchName}", style = MaterialTheme.typography.bodySmall)
                        Text("Status: ${st.status} • Attendance: ${st.attendancePct}%", style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { idCardStudent = null }) { Text("Close") } }
        )
    }

    if (deleteCandidate != null) {
        ConfirmActionDialog(
            title = "Remove Student?",
            message = "Delete ${deleteCandidate!!.name} (${deleteCandidate!!.rollNo}) permanently?",
            confirmLabel = "Delete",
            isDestructive = true,
            onConfirm = {
                onDeleteStudent(deleteCandidate!!.id)
                deleteCandidate = null
            },
            onDismiss = { deleteCandidate = null }
        )
    }

    val filtered = students.filter {
        (query.isBlank() || it.name.contains(query, true) || it.rollNo.contains(query, true) || it.courseName.contains(query, true)) &&
            if (subPage == "Admission Approval") it.status == "PENDING" else true
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.STUDENT_MGMT.subMenus, subPage, onSubPageChange)
        SearchAndActionHeader(
            title = "Student Management",
            subtitle = "$subPage • Admissions, roll numbers, ID cards, promotion & transfers",
            searchQuery = query,
            onSearchChange = { query = it },
            primaryActionLabel = "Add Student",
            primaryActionIcon = Icons.Default.Add,
            onPrimaryAction = { showAdd = true }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    EmptyModuleState(
                        icon = Icons.Default.School,
                        title = "No Student Records",
                        subtitle = "Enroll students or review pending admissions from connected portals.",
                        actionLabel = "Add Student",
                        onAction = { showAdd = true }
                    )
                }
            }
            items(filtered, key = { it.id }) { st ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                            TextButton(onClick = { onUpdateStatus(st.id, "TRANSFERRED") }) { Text("Transfer") }
                            IconButton(onClick = { deleteCandidate = st }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Student", tint = MvciRoseError)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentPortalScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    students: List<StudentRecord>,
    onSaveStudent: (StudentRecord, String?) -> Unit
) {
    var attendanceVisible by remember { mutableStateOf(true) }
    var syllabusVisible by remember { mutableStateOf(true) }
    var resultVisible by remember { mutableStateOf(true) }
    var feeVisible by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.PARENT_PORTAL.subMenus, subPage, onSubPageChange)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Parent Portal Visibility & Permissions", style = MaterialTheme.typography.titleLarge)
                        VisibilityRow("Attendance Visibility", attendanceVisible) { attendanceVisible = it }
                        VisibilityRow("Syllabus Visibility", syllabusVisible) { syllabusVisible = it }
                        VisibilityRow("Result Visibility", resultVisible) { resultVisible = it }
                        VisibilityRow("Fee Visibility", feeVisible) { feeVisible = it }
                    }
                }
            }
            items(students, key = { it.id }) { st ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Parent of ${st.name}", style = MaterialTheme.typography.titleMedium)
                            Text("${st.parentName.ifBlank { "Unassigned" }} • ${st.parentPhone.ifBlank { "No phone" }}", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { onSaveStudent(st.copy(parentLinked = !st.parentLinked), st.id) }) {
                            Text(if (st.parentLinked) "Linked" else "Link Parent")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisibilityRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
