package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.model.ExamQuestion
import com.example.data.model.ExamStatus
import com.example.data.model.StudentExamState
import com.example.ui.CbtViewModel
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExamScreen(
    viewModel: CbtViewModel,
    studentId: String
) {
    val questions by viewModel.questions.collectAsState()
    val sessions by viewModel.studentSessions.collectAsState()
    val session = sessions[studentId] ?: StudentExamState(studentId = studentId, studentName = "", className = "")

    var showQuestionGridSheet by remember { mutableStateOf(false) }
    var showSubmitConfirmDialog by remember { mutableStateOf(false) }
    var showExitAttemptDialog by remember { mutableStateOf(false) }

    // Intercept back button to prevent leaving exam accidentally
    BackHandler {
        showExitAttemptDialog = true
    }

    // Lifecycle monitoring: detect if student switches away or minimizes the app!
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                // Student left or minimized the app! Trigger automatic alarm and report to admin
                if (session.status == ExamStatus.IN_PROGRESS) {
                    viewModel.triggerAppExitedViolation("Beralih aplikasi / Minimize layar")
                }
            } else if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.triggerAppResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currentIdx = session.currentQuestionIndex.coerceIn(0, (questions.size - 1).coerceAtLeast(0))
    val currentQuestion = if (questions.isNotEmpty()) questions[currentIdx] else null

    // Timer calculation
    val minutes = session.timeRemainingSeconds / 60
    val seconds = session.timeRemainingSeconds % 60
    val timerString = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    val isTimerLow = session.timeRemainingSeconds < 300 // under 5 minutes

    // Pulsing transition for alarm indicator
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("CBT PIK-R SMANDA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("${session.studentName} (${session.className})", fontSize = 11.sp, color = AmberAccent)
                    }
                },
                actions = {
                    // REAL-TIME COUNTDOWN TIMER BADGE
                    Surface(
                        color = if (isTimerLow) CrimsonAlert else Color.White.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("cbt_timer_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.HourglassBottom,
                                contentDescription = null,
                                tint = if (isTimerLow) Color.White else AmberAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timerString,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Question Grid Sheet Button
                    IconButton(
                        onClick = { showQuestionGridSheet = true },
                        modifier = Modifier.testTag("btn_open_question_grid")
                    ) {
                        Icon(Icons.Default.GridOn, contentDescription = "Daftar Soal", tint = Color.White)
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
            // Navigation action footer
            Surface(
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous button
                    OutlinedButton(
                        onClick = {
                            if (currentIdx > 0) {
                                viewModel.goToQuestion(currentIdx - 1)
                            }
                        },
                        enabled = currentIdx > 0,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_prev_question")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sebelumnya")
                    }

                    // Ragu-ragu Checkbox
                    if (currentQuestion != null) {
                        val isDoubtful = session.doubtfulQuestionIds.contains(currentQuestion.id)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.toggleDoubtful(currentQuestion.id) }
                                .background(
                                    if (isDoubtful) AmberAccent.copy(alpha = 0.15f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("btn_toggle_doubtful")
                        ) {
                            Checkbox(
                                checked = isDoubtful,
                                onCheckedChange = { viewModel.toggleDoubtful(currentQuestion.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AmberAccent,
                                    checkmarkColor = Color.White
                                )
                            )
                            Text(
                                text = "Ragu",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDoubtful) AmberAccent else Slate700
                            )
                        }
                    }

                    // Next or Finish button
                    if (currentIdx < questions.size - 1) {
                        Button(
                            onClick = {
                                viewModel.goToQuestion(currentIdx + 1)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_next_question")
                        ) {
                            Text("Berikutnya")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = { showSubmitConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_submit_exam")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kumpulkan")
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Anti-cheat status banner + Simulation button for testing
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Pengawasan Ujian Aktif",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Text(
                                text = "Layar terhubung ke monitor pengawas.",
                                fontSize = 10.sp,
                                color = Slate700
                            )
                        }
                    }

                    // Button to simulate exiting app (easy test trigger in streaming emulator!)
                    OutlinedButton(
                        onClick = {
                            viewModel.triggerAppExitedViolation("Simulasi keluar / minimize aplikasi")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAlert),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_simulate_exit_test")
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Uji Alarm Keluar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (currentQuestion != null) {
                // Question Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                color = NavyPrimary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "SOAL NO. ${currentIdx + 1} DARI ${questions.size}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                color = AmberAccent.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = currentQuestion.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AmberAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentQuestion.questionText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate900,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Options A, B, C, D, E
                        val selectedOption = session.answers[currentQuestion.id]

                        currentQuestion.options.forEachIndexed { optIndex, optionText ->
                            val isSelected = selectedOption == optIndex
                            val letter = ('A' + optIndex).toString()

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.selectAnswer(currentQuestion.id, optIndex)
                                    }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) CyanAccent else Slate200,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .testTag("option_${currentQuestion.id}_$optIndex"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CyanAccent.copy(alpha = 0.08f) else Color.White
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) CyanAccent else Slate100),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = letter,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) Color.White else Slate700
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = optionText,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Slate900 else Slate800,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
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
    // AUTOMATIC ALARM POPUP MODAL (ANTI-CHEAT)
    // ==========================================
    if (session.isAlarmActive) {
        Dialog(
            onDismissRequest = { /* Do not dismiss by clicking outside */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(3.dp, CrimsonAlert, RoundedCornerShape(24.dp)),
                color = Color.White,
                shadowElevation = 24.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CrimsonAlert),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "🚨 PERINGATAN KERAS! 🚨",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CrimsonAlert,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ANDA TERDETEKSI KELUAR DARI APLIKASI UJIAN!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Alarm berbunyi keras secara otomatis. Insiden ini telah dicatat dan dilaporkan langsung ke layar monitor Pengawas.\n\nTotal Pelanggaran Anda: ${session.violationCount} kali.",
                        fontSize = 12.sp,
                        color = Slate700,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.dismissAlarm() },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_dismiss_alarm")
                    ) {
                        Text(
                            text = "Saya Mengerti & Matikan Alarm",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // ==========================================
    // ADMIN WARNING MESSAGE ALERT MODAL
    // ==========================================
    if (session.adminWarningMessage != null) {
        Dialog(onDismissRequest = { viewModel.clearAdminWarning() }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Pesan Peringatan dari Pengawas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = session.adminWarningMessage ?: "", fontSize = 13.sp, color = Slate800, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.clearAdminWarning() },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Saya Mengerti")
                    }
                }
            }
        }
    }

    // Back attempt dialog
    if (showExitAttemptDialog) {
        Dialog(onDismissRequest = { showExitAttemptDialog = false }) {
            Surface(modifier = Modifier.clip(RoundedCornerShape(16.dp)), color = Color.White) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Dilarang Keluar Ujian", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = CrimsonAlert)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Anda tidak diperkenankan keluar selama sesi ujian berlangsung. Jika Anda keluar, alarm otomatis akan berbunyi dan insiden dicatat oleh pengawas.",
                        fontSize = 12.sp,
                        color = Slate700
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = { showExitAttemptDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text("Tetap di Ujian")
                        }
                    }
                }
            }
        }
    }

    // Question Grid Bottom Sheet
    if (showQuestionGridSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuestionGridSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Daftar Nomor Soal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    LegendItem(color = EmeraldSuccess, label = "Dijawab")
                    LegendItem(color = AmberAccent, label = "Ragu-ragu")
                    LegendItem(color = Slate200, label = "Belum")
                }

                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    questions.forEachIndexed { idx, q ->
                        val isCurrent = idx == currentIdx
                        val isAnswered = session.answers.containsKey(q.id)
                        val isDoubtful = session.doubtfulQuestionIds.contains(q.id)

                        val bgColor = when {
                            isDoubtful -> AmberAccent
                            isAnswered -> EmeraldSuccess
                            else -> Slate100
                        }

                        val textColor = if (isDoubtful || isAnswered) Color.White else Slate900

                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bgColor)
                                .border(
                                    width = if (isCurrent) 2.5.dp else 1.dp,
                                    color = if (isCurrent) NavyDark else Slate200,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    viewModel.goToQuestion(idx)
                                    showQuestionGridSheet = false
                                }
                                .testTag("grid_q_${idx + 1}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Submit confirmation dialog
    if (showSubmitConfirmDialog) {
        val totalAnswered = session.answers.size
        val totalDoubtful = session.doubtfulQuestionIds.size
        val totalUnanswered = questions.size - totalAnswered

        Dialog(onDismissRequest = { showSubmitConfirmDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = Color.White
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Konfirmasi Pengumpulan Ujian",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Apakah Anda yakin ingin menyelesaikan dan mengumpulkan ujian ini sekarang?",
                        fontSize = 13.sp,
                        color = Slate700
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate100, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(text = "• Soal Dijawab: $totalAnswered / ${questions.size}", fontSize = 12.sp, color = EmeraldSuccess, fontWeight = FontWeight.SemiBold)
                        Text(text = "• Soal Ragu-ragu: $totalDoubtful", fontSize = 12.sp, color = AmberAccent, fontWeight = FontWeight.SemiBold)
                        Text(text = "• Belum Dijawab: $totalUnanswered", fontSize = 12.sp, color = if (totalUnanswered > 0) CrimsonAlert else Slate700, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { showSubmitConfirmDialog = false }) {
                            Text("Periksa Lagi")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showSubmitConfirmDialog = false
                                viewModel.finishExam()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier.testTag("btn_confirm_submit_exam")
                        ) {
                            Text("Ya, Kumpulkan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 11.sp, color = Slate700)
    }
}
