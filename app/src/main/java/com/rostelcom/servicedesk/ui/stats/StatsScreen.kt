package com.rostelcom.servicedesk.ui.stats

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rostelcom.servicedesk.ui.theme.Purple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    userId: String,
    onBack: () -> Unit,
    viewModel: StatsViewModel = viewModel(
        factory = StatsViewModelFactory(userId, LocalContext.current.applicationContext as Application)
    )
) {
    val stats by viewModel.stats.collectAsState()
    var selectedPeriod by remember { mutableStateOf("week") }

    LaunchedEffect(selectedPeriod) {
        viewModel.loadStats(selectedPeriod)
    }

    val rateText = remember(stats.successRate) { "%.0f".format(stats.successRate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика", color = androidx.compose.ui.graphics.Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", color = androidx.compose.ui.graphics.Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple)
            )
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("day" to "День", "week" to "Неделя", "month" to "Месяц").forEach { (key, label) ->
                    FilterChip(
                        selected = selectedPeriod == key,
                        onClick = { selectedPeriod = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Purple, selectedLabelColor = androidx.compose.ui.graphics.Color.White)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(
                Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Выполнено заявок", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Text("${stats.totalCompleted}", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Purple, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Успешных замен", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Text("$rateText%", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Purple, textAlign = TextAlign.Center)
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Среднее время на объекте", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Text("${stats.avgTimeMinutes.toInt()} мин", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Purple, textAlign = TextAlign.Center)
                }
            }
        }
    }
}