package com.rostelcom.servicedesk.ui.main

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rostelcom.servicedesk.data.model.ServiceRequest
import com.rostelcom.servicedesk.ui.theme.Purple
import com.rostelcom.servicedesk.util.SessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userId: String,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onRequestClick: (ServiceRequest) -> Unit,
    onLogout: () -> Unit,
    onStatsClick: () -> Unit,
    viewModel: MainViewModel = viewModel(
        factory = MainViewModelFactory(
            userId = userId,
            application = LocalContext.current.applicationContext as Application
        )
    )
) {
    val allRequests by viewModel.requests.collectAsState()
    val searchQueryState by viewModel.searchQuery.collectAsState()
    val statusFilterState by viewModel.statusFilter.collectAsState()
    val typeFilterState by viewModel.typeFilter.collectAsState()
    val priorityFilterState by viewModel.priorityFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    val requests = remember(allRequests, searchQueryState, statusFilterState, typeFilterState, priorityFilterState) {
        viewModel.getFilteredRequests()
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        SessionManager(context).lastActiveTime = System.currentTimeMillis()
    }

    LaunchedEffect(Unit) {
        viewModel.loadRequests()
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Выход") },
            text = { Text("Вы уверены, что хотите выйти?") },
            confirmButton = {
                TextButton(onClick = { showExitDialog = false; onLogout() }) { Text("Да") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Нет") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои заявки", color = Color.White, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Text(if (isDarkTheme) "☀" else "🌙", color = Color.White, fontSize = 20.sp)
                    }
                    IconButton(onClick = onStatsClick) {
                        Text("📊", color = Color.White, fontSize = 20.sp)
                    }
                    IconButton(onClick = { viewModel.loadRequests() }) {
                        Text("🔄", color = Color.White, fontSize = 20.sp)
                    }
                    Button(
                        onClick = { showExitDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(end = 8.dp).height(36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("Выход", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple)
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQueryState,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Поиск...") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterDropdown("Статус", statusFilterState, listOf("Все", "Новая", "Назначена", "В работе", "Выполнена", "Отложена", "Отменена"), 110.dp) { viewModel.setStatusFilter(it) }
                FilterDropdown("Тип", typeFilterState, listOf("Все", "ONT-модем", "Wi-Fi маршрутизатор", "ТВ-приставка", "Видеокамера"), 170.dp) { viewModel.setTypeFilter(it) }
                FilterDropdown("Приоритет", priorityFilterState, listOf("Все", "Плановый", "Срочный", "Аварийный"), 110.dp) { viewModel.setPriorityFilter(it) }
            }

            Spacer(Modifier.height(4.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (requests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📭", fontSize = 48.sp)
                        Text("Нет назначенных заявок", fontSize = 16.sp)
                        Text("Диспетчер пока не назначил вам заявки", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(requests) { request ->
                        val cardColor = when (request.priority) {
                            "Аварийный" -> if (isDarkTheme) Color(0xFF5C1A1A) else Color(0xFFFFCDD2)
                            "Срочный" -> if (isDarkTheme) Color(0xFF5C3A1A) else Color(0xFFFFE0B2)
                            else -> MaterialTheme.colorScheme.surface
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onRequestClick(request) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(request.id, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(request.address, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(request.status, color = Purple, fontWeight = FontWeight.Bold)
                                    Text(request.priority, color = if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFB71C1C))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(label: String, selected: String, options: List<String>, width: androidx.compose.ui.unit.Dp, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 11.sp) },
            modifier = Modifier.menuAnchor().width(width),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option, fontSize = 13.sp) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}