package com.example.data.repository

import com.example.data.model.ExamConfig
import com.example.data.model.ExamQuestion
import com.example.data.model.ExamStatus
import com.example.data.model.StudentExamState
import com.example.data.model.StudentUser
import com.example.data.model.ViolationRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object CbtRepository {

    // --- Admin Authentication ---
    private val _adminPassword = MutableStateFlow("admin123")
    val adminPassword: StateFlow<String> = _adminPassword.asStateFlow()

    fun verifyAdminPassword(password: String): Boolean {
        return password == _adminPassword.value
    }

    fun updateAdminPassword(oldPass: String, newPass: String): Boolean {
        if (oldPass == _adminPassword.value && newPass.isNotBlank()) {
            _adminPassword.value = newPass
            return true
        }
        return false
    }

    // --- Exam Configuration ---
    private val _examConfig = MutableStateFlow(ExamConfig())
    val examConfig: StateFlow<ExamConfig> = _examConfig.asStateFlow()

    fun updateConfig(config: ExamConfig) {
        _examConfig.value = config
    }

    // --- Registered Students ---
    private val initialStudents = listOf(
        StudentUser(
            id = "std_01",
            username = "siswa1",
            password = "123",
            fullName = "Ahmad Rizky Pratama",
            className = "XI MIPA 1",
            nisn = "00723145"
        ),
        StudentUser(
            id = "std_02",
            username = "siswa2",
            password = "123",
            fullName = "Siti Nur Aisyah",
            className = "XI IPS 2",
            nisn = "00723146"
        ),
        StudentUser(
            id = "std_03",
            username = "siswa3",
            password = "123",
            fullName = "Budi Setiawan",
            className = "X-4",
            nisn = "00812301"
        ),
        StudentUser(
            id = "std_04",
            username = "siswa4",
            password = "123",
            fullName = "Dinda Putri Maharani",
            className = "XII MIPA 2",
            nisn = "00698124"
        )
    )

    private val _students = MutableStateFlow<List<StudentUser>>(initialStudents)
    val students: StateFlow<List<StudentUser>> = _students.asStateFlow()

    fun loginStudent(username: String, pass: String): StudentUser? {
        return _students.value.firstOrNull {
            it.username.equals(username.trim(), ignoreCase = true) && it.password == pass.trim()
        }
    }

    fun registerStudent(
        username: String,
        pass: String,
        fullName: String,
        className: String,
        nisn: String
    ): Result<StudentUser> {
        if (username.isBlank() || pass.isBlank() || fullName.isBlank()) {
            return Result.failure(IllegalArgumentException("Semua bidang harus diisi."))
        }
        if (_students.value.any { it.username.equals(username.trim(), ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("Username sudah terdaftar."))
        }

        val newStudent = StudentUser(
            id = "std_" + System.currentTimeMillis(),
            username = username.trim(),
            password = pass.trim(),
            fullName = fullName.trim(),
            className = className.trim().ifBlank { "Umum" },
            nisn = nisn.trim().ifBlank { "-" }
        )
        _students.update { it + newStudent }
        return Result.success(newStudent)
    }

    fun deleteStudent(studentId: String) {
        _students.update { list -> list.filterNot { it.id == studentId } }
        _studentSessions.update { map -> map - studentId }
    }

    fun resetStudentPassword(studentId: String, newPass: String) {
        _students.update { list ->
            list.map { if (it.id == studentId) it.copy(password = newPass) else it }
        }
    }

    // --- Question Bank (PIK-R KUAS SMANDA) ---
    private val defaultQuestions = listOf(
        ExamQuestion(
            id = 1,
            questionText = "Apa kepanjangan dari Triad KRR yang menjadi fokus utama edukasi PIK-R bagi remaja?",
            options = listOf(
                "Seksualitas, HIV/AIDS, dan NAPZA",
                "Kesehatan, Pendidikan, dan Karir",
                "Pernikahan, Keluarga, dan Ekonomi",
                "Mental, Jasmani, dan Spiritual",
                "Sosialisasi, Advokasi, dan Konseling"
            ),
            correctAnswerIndex = 0,
            category = "Triad KRR",
            explanation = "Triad KRR (Kesehatan Reproduksi Remaja) mencakup tiga resiko ancaman utama bagi remaja: Seksualitas (kehamilan tidak diinginkan/aborsi), HIV/AIDS, dan NAPZA."
        ),
        ExamQuestion(
            id = 2,
            questionText = "Berdasarkan program Pendewasaan Usia Perkawinan (PUP) BKKBN, berapakah batas usia minimal ideal untuk menikah?",
            options = listOf(
                "17 tahun bagi perempuan dan 19 tahun bagi laki-laki",
                "19 tahun bagi perempuan dan 21 tahun bagi laki-laki",
                "21 tahun bagi perempuan dan 25 tahun bagi laki-laki",
                "20 tahun bagi perempuan dan 22 tahun bagi laki-laki",
                "22 tahun bagi perempuan dan 24 tahun bagi laki-laki"
            ),
            correctAnswerIndex = 2,
            category = "PUP & GenRe",
            explanation = "BKKBN menetapkan usia minimal menikah ideal adalah 21 tahun untuk perempuan (kesiapan panggul & reproduksi) dan 25 tahun untuk laki-laki (kematangan finansial & mental)."
        ),
        ExamQuestion(
            id = 3,
            questionText = "Di antara media berikut, manakah yang BUKAN merupakan media penularan virus HIV?",
            options = listOf(
                "Hubungan seksual berisiko tanpa pengaman",
                "Berbagi jarum suntik terkontaminasi",
                "Transfusi darah yang tercemar",
                "Gigitan nyamuk, jabat tangan, atau makan bersama",
                "Air Susu Ibu (ASI) dari ibu positif HIV yang belum terapi"
            ),
            correctAnswerIndex = 3,
            category = "HIV & AIDS",
            explanation = "HIV TIDAK menular melalui gigitan serangga, jabat tangan, keringat, batuk, atau penggunaan alat makan bersama. HIV menular lewat darah, cairan kelamin, dan ASI."
        ),
        ExamQuestion(
            id = 4,
            questionText = "Salah satu fungsi konseling sebaya di PIK-R KUAS SMANDA adalah melatih Pendidik Sebaya. Apa peran utama dari seorang Pendidik Sebaya (Peer Educator)?",
            options = listOf(
                "Memberikan vonis hukuman bagi siswa yang melanggar aturan",
                "Menyebarkan informasi dan pengetahuan KRR secara santai kepada teman sebaya",
                "Menggantikan peran dokter spesialis di sekolah",
                "Menentukan kelulusan siswa dalam ujian sekolah",
                "Melakukan razia tas dan gawai siswa secara berkala"
            ),
            correctAnswerIndex = 1,
            category = "Konseling Sebaya",
            explanation = "Pendidik Sebaya (Peer Educator) berperan sebagai agen penyampai informasi edukatif seputar kesehatan reproduksi dan life skills kepada teman sebayanya dengan pendekatan hangat dan setara."
        ),
        ExamQuestion(
            id = 5,
            questionText = "Keterampilan hidup (Life Skills) yang sangat krusial dimiliki remaja untuk menolak ajakan merokok, alkohol, atau narkoba tanpa merusak pertemanan disebut...",
            options = listOf(
                "Perilaku Agresif",
                "Komunikasi Asertif",
                "Sikap Pasif",
                "Konformitas Negatif",
                "Manipulasi Sosial"
            ),
            correctAnswerIndex = 1,
            category = "Life Skills",
            explanation = "Komunikasi asertif adalah kemampuan mengungkapkan pendapat, kebutuhan, dan penolakan secara tegas dan jujur namun tetap sopan dan menghargai orang lain."
        ),
        ExamQuestion(
            id = 6,
            questionText = "Zat adiktif dalam rokok konvensional maupun cairan vape yang menyebabkan ketergantungan fisik dan psikis berat adalah...",
            options = listOf(
                "Nikotin",
                "Tar",
                "Karbon Monoksida",
                "Glukosa",
                "Metanol"
            ),
            correctAnswerIndex = 0,
            category = "NAPZA",
            explanation = "Nikotin adalah senyawa stimulan yang sangat adiktif, memicu pelepasan dopamin di otak dan menyebabkan ketergantungan berat pada pengguna tembakau maupun rokok elektrik/vape."
        ),
        ExamQuestion(
            id = 7,
            questionText = "Dari 8 Fungsi Keluarga, fungsi yang bertugas memberikan rasa aman, kehangatan emosional, dan perlindungan bagi seluruh anggota keluarga adalah...",
            options = listOf(
                "Fungsi Ekonomi dan Lingkungan",
                "Fungsi Perlindungan dan Cinta Kasih",
                "Fungsi Reproduksi semata",
                "Fungsi Sosialisasi Formal",
                "Fungsi Hukum dan Disiplin"
            ),
            correctAnswerIndex = 1,
            category = "8 Fungsi Keluarga",
            explanation = "Fungsi cinta kasih dan perlindungan memastikan keluarga menjadi tempat bernaung pertama yang memberi rasa nyaman, disayangi, dan aman dari bahaya luar."
        ),
        ExamQuestion(
            id = 8,
            questionText = "Pencegahan stunting pada generasi masa depan dimulai sejak masa remaja. Upaya utama yang dapat dilakukan oleh remaja putri adalah...",
            options = listOf(
                "Diet ekstrem agar tetap ramping",
                "Mengonsumsi Tablet Tambah Darah (TTD) secara teratur dan mencegah anemia",
                "Mengurangi asupan protein hewani",
                "Menikah di usia sedini mungkin",
                "Menghindari olahraga agar tidak kelelahan"
            ),
            correctAnswerIndex = 1,
            category = "Kesehatan Remaja",
            explanation = "Remaja putri yang bebas dari anemia dengan konsumsi rutin Tablet Tambah Darah (TTD) dan gizi seimbang akan melahirkan generasi yang sehat dan bebas dari ancaman stunting."
        )
    )

    private val _questions = MutableStateFlow<List<ExamQuestion>>(defaultQuestions)
    val questions: StateFlow<List<ExamQuestion>> = _questions.asStateFlow()

    fun addQuestion(question: ExamQuestion) {
        _questions.update { it + question }
    }

    fun updateQuestion(question: ExamQuestion) {
        _questions.update { list -> list.map { if (it.id == question.id) question else it } }
    }

    fun deleteQuestion(id: Int) {
        _questions.update { list -> list.filterNot { it.id == id } }
    }

    fun resetQuestionsToDefault() {
        _questions.value = defaultQuestions
    }

    // --- Student Exam Sessions (Proctored Live State) ---
    private val _studentSessions = MutableStateFlow<Map<String, StudentExamState>>(emptyMap())
    val studentSessions: StateFlow<Map<String, StudentExamState>> = _studentSessions.asStateFlow()

    fun getOrCreateSession(student: StudentUser): StudentExamState {
        val existing = _studentSessions.value[student.id]
        if (existing != null) return existing

        val config = _examConfig.value
        val initialSession = StudentExamState(
            studentId = student.id,
            studentName = student.fullName,
            className = student.className,
            timeRemainingSeconds = config.durationMinutes * 60,
            totalDurationSeconds = config.durationMinutes * 60,
            totalQuestions = _questions.value.size,
            status = ExamStatus.NOT_STARTED
        )
        _studentSessions.update { it + (student.id to initialSession) }
        return initialSession
    }

    fun startExam(studentId: String) {
        val config = _examConfig.value
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            map + (studentId to cur.copy(
                status = ExamStatus.IN_PROGRESS,
                totalQuestions = _questions.value.size,
                timeRemainingSeconds = config.durationMinutes * 60,
                totalDurationSeconds = config.durationMinutes * 60,
                isFocused = true,
                isAlarmActive = false,
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }

    fun selectAnswer(studentId: String, questionId: Int, optionIndex: Int) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            val updatedAnswers = cur.answers.toMutableMap()
            updatedAnswers[questionId] = optionIndex
            map + (studentId to cur.copy(
                answers = updatedAnswers,
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }

    fun toggleDoubtful(studentId: String, questionId: Int) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            val updated = cur.doubtfulQuestionIds.toMutableSet()
            if (updated.contains(questionId)) {
                updated.remove(questionId)
            } else {
                updated.add(questionId)
            }
            map + (studentId to cur.copy(
                doubtfulQuestionIds = updated,
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }

    fun setCurrentQuestionIndex(studentId: String, index: Int) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            map + (studentId to cur.copy(
                currentQuestionIndex = index.coerceIn(0, (_questions.value.size - 1).coerceAtLeast(0)),
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }

    fun tickTimer(studentId: String) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            if (cur.status != ExamStatus.IN_PROGRESS) return@update map

            val newRemaining = cur.timeRemainingSeconds - 1
            if (newRemaining <= 0) {
                // Auto submit when time expires
                calculateAndSubmitSession(cur.copy(timeRemainingSeconds = 0))
                map + (studentId to cur.copy(
                    timeRemainingSeconds = 0,
                    status = ExamStatus.SUBMITTED
                ))
            } else {
                map + (studentId to cur.copy(
                    timeRemainingSeconds = newRemaining,
                    lastHeartbeat = System.currentTimeMillis()
                ))
            }
        }
    }

    fun recordViolation(studentId: String, reason: String = "Keluar / Minimize Aplikasi Ujian") {
        val config = _examConfig.value
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            if (cur.status != ExamStatus.IN_PROGRESS) return@update map

            val newCount = cur.violationCount + 1
            val newViolations = cur.violations + ViolationRecord(reason = reason)
            val shouldLock = config.autoLockOnViolation && newCount >= config.maxViolations

            map + (studentId to cur.copy(
                violationCount = newCount,
                violations = newViolations,
                isAlarmActive = true,
                isFocused = false,
                status = if (shouldLock) ExamStatus.LOCKED_VIOLATION else cur.status,
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }

    fun reportAppResumed(studentId: String) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            map + (studentId to cur.copy(
                isFocused = true,
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }

    fun dismissAlarm(studentId: String) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            map + (studentId to cur.copy(
                isAlarmActive = false,
                isFocused = true,
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }

    fun submitExam(studentId: String) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            val finalSubmitted = calculateAndSubmitSession(cur)
            map + (studentId to finalSubmitted)
        }
    }

    private fun calculateAndSubmitSession(session: StudentExamState): StudentExamState {
        val allQuestions = _questions.value
        var correct = 0
        allQuestions.forEach { q ->
            val chosen = session.answers[q.id]
            if (chosen != null && chosen == q.correctAnswerIndex) {
                correct++
            }
        }
        val calculatedScore = if (allQuestions.isNotEmpty()) {
            ((correct.toDouble() / allQuestions.size.toDouble()) * 100).toInt()
        } else 0

        return session.copy(
            status = ExamStatus.SUBMITTED,
            score = calculatedScore,
            correctCount = correct,
            totalQuestions = allQuestions.size,
            isAlarmActive = false,
            isFocused = true,
            lastHeartbeat = System.currentTimeMillis()
        )
    }

    // --- Admin Remote Proctor Actions ---
    fun adminSendWarning(studentId: String, message: String) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            map + (studentId to cur.copy(
                adminWarningMessage = message,
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }

    fun adminClearWarning(studentId: String) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            map + (studentId to cur.copy(adminWarningMessage = null))
        }
    }

    fun adminForceSubmit(studentId: String) {
        submitExam(studentId)
    }

    fun adminResetExam(studentId: String) {
        val config = _examConfig.value
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            map + (studentId to cur.copy(
                answers = emptyMap(),
                doubtfulQuestionIds = emptySet(),
                currentQuestionIndex = 0,
                timeRemainingSeconds = config.durationMinutes * 60,
                totalDurationSeconds = config.durationMinutes * 60,
                status = ExamStatus.NOT_STARTED,
                violationCount = 0,
                violations = emptyList(),
                isAlarmActive = false,
                score = 0,
                correctCount = 0,
                isFocused = true,
                adminWarningMessage = null,
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }

    fun adminUnlockExam(studentId: String) {
        _studentSessions.update { map ->
            val cur = map[studentId] ?: return@update map
            map + (studentId to cur.copy(
                status = ExamStatus.IN_PROGRESS,
                isAlarmActive = false,
                isFocused = true,
                lastHeartbeat = System.currentTimeMillis()
            ))
        }
    }
}
