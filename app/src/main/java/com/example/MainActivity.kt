package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.CbtViewModel
import com.example.ui.Screen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.ExamResultScreen
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.StudentLoginScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: CbtViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val currentStudent by viewModel.currentStudent.collectAsState()
                val sessions by viewModel.studentSessions.collectAsState()
                val questions by viewModel.questions.collectAsState()
                val config by viewModel.examConfig.collectAsState()

                Surface(modifier = Modifier.fillMaxSize()) {
                    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                        when (screen) {
                            Screen.RoleSelection -> {
                                RoleSelectionScreen(
                                    onSelectAdmin = { viewModel.navigateTo(Screen.AdminLogin) },
                                    onSelectStudent = { viewModel.navigateTo(Screen.StudentLogin) }
                                )
                            }
                            Screen.AdminLogin -> {
                                AdminLoginScreen(
                                    onBack = { viewModel.navigateTo(Screen.RoleSelection) },
                                    onLoginSuccess = { viewModel.navigateTo(Screen.AdminDashboard) },
                                    onVerifyPassword = { pass -> viewModel.loginAdmin(pass) }
                                )
                            }
                            Screen.AdminDashboard -> {
                                AdminDashboardScreen(
                                    viewModel = viewModel,
                                    onBackToRoleSelect = { viewModel.navigateTo(Screen.RoleSelection) }
                                )
                            }
                            Screen.StudentLogin -> {
                                StudentLoginScreen(
                                    onBack = { viewModel.navigateTo(Screen.RoleSelection) },
                                    onLogin = { username, pass -> viewModel.loginStudent(username, pass) }
                                )
                            }
                            Screen.StudentDashboard -> {
                                currentStudent?.let { std ->
                                    StudentDashboardScreen(
                                        student = std,
                                        session = sessions[std.id],
                                        examConfig = config,
                                        totalQuestions = questions.size,
                                        onStartExam = { viewModel.startStudentExam() },
                                        onViewResult = { viewModel.navigateTo(Screen.StudentResult) },
                                        onLogout = { viewModel.logoutStudent() }
                                    )
                                } ?: run {
                                    viewModel.navigateTo(Screen.StudentLogin)
                                }
                            }
                            Screen.StudentExam -> {
                                currentStudent?.let { std ->
                                    ExamScreen(
                                        viewModel = viewModel,
                                        studentId = std.id
                                    )
                                } ?: run {
                                    viewModel.navigateTo(Screen.StudentLogin)
                                }
                            }
                            Screen.StudentResult -> {
                                currentStudent?.let { std ->
                                    val session = sessions[std.id]
                                    if (session != null) {
                                        ExamResultScreen(
                                            session = session,
                                            questions = questions,
                                            config = config,
                                            onBackToDashboard = { viewModel.navigateTo(Screen.StudentDashboard) }
                                        )
                                    } else {
                                        viewModel.navigateTo(Screen.StudentDashboard)
                                    }
                                } ?: run {
                                    viewModel.navigateTo(Screen.RoleSelection)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
