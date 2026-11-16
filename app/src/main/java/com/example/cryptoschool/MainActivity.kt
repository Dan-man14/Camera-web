package com.example.cryptoschool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.cryptoschool.data.LessonLevel
import com.example.cryptoschool.data.ProgressStore
import com.example.cryptoschool.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = ProgressStore(this)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = NeonGreen, background = BgDark, surface = CardDark)) {
                var selectedTab by remember { mutableStateOf(0) }
                var activeLesson by remember { mutableStateOf<LessonLevel?>(null) }

                val currentLesson = activeLesson
                if (currentLesson != null) {
                    LessonStoryScreen(
                        lesson = currentLesson,
                        onFinish = {
                            store.markLevelCompleted(currentLesson.id, currentLesson.xpReward)
                            activeLesson = null
                        },
                        onBack = { activeLesson = null }
                    )
                } else {
                    Scaffold(
                        containerColor = BgDark,
                        bottomBar = {
                            NavigationBar(containerColor = CardDark) {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Default.School, null) },
                                    label = { Text("Обучение") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Default.ShowChart, null) },
                                    label = { Text("Тренажер") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Default.MenuBook, null) },
                                    label = { Text("Шпаргалки") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    icon = { Icon(Icons.Default.Person, null) },
                                    label = { Text("Профиль") }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (selectedTab) {
                                0 -> RoadmapScreen(store = store, onOpenLesson = { activeLesson = it })
                                1 -> SandboxScreen(store = store)
                                2 -> CheatSheetsScreen()
                                3 -> ProfileScreen(store = store)
                            }
                        }
                    }
                }
            }
        }
    }
}
