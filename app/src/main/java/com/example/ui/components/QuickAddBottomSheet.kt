package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.Priority
import com.example.ui.viewmodel.MySpaceViewModel

enum class QuickAddTab(val label: String, val icon: ImageVector) {
    TASK("Task", Icons.Default.CheckCircle),
    ATTENDANCE("Attendance", Icons.Default.FactCheck),
    NOTE("Note", Icons.Default.Description),
    EXAM("Exam", Icons.Default.School),
    ASSIGNMENT("Assignment", Icons.Default.Assignment),
    HABIT("Habit", Icons.Default.Favorite)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddBottomSheet(
    viewModel: MySpaceViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember { mutableStateOf(QuickAddTab.TASK) }

    val subjects by viewModel.subjects.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Universal Quick Add",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickAddTab.entries.forEach { tab ->
                    FilterChip(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.label) },
                        leadingIcon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("quick_tab_${tab.name.lowercase()}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form by Selected Tab
            when (selectedTab) {
                QuickAddTab.TASK -> {
                    QuickAddTaskForm(
                        onAdd = { title, desc, cat, prio, due, time ->
                            viewModel.addTask(title, desc, cat, prio, due, time)
                            onDismiss()
                        }
                    )
                }
                QuickAddTab.ATTENDANCE -> {
                    QuickAddAttendanceForm(
                        subjects = subjects,
                        onRecord = { subjectId, status, notes ->
                            viewModel.recordAttendance(subjectId, status, notes)
                            onDismiss()
                        }
                    )
                }
                QuickAddTab.NOTE -> {
                    QuickAddNoteForm(
                        onAdd = { title, content, cat ->
                            viewModel.addNote(title, content, cat)
                            onDismiss()
                        }
                    )
                }
                QuickAddTab.EXAM -> {
                    QuickAddExamForm(
                        subjects = subjects,
                        onAdd = { subId, name, type, date, time, duration, maxMarks ->
                            viewModel.addExam(subId, name, type, date, time, duration, maxMarks)
                            onDismiss()
                        }
                    )
                }
                QuickAddTab.ASSIGNMENT -> {
                    QuickAddAssignmentForm(
                        subjects = subjects,
                        onAdd = { subId, title, desc, dueDate ->
                            viewModel.addAssignment(subId, title, desc, dueDate)
                            onDismiss()
                        }
                    )
                }
                QuickAddTab.HABIT -> {
                    QuickAddHabitForm(
                        onAdd = { name, desc, target, unit ->
                            viewModel.addHabit(name, desc, target = target, unit = unit)
                            onDismiss()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun QuickAddTaskForm(
    onAdd: (String, String, String, Priority, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("College") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var dueDate by remember { mutableStateOf("Today") }
    var dueTime by remember { mutableStateOf("05:00 PM") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task Title *") },
            placeholder = { Text("e.g. Revise Computer Networks") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("task_title_input")
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (Optional)") },
            placeholder = { Text("Add key details or references") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        // Category chips
        Text("Category", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("College", "Coding", "Projects", "Exam Prep", "Personal").forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) }
                )
            }
        }

        // Priority chips
        Text("Priority", style = MaterialTheme.typography.labelMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Priority.entries.forEach { p ->
                FilterChip(
                    selected = priority == p,
                    onClick = { priority = p },
                    label = { Text(p.name) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due Date") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = dueTime,
                onValueChange = { dueTime = it },
                label = { Text("Due Time") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    onAdd(title, description, category, priority, dueDate, dueTime)
                }
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("create_task_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Task")
        }
    }
}

@Composable
fun QuickAddAttendanceForm(
    subjects: List<com.example.data.model.SubjectItem>,
    onRecord: (Long, AttendanceStatus, String) -> Unit
) {
    if (subjects.isEmpty()) {
        Text("No subjects found. Please add a subject first.")
        return
    }

    var selectedSubjectId by remember { mutableStateOf(subjects.first().id) }
    var status by remember { mutableStateOf(AttendanceStatus.PRESENT) }
    var notes by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Select Subject *", style = MaterialTheme.typography.labelMedium)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            subjects.forEach { sub ->
                Surface(
                    color = if (selectedSubjectId == sub.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedSubjectId = sub.id }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSubjectId == sub.id,
                            onClick = { selectedSubjectId = sub.id }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(sub.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${sub.code} • ${sub.faculty}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Text("Status", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AttendanceStatus.entries.forEach { st ->
                FilterChip(
                    selected = status == st,
                    onClick = { status = st },
                    label = { Text(st.name) }
                )
            }
        }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Lecture Topic / Notes (Optional)") },
            placeholder = { Text("e.g. Chapter 4 Sliding Window") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = { onRecord(selectedSubjectId, status, notes) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("record_attendance_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Record Attendance")
        }
    }
}

@Composable
fun QuickAddNoteForm(
    onAdd: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Lecture Notes") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Note Title *") },
            placeholder = { Text("e.g. Peterson's Solution Algorithm") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content *") },
            placeholder = { Text("Type key formulas, definitions, code snippets...") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Lecture Notes", "Exam Summary", "Formula Sheet", "Project Idea").forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat) }
                )
            }
        }

        Button(
            onClick = {
                if (title.isNotBlank()) onAdd(title, content, category)
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Save Note")
        }
    }
}

@Composable
fun QuickAddExamForm(
    subjects: List<com.example.data.model.SubjectItem>,
    onAdd: (Long, String, String, String, String, Int, Float) -> Unit
) {
    if (subjects.isEmpty()) {
        Text("No subjects found.")
        return
    }
    var selectedSubjectId by remember { mutableStateOf(subjects.first().id) }
    var name by remember { mutableStateOf("Series Exam 2") }
    var examType by remember { mutableStateOf("Series 2") }
    var examDate by remember { mutableStateOf("2026-11-25") }
    var startTime by remember { mutableStateOf("10:00 AM") }
    var duration by remember { mutableStateOf("90") }
    var maxMarks by remember { mutableStateOf("50") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Exam Name *") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = examDate,
                onValueChange = { examDate = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = startTime,
                onValueChange = { startTime = it },
                label = { Text("Time") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = duration,
                onValueChange = { duration = it },
                label = { Text("Duration (Mins)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxMarks,
                onValueChange = { maxMarks = it },
                label = { Text("Max Marks") },
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                onAdd(
                    selectedSubjectId,
                    name,
                    examType,
                    examDate,
                    startTime,
                    duration.toIntOrNull() ?: 90,
                    maxMarks.toFloatOrNull() ?: 50f
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Add Examination")
        }
    }
}

@Composable
fun QuickAddAssignmentForm(
    subjects: List<com.example.data.model.SubjectItem>,
    onAdd: (Long, String, String, String) -> Unit
) {
    if (subjects.isEmpty()) {
        Text("No subjects found.")
        return
    }
    var selectedSubjectId by remember { mutableStateOf(subjects.first().id) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-11-30") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Assignment Title *") },
            placeholder = { Text("e.g. Lab 4 Buffer Overflow Analysis") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = dueDate,
            onValueChange = { dueDate = it },
            label = { Text("Deadline (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                if (title.isNotBlank()) onAdd(selectedSubjectId, title, description, dueDate)
            },
            enabled = title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Add Assignment")
        }
    }
}

@Composable
fun QuickAddHabitForm(
    onAdd: (String, String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("times") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Habit Name *") },
            placeholder = { Text("e.g. Review Flashcards") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            placeholder = { Text("e.g. 15 minutes before bed") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = target,
                onValueChange = { target = it },
                label = { Text("Target Count") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = unit,
                onValueChange = { unit = it },
                label = { Text("Unit (mins/times/pages)") },
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = {
                if (name.isNotBlank()) onAdd(name, description, target.toIntOrNull() ?: 1, unit)
            },
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Create Habit")
        }
    }
}
