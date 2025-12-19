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
import androidx.compose.material3.IconButton
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
import com.sangyoon.parkingpass.domain.model.Session
import com.sangyoon.parkingpass.presentation.viewmodel.SessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    viewModel: SessionViewModel,
    parkingLotId: Long,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(parkingLotId) {
        viewModel.loadSessions(parkingLotId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("입출차 세션") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("<") }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            uiState.error != null -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadSessions(parkingLotId) }) {
                        Text("다시 시도")
                    }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("현재 주차 중", style = MaterialTheme.typography.titleLarge)
                }
                if (uiState.openSessions.isEmpty()) {
                    item {
                        Text(
                            "주차 중인 차량이 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.openSessions) { session ->
                        SessionItem(session)
                    }
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Text("입출차 기록", style = MaterialTheme.typography.titleLarge)
                }
                item {
                    DateSelector(
                        selected = uiState.selectedDate,
                        onSelect = { date ->
                            viewModel.loadSessions(parkingLotId, date)
                        }
                    )
                }
                if (uiState.history.isEmpty()) {
                    item {
                        Text(
                            "조회된 기록이 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(uiState.history) { session ->
                        SessionItem(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionItem(session: Session) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    session.plateNumber,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    session.status.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (session.status) {
                        com.sangyoon.parkingpass.domain.model.SessionStatus.OPEN ->
                            MaterialTheme.colorScheme.primary
                        com.sangyoon.parkingpass.domain.model.SessionStatus.CLOSED ->
                            MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            session.vehicleLabel?.let {
                Spacer(Modifier.height(4.dp))
                Text("차량명: $it", style = MaterialTheme.typography.bodyMedium)
            }
            session.vehicleCategory?.let {
                Text("분류: ${it.name}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(4.dp))
            Text("입차: ${session.enteredAt}", style = MaterialTheme.typography.bodySmall)
            session.exitedAt?.let {
                Text("출차: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun DateSelector(selected: String?, onSelect: (String) -> Unit) {
    var input by remember { mutableStateOf(selected ?: "") }
    LaunchedEffect(selected) {
        input = selected ?: ""
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("YYYY-MM-DD") },
            modifier = Modifier.weight(1f)
        )
        Button(onClick = { onSelect(input) }) {
            Text("조회")
        }
    }
}