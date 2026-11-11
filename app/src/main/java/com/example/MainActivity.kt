package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.QuickAddBottomSheet
import com.example.ui.screens.*
import com.example.ui.theme.MySpaceTheme
import com.example.ui.viewmodel.MySpaceViewModel

enum class NavigationTab(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Default.Dashboard),
    TASKS("Tasks", Icons.Default.Checklist),
    COLLEGE("College", Icons.Default.School),
    FOCUS("Study", Icons.Default.Timer),
    ANALYTICS("Stats", Icons.Default.BarChart)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MySpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MySpaceTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: MySpaceViewModel) {
    var currentTab by remember { mutableStateOf(NavigationTab.DASHBOARD) }
    var showQuickAddSheet by remember { mutableStateOf(false) }

    // Handle Back Press to return to Dashboard before exiting
    BackHandler(enabled = currentTab != NavigationTab.DASHBOARD) {
        currentTab = NavigationTab.DASHBOARD
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text(
                            text = "MySpace",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "OFFLINE-FIRST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showQuickAddSheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("top_quick_add_button")
                    ) {
                        Icon(
                            Icons.Default.AddCircleOutline,
                            contentDescription = "Quick Add",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontWeight = if (currentTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("quick_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Universal Quick Add")
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavigationTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTasks = { currentTab = NavigationTab.TASKS },
                    onNavigateToCollege = { currentTab = NavigationTab.COLLEGE },
                    onNavigateToFocus = { currentTab = NavigationTab.FOCUS }
                )
                NavigationTab.TASKS -> TasksScreen(viewModel = viewModel)
                NavigationTab.COLLEGE -> CollegeScreen(viewModel = viewModel)
                NavigationTab.FOCUS -> StudyFocusScreen(viewModel = viewModel)
                NavigationTab.ANALYTICS -> AnalyticsSettingsScreen(viewModel = viewModel)
            }
        }

        if (showQuickAddSheet) {
            QuickAddBottomSheet(
                viewModel = viewModel,
                onDismiss = { showQuickAddSheet = false }
            )
        }
    }
}
