package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Score
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExamConfig
import com.example.data.model.ExamStatus
import com.example.data.model.StudentExamState
import com.example.data.model.StudentUser
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(
    student: StudentUser,
    session: StudentExamState?,
    examConfig: ExamConfig,
    totalQuestions: Int,
    onStartExam: () -> Unit,
    onViewResult: () -> Unit,
    onLogout: () -> Unit
) {
    val isSubmitted = session?.status == ExamStatus.SUBMITTED
    val isLocked = session?.status == ExamStatus.LOCKED_VIOLATION

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Ruang Ujian Siswa", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("PIK-R KUAS SMANDA", fontSize = 11.sp, color = AmberAccent)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("student_dash_logout")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Keluar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyDark,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Student Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.fullName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "${student.className} • NISN: ${student.nisn}",
                            fontSize = 13.sp,
                            color = Slate700
                        )
                    }
                }
            }

            // If already submitted, show result banner
            if (isSubmitted && session != null) {
                val isPassed = session.score >= examConfig.passingGrade
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPassed) EmeraldSuccess.copy(alpha = 0.1f) else CrimsonAlert.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Hasil Ujian Anda",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPassed) EmeraldSuccess else CrimsonAlert
                            )
                            Surface(
                                color = if (isPassed) EmeraldSuccess else CrimsonAlert,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isPassed) "LULUS" else "REMEDIAL",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Nilai Akhir: ${session.score} / 100",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Slate900
                        )

                        Text(
                            text = "Jawaban Benar: ${session.correctCount} dari $totalQuestions soal",
                            fontSize = 13.sp,
                            color = Slate700
                        )

                        if (session.violationCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Peringatan keluar aplikasi: ${session.violationCount}x",
                                fontSize = 12.sp,
                                color = CrimsonAlert,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onViewResult,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_view_student_result"),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Score, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lihat Pembahasan Lengkap")
                        }
                    }
                }
            }

            // Locked Violation State
            if (isLocked) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CrimsonAlert.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = CrimsonAlert)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ujian Anda Terkunci!",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonAlert
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Sistem mendeteksi Anda keluar dari aplikasi lebih dari ${examConfig.maxViolations} kali. Silakan temui pengawas ujian untuk membuka kunci akses.",
                            fontSize = 12.sp,
                            color = Slate800
                        )
                    }
                }
            }

            // Active Exam Information Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = examConfig.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = examConfig.description,
                        fontSize = 13.sp,
                        color = Slate700
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Exam Specs Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SpecChip(
                            icon = Icons.Default.HourglassTop,
                            title = "Durasi",
                            value = "${examConfig.durationMinutes} Menit",
                            modifier = Modifier.weight(1f)
                        )
                        SpecChip(
                            icon = Icons.Default.Assignment,
                            title = "Jumlah",
                            value = "$totalQuestions Soal",
                            modifier = Modifier.weight(1f)
                        )
                        SpecChip(
                            icon = Icons.Default.CheckCircle,
                            title = "KKM",
                            value = "${examConfig.passingGrade}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Anti-Cheat Rules Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CrimsonAlert.copy(alpha = 0.08f))
                            .border(1.dp, CrimsonAlert.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = CrimsonAlert,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PERATURAN KEAMANAN & ALARM:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CrimsonAlert
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "1. Dilarang menekan tombol Home, minimize, atau membuka aplikasi lain.\n" +
                                        "2. Sensor sistem akan mendeteksi perpindahan aplikasi secara otomatis.\n" +
                                        "3. ALARM AKAN OTOMATIS BERBUNYI KERAS jika terdeteksi keluar dari layar ujian!\n" +
                                        "4. Layar Anda terpantau langsung di monitor pengawas.",
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = Slate800
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isSubmitted && !isLocked) {
                        Button(
                            onClick = onStartExam,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_start_cbt_exam"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (session?.status == ExamStatus.IN_PROGRESS) "Lanjutkan Mengerjakan Ujian" else "Mulai Mengerjakan Ujian",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Slate100,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 10.sp, color = Slate700)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
        }
    }
}
