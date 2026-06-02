package com.rostelcom.servicedesk.ui.detail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rostelcom.servicedesk.data.model.ServiceRequest
import com.rostelcom.servicedesk.ui.theme.Purple

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    request: ServiceRequest,
    onBack: () -> Unit,
    viewModel: DetailViewModel = viewModel()
) {
    var comment by remember { mutableStateOf(request.externalComment) }
    var selectedStatus by remember { mutableStateOf(request.status) }
    val statuses = listOf("Назначена", "В работе", "Выполнена", "Отложена")
    val context = LocalContext.current

    val dateFormatted = remember(request.createdAt?.seconds) {
        request.createdAt?.toDate()?.let { d ->
            java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale("ru")).format(d)
        } ?: "—"
    }

    val dividerColor = Color.Gray.copy(alpha = 0.4f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(request.id, color = Color.White, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Purple)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    DetailRow("Клиент", request.clientInfo)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                    DetailRow("Адрес", request.address)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                    DetailRow("Телефон", request.phone)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                    DetailRow("Тип", request.equipmentType)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                    DetailRow("Приоритет", request.priority)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                    DetailRow("Статус", request.status)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                    DetailRow("Дата", dateFormatted)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor)
                    DetailRow("Серийный номер", request.serialNumber.ifBlank { "—" })
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Описание неисправности", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Purple)
                    Spacer(Modifier.height(4.dp))
                    Text(request.issueDescription.ifBlank { "—" }, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text("Комментарий диспетчера", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Purple)
                    Spacer(Modifier.height(4.dp))
                    Text(request.externalComment.ifBlank { "—" }, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("Сменить статус:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Purple)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statuses.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = {
                            selectedStatus = status
                            viewModel.updateStatus(request.documentId, status)
                            Toast.makeText(context, "Статус изменён на «$status»", Toast.LENGTH_SHORT).show()
                        },
                        label = { Text(status, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Purple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = { if (it.length <= 250) comment = it },
                label = { Text("Ваш комментарий (до 250 символов)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Button(
                onClick = {
                    viewModel.addComment(request.documentId, comment)
                    Toast.makeText(context, "Комментарий сохранён", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Purple)
            ) {
                Text("💾 Сохранить комментарий", color = Color.White, fontSize = 15.sp)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Purple, modifier = Modifier.width(100.dp))
        Text(value, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}