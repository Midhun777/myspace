package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.TaskStatus
import com.example.ui.components.MetricStatCard
import com.example.ui.components.PriorityBadge
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CoralUrgent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.viewmodel.MySpaceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: MySpaceViewModel,
    onNavigateToTasks: () -> Unit,
    onNavigateToCollege: () -> Unit,
    onNavigateToFocus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val timetable by viewModel.timetable.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val attendanceStats by viewModel.attendanceStats.collectAsState()
    val overallAttendance by viewModel.overallAttendancePercentage.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val exams by viewModel.exams.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val pomodoroState by viewModel.pomodoroState.collectAsState()

    val todayDateString = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    }

    // Filter today's timetable
    val todayDay = remember {
        SimpleDateFormat("EEEE", Locale.getDefault()).format(Date())
    }
    val todayClasses = remember(timetable, todayDay) {
        timetable.filter { it.dayOfWeek.equals(todayDay, ignoreCase = true) }
            .ifEmpty { timetable.filter { it.dayOfWeek == "Monday" } } // Fallback to Monday for weekend viewing
    }

    val pendingTasks = remember(tasks) { tasks.filter { it.status != TaskStatus.COMPLETED } }
    val completedTasks = remember(tasks) { tasks.filter { it.status == TaskStatus.COMPLETED } }
    val lowAttendanceSubjects = remember(attendanceStats) { attendanceStats.filter { it.isBelowTarget } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Semester 5 (B.Tech CSE)",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = todayDateString,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Good day, Alex! 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "You have ${pendingTasks.size} tasks pending and ${todayClasses.size} scheduled lectures today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Attendance",
                    value = String.format(Locale.getDefault(), "%.1f%%", overallAttendance),
                    subtitle = if (lowAttendanceSubjects.isEmpty()) "Safe (≥75%)" else "${lowAttendanceSubjects.size} Below Target",
                    icon = Icons.Default.FactCheck,
                    iconColor = if (lowAttendanceSubjects.isEmpty()) IndigoSecondary else CoralUrgent,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToCollege
                )
                MetricStatCard(
                    title = "Focus Time",
                    value = "${pomodoroState.totalFocusMinutesToday}m",
                    subtitle = "${pomodoroState.completedSessionsToday} Pomodoros",
                    icon = Icons.Default.Timer,
                    iconColor = AmberAccent,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToFocus
                )
                MetricStatCard(
                    title = "Tasks Done",
                    value = "${completedTasks.size}/${tasks.size}",
                    subtitle = if (tasks.isNotEmpty()) "${(completedTasks.size * 100 / tasks.size)}% finished" else "No tasks",
                    icon = Icons.Default.CheckCircle,
                    iconColor = IndigoPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToTasks
                )
            }
        }

        // Attendance Warning Banner (if any subject < target)
        if (lowAttendanceSubjects.isNotEmpty()) {
            item {
                val warningSub = lowAttendanceSubjects.first()
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = CoralUrgent.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CoralUrgent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low Attendance Warning: ${warningSub.subject.name}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CoralUrgent
                            )
                            Text(
                                text = "Current: ${String.format(Locale.getDefault(), "%.1f%%", warningSub.percentage)} • Attend next ${warningSub.classesNeededForTarget} classes to reach 75%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Today's Timetable Section
        item {
            SectionHeader(
                title = "Today's Schedule (${todayClasses.size} Classes)",
                actionText = "Full Timetable",
                onActionClick = onNavigateToCollege
            )
        }

        if (todayClasses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No classes scheduled for today. Enjoy your study time!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(todayClasses) { entry ->
                val sub = subjects.firstOrNull { it.id == entry.subjectId }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
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
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(72.dp)
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
                                .height(36.dp)
                                .width(1.dp)
                                .padding(horizontal = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sub?.name ?: "Subject Class",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${entry.classroom} • ${entry.teacher}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 1-tap Attendance quick logger
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = {
                                    viewModel.recordAttendance(entry.subjectId, AttendanceStatus.PRESENT, "From Dashboard")
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(IndigoSecondary.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Mark Present",
                                    tint = IndigoSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.recordAttendance(entry.subjectId, AttendanceStatus.ABSENT, "From Dashboard")
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(CoralUrgent.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Mark Absent",
                                    tint = CoralUrgent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Today's Top Tasks
        item {
            SectionHeader(
                title = "Today's Tasks",
                subtitle = "${pendingTasks.size} pending",
                actionText = "All Tasks",
                onActionClick = onNavigateToTasks
            )
        }

        items(tasks.take(4)) { task ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.toggleTaskStatus(task) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.status == TaskStatus.COMPLETED,
                        onCheckedChange = { viewModel.toggleTaskStatus(task) },
                        modifier = Modifier.testTag("task_check_${task.id}")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough else null,
                            color = if (task.status == TaskStatus.COMPLETED) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "${task.category} • Due ${task.dueTime.ifBlank { task.dueDate }}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    PriorityBadge(priority = task.priority)
                }
            }
        }

        // Habits Streak Horizontal Row
        item {
            SectionHeader(
                title = "Habit Consistency",
                actionText = "Manage Habits",
                onActionClick = onNavigateToTasks
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(habits) { habit ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { viewModel.checkInHabit(habit) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔥 ${habit.currentStreak}d",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberAccent
                                )
                                Icon(
                                    imageVector = if (habit.lastCompletedDate.isNotEmpty()) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (habit.lastCompletedDate.isNotEmpty()) IndigoSecondary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Target: ${habit.targetValue} ${habit.unit}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Upcoming Deadlines / Exams
        item {
            SectionHeader(
                title = "Upcoming Deadlines & Exams",
                actionText = "College Hub",
                onActionClick = onNavigateToCollege
            )
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    exams.take(2).forEach { exam ->
                        val sub = subjects.firstOrNull { it.id == exam.subjectId }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IndigoPrimary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${exam.name}: ${sub?.name ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${exam.examDate} at ${exam.startTime} (${exam.durationMinutes} mins)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    assignments.take(2).forEach { asgn ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmberAccent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = asgn.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Due: ${asgn.dueDate} • ${asgn.status.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
