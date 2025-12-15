package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotDetailScreen(
    viewModel: ParkingLotDetailViewModel,
    parkingLotId: Long,
    onCreateVehicleClick: () -> Unit,
    onManageGateClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(parkingLotId) {
        viewModel.load(parkingLotId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("주차장 상세") }) },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(onClick = onCreateVehicleClick) { Text("차량+") }
                FloatingActionButton(onClick = onManageGateClick) { Text("게이트") }
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            uiState.error != null -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.load(parkingLotId) }) { Text("다시 시도") }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(uiState.parkingLot?.name ?: "", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(uiState.parkingLot?.location ?: "", style = MaterialTheme.typography.bodyMedium)
                    }

                    item {
                        Text("현재 주차 중", style = MaterialTheme.typography.titleMedium)
                    }
                    items(uiState.openSessions) { session ->
                        SessionItem(session)
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("입출차 기록", style = MaterialTheme.typography.titleMedium)
                        DateSelector(
                            selected = uiState.selectedDate,
                            onSelect = { date ->
                                viewModel.load(parkingLotId, date)
                            }
                        )
                    }
                    items(uiState.history) { session ->
                        SessionItem(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionItem(session: com.sangyoon.parkingpass.domain.model.Session) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("번호판: ${session.plateNumber}", style = MaterialTheme.typography.titleMedium)
            Text("상태: ${session.status}", style = MaterialTheme.typography.bodyMedium)
            Text("입차: ${session.enteredAt}", style = MaterialTheme.typography.bodySmall)
            session.exitedAt?.let { Text("출차: $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun DateSelector(selected: String?, onSelect: (String) -> Unit) {
    // 단순 입력용(추후 DatePicker 대체 가능)
    var input by remember { mutableStateOf(selected ?: "") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("YYYY-MM-DD") },
            modifier = Modifier.weight(1f)
        )
        Button(onClick = { onSelect(input) }) { Text("조회") }
    }
}