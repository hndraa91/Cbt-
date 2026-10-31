package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Score
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExamConfig
import com.example.data.model.ExamQuestion
import com.example.data.model.ExamStatus
import com.example.data.model.StudentExamState
import com.example.data.model.StudentUser
import com.example.ui.CbtViewModel
import com.example.ui.components.AdminLiveScreenModal
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: CbtViewModel,
    onBackToRoleSelect: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val students by viewModel.students.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val examConfig by viewModel.examConfig.collectAsState()
    val sessions by viewModel.studentSessions.collectAsState()
    val proctoredStudentId by viewModel.proctoredStudentId.collectAsState()

    // Dialog state for adding student
    var showAddStudentDialog by remember { mutableStateOf(false) }

    // Dialog state for adding question
    var showAddQuestionDialog by remember { mutableStateOf(false) }

    // Proctored student modal
    val proctoredSession = proctoredStudentId?.let { id ->
        sessions[id] ?: students.firstOrNull { it.id == id }?.let { std ->
            StudentExamState(studentId = std.id, studentName = std.fullName, className = std.className)
        }
    }

    if (proctoredSession != null) {
        AdminLiveScreenModal(
            session = proctoredSession,
            questions = questions,
            onDismiss = { viewModel.setProctoredStudent(null) },
            onSendWarning = { msg -> viewModel.adminSendWarning(proctoredSession.studentId, msg) },
            onForceSubmit = { viewModel.adminForceSubmit(proctoredSession.studentId) },
            onResetExam = { viewModel.adminResetExam(proctoredSession.studentId) },
            onUnlockExam = { viewModel.adminUnlockExam(proctoredSession.studentId) },
            onDismissAlarm = { viewModel.adminUnlockExam(proctoredSession.studentId) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel Pengawas CBT", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("PIK-R KUAS SMANDA", fontSize = 11.sp, color = AmberAccent)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToRoleSelect, modifier = Modifier.testTag("admin_dashboard_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali ke Beranda")
                    }
                },
                actions = {
                    // Quick indicator of alarm count
                    val activeAlarmsCount = sessions.values.count { it.isAlarmActive }
                    if (activeAlarmsCount > 0) {
                        Surface(
                            color = CrimsonAlert,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$activeAlarmsCount ALARM AKTIF",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                    label = { Text("Monitor", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_monitor")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    label = { Text("Peserta", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_students")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("Soal", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_questions")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Score, contentDescription = null) },
                    label = { Text("Nilai", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_scores")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Pengaturan", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> MonitorTabContent(
                    students = students,
                    sessions = sessions,
                    questionsCount = questions.size,
                    onInspectStudent = { id -> viewModel.setProctoredStudent(id) }
                )
                1 -> StudentsTabContent(
                    students = students,
                    onAddStudent = { showAddStudentDialog = true },
                    onDeleteStudent = { id -> viewModel.deleteStudent(id) },
                    onResetPassword = { id, pass -> viewModel.resetStudentPassword(id, pass) }
                )
                2 -> QuestionsTabContent(
                    questions = questions,
                    onAddQuestion = { showAddQuestionDialog = true },
                    onDeleteQuestion = { id -> viewModel.deleteQuestion(id) },
                    onResetQuestions = { viewModel.resetQuestionsToDefault() }
                )
                3 -> ScoresTabContent(
                    students = students,
                    sessions = sessions,
                    questions = questions,
                    passingGrade = examConfig.passingGrade,
                    onInspectStudent = { id -> viewModel.setProctoredStudent(id) }
                )
                4 -> SettingsTabContent(
                    config = examConfig,
                    onSaveConfig = { newConfig -> viewModel.updateConfig(newConfig) },
                    onChangeAdminPassword = { oldPass, newPass ->
                        viewModel.updateAdminPassword(oldPass, newPass)
                    }
                )
            }
        }
    }

    // Dialog for adding student
    if (showAddStudentDialog) {
        AddStudentDialog(
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { username, pass, name, className, nisn ->
                val result = viewModel.registerStudent(username, pass, name, className, nisn)
                if (result.isSuccess) {
                    showAddStudentDialog = false
                }
            }
        )
    }

    // Dialog for adding question
    if (showAddQuestionDialog) {
        AddQuestionDialog(
            nextId = (questions.maxOfOrNull { it.id } ?: 0) + 1,
            onDismiss = { showAddQuestionDialog = false },
            onConfirm = { q ->
                viewModel.addQuestion(q)
                showAddQuestionDialog = false
            }
        )
    }
}

