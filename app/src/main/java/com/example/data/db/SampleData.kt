package com.example.data.db

import com.example.data.model.*
import java.time.LocalDate

object SampleData {

    suspend fun populateInitialDataIfEmpty(dao: MySpaceDao) {
        // Create Active Semester
        val semId = dao.insertSemester(
            SemesterItem(
                name = "Semester 5 (B.Tech CSE)",
                semesterNumber = 5,
                academicYear = "2026-2027",
                isActive = true,
                targetAttendance = 75f
            )
        )

        // Create Subjects
        val dbmsId = dao.insertSubject(
            SubjectItem(
                semesterId = semId,
                name = "Database Management Systems",
                code = "CS301",
                credits = 4,
                faculty = "Dr. Alan Vance",
                targetAttendance = 75f,
                colorHex = "#3F51B5"
            )
        )
        val osId = dao.insertSubject(
            SubjectItem(
                semesterId = semId,
                name = "Operating Systems",
                code = "CS302",
                credits = 4,
                faculty = "Prof. Sarah Jenkins",
                targetAttendance = 75f,
                colorHex = "#00897B"
            )
        )
        val cnId = dao.insertSubject(
            SubjectItem(
                semesterId = semId,
                name = "Computer Networks",
                code = "CS303",
                credits = 3,
                faculty = "Dr. Rajesh Kumar",
                targetAttendance = 75f,
                colorHex = "#E65100"
            )
        )
        val seId = dao.insertSubject(
            SubjectItem(
                semesterId = semId,
                name = "Software Engineering",
                code = "CS304",
                credits = 3,
                faculty = "Prof. Emily Chen",
                targetAttendance = 75f,
                colorHex = "#6A1B9A"
            )
        )
        val mathId = dao.insertSubject(
            SubjectItem(
                semesterId = semId,
                name = "Discrete Mathematics",
                code = "MA301",
                credits = 3,
                faculty = "Dr. Thomas White",
                targetAttendance = 75f,
                colorHex = "#1565C0"
            )
        )

        // Timetable (Monday to Friday)
        val timetableEntries = listOf(
            TimetableEntry(semesterId = semId, subjectId = dbmsId, dayOfWeek = "Monday", startTime = "09:00 AM", endTime = "10:00 AM", classroom = "Hall 204", teacher = "Dr. Alan Vance"),
            TimetableEntry(semesterId = semId, subjectId = osId, dayOfWeek = "Monday", startTime = "10:15 AM", endTime = "11:15 AM", classroom = "Lab 2B", teacher = "Prof. Sarah Jenkins"),
            TimetableEntry(semesterId = semId, subjectId = cnId, dayOfWeek = "Monday", startTime = "11:30 AM", endTime = "12:30 PM", classroom = "Hall 204", teacher = "Dr. Rajesh Kumar"),
            TimetableEntry(semesterId = semId, subjectId = seId, dayOfWeek = "Monday", startTime = "02:00 PM", endTime = "03:00 PM", classroom = "Seminar Room A", teacher = "Prof. Emily Chen"),

            TimetableEntry(semesterId = semId, subjectId = mathId, dayOfWeek = "Tuesday", startTime = "09:00 AM", endTime = "10:00 AM", classroom = "Hall 301", teacher = "Dr. Thomas White"),
            TimetableEntry(semesterId = semId, subjectId = dbmsId, dayOfWeek = "Tuesday", startTime = "10:15 AM", endTime = "11:15 AM", classroom = "Hall 204", teacher = "Dr. Alan Vance"),
            TimetableEntry(semesterId = semId, subjectId = osId, dayOfWeek = "Tuesday", startTime = "01:30 PM", endTime = "03:30 PM", classroom = "Systems Lab", teacher = "Prof. Sarah Jenkins"),

            TimetableEntry(semesterId = semId, subjectId = cnId, dayOfWeek = "Wednesday", startTime = "09:00 AM", endTime = "10:00 AM", classroom = "Hall 204", teacher = "Dr. Rajesh Kumar"),
            TimetableEntry(semesterId = semId, subjectId = seId, dayOfWeek = "Wednesday", startTime = "10:15 AM", endTime = "11:15 AM", classroom = "Seminar Room A", teacher = "Prof. Emily Chen"),
            TimetableEntry(semesterId = semId, subjectId = mathId, dayOfWeek = "Wednesday", startTime = "11:30 AM", endTime = "12:30 PM", classroom = "Hall 301", teacher = "Dr. Thomas White"),

            TimetableEntry(semesterId = semId, subjectId = dbmsId, dayOfWeek = "Thursday", startTime = "09:00 AM", endTime = "10:00 AM", classroom = "Hall 204", teacher = "Dr. Alan Vance"),
            TimetableEntry(semesterId = semId, subjectId = osId, dayOfWeek = "Thursday", startTime = "10:15 AM", endTime = "11:15 AM", classroom = "Hall 204", teacher = "Prof. Sarah Jenkins"),
            TimetableEntry(semesterId = semId, subjectId = cnId, dayOfWeek = "Thursday", startTime = "01:30 PM", endTime = "03:30 PM", classroom = "Network Lab", teacher = "Dr. Rajesh Kumar"),

            TimetableEntry(semesterId = semId, subjectId = seId, dayOfWeek = "Friday", startTime = "09:00 AM", endTime = "10:00 AM", classroom = "Seminar Room A", teacher = "Prof. Emily Chen"),
            TimetableEntry(semesterId = semId, subjectId = mathId, dayOfWeek = "Friday", startTime = "10:15 AM", endTime = "11:15 AM", classroom = "Hall 301", teacher = "Dr. Thomas White"),
            TimetableEntry(semesterId = semId, subjectId = dbmsId, dayOfWeek = "Friday", startTime = "11:30 AM", endTime = "12:30 PM", classroom = "Database Lab", teacher = "Dr. Alan Vance")
        )
        for (entry in timetableEntries) {
            dao.insertTimetable(entry)
        }

        // Attendance records for healthy & warning calculations
        // DBMS: 17 attended out of 20 (85%)
        repeat(17) { dao.insertAttendance(AttendanceRecord(subjectId = dbmsId, semesterId = semId, date = "2026-11-0$it", period = 1, status = AttendanceStatus.PRESENT)) }
        repeat(3) { dao.insertAttendance(AttendanceRecord(subjectId = dbmsId, semesterId = semId, date = "2026-10-2$it", period = 1, status = AttendanceStatus.ABSENT)) }

        // OS: 16 attended out of 20 (80%)
        repeat(16) { dao.insertAttendance(AttendanceRecord(subjectId = osId, semesterId = semId, date = "2026-11-0$it", period = 2, status = AttendanceStatus.PRESENT)) }
        repeat(4) { dao.insertAttendance(AttendanceRecord(subjectId = osId, semesterId = semId, date = "2026-10-2$it", period = 2, status = AttendanceStatus.ABSENT)) }

        // CN: 12 attended out of 17 (70.5% - below 75% target warning!)
        repeat(12) { dao.insertAttendance(AttendanceRecord(subjectId = cnId, semesterId = semId, date = "2026-11-0$it", period = 3, status = AttendanceStatus.PRESENT)) }
        repeat(5) { dao.insertAttendance(AttendanceRecord(subjectId = cnId, semesterId = semId, date = "2026-10-1$it", period = 3, status = AttendanceStatus.ABSENT)) }

        // Software Engineering: 18 attended out of 20 (90%)
        repeat(18) { dao.insertAttendance(AttendanceRecord(subjectId = seId, semesterId = semId, date = "2026-11-0$it", period = 4, status = AttendanceStatus.PRESENT)) }
        repeat(2) { dao.insertAttendance(AttendanceRecord(subjectId = seId, semesterId = semId, date = "2026-10-1$it", period = 4, status = AttendanceStatus.ABSENT)) }

        // Discrete Math: 15 attended out of 18 (83.3%)
        repeat(15) { dao.insertAttendance(AttendanceRecord(subjectId = mathId, semesterId = semId, date = "2026-11-0$it", period = 1, status = AttendanceStatus.PRESENT)) }
        repeat(3) { dao.insertAttendance(AttendanceRecord(subjectId = mathId, semesterId = semId, date = "2026-10-1$it", period = 1, status = AttendanceStatus.ABSENT)) }

        // Tasks
        dao.insertTask(TaskItem(title = "Revise Normalization & BCNF", description = "Review 3NF vs BCNF examples for Series Exam", category = "College", priority = Priority.HIGH, status = TaskStatus.PENDING, dueDate = "Today", dueTime = "05:00 PM"))
        dao.insertTask(TaskItem(title = "Implement Dijkstra Shortest Path", description = "For Computer Networks routing lab assignment", category = "Coding", priority = Priority.URGENT, status = TaskStatus.IN_PROGRESS, dueDate = "Tomorrow", dueTime = "11:59 PM"))
        dao.insertTask(TaskItem(title = "Complete Software Architecture UML", description = "Draw Component and Sequence diagrams in StarUML", category = "College", priority = Priority.MEDIUM, status = TaskStatus.PENDING, dueDate = "In 2 days", dueTime = "02:00 PM"))
        dao.insertTask(TaskItem(title = "Study Process Synchronization", description = "Peterson's solution and Semaphores chapter", category = "College", priority = Priority.HIGH, status = TaskStatus.COMPLETED, dueDate = "Yesterday", completedAt = System.currentTimeMillis()))
        dao.insertTask(TaskItem(title = "Buy Spiral Notebooks & Flashcards", description = "Stationery run for exam revision notes", category = "Personal", priority = Priority.LOW, status = TaskStatus.PENDING, dueDate = "Saturday", dueTime = "06:00 PM"))

        // Exams with marks
        val dbmsExam = dao.insertExam(ExamItem(semesterId = semId, subjectId = dbmsId, name = "Series Examination 1", examType = "Series 1", examDate = "2026-10-15", startTime = "10:00 AM", durationMinutes = 90, maxMarks = 50f, obtainedMarks = 44f))
        val osExam = dao.insertExam(ExamItem(semesterId = semId, subjectId = osId, name = "Series Examination 1", examType = "Series 1", examDate = "2026-10-17", startTime = "10:00 AM", durationMinutes = 90, maxMarks = 50f, obtainedMarks = 41f))
        val cnExam = dao.insertExam(ExamItem(semesterId = semId, subjectId = cnId, name = "Series Examination 1", examType = "Series 1", examDate = "2026-10-19", startTime = "10:00 AM", durationMinutes = 90, maxMarks = 50f, obtainedMarks = 38f))
        val seExam = dao.insertExam(ExamItem(semesterId = semId, subjectId = seId, name = "Series Examination 1", examType = "Series 1", examDate = "2026-10-21", startTime = "10:00 AM", durationMinutes = 90, maxMarks = 50f, obtainedMarks = 47f))
        val mathExam = dao.insertExam(ExamItem(semesterId = semId, subjectId = mathId, name = "Series Examination 1", examType = "Series 1", examDate = "2026-10-23", startTime = "10:00 AM", durationMinutes = 90, maxMarks = 50f, obtainedMarks = 43f))

        // Upcoming Exam
        dao.insertExam(ExamItem(semesterId = semId, subjectId = dbmsId, name = "Mid-term Model Exam", examType = "Model Exam", examDate = "2026-11-20", startTime = "09:30 AM", durationMinutes = 180, maxMarks = 100f, obtainedMarks = null, notes = "Covers Modules 1 through 4"))
        dao.insertExam(ExamItem(semesterId = semId, subjectId = osId, name = "Mid-term Model Exam", examType = "Model Exam", examDate = "2026-11-22", startTime = "09:30 AM", durationMinutes = 180, maxMarks = 100f, obtainedMarks = null, notes = "Includes CPU Scheduling & Memory Virtualization"))

        // Study Topics
        dao.insertStudyTopic(StudyTopic(subjectId = dbmsId, examId = dbmsExam, title = "Relational Algebra & Tuple Calculus", status = TopicStatus.COMPLETED, revisionCount = 2, estimatedMinutes = 75))
        dao.insertStudyTopic(StudyTopic(subjectId = dbmsId, examId = dbmsExam, title = "ER-to-Relational Mapping & Normalization (1NF to BCNF)", status = TopicStatus.IN_PROGRESS, revisionCount = 1, estimatedMinutes = 90))
        dao.insertStudyTopic(StudyTopic(subjectId = dbmsId, examId = dbmsExam, title = "Indexing: B+ Trees and Hashing Strategies", status = TopicStatus.NEEDS_REVISION, revisionCount = 1, estimatedMinutes = 60))
        dao.insertStudyTopic(StudyTopic(subjectId = osId, examId = osExam, title = "CPU Scheduling: Multi-level Feedback Queues", status = TopicStatus.COMPLETED, revisionCount = 2, estimatedMinutes = 60))
        dao.insertStudyTopic(StudyTopic(subjectId = osId, examId = osExam, title = "Deadlock Prevention, Avoidance & Banker's Algorithm", status = TopicStatus.IN_PROGRESS, revisionCount = 1, estimatedMinutes = 90))
        dao.insertStudyTopic(StudyTopic(subjectId = cnId, examId = cnExam, title = "TCP Flow Control, Sliding Window & Congestion Control", status = TopicStatus.NOT_STARTED, revisionCount = 0, estimatedMinutes = 120))

        // Assignments
        dao.insertAssignment(AssignmentItem(semesterId = semId, subjectId = dbmsId, title = "SQL Complex Queries & Triggers Submission", description = "Write queries for airline booking database and create audit triggers", dueDate = "2026-11-18", status = AssignmentStatus.IN_PROGRESS))
        dao.insertAssignment(AssignmentItem(semesterId = semId, subjectId = cnId, title = "Packet Sniffing Report using Wireshark", description = "Capture 3-way handshake and analyze TCP flags and window sizing", dueDate = "2026-11-22", status = AssignmentStatus.NOT_STARTED))
        dao.insertAssignment(AssignmentItem(semesterId = semId, subjectId = seId, title = "Agile Scrum Sprint Documentation & User Stories", description = "Submit Product Backlog and Sprint Burndown chart analysis", dueDate = "2026-11-14", status = AssignmentStatus.COMPLETED))

        // Habits
        dao.insertHabit(HabitItem(name = "Deep Study Focus (2 Hours)", description = "Daily uninterrupted study session", frequency = "Daily", targetValue = 2, unit = "hours", currentStreak = 7, bestStreak = 14, lastCompletedDate = "2026-11-10"))
        dao.insertHabit(HabitItem(name = "Code 1 LeetCode Problem", description = "Maintain problem solving agility", frequency = "Daily", targetValue = 1, unit = "problem", currentStreak = 12, bestStreak = 18, lastCompletedDate = "2026-11-10"))
        dao.insertHabit(HabitItem(name = "Daily Syllabus Revision", description = "Spend 30 minutes revising previous day notes", frequency = "Daily", targetValue = 30, unit = "mins", currentStreak = 5, bestStreak = 9, lastCompletedDate = "2026-11-10"))
        dao.insertHabit(HabitItem(name = "Hydration & Walk Break", description = "Drink 2L water and walk around campus", frequency = "Daily", targetValue = 2, unit = "liters", currentStreak = 4, bestStreak = 6, lastCompletedDate = "2026-11-10"))

        // Notes
        dao.insertNote(NoteItem(semesterId = semId, subjectId = dbmsId, title = "ACID Properties & Two-Phase Locking (2PL)", content = "Atomicity: All-or-nothing execution.\nConsistency: Database remains in valid state.\nIsolation: Transactions execute concurrently without interference.\nDurability: Committed changes survive system crashes.\n\nStrict 2PL prevents cascading rollbacks by holding exclusive locks until EOT.", category = "Exam Summary", isPinned = true))
        dao.insertNote(NoteItem(semesterId = semId, subjectId = osId, title = "Virtual Memory & Page Replacement Policies", content = "FIFO: High page fault rate, Belady's anomaly.\nLRU: Optimal practical policy, requires stack/timestamp hardware.\nOptimal: Replaces page not needed for longest future time (theoretical benchmark).", category = "Formula Sheet", isPinned = true))
        dao.insertNote(NoteItem(semesterId = semId, subjectId = cnId, title = "OSI 7 Layers Quick Cheat Sheet", content = "1. Physical (Bits)\n2. Data Link (Frames, MAC)\n3. Network (Packets, IP, Routing)\n4. Transport (Segments, TCP/UDP)\n5. Session (Sockets)\n6. Presentation (Encryption/Encoding)\n7. Application (HTTP, DNS, SSH)", category = "Quick Reference", isPinned = false))

        // Focus Sessions Logged
        dao.insertFocusSession(FocusSession(subjectId = dbmsId, durationMinutes = 25, sessionType = "Focus", date = "Today"))
        dao.insertFocusSession(FocusSession(subjectId = dbmsId, durationMinutes = 25, sessionType = "Focus", date = "Today"))
        dao.insertFocusSession(FocusSession(subjectId = osId, durationMinutes = 25, sessionType = "Focus", date = "Today"))
    }
}
