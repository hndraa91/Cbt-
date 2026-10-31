package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExamQuestion
import com.example.data.model.ExamStatus
import com.example.data.model.StudentExamState
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminLiveScreenModal(
    session: StudentExamState,
    questions: List<ExamQuestion>,
    onDismiss: () -> Unit,
    onSendWarning: (String) -> Unit,
    onForceSubmit: () -> Unit,
    onResetExam: () -> Unit,
    onUnlockExam: () -> Unit,
    onDismissAlarm: () -> Unit
) {
    var warningInput by remember { mutableStateOf("") }
    var showWarningInput by remember { mutableStateOf(false) }

    val currentQuestion = if (questions.isNotEmpty()) {
        val idx = session.currentQuestionIndex.coerceIn(0, questions.size - 1)
        questions[idx]
    } else null

    val minutes = session.timeRemainingSeconds / 60
    val seconds = session.timeRemainingSeconds % 60
    val timerText = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White,
            shape = RoundedCornerShape(20.dp),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NavyPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = NavyPrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Pantau Layar Siswa Real-Time",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "${session.studentName} • ${session.className}",
                                fontSize = 12.sp,
                                color = Slate700
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("modal_live_screen_close")) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ALARM ALERT BANNER IF ACTIVE
                if (session.isAlarmActive) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = CrimsonAlert.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = CrimsonAlert)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ALARM SISWA SEDANG BERBUNYI!",
                                    color = CrimsonAlert,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Siswa terdeteksi meninggalkan aplikasi ujian! Alarm bersuara keras pada perangkat siswa.",
                                    color = Slate800,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = onDismissAlarm,
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("admin_dismiss_alarm_btn")
                            ) {
                                Text("Matikan", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Status Bar: Time & Focus
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate100, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Waktu: $timerText",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }

                    // Focus badge
                    Surface(
                        color = if (session.isFocused) EmeraldSuccess.copy(alpha = 0.15f) else CrimsonAlert.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (session.isFocused) EmeraldSuccess else CrimsonAlert)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (session.isFocused) "Di Aplikasi Ujian" else "Keluar Aplikasi!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (session.isFocused) EmeraldSuccess else CrimsonAlert
                            )
                        }
                    }

                    // Violation counter
                    Surface(
                        color = if (session.violationCount > 0) AmberAccent.copy(alpha = 0.15f) else Slate200,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Pelanggaran: ${session.violationCount}x",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (session.violationCount > 0) AmberAccent else Slate700,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SIMULATED REAL-TIME STUDENT SCREEN
                Text(
                    text = "Tampilan Layar Siswa (Live Mirror)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .border(2.dp, if (session.isAlarmActive) CrimsonAlert else CyanAccent, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        // Phone status bar mockup
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SOAL NO. ${(session.currentQuestionIndex + 1)} / ${questions.size}",
                                color = AmberAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentQuestion != null && session.doubtfulQuestionIds.contains(currentQuestion.id)) {
                                Surface(
                                    color = AmberAccent.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Ragu-ragu",
                                        color = AmberAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (currentQuestion != null) {
                            Text(
                                text = currentQuestion.questionText,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val studentChoice = session.answers[currentQuestion.id]

                            currentQuestion.options.forEachIndexed { optIndex, optionText ->
                                val isSelected = studentChoice == optIndex
                                val letter = ('A' + optIndex).toString()

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) CyanAccent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)
                                        )
                                        .border(
                                            1.dp,
                                            if (isSelected) CyanAccent else Color.White.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) CyanAccent else Color.Transparent)
                                            .border(1.dp, if (isSelected) CyanAccent else Color.White.copy(alpha = 0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = letter,
                                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = optionText,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Siswa belum membuka soal.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // VIOLATION LOGS
                if (session.violations.isNotEmpty()) {
                    Text(
                        text = "Riwayat Peringatan Keluar Aplikasi (${session.violations.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonAlert
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrimsonAlert.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        session.violations.takeLast(4).forEach { v ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${timeFormatter.format(Date(v.timestamp))} - ${v.reason}",
                                    fontSize = 11.sp,
                                    color = Slate900,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // REMOTE PROCTOR CONTROLS
                Text(
                    text = "Aksi Kontrol Pengawas",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Send warning
                if (!showWarningInput) {
                    OutlinedButton(
                        onClick = { showWarningInput = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_open_send_warning"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim Pesan Teguran ke Siswa", fontSize = 12.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = warningInput,
                            onValueChange = { warningInput = it },
                            placeholder = { Text("Tulis pesan teguran...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("warning_text_input"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (warningInput.isNotBlank()) {
                                    onSendWarning(warningInput)
                                    warningInput = ""
                                    showWarningInput = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_submit_warning")
                        ) {
                            Text("Kirim", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Unlock if locked
                    if (session.status == ExamStatus.LOCKED_VIOLATION) {
                        Button(
                            onClick = onUnlockExam,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_admin_unlock"),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Buka Kunci", fontSize = 12.sp)
                        }
                    }

                    // Force Submit
                    OutlinedButton(
                        onClick = onForceSubmit,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_admin_force_submit"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAlert),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.StopCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paksa Kumpul", fontSize = 12.sp)
                    }

                    // Reset Exam
                    OutlinedButton(
                        onClick = onResetExam,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_admin_reset_exam"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate700),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Ujian", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
