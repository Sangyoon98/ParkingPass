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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.Vehicle
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.presentation.ui.components.RoundedSearchBar
import com.sangyoon.parkingpass.presentation.ui.components.VehicleTypeIcon
import com.sangyoon.parkingpass.presentation.ui.theme.PrimaryBlue
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary
import com.sangyoon.parkingpass.presentation.viewmodel.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListScreen(
    viewModel: VehicleViewModel,
    parkingLotId: Long,
    onBack: () -> Unit,
    onCreateClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }
    var vehicleToDelete by remember { mutableStateOf<Vehicle?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(parkingLotId) {
        viewModel.loadVehicles(parkingLotId)
    }

    // Filter vehicles based on search query
    val filteredVehicles = if (searchQuery.isBlank()) {
        uiState.vehicles
    } else {
        uiState.vehicles.filter { vehicle ->
            vehicle.plateNumber.contains(searchQuery, ignoreCase = true) ||
            vehicle.label.contains(searchQuery, ignoreCase = true) ||
            vehicle.memo?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("차량 목록") },
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
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "차량 추가"
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
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadVehicles(parkingLotId) }) {
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
                        placeholder = "차량 번호 또는 이름 검색",
                        onSearch = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vehicle Count
                    Text(
                        text = "총 ${filteredVehicles.size}대",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vehicle List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredVehicles, key = { it.id }) { vehicle ->
                            VehicleItem(
                                vehicle = vehicle,
                                onEditClick = {
                                    vehicleToEdit = vehicle
                                    showEditDialog = true
                                },
                                onDeleteClick = {
                                    vehicleToDelete = vehicle
                                    showDeleteDialog = true
                                }
                            )
                        }

                        if (filteredVehicles.isEmpty() && searchQuery.isNotBlank()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "검색 결과가 없습니다",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Dialog
    if (showEditDialog && vehicleToEdit != null) {
        EditVehicleDialog(
            vehicle = vehicleToEdit!!,
            onDismiss = {
                showEditDialog = false
                vehicleToEdit = null
            },
            onConfirm = { label, category, memo ->
                viewModel.updateVehicle(
                    vehicleId = vehicleToEdit!!.id,
                    parkingLotId = parkingLotId,
                    label = label,
                    category = category,
                    memo = memo,
                    onSuccess = {
                        showEditDialog = false
                        vehicleToEdit = null
                    }
                )
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && vehicleToDelete != null) {
        DeleteVehicleDialog(
            plateNumber = vehicleToDelete!!.plateNumber,
            onDismiss = {
                showDeleteDialog = false
                vehicleToDelete = null
            },
            onConfirm = {
                viewModel.deleteVehicle(
                    vehicleId = vehicleToDelete!!.id,
                    parkingLotId = parkingLotId,
                    onSuccess = {
                        showDeleteDialog = false
                        vehicleToDelete = null
                    }
                )
            }
        )
    }
}

@Composable
private fun VehicleItem(
    vehicle: Vehicle,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Vehicle Type Icon
            VehicleTypeIcon(
                category = vehicle.category,
                size = 48.dp
            )

            // Vehicle Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = vehicle.plateNumber,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = vehicle.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                vehicle.memo?.let { memo ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = memo,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // Action Icons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "편집",
                        tint = MaterialTheme.colorScheme.primary,
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
private fun EditVehicleDialog(
    vehicle: Vehicle,
    onDismiss: () -> Unit,
    onConfirm: (label: String, category: VehicleCategory, memo: String?) -> Unit
) {
    var label by remember(vehicle.id) { mutableStateOf(vehicle.label) }
    var memo by remember(vehicle.id) { mutableStateOf(vehicle.memo ?: "") }
    var selectedCategory by remember(vehicle.id) { mutableStateOf(vehicle.category) }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("차량 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 차량 번호는 수정 불가 (readOnly)
                OutlinedTextField(
                    value = vehicle.plateNumber,
                    onValueChange = {},
                    label = { Text("차량 번호 (수정 불가)") },
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("차량 이름") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = when (selectedCategory) {
                            VehicleCategory.SEDAN -> "승용차"
                            VehicleCategory.SUV -> "SUV"
                            VehicleCategory.ELECTRIC -> "전기차"
                            VehicleCategory.TRUCK -> "트럭"
                            VehicleCategory.VAN -> "밴"
                            VehicleCategory.MOTORCYCLE -> "오토바이"
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("차량 종류") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("승용차") },
                            onClick = {
                                selectedCategory = VehicleCategory.SEDAN
                                categoryExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("SUV") },
                            onClick = {
                                selectedCategory = VehicleCategory.SUV
                                categoryExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("전기차") },
                            onClick = {
                                selectedCategory = VehicleCategory.ELECTRIC
                                categoryExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("트럭") },
                            onClick = {
                                selectedCategory = VehicleCategory.TRUCK
                                categoryExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("밴") },
                            onClick = {
                                selectedCategory = VehicleCategory.VAN
                                categoryExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("오토바이") },
                            onClick = {
                                selectedCategory = VehicleCategory.MOTORCYCLE
                                categoryExpanded = false
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("메모 (선택)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (label.isNotBlank()) {
                        onConfirm(label, selectedCategory, memo.ifBlank { null })
                    }
                },
                enabled = label.isNotBlank()
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
private fun DeleteVehicleDialog(
    plateNumber: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("차량 삭제") },
        text = { Text("'$plateNumber' 차량을 삭제하시겠습니까?\n이 작업은 취소할 수 없습니다.") },
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