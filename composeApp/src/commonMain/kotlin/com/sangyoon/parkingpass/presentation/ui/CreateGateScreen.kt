package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.GateDirection
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGateScreen(
    viewModel: GateViewModel,
    parkingLotId: Long,
    onCreated: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var deviceKey by remember { mutableStateOf("") }
    var direction by remember { mutableStateOf(GateDirection.ENTER) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게이트 등록") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("<") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("게이트 이름") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = deviceKey,
                onValueChange = { deviceKey = it },
                label = { Text("디바이스 키") },
                modifier = Modifier.fillMaxWidth()
            )

            // 방향 선택
            Text("방향", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GateDirection.entries.forEach { dir ->
                    FilterChip(
                        selected = direction == dir,
                        onClick = { direction = dir },
                        label = { Text(dir.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        viewModel.registerGate(
                            parkingLotId = parkingLotId,
                            name = name,
                            deviceKey = deviceKey,
                            direction = direction,
                            onSuccess = onCreated
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("등록")
                }
            }

            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}