package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HabitItem
import com.example.data.model.Priority
import com.example.data.model.TaskItem
import com.example.data.model.TaskStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.IndigoSecondary
import com.example.ui.viewmodel.MySpaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: MySpaceViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tasks, 1: Habits
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()

    val filteredTasks = remember(tasks, selectedCategory, selectedStatusFilter) {
        tasks.filter { task ->
            val matchCat = selectedCategory == "All" || task.category.equals(selectedCategory, ignoreCase = true)
            val matchStatus = when (selectedStatusFilter) {
                "Pending" -> task.status != TaskStatus.COMPLETED
                "Completed" -> task.status == TaskStatus.COMPLETED
                else -> true
            }
            matchCat && matchStatus
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Top Sub-Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .padding(top = 12.dp, bottom = 12.dp)
                .fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Tasks (${tasks.count { it.status != TaskStatus.COMPLETED }})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Checklist, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_tasks")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Daily Habits (${habits.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_habits")
            )
        }

        if (selectedTab == 0) {
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "College", "Coding", "Projects", "Exam Prep", "Personal").forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            // Status Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pending", "Completed").forEach { st ->
                    AssistChip(
                        onClick = { selectedStatusFilter = st },
                        label = { Text(st) },
                        leadingIcon = if (selectedStatusFilter == st) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredTasks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.TaskAlt,
                    title = "No Tasks Found",
                    message = "Tap the Quick Add (+) button below to create your next task or assignment."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCardItem(
                            task = task,
                            onToggle = { viewModel.toggleTaskStatus(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        } else {
            // Habits Tab
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Text(
                        text = "Build consistent college routines. Check-in daily to grow your streaks!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(habits, key = { it.id }) { habit ->
                    HabitCardItem(
                        habit = habit,
                        onCheckIn = { viewModel.checkInHabit(habit) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCardItem(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .testTag("task_item_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.status == TaskStatus.COMPLETED,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("task_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough else null,
                    color = if (task.status == TaskStatus.COMPLETED) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )

                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = task.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (task.dueDate.isNotBlank()) {
                        Text(
                            text = "Due: ${task.dueDate} ${task.dueTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                PriorityBadge(priority = task.priority)
                Spacer(modifier = Modifier.height(12.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Task",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HabitCardItem(
    habit: HabitItem,
    onCheckIn: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔥 ${habit.currentStreak} Days Streak",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberAccent
                    )
                    Text(
                        text = "• Best: ${habit.bestStreak}d",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (habit.description.isNotBlank()) {
                    Text(
                        text = habit.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Goal: ${habit.targetValue} ${habit.unit} (${habit.frequency})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onCheckIn,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (habit.lastCompletedDate.isNotEmpty()) IndigoSecondary else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("habit_checkin_${habit.id}")
            ) {
                Icon(
                    imageVector = if (habit.lastCompletedDate.isNotEmpty()) Icons.Default.Check else Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (habit.lastCompletedDate.isNotEmpty()) "Done!" else "Check In")
            }
        }
    }
}
