package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ExamConfig
import com.example.data.model.ExamQuestion
import com.example.data.model.ExamStatus
import com.example.data.model.StudentExamState
import com.example.data.model.StudentUser
import com.example.data.repository.CbtRepository
import com.example.util.AlarmSoundHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class Screen {
    object RoleSelection : Screen()
    object AdminLogin : Screen()
    object AdminDashboard : Screen()
    object StudentLogin : Screen()
    object StudentDashboard : Screen()
    object StudentExam : Screen()
    object StudentResult : Screen()
}

class CbtViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CbtRepository
    private val alarmHelper = AlarmSoundHelper(application)

    // Current Screen Navigation
    private val _currentScreen = MutableStateFlow<Screen>(Screen.RoleSelection)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Logged in student
    private val _currentStudent = MutableStateFlow<StudentUser?>(null)
    val currentStudent: StateFlow<StudentUser?> = _currentStudent.asStateFlow()

    // Observable flows from repository
    val students: StateFlow<List<StudentUser>> = repository.students
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val questions: StateFlow<List<ExamQuestion>> = repository.questions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val examConfig: StateFlow<ExamConfig> = repository.examConfig
        .stateIn(viewModelScope, SharingStarted.Lazily, ExamConfig())

    val studentSessions: StateFlow<Map<String, StudentExamState>> = repository.studentSessions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Admin Proctor selected student for Live Screen Inspection
    private val _proctoredStudentId = MutableStateFlow<String?>(null)
    val proctoredStudentId: StateFlow<String?> = _proctoredStudentId.asStateFlow()

    // Global Alarm State for active student
    private val _isAlarmPlaying = MutableStateFlow(false)
    val isAlarmPlaying: StateFlow<Boolean> = _isAlarmPlaying.asStateFlow()

    // Timer Job for active student exam
    private var examTimerJob: Job? = null

    init {
        // Observe student session alarm state to trigger audio alarm
        viewModelScope.launch {
            studentSessions.collect { sessions ->
                val currentStd = _currentStudent.value
                if (currentStd != null) {
                    val session = sessions[currentStd.id]
                    if (session?.isAlarmActive == true) {
                        if (!_isAlarmPlaying.value) {
                            _isAlarmPlaying.value = true
                            alarmHelper.startEmergencyAlarm()
                        }
                    } else {
                        if (_isAlarmPlaying.value) {
                            _isAlarmPlaying.value = false
                            alarmHelper.stopAlarm()
                        }
                    }
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Admin Actions ---
    fun loginAdmin(pass: String): Boolean {
        return repository.verifyAdminPassword(pass)
    }

    fun updateAdminPassword(oldPass: String, newPass: String): Boolean {
        return repository.updateAdminPassword(oldPass, newPass)
    }

    fun updateConfig(config: ExamConfig) {
        repository.updateConfig(config)
    }

    fun registerStudent(username: String, pass: String, fullName: String, className: String, nisn: String): Result<StudentUser> {
        return repository.registerStudent(username, pass, fullName, className, nisn)
    }

    fun deleteStudent(id: String) {
        repository.deleteStudent(id)
    }

    fun resetStudentPassword(id: String, newPass: String) {
        repository.resetStudentPassword(id, newPass)
    }

    fun addQuestion(question: ExamQuestion) {
        repository.addQuestion(question)
    }

    fun updateQuestion(question: ExamQuestion) {
        repository.updateQuestion(question)
    }

    fun deleteQuestion(id: Int) {
        repository.deleteQuestion(id)
    }

    fun resetQuestionsToDefault() {
        repository.resetQuestionsToDefault()
    }

    fun setProctoredStudent(studentId: String?) {
        _proctoredStudentId.value = studentId
    }

    fun adminSendWarning(studentId: String, message: String) {
        repository.adminSendWarning(studentId, message)
    }

    fun adminForceSubmit(studentId: String) {
        repository.adminForceSubmit(studentId)
    }

    fun adminResetExam(studentId: String) {
        repository.adminResetExam(studentId)
    }

    fun adminUnlockExam(studentId: String) {
        repository.adminUnlockExam(studentId)
    }

    // --- Student Actions ---
    fun loginStudent(username: String, pass: String): Boolean {
        val student = repository.loginStudent(username, pass)
        if (student != null) {
            _currentStudent.value = student
            repository.getOrCreateSession(student)
            _currentScreen.value = Screen.StudentDashboard
            return true
        }
        return false
    }

    fun logoutStudent() {
        _currentStudent.value = null
        stopExamTimer()
        alarmHelper.stopAlarm()
        _isAlarmPlaying.value = false
        _currentScreen.value = Screen.RoleSelection
    }

    fun startStudentExam() {
        val student = _currentStudent.value ?: return
        repository.startExam(student.id)
        _currentScreen.value = Screen.StudentExam
        startExamTimer(student.id)
    }

    private fun startExamTimer(studentId: String) {
        examTimerJob?.cancel()
        examTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                repository.tickTimer(studentId)
                val session = repository.studentSessions.value[studentId]
                if (session?.status == ExamStatus.SUBMITTED) {
                    stopExamTimer()
                    _currentScreen.value = Screen.StudentResult
                    break
                }
            }
        }
    }

    private fun stopExamTimer() {
        examTimerJob?.cancel()
        examTimerJob = null
    }

    fun selectAnswer(questionId: Int, optionIndex: Int) {
        val student = _currentStudent.value ?: return
        repository.selectAnswer(student.id, questionId, optionIndex)
    }

    fun toggleDoubtful(questionId: Int) {
        val student = _currentStudent.value ?: return
        repository.toggleDoubtful(student.id, questionId)
    }

    fun goToQuestion(index: Int) {
        val student = _currentStudent.value ?: return
        repository.setCurrentQuestionIndex(student.id, index)
    }

    fun triggerAppExitedViolation(reason: String = "Terdeteksi keluar dari aplikasi CBT") {
        val student = _currentStudent.value ?: return
        repository.recordViolation(student.id, reason)
    }

    fun triggerAppResumed() {
        val student = _currentStudent.value ?: return
        repository.reportAppResumed(student.id)
    }

    fun dismissAlarm() {
        val student = _currentStudent.value ?: return
        repository.dismissAlarm(student.id)
        alarmHelper.stopAlarm()
        _isAlarmPlaying.value = false
    }

    fun clearAdminWarning() {
        val student = _currentStudent.value ?: return
        repository.adminClearWarning(student.id)
    }

    fun finishExam() {
        val student = _currentStudent.value ?: return
        stopExamTimer()
        alarmHelper.stopAlarm()
        _isAlarmPlaying.value = false
        repository.submitExam(student.id)
        _currentScreen.value = Screen.StudentResult
    }

    override fun onCleared() {
        super.onCleared()
        stopExamTimer()
        alarmHelper.release()
    }
}
