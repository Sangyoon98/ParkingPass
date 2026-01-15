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
import androidx.compose.runtime.remember
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
    var gateToEdit by remember { mutableStateOf<Gate?>(null) }
    var gateToDelete by remember { mutableStateOf<Gate?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                                    onEditClick = {
                                        gateToEdit = gate
                                        showEditDialog = true
                                    },
                                    onDeleteClick = {
                                        gateToDelete = gate
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && gateToEdit != null) {
        EditGateDialog(
            gate = gateToEdit!!,
            onDismiss = {
                showEditDialog = false
                gateToEdit = null
            },
            onConfirm = { name, deviceKey, direction ->
                viewModel.updateGate(
                    gateId = gateToEdit!!.id,
                    parkingLotId = parkingLotId,
                    name = name,
                    deviceKey = deviceKey,
                    direction = direction,
                    onSuccess = {
                        showEditDialog = false
                        gateToEdit = null
                    }
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && gateToDelete != null) {
        DeleteGateDialog(
            gateName = gateToDelete!!.name,
            onDismiss = {
                showDeleteDialog = false
                gateToDelete = null
            },
            onConfirm = {
                viewModel.deleteGate(
                    gateId = gateToDelete!!.id,
                    parkingLotId = parkingLotId,
                    onSuccess = {
                        showDeleteDialog = false
                        gateToDelete = null
                    }
                )
            }
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditGateDialog(
    gate: Gate,
    onDismiss: () -> Unit,
    onConfirm: (name: String, deviceKey: String, direction: GateDirection) -> Unit
) {
    var name by remember { mutableStateOf(gate.name) }
    var deviceKey by remember { mutableStateOf(gate.deviceKey) }
    var selectedDirection by remember { mutableStateOf(gate.direction) }
    var directionExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("게이트 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

                ExposedDropdownMenuBox(
                    expanded = directionExpanded,
                    onExpandedChange = { directionExpanded = it }
                ) {
                    OutlinedTextField(
                        value = when (selectedDirection) {
                            GateDirection.ENTER -> "입구"
                            GateDirection.EXIT -> "출구"
                            GateDirection.BOTH -> "양방향"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("방향") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = directionExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = directionExpanded,
                        onDismissRequest = { directionExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("입구") },
                            onClick = {
                                selectedDirection = GateDirection.ENTER
                                directionExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("출구") },
                            onClick = {
                                selectedDirection = GateDirection.EXIT
                                directionExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("양방향") },
                            onClick = {
                                selectedDirection = GateDirection.BOTH
                                directionExpanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && deviceKey.isNotBlank()) {
                        onConfirm(name, deviceKey, selectedDirection)
                    }
                },
                enabled = name.isNotBlank() && deviceKey.isNotBlank()
            ) {
                Text("수정")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

@Composable
private fun DeleteGateDialog(
    gateName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("게이트 삭제") },
        text = { Text("'$gateName' 게이트를 삭제하시겠습니까?\n이 작업은 취소할 수 없습니다.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
