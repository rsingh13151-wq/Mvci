package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.example.model.ContentItem
import com.example.model.CourseRecord
import com.example.model.ExamRecord
import com.example.model.MenuModule
import com.example.model.PaymentRecord
import com.example.model.StudentRecord
import com.example.ui.components.EmptyModuleState
import com.example.ui.components.SearchAndActionHeader
import com.example.ui.components.StatusPill
import com.example.ui.components.SubPageTabsStrip
import com.example.ui.theme.MvciAmberAccent
import com.example.ui.theme.MvciCyanAccent
import com.example.ui.theme.MvciEmerald
import com.example.ui.theme.MvciRoseError

@Composable
fun AttendanceManagementScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    students: List<StudentRecord>,
    onMarkAttendance: (StudentRecord, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.ATTENDANCE_MGMT.subMenus, subPage, onSubPageChange)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("$subPage — Automated Parent Alerts Enabled", style = MaterialTheme.typography.titleMedium)
                        Text("Marking a student Absent automatically triggers the Absent Parent Alert workflow for linked parents.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (students.isEmpty()) {
                item {
                    EmptyModuleState(
                        icon = Icons.Default.CheckCircle,
                        title = "No Students Enrolled",
                        subtitle = "Enroll students in Student Management to mark daily attendance."
                    )
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
                            Text(st.name, style = MaterialTheme.typography.titleMedium)
                            Text("${st.rollNo} • ${st.batchName} • ${st.attendancePct}%", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(
                                onClick = { onMarkAttendance(st, true) },
                                colors = ButtonDefaults.buttonColors(containerColor = MvciEmerald)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Present", modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("P")
                            }
                            OutlinedButton(onClick = { onMarkAttendance(st, false) }) {
                                Icon(Icons.Default.Close, contentDescription = "Absent", modifier = Modifier.size(16.dp), tint = MvciRoseError)
                                Spacer(Modifier.width(4.dp))
                                Text("A", color = MvciRoseError)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExaminationManagementScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    exams: List<ExamRecord>,
    courses: List<CourseRecord>,
    onSaveExam: (ExamRecord, String?) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var course by remember { mutableStateOf(courses.firstOrNull()?.title ?: "ADCA") }
        var examType by remember { mutableStateOf("ONLINE") }
        var date by remember { mutableStateOf("2026-12-10") }
        var qCount by remember { mutableStateOf("50") }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Schedule Examination") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Exam Title") })
                    OutlinedTextField(value = course, onValueChange = { course = it }, label = { Text("Course") })
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ONLINE", "INTERNAL", "FINAL", "PRACTICAL").forEach { t ->
                            FilterChip(selected = examType == t, onClick = { examType = t }, label = { Text(t) })
                        }
                    }
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Scheduled Date") })
                    OutlinedTextField(value = qCount, onValueChange = { qCount = it.filter { c -> c.isDigit() } }, label = { Text("Question Count") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveExam(
                            ExamRecord(
                                title = title,
                                courseName = course,
                                examType = examType,
                                scheduledDate = date,
                                questionCount = qCount.toLongOrNull() ?: 50L,
                                certificatesIssued = 0L,
                                status = "SCHEDULED"
                            ),
                            null
                        )
                        showAdd = false
                    },
                    enabled = title.isNotBlank() && course.isNotBlank() && date.isNotBlank()
                ) { Text("Create Exam") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.EXAM_MGMT.subMenus, subPage, onSubPageChange)
        SearchAndActionHeader(
            title = "Examination Management",
            subtitle = "$subPage • Online exams, question bank, results & certificates",
            searchQuery = "",
            onSearchChange = {},
            primaryActionLabel = "New Exam",
            primaryActionIcon = Icons.Default.Quiz,
            onPrimaryAction = { showAdd = true }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (exams.isEmpty()) {
                item {
                    EmptyModuleState(
                        icon = Icons.Default.Quiz,
                        title = "No Examinations Scheduled",
                        subtitle = "Schedule Online, Internal, Final, or Practical exams and publish results.",
                        actionLabel = "Schedule Exam",
                        onAction = { showAdd = true }
                    )
                }
            }
            items(exams, key = { it.id }) { ex ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(ex.title, style = MaterialTheme.typography.titleMedium)
                                Text("${ex.courseName} • ${ex.examType} • ${ex.scheduledDate}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusPill(ex.status)
                        }
                        Text("Questions: ${ex.questionCount} • Certificates Issued: ${ex.certificatesIssued}", style = MaterialTheme.typography.labelMedium, color = MvciAmberAccent)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (ex.status != "PUBLISHED") {
                                Button(onClick = { onSaveExam(ex.copy(status = "PUBLISHED", certificatesIssued = ex.certificatesIssued + 10L), ex.id) }) {
                                    Text("Publish Result & Issue Certs")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LmsLibraryScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    contents: List<ContentItem>,
    onSaveContent: (ContentItem) -> Unit,
    onDeleteContent: (String) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        var title by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("LMS_PDF") }
        var desc by remember { mutableStateOf("") }
        var url by remember { mutableStateOf("https://www.mvci.online/library/") }
        var downloadable by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Publish Digital Library Content") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
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
                        onSaveContent(
                            ContentItem(
                                title = title,
                                moduleCategory = category,
                                description = desc,
                                urlOrMeta = url,
                                downloadable = downloadable,
                                status = "PUBLISHED"
                            )
                        )
                        showAdd = false
                    },
                    enabled = title.isNotBlank()
                ) { Text("Publish") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    val lmsItems = contents.filter { it.moduleCategory.startsWith("LMS_") }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.LMS_LIBRARY.subMenus, subPage, onSubPageChange)
        SearchAndActionHeader(
            title = "LMS & Digital Library",
            subtitle = "$subPage • Study PDFs, video lectures, e-books & download permissions",
            searchQuery = "",
            onSearchChange = {},
            primaryActionLabel = "Add Material",
            primaryActionIcon = Icons.Default.LibraryBooks,
            onPrimaryAction = { showAdd = true }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (lmsItems.isEmpty()) {
                item {
                    EmptyModuleState(
                        icon = Icons.Default.LibraryBooks,
                        title = "Digital Library Ready",
                        subtitle = "Publish PDFs, video lectures, digital books, or assignments for enrolled students.",
                        actionLabel = "Upload Study Material",
                        onAction = { showAdd = true }
                    )
                }
            }
            items(lmsItems, key = { it.id }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text("${item.moduleCategory} • Download: ${if (item.downloadable) "Allowed" else "Restricted"}", style = MaterialTheme.typography.bodySmall)
                            if (item.urlOrMeta.isNotBlank()) Text(item.urlOrMeta, style = MaterialTheme.typography.labelSmall, color = MvciCyanAccent)
                        }
                        IconButton(onClick = { onDeleteContent(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Content", tint = MvciRoseError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinanceAccountingScreen(
    subPage: String,
    onSubPageChange: (String) -> Unit,
    payments: List<PaymentRecord>,
    students: List<StudentRecord>,
    onSavePayment: (PaymentRecord, String?) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        var studentName by remember { mutableStateOf(students.firstOrNull()?.name ?: "") }
        var branch by remember { mutableStateOf("Main Campus") }
        var category by remember { mutableStateOf("COURSE_FEE") }
        var amount by remember { mutableStateOf("2500") }
        var mode by remember { mutableStateOf("UPI") }
        var receipt by remember { mutableStateOf("MVCI-RCP-${1000 + payments.size + 1}") }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("Record Fee / Ledger Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = studentName, onValueChange = { studentName = it }, label = { Text("Payer / Student Name") })
                    OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("Branch") })
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("COURSE_FEE", "ADMISSION", "EXAM_FEE", "FRANCHISE_FEE", "EXPENSE", "REFUND").forEach { c ->
                            FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                        }
                    }
                    OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } }, label = { Text("Amount (INR)") })
                    OutlinedTextField(value = mode, onValueChange = { mode = it }, label = { Text("Payment Mode (UPI / Cash / Gateway)") })
                    OutlinedTextField(value = receipt, onValueChange = { receipt = it }, label = { Text("Receipt No") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSavePayment(
                            PaymentRecord(
                                studentName = studentName,
                                branchName = branch,
                                category = category,
                                amount = amount.toLongOrNull() ?: 0L,
                                paymentMode = mode,
                                receiptNo = receipt,
                                status = if (category == "REFUND") "REFUNDED" else "VERIFIED"
                            ),
                            null
                        )
                        showAdd = false
                    },
                    enabled = studentName.isNotBlank() && branch.isNotBlank() && mode.isNotBlank() && receipt.isNotBlank() && (amount.toLongOrNull() ?: 0L) > 0L
                ) { Text("Save Receipt") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(MenuModule.FINANCE_ACCOUNTING.subMenus, subPage, onSubPageChange)
        SearchAndActionHeader(
            title = "Finance & Accounting",
            subtitle = "$subPage • Fee receipts, verification, refunds, expenses & gateway",
            searchQuery = "",
            onSearchChange = {},
            primaryActionLabel = "Record Payment",
            primaryActionIcon = Icons.Default.Payments,
            onPrimaryAction = { showAdd = true }
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (payments.isEmpty()) {
                item {
                    EmptyModuleState(
                        icon = Icons.Default.Payments,
                        title = "No Transactions Recorded",
                        subtitle = "Record course fees, admission payments, franchise fees, or expenses.",
                        actionLabel = "Record Payment",
                        onAction = { showAdd = true }
                    )
                }
            }
            items(payments, key = { it.id }) { p ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text("${p.studentName} • INR ${p.amount}", style = MaterialTheme.typography.titleMedium)
                                Text("${p.receiptNo} • ${p.category} (${p.paymentMode}) • ${p.branchName}", style = MaterialTheme.typography.bodySmall)
                            }
                            StatusPill(p.status)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (p.status == "PENDING") {
                                Button(onClick = { onSavePayment(p.copy(status = "VERIFIED"), p.id) }) { Text("Verify") }
                            }
                            if (p.status == "VERIFIED") {
                                OutlinedButton(onClick = { onSavePayment(p.copy(status = "REFUNDED"), p.id) }) { Text("Mark Refunded") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunicationAndMobileAppScreen(
    module: MenuModule,
    subPage: String,
    onSubPageChange: (String) -> Unit,
    contents: List<ContentItem>,
    onSaveContent: (ContentItem) -> Unit,
    onDeleteContent: (String) -> Unit
) {
    val isComm = module == MenuModule.COMMUNICATION_CENTER
    val targetCat = if (isComm) "COMM_NOTICE" else "APP_VERSION"
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        SubPageTabsStrip(module.subMenus, subPage, onSubPageChange)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (isComm) "$subPage — Broadcast & Gateway Config" else "$subPage — MVCI Mobile App Control",
                            style = MaterialTheme.typography.titleLarge
                        )
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text(if (isComm) "Notice / Template Title" else "App Release / Config Title (e.g. v2.4.0)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = body,
                            onValueChange = { body = it },
                            label = { Text(if (isComm) "Message / Channel Details (SMS, WhatsApp, Email, Push)" else "Release Notes / Branding / Permission Flags") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                onSaveContent(
                                    ContentItem(
                                        title = title,
                                        moduleCategory = targetCat,
                                        description = body,
                                        urlOrMeta = subPage,
                                        downloadable = false,
                                        status = "PUBLISHED"
                                    )
                                )
                                title = ""
                                body = ""
                            },
                            enabled = title.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (isComm) "Publish Communication" else "Save Mobile App Config")
                        }
                    }
                }
            }
            items(contents.filter { it.moduleCategory == targetCat }, key = { it.id }) { item ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text("${item.urlOrMeta} • ${item.description}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { onDeleteContent(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MvciRoseError)
                        }
                    }
                }
            }
        }
    }
}
