package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MySpaceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollegeScreen(
    viewModel: MySpaceViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCollegeTab by remember { mutableIntStateOf(0) }
    // 0: Attendance, 1: Timetable, 2: Exams & Prep, 3: Marks & SGPA, 4: Assignments

    val collegeTabs = listOf("Attendance", "Timetable", "Exams & Prep", "Marks & GPA", "Assignments")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // College Sub Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedCollegeTab,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .padding(top = 12.dp, bottom = 8.dp)
                .fillMaxWidth()
        ) {
            collegeTabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedCollegeTab == index,
                    onClick = { selectedCollegeTab = index },
                    text = { Text(label, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("college_tab_$index")
                )
            }
        }

        when (selectedCollegeTab) {
            0 -> AttendanceSubScreen(viewModel)
            1 -> TimetableSubScreen(viewModel)
            2 -> ExamsPrepSubScreen(viewModel)
            3 -> MarksGpaSubScreen(viewModel)
            4 -> AssignmentsSubScreen(viewModel)
        }
    }
}

@Composable
fun AttendanceSubScreen(viewModel: MySpaceViewModel) {
    val stats by viewModel.attendanceStats.collectAsState()
    val overallPct by viewModel.overallAttendancePercentage.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Overall Summary Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Semester Attendance Health",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Target requirement is 75.0% for university exams",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Surface(
                        color = if (overallPct >= 75f) IndigoSecondary else CoralUrgent,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%%", overallPct),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        items(stats) { stat ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stat.subject.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${stat.subject.code} • ${stat.subject.faculty} (${stat.subject.credits} Credits)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = if (stat.percentage >= 75f) PriorityLowColor.copy(alpha = 0.15f) else CoralUrgent.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", stat.percentage),
                                color = if (stat.percentage >= 75f) PriorityLowColor else CoralUrgent,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                        color = if (stat.percentage >= 75f) PriorityLowColor else CoralUrgent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Attended: ${stat.attendedClasses} / ${stat.totalClasses} classes (${stat.absentClasses} missed)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Smart calculation info
                        Text(
                            text = if (stat.isBelowTarget) {
                                "Need ${stat.classesNeededForTarget} more classes"
                            } else {
                                "Can miss ${stat.classesCanMiss} classes"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (stat.isBelowTarget) CoralUrgent else IndigoSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1-Tap Attendance Logging Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.recordAttendance(stat.subject.id, AttendanceStatus.PRESENT, "Lecture attended")
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Present")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.recordAttendance(stat.subject.id, AttendanceStatus.ABSENT, "Class missed")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralUrgent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Absent")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableSubScreen(viewModel: MySpaceViewModel) {
    val timetable by viewModel.timetable.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    var selectedDay by remember { mutableStateOf("Monday") }

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val dayClasses = remember(timetable, selectedDay) {
        timetable.filter { it.dayOfWeek.equals(selectedDay, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            days.forEach { day ->
                FilterChip(
                    selected = selectedDay == day,
                    onClick = { selectedDay = day },
                    label = { Text(day) }
                )
            }
        }

        if (dayClasses.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.CalendarToday,
                title = "No Classes",
                message = "No timetable classes scheduled for $selectedDay."
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(dayClasses) { entry ->
                    val sub = subjects.firstOrNull { it.id == entry.subjectId }
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.width(76.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = entry.startTime,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = entry.endTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Divider(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp)
                                    .padding(horizontal = 6.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub?.name ?: "Subject Lecture",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${entry.classroom} • ${entry.teacher}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.recordAttendance(entry.subjectId, AttendanceStatus.PRESENT, "Attended ${entry.startTime}")
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Mark", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamsPrepSubScreen(viewModel: MySpaceViewModel) {
    val exams by viewModel.exams.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val studyTopics by viewModel.studyTopics.collectAsState()

    var newTopicTitle by remember { mutableStateOf("") }
    var selectedSubjectForTopic by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 1L) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionHeader(title = "Scheduled Examinations (${exams.size})")
        }

        items(exams) { exam ->
            val sub = subjects.firstOrNull { it.id == exam.subjectId }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = exam.examType,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = exam.examDate,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${exam.name} – ${sub?.name ?: ""}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Time: ${exam.startTime} (${exam.durationMinutes} mins) • Max Marks: ${exam.maxMarks.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (exam.obtainedMarks != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Score: ${exam.obtainedMarks.toInt()}/${exam.maxMarks.toInt()} (${((exam.obtainedMarks / exam.maxMarks) * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndigoSecondary
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Syllabus Preparation & Revisions",
                subtitle = "${studyTopics.count { it.status == TopicStatus.COMPLETED }} of ${studyTopics.size} topics mastered"
            )
        }

        // Add Study Topic Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTopicTitle,
                    onValueChange = { newTopicTitle = it },
                    placeholder = { Text("Add syllabus topic...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        if (newTopicTitle.isNotBlank()) {
                            viewModel.addStudyTopic(selectedSubjectForTopic, newTopicTitle)
                            newTopicTitle = ""
                        }
                    },
                    enabled = newTopicTitle.isNotBlank()
                ) {
                    Text("Add")
                }
            }
        }

        items(studyTopics) { topic ->
            val sub = subjects.firstOrNull { it.id == topic.subjectId }
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleTopicStatus(topic) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (topic.status) {
                            TopicStatus.COMPLETED -> Icons.Default.CheckCircle
                            TopicStatus.IN_PROGRESS -> Icons.Default.Timelapse
                            TopicStatus.NEEDS_REVISION -> Icons.Default.Autorenew
                            else -> Icons.Default.RadioButtonUnchecked
                        },
                        contentDescription = null,
                        tint = when (topic.status) {
                            TopicStatus.COMPLETED -> PriorityLowColor
                            TopicStatus.IN_PROGRESS -> AmberAccent
                            TopicStatus.NEEDS_REVISION -> CoralUrgent
                            else -> MaterialTheme.colorScheme.outline
                        },
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = topic.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${sub?.code ?: "Subject"} • Status: ${topic.status.name} • Revised ${topic.revisionCount}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AssistChip(
                        onClick = { viewModel.toggleTopicStatus(topic) },
                        label = { Text("Revise +1") },
                        leadingIcon = { Icon(Icons.Default.Repeat, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )
                }
            }
        }
    }
}

@Composable
fun MarksGpaSubScreen(viewModel: MySpaceViewModel) {
    val gpaStat by viewModel.gpaStat.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    var showEditMarksDialog by remember { mutableStateOf<ExamItem?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // SGPA & CGPA Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Current SGPA", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f", gpaStat.sgpa),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text("${gpaStat.totalCredits} Total Credits", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cumulative CGPA", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f", gpaStat.cgpa),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text("Across Semesters 1-5", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Subject Examination Results",
                subtitle = "Tap any exam score to edit marks and re-calculate SGPA"
            )
        }

        items(exams) { exam ->
            val sub = subjects.firstOrNull { it.id == exam.subjectId }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEditMarksDialog = exam }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = sub?.name ?: "Subject",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${exam.name} • ${sub?.credits ?: 3} Credits",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (exam.obtainedMarks != null) {
                            val pct = (exam.obtainedMarks / exam.maxMarks) * 100f
                            val grade = when {
                                pct >= 90 -> "Grade S (10.0)"
                                pct >= 80 -> "Grade A (9.0)"
                                pct >= 70 -> "Grade B (8.0)"
                                pct >= 60 -> "Grade C (7.0)"
                                else -> "Grade D (6.0)"
                            }
                            Text(
                                text = "${exam.obtainedMarks.toInt()} / ${exam.maxMarks.toInt()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = grade,
                                style = MaterialTheme.typography.labelSmall,
                                color = IndigoSecondary
                            )
                        } else {
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Marks Dialog
    if (showEditMarksDialog != null) {
        val exam = showEditMarksDialog!!
        var marksText by remember { mutableStateOf(exam.obtainedMarks?.toInt()?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { showEditMarksDialog = null },
            title = { Text("Update Exam Score") },
            text = {
                Column {
                    Text("Enter obtained marks out of ${exam.maxMarks.toInt()}:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = marksText,
                        onValueChange = { marksText = it },
                        singleLine = true,
                        placeholder = { Text("e.g. 45") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = marksText.toFloatOrNull()
                        if (parsed != null) {
                            viewModel.updateExamMarks(exam, parsed.coerceIn(0f, exam.maxMarks))
                        }
                        showEditMarksDialog = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditMarksDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AssignmentsSubScreen(viewModel: MySpaceViewModel) {
    val assignments by viewModel.assignments.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Text(
                text = "Track your lab assignments, problem sets and submissions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(assignments) { asgn ->
            val sub = subjects.firstOrNull { it.id == asgn.subjectId }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleAssignmentStatus(asgn) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = asgn.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (asgn.description.isNotBlank()) {
                            Text(
                                text = asgn.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${sub?.name ?: "Subject"} • Deadline: ${asgn.dueDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    AssistChip(
                        onClick = { viewModel.toggleAssignmentStatus(asgn) },
                        label = { Text(asgn.status.name) }
                    )
                }
            }
        }
    }
}