// ==========================================
// 1. MONITOR TAB CONTENT (REAL-TIME PROCTOR)
// ==========================================
@Composable
fun MonitorTabContent(
    students: List<StudentUser>,
    sessions: Map<String, StudentExamState>,
    questionsCount: Int,
    onInspectStudent: (String) -> Unit
) {
    val activeCount = sessions.values.count { it.status == ExamStatus.IN_PROGRESS }
    val alarmCount = sessions.values.count { it.isAlarmActive }
    val violationsCount = sessions.values.sumOf { it.violationCount }
    val finishedCount = sessions.values.count { it.status == ExamStatus.SUBMITTED }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary KPI Cards Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiCard(
                    title = "Aktif Ujian",
                    value = activeCount.toString(),
                    color = EmeraldSuccess,
                    icon = Icons.Default.HourglassTop,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Alarm Berbunyi",
                    value = alarmCount.toString(),
                    color = CrimsonAlert,
                    icon = Icons.Default.NotificationsActive,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Pelanggaran",
                    value = violationsCount.toString(),
                    color = AmberAccent,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Selesai",
                    value = finishedCount.toString(),
                    color = CyanAccent,
                    icon = Icons.Default.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Status Peserta Real-Time",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Klik kartu siswa untuk menampilkan layar ujian secara langsung.",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                }
            }
        }

        // Student monitor items
        items(students) { std ->
            val session = sessions[std.id]
            StudentMonitorCard(
                student = std,
                session = session,
                totalQuestions = questionsCount,
                onClick = { onInspectStudent(std.id) }
            )
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = title, fontSize = 10.sp, color = Slate700, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun StudentMonitorCard(
    student: StudentUser,
    session: StudentExamState?,
    totalQuestions: Int,
    onClick: () -> Unit
) {
    val isAlarm = session?.isAlarmActive == true
    val status = session?.status ?: ExamStatus.NOT_STARTED
    val answeredCount = session?.answers?.size ?: 0
    val timeRemaining = session?.timeRemainingSeconds ?: (30 * 60)
    val minutes = timeRemaining / 60
    val seconds = timeRemaining % 60
    val timeString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isAlarm) 2.dp else 1.dp,
                color = if (isAlarm) CrimsonAlert else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("student_card_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlarm) CrimsonAlert.copy(alpha = 0.08f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Student name, class, and status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.fullName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "${student.className} • NISN: ${student.nisn} • @${student.username}",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                }

                // Status Badge
                StatusBadge(status = status, isAlarm = isAlarm)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row: Waktu tersisa, Progress dijawab, Pelanggaran
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate100, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (status == ExamStatus.IN_PROGRESS) "Sisa: $timeString" else "Waktu: 30:00",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                }

                Text(
                    text = "Dijawab: $answeredCount/$totalQuestions",
                    fontSize = 12.sp,
                    color = Slate700,
                    fontWeight = FontWeight.Medium
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = if ((session?.violationCount ?: 0) > 0) AmberAccent else Slate700,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${session?.violationCount ?: 0}x Keluar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if ((session?.violationCount ?: 0) > 0) AmberAccent else Slate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Button: Pantau Layar Siswa
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("btn_pantau_layar_${student.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAlarm) CrimsonAlert else NavyPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = if (isAlarm) Icons.Default.NotificationsActive else Icons.Default.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAlarm) "ALARM AKTIF! Buka Layar Siswa" else "Tampilkan Layar Real-Time",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: ExamStatus, isAlarm: Boolean) {
    if (isAlarm) {
        Surface(
            color = CrimsonAlert,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ALARM KELUAR!",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        return
    }

    val (bgColor, textColor, text) = when (status) {
        ExamStatus.NOT_STARTED -> Triple(Slate200, Slate700, "Belum Mulai")
        ExamStatus.IN_PROGRESS -> Triple(EmeraldSuccess.copy(alpha = 0.15f), EmeraldSuccess, "Mengerjakan")
        ExamStatus.SUBMITTED -> Triple(CyanAccent.copy(alpha = 0.15f), CyanAccent, "Selesai")
        ExamStatus.LOCKED_VIOLATION -> Triple(CrimsonAlert.copy(alpha = 0.15f), CrimsonAlert, "Terkunci")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ==========================================
// 2. STUDENTS TAB CONTENT (REGISTRATION)
// ==========================================
@Composable
fun StudentsTabContent(
    students: List<StudentUser>,
    onAddStudent: () -> Unit,
    onDeleteStudent: (String) -> Unit,
    onResetPassword: (String, String) -> Unit
) {
    var resetPassId by remember { mutableStateOf<String?>(null) }
    var newPasswordInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Daftar Akun Peserta CBT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Admin mendaftarkan akun username & password untuk siswa.",
                    fontSize = 12.sp,
                    color = Slate700
                )
            }

            Button(
                onClick = onAddStudent,
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_add_student")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Daftar Siswa", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(students) { std ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = std.fullName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "Kelas: ${std.className} | NISN: ${std.nisn}",
                                fontSize = 12.sp,
                                color = Slate700
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Slate100,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Username: ${std.username}  |  Password: ${std.password}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NavyPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                resetPassId = std.id
                                newPasswordInput = ""
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Ganti Password", tint = CyanAccent)
                            }
                            IconButton(onClick = { onDeleteStudent(std.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = CrimsonAlert)
                            }
                        }
                    }
                }
            }
        }
    }

    if (resetPassId != null) {
        Dialog(onDismissRequest = { resetPassId = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Ubah Password Siswa", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newPasswordInput,
                        onValueChange = { newPasswordInput = it },
                        label = { Text("Password Baru") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = { resetPassId = null }) {
                            Text("Batal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                resetPassId?.let { id ->
                                    if (newPasswordInput.isNotBlank()) {
                                        onResetPassword(id, newPasswordInput)
                                    }
                                }
                                resetPassId = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. QUESTIONS TAB CONTENT (QUESTION BANK)
// ==========================================
@Composable
fun QuestionsTabContent(
    questions: List<ExamQuestion>,
    onAddQuestion: () -> Unit,
    onDeleteQuestion: (Int) -> Unit,
    onResetQuestions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Bank Soal CBT (${questions.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Modul Edukasi PIK-R KUAS SMANDA",
                    fontSize = 12.sp,
                    color = AmberAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row {
                IconButton(onClick = onResetQuestions) {
                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset Bawaan", tint = Slate700)
                }
                Button(
                    onClick = onAddQuestion,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_add_question")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Soal", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(questions) { q ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = NavyPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "No. ${q.id} • ${q.category}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            IconButton(onClick = { onDeleteQuestion(q.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = CrimsonAlert)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = q.questionText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate900
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        q.options.forEachIndexed { idx, opt ->
                            val isCorrect = idx == q.correctAnswerIndex
                            val letter = ('A' + idx).toString()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .background(
                                        if (isCorrect) EmeraldSuccess.copy(alpha = 0.1f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$letter.",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isCorrect) EmeraldSuccess else Slate700
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = opt,
                                    fontSize = 12.sp,
                                    color = if (isCorrect) EmeraldSuccess else Slate700,
                                    fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. SCORES TAB CONTENT (RECAP & RESULTS)
// ==========================================
@Composable
fun ScoresTabContent(
    students: List<StudentUser>,
    sessions: Map<String, StudentExamState>,
    questions: List<ExamQuestion>,
    passingGrade: Int,
    onInspectStudent: (String) -> Unit
) {
    val submittedList = students.mapNotNull { std ->
        val s = sessions[std.id]
        if (s?.status == ExamStatus.SUBMITTED) Pair(std, s) else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Rekap Nilai & Hasil Ujian",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Text(
            text = "Batas Kelulusan (KKM): $passingGrade | Total Peserta Mengumpulkan: ${submittedList.size}",
            fontSize = 12.sp,
            color = Slate700
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (submittedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Score, contentDescription = null, tint = Slate200, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum ada siswa yang mengumpulkan ujian.",
                        color = Slate700,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(submittedList) { (std, s) ->
                    val isPassed = s.score >= passingGrade

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInspectStudent(std.id) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = std.fullName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "${std.className} • Benar ${s.correctCount}/${s.totalQuestions}",
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                                if (s.violationCount > 0) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Catatan: ${s.violationCount}x keluar aplikasi selama tes",
                                        fontSize = 11.sp,
                                        color = CrimsonAlert,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${s.score}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isPassed) EmeraldSuccess else CrimsonAlert
                                )
                                Surface(
                                    color = if (isPassed) EmeraldSuccess.copy(alpha = 0.15f) else CrimsonAlert.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isPassed) "LULUS" else "REMEDIAL",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPassed) EmeraldSuccess else CrimsonAlert,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. SETTINGS TAB CONTENT (CONFIG & ADMIN PASS)
// ==========================================
@Composable
fun SettingsTabContent(
    config: ExamConfig,
    onSaveConfig: (ExamConfig) -> Unit,
    onChangeAdminPassword: (String, String) -> Boolean
) {
    var title by remember { mutableStateOf(config.title) }
    var duration by remember { mutableStateOf(config.durationMinutes.toString()) }
    var kkm by remember { mutableStateOf(config.passingGrade.toString()) }
    var maxViolations by remember { mutableStateOf(config.maxViolations.toString()) }
    var configSavedMessage by remember { mutableStateOf(false) }

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var passChangeMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Pengaturan Ujian & Keamanan",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )

        // Exam parameters card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Parameter Ujian CBT", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Ujian") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Durasi (Menit)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = kkm,
                        onValueChange = { kkm = it },
                        label = { Text("KKM (Kelulusan)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = maxViolations,
                    onValueChange = { maxViolations = it },
                    label = { Text("Maksimal Pelanggaran Keluar (Auto-Lock)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val dur = duration.toIntOrNull() ?: 30
                        val passG = kkm.toIntOrNull() ?: 75
                        val maxV = maxViolations.toIntOrNull() ?: 3
                        onSaveConfig(
                            config.copy(
                                title = title,
                                durationMinutes = dur,
                                passingGrade = passG,
                                maxViolations = maxV
                            )
                        )
                        configSavedMessage = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simpan Pengaturan Ujian")
                }

                if (configSavedMessage) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Pengaturan berhasil diperbarui!", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // Change Admin Password Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Ubah Password Admin", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Text(text = "Password ini digunakan untuk mengunci akses panel admin pengawas.", fontSize = 12.sp, color = Slate700)

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = oldPass,
                    onValueChange = { oldPass = it },
                    label = { Text("Password Admin Saat Ini") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("Password Admin Baru") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        if (onChangeAdminPassword(oldPass, newPass)) {
                            passChangeMessage = "Password admin berhasil diganti!"
                            oldPass = ""
                            newPass = ""
                        } else {
                            passChangeMessage = "Password lama salah! Tidak dapat mengganti password."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Perbarui Password Admin")
                }

                passChangeMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = msg,
                        color = if (msg.contains("berhasil")) EmeraldSuccess else CrimsonAlert,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Dialog to Add Student
@Composable
fun AddStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (username: String, pass: String, fullName: String, className: String, nisn: String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("XI MIPA") }
    var nisn by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Daftarkan Peserta Ujian Baru", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nama Lengkap Siswa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Kelas") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = nisn,
                        onValueChange = { nisn = it },
                        label = { Text("NISN") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username Login") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password Login") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (username.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()) {
                                onConfirm(username, password, fullName, className, nisn)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Daftarkan")
                    }
                }
            }
        }
    }
}

// Dialog to Add Question
@Composable
fun AddQuestionDialog(
    nextId: Int,
    onDismiss: () -> Unit,
    onConfirm: (ExamQuestion) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Triad KRR") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var optE by remember { mutableStateOf("") }
    var correctIndex by remember { mutableIntStateOf(0) }
    var explanation by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(text = "Tambah Soal Ujian Baru", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Kategori (misal: Triad KRR, PUP, NAPZA)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("Teks Pertanyaan Soal") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Pilihan Ganda:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Pilihan A") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Pilihan B") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Pilihan C") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("Pilihan D") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = optE, onValueChange = { optE = it }, label = { Text("Pilihan E") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Kunci Jawaban Benar (0=A, 1=B, 2=C, 3=D, 4=E):", fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("A", "B", "C", "D", "E").forEachIndexed { i, letter ->
                        Surface(
                            color = if (correctIndex == i) NavyPrimary else Slate100,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .clickable { correctIndex = i }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = letter,
                                color = if (correctIndex == i) Color.White else Slate900,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Pembahasan / Penjelasan Jawaban") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val options = listOf(optA, optB, optC, optD, optE).filter { it.isNotBlank() }
                            if (questionText.isNotBlank() && options.size >= 2) {
                                onConfirm(
                                    ExamQuestion(
                                        id = nextId,
                                        questionText = questionText,
                                        options = options,
                                        correctAnswerIndex = correctIndex.coerceIn(0, options.size - 1),
                                        category = category,
                                        explanation = explanation
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Simpan Soal")
                    }
                }
            }
        }
    }
}
