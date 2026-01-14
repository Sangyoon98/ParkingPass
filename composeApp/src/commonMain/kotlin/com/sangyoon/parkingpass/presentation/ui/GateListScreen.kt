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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.model.GateDirection
import com.sangyoon.parkingpass.presentation.ui.components.RoundedSearchBar
import com.sangyoon.parkingpass.presentation.ui.theme.PrimaryBlue
import com.sangyoon.parkingpass.presentation.ui.theme.StatusEntry
import com.sangyoon.parkingpass.presentation.ui.theme.StatusExit
import androidx.compose.material3.Surface
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary
import com.sangyoon.parkingpass.presentation.viewmodel.GateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateListScreen(
    viewModel: GateViewModel,
    parkingLotId: Long,
    onBack: () -> Unit,
    onCreateClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(parkingLotId) {
        viewModel.loadGates(parkingLotId)
    }

    // Filter gates based on search query
    val filteredGates = if (searchQuery.isBlank()) {
        uiState.gates
    } else {
        uiState.gates.filter { gate ->
            gate.name.contains(searchQuery, ignoreCase = true) ||
            gate.deviceKey.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("게이트 목록") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                shape = RoundedCornerShape(16.dp),
                containerColor = PrimaryBlue
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "게이트 추가",
                    tint = Color.White
                )
            }
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "오류가 발생했습니다",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadGates(parkingLotId) }) {
                            Text("다시 시도")
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Search Bar
                    RoundedSearchBar(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "게이트 이름 또는 디바이스 키로 검색",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )

                    // Gate List
                    if (filteredGates.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) {
                                    "등록된 게이트가 없습니다"
                                } else {
                                    "검색 결과가 없습니다"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredGates, key = { it.id }) { gate ->
                                GateCard(
                                    gate = gate,
                                    onEditClick = { /* TODO: 편집 기능 */ },
                                    onDeleteClick = { /* TODO: 삭제 기능 */ }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GateCard(
    gate: Gate,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Direction Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(12.dp),
                color = when (gate.direction) {
                    GateDirection.ENTER -> StatusEntry.copy(alpha = 0.2f)
                    GateDirection.EXIT -> StatusExit.copy(alpha = 0.2f)
                    GateDirection.BOTH -> PrimaryBlue.copy(alpha = 0.2f)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (gate.direction) {
                            GateDirection.ENTER -> Icons.Default.Login
                            GateDirection.EXIT -> Icons.Default.ExitToApp
                            GateDirection.BOTH -> Icons.Default.SwitchAccount
                        },
                        contentDescription = null,
                        tint = when (gate.direction) {
                            GateDirection.ENTER -> StatusEntry
                            GateDirection.EXIT -> StatusExit
                            GateDirection.BOTH -> PrimaryBlue
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Gate Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = gate.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Direction Badge
                    FilterChip(
                        selected = false,
                        onClick = { },
                        enabled = false,
                        label = {
                            Text(
                                text = when (gate.direction) {
                                    GateDirection.ENTER -> "입구"
                                    GateDirection.EXIT -> "출구"
                                    GateDirection.BOTH -> "양방향"
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = when (gate.direction) {
                                GateDirection.ENTER -> StatusEntry.copy(alpha = 0.2f)
                                GateDirection.EXIT -> StatusExit.copy(alpha = 0.2f)
                                GateDirection.BOTH -> PrimaryBlue.copy(alpha = 0.2f)
                            },
                            labelColor = when (gate.direction) {
                                GateDirection.ENTER -> StatusEntry
                                GateDirection.EXIT -> StatusExit
                                GateDirection.BOTH -> PrimaryBlue
                            },
                            disabledContainerColor = when (gate.direction) {
                                GateDirection.ENTER -> StatusEntry.copy(alpha = 0.2f)
                                GateDirection.EXIT -> StatusExit.copy(alpha = 0.2f)
                                GateDirection.BOTH -> PrimaryBlue.copy(alpha = 0.2f)
                            },
                            disabledLabelColor = when (gate.direction) {
                                GateDirection.ENTER -> StatusEntry
                                GateDirection.EXIT -> StatusExit
                                GateDirection.BOTH -> PrimaryBlue
                            }
                        ),
                        border = null,
                        modifier = Modifier.height(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Device: ${gate.deviceKey}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            // Action Icons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "편집",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
