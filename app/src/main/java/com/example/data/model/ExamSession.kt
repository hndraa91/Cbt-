package com.example.data.model

enum class ExamStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SUBMITTED,
    LOCKED_VIOLATION
}

data class ViolationRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String,
    val acknowledged: Boolean = false
)

data class StudentExamState(
    val studentId: String,
    val studentName: String,
    val className: String,
    val answers: Map<Int, Int> = emptyMap(), // questionId -> optionIndex
    val doubtfulQuestionIds: Set<Int> = emptySet(),
    val currentQuestionIndex: Int = 0,
    val timeRemainingSeconds: Int = 30 * 60,
    val totalDurationSeconds: Int = 30 * 60,
    val status: ExamStatus = ExamStatus.NOT_STARTED,
    val violationCount: Int = 0,
    val violations: List<ViolationRecord> = emptyList(),
    val isAlarmActive: Boolean = false,
    val score: Int = 0,
    val correctCount: Int = 0,
    val totalQuestions: Int = 0,
    val lastHeartbeat: Long = System.currentTimeMillis(),
    val isFocused: Boolean = true,
    val adminWarningMessage: String? = null
)

data class ExamConfig(
    val title: String = "Ujian CBT PIK-R KUAS SMANDA 2026",
    val description: String = "Materi Triad KRR, Pendewasaan Usia Perkawinan (PUP), Life Skills, dan Konseling Sebaya.",
    val durationMinutes: Int = 30,
    val passingGrade: Int = 75,
    val maxViolations: Int = 3,
    val autoLockOnViolation: Boolean = true
)
