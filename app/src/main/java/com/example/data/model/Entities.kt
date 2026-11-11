package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}

enum class TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED, CANCELLED
}

enum class AttendanceStatus {
    PRESENT, ABSENT, EXCUSED
}

enum class AssignmentStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED, SUBMITTED
}

enum class TopicStatus {
    NOT_STARTED, IN_PROGRESS, COMPLETED, NEEDS_REVISION
}

enum class ImageSizeOption(val label: String, val resolution: String) {
    SIZE_1K("1K", "1024 x 1024 px"),
    SIZE_2K("2K", "2048 x 2048 px"),
    SIZE_4K("4K", "4096 x 4096 px")
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "College",
    val priority: Priority = Priority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val dueDate: String = "", // e.g. "2026-11-15"
    val dueTime: String = "", // e.g. "14:00"
    val isRecurring: Boolean = false,
    val recurrence: String = "None", // Daily, Weekly, Monthly
    val completedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "semesters")
data class SemesterItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // e.g. "Semester 5"
    val semesterNumber: Int = 5,
    val academicYear: String = "2026-2027",
    val isActive: Boolean = true,
    val targetAttendance: Float = 75f
)

@Entity(tableName = "subjects")
data class SubjectItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long = 1,
    val name: String,
    val code: String,
    val credits: Int = 3,
    val faculty: String = "",
    val targetAttendance: Float = 75f,
    val colorHex: String = "#3F51B5"
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val semesterId: Long = 1,
    val date: String, // "YYYY-MM-DD"
    val period: Int = 1,
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val notes: String = ""
)

@Entity(tableName = "timetable")
data class TimetableEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long = 1,
    val subjectId: Long,
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val startTime: String, // "09:00"
    val endTime: String,   // "10:00"
    val classroom: String = "Hall 302",
    val teacher: String = ""
)

@Entity(tableName = "exams")
data class ExamItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long = 1,
    val subjectId: Long,
    val name: String, // e.g. "Series Exam 1"
    val examType: String = "Internal", // Series 1, Series 2, Model, Final
    val examDate: String, // "YYYY-MM-DD"
    val startTime: String = "10:00 AM",
    val durationMinutes: Int = 90,
    val maxMarks: Float = 50f,
    val obtainedMarks: Float? = null,
    val notes: String = ""
)

@Entity(tableName = "assignments")
data class AssignmentItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long = 1,
    val subjectId: Long,
    val title: String,
    val description: String = "",
    val dueDate: String, // "YYYY-MM-DD"
    val status: AssignmentStatus = AssignmentStatus.NOT_STARTED,
    val notes: String = ""
)

@Entity(tableName = "study_topics")
data class StudyTopic(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val examId: Long? = null,
    val title: String,
    val status: TopicStatus = TopicStatus.NOT_STARTED,
    val revisionCount: Int = 0,
    val estimatedMinutes: Int = 60,
    val notes: String = ""
)

@Entity(tableName = "habits")
data class HabitItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequency: String = "Daily",
    val targetValue: Int = 1,
    val unit: String = "times",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String = ""
)

@Entity(tableName = "notes")
data class NoteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val semesterId: Long = 1,
    val subjectId: Long? = null,
    val title: String,
    val content: String,
    val category: String = "Lecture Notes",
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long? = null,
    val durationMinutes: Int = 25,
    val sessionType: String = "Focus", // Focus, ShortBreak, LongBreak
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)
