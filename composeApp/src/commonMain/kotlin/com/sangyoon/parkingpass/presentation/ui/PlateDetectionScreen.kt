package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlateDetectionScreen(
    viewModel: PlateDetectionViewModel,
    parkingLotId: Long,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(parkingLotId) {
        viewModel.loadGates(parkingLotId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("입출차 체크") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("<") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 게이트 선택
            Text("게이트 선택", style = MaterialTheme.typography.titleMedium)

            if(uiState.isLoading && uiState.gates.isEmpty()) {
                CircularProgressIndicator()
            } else if (uiState.gates.isEmpty()) {
                Text("등록된 게이트가 없습니다", color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(
                    modifier = Modifier.height(150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                   items(uiState.gates) { gate ->
                       FilterChip(
                           selected = uiState.selectedGate?.id == gate.id,
                           onClick = { viewModel.selectGate(gate) },
                           label = {
                               Column {
                                   Text(gate.name)
                                   Text(gate.deviceKey, style = MaterialTheme.typography.bodySmall)
                               }
                           },
                           modifier = Modifier.fillMaxWidth()
                       )
                   }
                }
            }

            HorizontalDivider()

            // 번호판 입력
            Text("번호판 번호", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = uiState.plateNumber,
                onValueChange = { viewModel.updatePlateNumber(it) },
                label = { Text("번호판 번호 입력") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.selectedGate != null
            )

            // 체크 버튼
            Button(
                onClick = {
                    viewModel.detectPlate {
                        // 성공 시 결과 화면 표시 (AlertDialog 등)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading &&
                        uiState.selectedGate != null &&
                        uiState.plateNumber.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text("입출차 체크")
            }

            // 에러 표시
            uiState.error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // 결과 표시
            uiState.result?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.action == "ENTER") {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (result.action == "ENTER") "입차 성공" else "출차 성공",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("번호판: ${result.plateNumber}")
                        Text("세션 ID: ${result.sessionId}")
                        Text("등록 차량: ${if (result.isRegistered) "예" else "아니오"}")
                        result.vehicleLabel?.let {
                            Text("차량 라벨: $it")
                        }
                        result.vehicleCategory?.let {
                            Text("카테고리: $it")
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                viewModel.clearResult()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("확인")
                        }
                    }
                }
            }
        }
    }
}