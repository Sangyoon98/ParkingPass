package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotSearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotSearchScreen(
    viewModel: ParkingLotSearchViewModel,
    onBack: () -> Unit,
    onParkingLotClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("주차장 검색") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = {
                    viewModel.updateQuery(it)
                    viewModel.clearMessage()
                },
                label = { Text("검색어") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.search() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("검색")
            }
            uiState.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (uiState.isLoading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.results) { parkingLot ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(parkingLot.name, style = MaterialTheme.typography.titleMedium)
                            Text(parkingLot.location, style = MaterialTheme.typography.bodyMedium)
                            if (!parkingLot.isPublic) {
                                Text("비공개 주차장", color = MaterialTheme.colorScheme.primary)
                                parkingLot.joinCode?.let {
                                    Text("초대 코드: $it", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            RowActions(
                                onJoin = { viewModel.requestJoin(parkingLot.id) },
                                onDetail = { onParkingLotClick(parkingLot.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowActions(
    onJoin: () -> Unit,
    onDetail: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onJoin,
            modifier = Modifier.weight(1f)
        ) {
            Text("가입 요청")
        }
        Button(
            onClick = onDetail,
            modifier = Modifier.weight(1f)
        ) {
            Text("상세 보기")
        }
    }
}
