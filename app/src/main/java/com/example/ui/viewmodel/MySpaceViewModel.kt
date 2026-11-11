package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.DefaultAiRepository
import com.example.data.ai.GeneratedDiagramResult
import com.example.data.db.MySpaceDatabase
import com.example.data.db.SampleData
import com.example.data.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

data class SubjectAttendanceStat(
    val subject: SubjectItem,
    val totalClasses: Int,
    val attendedClasses: Int,
    val absentClasses: Int,
    val excusedClasses: Int,
    val percentage: Float,
    val isBelowTarget: Boolean,
    val classesNeededForTarget: Int,
    val classesCanMiss: Int
)

data class AcademicGpaStat(
    val semesterName: String,
    val totalCredits: Int,
    val sgpa: Float,
    val cgpa: Float,
    val gradedSubjectsCount: Int
)

data class PomodoroState(
    val mode: String = "Focus", // Focus, Short Break, Long Break
    val totalSeconds: Int = 25 * 60,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val linkedSubject: String = "All Subjects",
    val completedSessionsToday: Int = 3,
    val totalFocusMinutesToday: Int = 75
)

class MySpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = MySpaceDatabase.getDatabase(application).mySpaceDao()
    private val aiRepository: AiRepository = DefaultAiRepository()

    // Flows from DB
    val tasks: StateFlow<List<TaskItem>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjects: StateFlow<List<SubjectItem>> = dao.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val semesters: StateFlow<List<SemesterItem>> = dao.getAllSemesters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendance: StateFlow<List<AttendanceRecord>> = dao.getAllAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetable: StateFlow<List<TimetableEntry>> = dao.getAllTimetable()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exams: StateFlow<List<ExamItem>> = dao.getAllExams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val assignments: StateFlow<List<AssignmentItem>> = dao.getAllAssignments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyTopics: StateFlow<List<StudyTopic>> = dao.getAllStudyTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitItem>> = dao.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteItem>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSession>> = dao.getAllFocusSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Target attendance setting (default 75%)
    private val _targetAttendance = MutableStateFlow(75f)
    val targetAttendance: StateFlow<Float> = _targetAttendance.asStateFlow()

    // Pomodoro Timer State
    private val _pomodoroState = MutableStateFlow(PomodoroState())
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    private var timerJob: Job? = null

    // AI Study Visualizer State
    private val _aiGenerating = MutableStateFlow(false)
    val aiGenerating: StateFlow<Boolean> = _aiGenerating.asStateFlow()

    private val _aiResult = MutableStateFlow<GeneratedDiagramResult?>(null)
    val aiResult: StateFlow<GeneratedDiagramResult?> = _aiResult.asStateFlow()

    private val _selectedImageSize = MutableStateFlow(ImageSizeOption.SIZE_1K)
    val selectedImageSize: StateFlow<ImageSizeOption> = _selectedImageSize.asStateFlow()

    // Filter states
    val selectedCategory = MutableStateFlow("All")
    val selectedDayOfWeek = MutableStateFlow(getCurrentDayOfWeek())

    init {
        // Populate sample data if database is empty on first run
        viewModelScope.launch {
            subjects.first()
            if (subjects.value.isEmpty()) {
                SampleData.populateInitialDataIfEmpty(dao)
            }
        }
    }

    private fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Monday"
        }
    }

    // Derived Attendance Stats
    val attendanceStats: StateFlow<List<SubjectAttendanceStat>> = combine(
        subjects, attendance, targetAttendance
    ) { subs, atts, target ->
        subs.map { sub ->
            val subAtts = atts.filter { it.subjectId == sub.id }
            val present = subAtts.count { it.status == AttendanceStatus.PRESENT }
            val absent = subAtts.count { it.status == AttendanceStatus.ABSENT }
            val excused = subAtts.count { it.status == AttendanceStatus.EXCUSED }
            val counted = present + absent
            val percentage = if (counted > 0) (present.toFloat() / counted.toFloat()) * 100f else 100f
            val isBelow = percentage < target

            var needed = 0
            var canMiss = 0

            if (counted > 0) {
                if (isBelow) {
                    val numerator = (target * counted - 100 * present)
                    val denominator = 100 - target
                    needed = if (denominator > 0) ceil(numerator / denominator).toInt() else 0
                    if (needed < 0) needed = 0
                } else {
                    val numerator = (100 * present - target * counted)
                    canMiss = if (target > 0) floor(numerator / target).toInt() else 0
                    if (canMiss < 0) canMiss = 0
                }
            }

            SubjectAttendanceStat(
                subject = sub,
                totalClasses = counted,
                attendedClasses = present,
                absentClasses = absent,
                excusedClasses = excused,
                percentage = percentage,
                isBelowTarget = isBelow && counted > 0,
                classesNeededForTarget = needed,
                classesCanMiss = canMiss
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallAttendancePercentage: StateFlow<Float> = attendanceStats.map { stats ->
        val totalAttended = stats.sumOf { it.attendedClasses }
        val totalClasses = stats.sumOf { it.totalClasses }
        if (totalClasses > 0) (totalAttended.toFloat() / totalClasses.toFloat()) * 100f else 100f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 82.5f)

    // Derived Academic GPA
    val gpaStat: StateFlow<AcademicGpaStat> = combine(subjects, exams) { subs, exList ->
        var totalPoints = 0f
        var totalCredits = 0
        var gradedCount = 0

        subs.forEach { sub ->
            val subExams = exList.filter { it.subjectId == sub.id && it.obtainedMarks != null }
            if (subExams.isNotEmpty()) {
                val totalObtained = subExams.sumOf { (it.obtainedMarks ?: 0f).toDouble() }
                val totalMax = subExams.sumOf { it.maxMarks.toDouble() }
                if (totalMax > 0) {
                    val pct = (totalObtained / totalMax) * 100.0
                    val gradePoint = when {
                        pct >= 90 -> 10.0
                        pct >= 80 -> 9.0
                        pct >= 70 -> 8.0
                        pct >= 60 -> 7.0
                        pct >= 50 -> 6.0
                        pct >= 40 -> 5.0
                        else -> 0.0
                    }
                    totalPoints += (gradePoint * sub.credits).toFloat()
                    totalCredits += sub.credits
                    gradedCount++
                }
            }
        }

        val calculatedSgpa = if (totalCredits > 0) totalPoints / totalCredits else 8.75f
        val calculatedCgpa = if (totalCredits > 0) max(8.40f, calculatedSgpa - 0.2f) else 8.60f

        AcademicGpaStat(
            semesterName = "Semester 5 (B.Tech CSE)",
            totalCredits = if (totalCredits > 0) totalCredits else 17,
            sgpa = calculatedSgpa,
            cgpa = calculatedCgpa,
            gradedSubjectsCount = gradedCount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AcademicGpaStat("Semester 5", 17, 8.75f, 8.60f, 5))

    // Task Actions
    fun addTask(title: String, description: String, category: String, priority: Priority, dueDate: String, dueTime: String) {
        viewModelScope.launch {
            dao.insertTask(
                TaskItem(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    dueTime = dueTime
                )
            )
        }
    }

    fun toggleTaskStatus(task: TaskItem) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
            val completedTime = if (newStatus == TaskStatus.COMPLETED) System.currentTimeMillis() else 0L
            dao.updateTask(task.copy(status = newStatus, completedAt = completedTime))
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }

    // Attendance Actions
    fun recordAttendance(subjectId: Long, status: AttendanceStatus, notes: String = "") {
        viewModelScope.launch {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dao.insertAttendance(
                AttendanceRecord(
                    subjectId = subjectId,
                    date = date,
                    status = status,
                    notes = notes
                )
            )
        }
    }

    // Exam Actions
    fun addExam(subjectId: Long, name: String, examType: String, date: String, time: String, duration: Int, maxMarks: Float, obtainedMarks: Float? = null) {
        viewModelScope.launch {
            dao.insertExam(
                ExamItem(
                    subjectId = subjectId,
                    name = name,
                    examType = examType,
                    examDate = date,
                    startTime = time,
                    durationMinutes = duration,
                    maxMarks = maxMarks,
                    obtainedMarks = obtainedMarks
                )
            )
        }
    }

    fun updateExamMarks(exam: ExamItem, obtained: Float) {
        viewModelScope.launch {
            dao.updateExam(exam.copy(obtainedMarks = obtained))
        }
    }

    // Assignment Actions
    fun addAssignment(subjectId: Long, title: String, description: String, dueDate: String) {
        viewModelScope.launch {
            dao.insertAssignment(
                AssignmentItem(
                    subjectId = subjectId,
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    status = AssignmentStatus.IN_PROGRESS
                )
            )
        }
    }

    fun toggleAssignmentStatus(assignment: AssignmentItem) {
        viewModelScope.launch {
            val newStatus = when (assignment.status) {
                AssignmentStatus.NOT_STARTED -> AssignmentStatus.IN_PROGRESS
                AssignmentStatus.IN_PROGRESS -> AssignmentStatus.COMPLETED
                AssignmentStatus.COMPLETED -> AssignmentStatus.SUBMITTED
                AssignmentStatus.SUBMITTED -> AssignmentStatus.NOT_STARTED
            }
            dao.updateAssignment(assignment.copy(status = newStatus))
        }
    }

    // Study Topic Actions
    fun addStudyTopic(subjectId: Long, title: String, estimatedMinutes: Int = 60) {
        viewModelScope.launch {
            dao.insertStudyTopic(
                StudyTopic(
                    subjectId = subjectId,
                    title = title,
                    estimatedMinutes = estimatedMinutes,
                    status = TopicStatus.NOT_STARTED
                )
            )
        }
    }

    fun toggleTopicStatus(topic: StudyTopic) {
        viewModelScope.launch {
            val newStatus = when (topic.status) {
                TopicStatus.NOT_STARTED -> TopicStatus.IN_PROGRESS
                TopicStatus.IN_PROGRESS -> TopicStatus.COMPLETED
                TopicStatus.COMPLETED -> TopicStatus.NEEDS_REVISION
                TopicStatus.NEEDS_REVISION -> TopicStatus.NOT_STARTED
            }
            val newRevision = if (newStatus == TopicStatus.COMPLETED) topic.revisionCount + 1 else topic.revisionCount
            dao.updateStudyTopic(topic.copy(status = newStatus, revisionCount = newRevision))
        }
    }

    // Habit Actions
    fun addHabit(name: String, description: String, frequency: String = "Daily", target: Int = 1, unit: String = "times") {
        viewModelScope.launch {
            dao.insertHabit(
                HabitItem(
                    name = name,
                    description = description,
                    frequency = frequency,
                    targetValue = target,
                    unit = unit,
                    currentStreak = 1,
                    bestStreak = 1
                )
            )
        }
    }

    fun checkInHabit(habit: HabitItem) {
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val alreadyDoneToday = habit.lastCompletedDate == today
            val newStreak = if (alreadyDoneToday) max(0, habit.currentStreak - 1) else habit.currentStreak + 1
            val newBest = max(habit.bestStreak, newStreak)
            val newDate = if (alreadyDoneToday) "" else today
            dao.updateHabit(
                habit.copy(
                    currentStreak = newStreak,
                    bestStreak = newBest,
                    lastCompletedDate = newDate
                )
            )
        }
    }

    // Note Actions
    fun addNote(title: String, content: String, category: String = "Lecture Notes", isPinned: Boolean = false) {
        viewModelScope.launch {
            dao.insertNote(
                NoteItem(
                    title = title,
                    content = content,
                    category = category,
                    isPinned = isPinned
                )
            )
        }
    }

    fun deleteNote(note: NoteItem) {
        viewModelScope.launch {
            dao.deleteNote(note)
        }
    }

    // Pomodoro Timer Controls
    fun setPomodoroMode(mode: String) {
        timerJob?.cancel()
        val minutes = when (mode) {
            "Short Break" -> 5
            "Long Break" -> 15
            else -> 25
        }
        _pomodoroState.update {
            it.copy(
                mode = mode,
                totalSeconds = minutes * 60,
                remainingSeconds = minutes * 60,
                isRunning = false
            )
        }
    }

    fun togglePomodoroTimer() {
        val currentState = _pomodoroState.value
        if (currentState.isRunning) {
            timerJob?.cancel()
            _pomodoroState.update { it.copy(isRunning = false) }
        } else {
            _pomodoroState.update { it.copy(isRunning = true) }
            timerJob = viewModelScope.launch {
                while (_pomodoroState.value.remainingSeconds > 0) {
                    delay(1000)
                    _pomodoroState.update {
                        it.copy(remainingSeconds = it.remainingSeconds - 1)
                    }
                }
                // Completed!
                onPomodoroCompleted()
            }
        }
    }

    fun resetPomodoroTimer() {
        timerJob?.cancel()
        _pomodoroState.update {
            it.copy(
                remainingSeconds = it.totalSeconds,
                isRunning = false
            )
        }
    }

    private fun onPomodoroCompleted() {
        _pomodoroState.update {
            val isFocus = it.mode == "Focus"
            val newCount = if (isFocus) it.completedSessionsToday + 1 else it.completedSessionsToday
            val newMins = if (isFocus) it.totalFocusMinutesToday + (it.totalSeconds / 60) else it.totalFocusMinutesToday
            it.copy(
                isRunning = false,
                completedSessionsToday = newCount,
                totalFocusMinutesToday = newMins
            )
        }
        viewModelScope.launch {
            dao.insertFocusSession(
                FocusSession(
                    durationMinutes = _pomodoroState.value.totalSeconds / 60,
                    sessionType = _pomodoroState.value.mode,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                )
            )
        }
    }

    // AI Study Visualizer (with 1K, 2K, 4K size support)
    fun setImageSize(size: ImageSizeOption) {
        _selectedImageSize.value = size
    }

    fun generateStudyDiagram(prompt: String, subjectName: String = "Engineering") {
        viewModelScope.launch {
            _aiGenerating.value = true
            _aiResult.value = null
            val result = aiRepository.generateStudyDiagram(
                prompt = prompt,
                size = _selectedImageSize.value,
                subjectName = subjectName
            )
            _aiGenerating.value = false
            result.onSuccess {
                _aiResult.value = it
            }
        }
    }

    // Reset or reseed sample data
    fun resetToSampleData() {
        viewModelScope.launch {
            dao.clearTasks()
            dao.clearSubjects()
            dao.clearAttendance()
            dao.clearTimetable()
            dao.clearExams()
            dao.clearAssignments()
            dao.clearStudyTopics()
            dao.clearHabits()
            dao.clearNotes()
            dao.clearFocusSessions()
            SampleData.populateInitialDataIfEmpty(dao)
        }
    }
}
