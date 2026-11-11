package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskStatus
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.IndigoSecondary
import com.example.ui.viewmodel.MySpaceViewModel
import java.util.Locale

@Composable
fun AnalyticsSettingsScreen(
    viewModel: MySpaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()
    val pomodoroState by viewModel.pomodoroState.collectAsState()
    val attendanceStats by viewModel.attendanceStats.collectAsState()
    val overallAttendance by viewModel.overallAttendancePercentage.collectAsState()
    val gpaStat by viewModel.gpaStat.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val completedTasksCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val taskCompletionPct = if (tasks.isNotEmpty()) (completedTasksCount * 100 / tasks.size) else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Productivity Analytics & Settings",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Detailed metrics on your academic progress and productivity.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Overview Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricStatCard(
                    title = "Task Success",
                    value = "$taskCompletionPct%",
                    subtitle = "$completedTasksCount/${tasks.size} done",
                    icon = Icons.Default.CheckCircle,
                    iconColor = IndigoPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Avg Attendance",
                    value = String.format(Locale.getDefault(), "%.1f%%", overallAttendance),
                    subtitle = "Across all subjects",
                    icon = Icons.Default.FactCheck,
                    iconColor = IndigoSecondary,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Current SGPA",
                    value = String.format(Locale.getDefault(), "%.2f", gpaStat.sgpa),
                    subtitle = "CGPA: ${String.format(Locale.getDefault(), "%.2f", gpaStat.cgpa)}",
                    icon = Icons.Default.School,
                    iconColor = AmberAccent,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Subject Attendance Comparison
        item {
            SectionHeader(title = "Subject-Wise Attendance Analytics")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    attendanceStats.forEach { stat ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stat.subject.code,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f%% (%d/%d)", stat.percentage, stat.attendedClasses, stat.totalClasses),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (stat.percentage >= 75f) IndigoSecondary else MaterialTheme.colorScheme.error
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                                color = if (stat.percentage >= 75f) IndigoSecondary else MaterialTheme.colorScheme.error,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }

        // Habit Streak Consistency
        item {
            SectionHeader(title = "Habits & Focus Consistency")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    habits.forEach { habit ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(habit.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Goal: ${habit.targetValue} ${habit.unit}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("🔥 ${habit.currentStreak}d Streak", fontWeight = FontWeight.Bold, color = AmberAccent, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Data Management: Backup & Sample Data
        item {
            SectionHeader(title = "Data Management & Backup")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { showBackupDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_backup_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export JSON Backup Summary")
                    }

                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_sample_data_button")
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reload Starter College Data")
                    }
                }
            }
        }

        // Profile & Settings
        item {
            SectionHeader(title = "User Profile & Settings")
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Student Name: Alex Mercer", fontWeight = FontWeight.SemiBold)
                    Text("Program: B.Tech Computer Science & Engineering", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Active Semester: Semester 5 (2026-2027)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Primary Timezone: Asia/Kolkata", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Architecture: Native Android Jetpack Compose + Offline-first Room SQLite", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    // Backup JSON Dialog
    if (showBackupDialog) {
        val backupJson = """
            {
              "appName": "MySpace",
              "exportedAt": "2026-11-11",
              "semester": "Semester 5 (B.Tech CSE)",
              "stats": {
                "totalTasks": ${tasks.size},
                "completedTasks": $completedTasksCount,
                "overallAttendance": "${String.format(Locale.getDefault(), "%.1f", overallAttendance)}%",
                "sgpa": ${gpaStat.sgpa},
                "cgpa": ${gpaStat.cgpa},
                "totalFocusMinutes": ${pomodoroState.totalFocusMinutesToday}
              }
            }
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("JSON Backup Summary") },
            text = {
                Column {
                    Text("Structured export of your current academic and productivity records:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = backupJson,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("MySpace Backup", backupJson))
                        Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showBackupDialog = false
                    }
                ) {
                    Text("Copy JSON")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reload Sample Data?") },
            text = { Text("This will reset tasks, attendance, timetable, exams, and habits back to the initial Semester 5 starter pack.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetToSampleData()
                        showResetDialog = false
                        Toast.makeText(context, "Sample data reloaded!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
