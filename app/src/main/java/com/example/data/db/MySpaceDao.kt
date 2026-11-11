package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MySpaceDao {

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY status ASC, priority DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // Semesters
    @Query("SELECT * FROM semesters ORDER BY semesterNumber ASC")
    fun getAllSemesters(): Flow<List<SemesterItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: SemesterItem): Long

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectItem>>

    @Query("SELECT * FROM subjects WHERE semesterId = :semesterId ORDER BY name ASC")
    fun getSubjectsBySemester(semesterId: Long): Flow<List<SubjectItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectItem): Long

    @Delete
    suspend fun deleteSubject(subject: SubjectItem)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY date DESC, period ASC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getAttendanceBySubject(subjectId: Long): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord): Long

    @Delete
    suspend fun deleteAttendance(record: AttendanceRecord)

    // Timetable
    @Query("SELECT * FROM timetable ORDER BY startTime ASC")
    fun getAllTimetable(): Flow<List<TimetableEntry>>

    @Query("SELECT * FROM timetable WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getTimetableForDay(dayOfWeek: String): Flow<List<TimetableEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetable(entry: TimetableEntry): Long

    @Delete
    suspend fun deleteTimetable(entry: TimetableEntry)

    // Exams
    @Query("SELECT * FROM exams ORDER BY examDate ASC, startTime ASC")
    fun getAllExams(): Flow<List<ExamItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamItem): Long

    @Update
    suspend fun updateExam(exam: ExamItem)

    @Delete
    suspend fun deleteExam(exam: ExamItem)

    // Assignments
    @Query("SELECT * FROM assignments ORDER BY dueDate ASC")
    fun getAllAssignments(): Flow<List<AssignmentItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentItem): Long

    @Update
    suspend fun updateAssignment(assignment: AssignmentItem)

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentItem)

    // Study Topics
    @Query("SELECT * FROM study_topics ORDER BY title ASC")
    fun getAllStudyTopics(): Flow<List<StudyTopic>>

    @Query("SELECT * FROM study_topics WHERE subjectId = :subjectId")
    fun getTopicsForSubject(subjectId: Long): Flow<List<StudyTopic>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTopic(topic: StudyTopic): Long

    @Update
    suspend fun updateStudyTopic(topic: StudyTopic)

    @Delete
    suspend fun deleteStudyTopic(topic: StudyTopic)

    // Habits
    @Query("SELECT * FROM habits ORDER BY currentStreak DESC, name ASC")
    fun getAllHabits(): Flow<List<HabitItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitItem): Long

    @Update
    suspend fun updateHabit(habit: HabitItem)

    @Delete
    suspend fun deleteHabit(habit: HabitItem)

    // Notes
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteItem): Long

    @Update
    suspend fun updateNote(note: NoteItem)

    @Delete
    suspend fun deleteNote(note: NoteItem)

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSession): Long

    // Bulk deletion for data reset
    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()

    @Query("DELETE FROM attendance")
    suspend fun clearAttendance()

    @Query("DELETE FROM timetable")
    suspend fun clearTimetable()

    @Query("DELETE FROM exams")
    suspend fun clearExams()

    @Query("DELETE FROM assignments")
    suspend fun clearAssignments()

    @Query("DELETE FROM study_topics")
    suspend fun clearStudyTopics()

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM notes")
    suspend fun clearNotes()

    @Query("DELETE FROM focus_sessions")
    suspend fun clearFocusSessions()
}
