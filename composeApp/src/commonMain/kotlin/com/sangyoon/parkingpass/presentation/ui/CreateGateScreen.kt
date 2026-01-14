package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.GateDirection
import com.sangyoon.parkingpass.presentation.ui.theme.PrimaryBlue
import com.sangyoon.parkingpass.presentation.ui.theme.StatusEntry
import com.sangyoon.parkingpass.presentation.ui.theme.StatusExit
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary
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
    var direction by remember { mutableStateOf(GateDirection.ENTRY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "게이트 등록",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Gate Name Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "게이트 이름",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("예: 정문 입구", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
            }

            // Device Key Field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "디바이스 키",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                OutlinedTextField(
                    value = deviceKey,
                    onValueChange = { deviceKey = it },
                    placeholder = { Text("예: GATE_001", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
            }

            // Direction Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "방향",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GateDirection.entries.forEach { dir ->
                        val isSelected = direction == dir
                        FilterChip(
                            selected = isSelected,
                            onClick = { direction = dir },
                            label = {
                                Text(
                                    text = when (dir) {
                                        GateDirection.ENTRY -> "입구"
                                        GateDirection.EXIT -> "출구"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = TextSecondary,
                                selectedContainerColor = when (dir) {
                                    GateDirection.ENTRY -> StatusEntry.copy(alpha = 0.2f)
                                    GateDirection.EXIT -> StatusExit.copy(alpha = 0.2f)
                                },
                                selectedLabelColor = when (dir) {
                                    GateDirection.ENTRY -> StatusEntry
                                    GateDirection.EXIT -> StatusExit
                                }
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = when (dir) {
                                    GateDirection.ENTRY -> StatusEntry
                                    GateDirection.EXIT -> StatusExit
                                },
                                enabled = true,
                                selected = isSelected
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error Message
            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Submit Button
            if (uiState.isLoading) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = false
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                Button(
                    onClick = {
                        viewModel.registerGate(
                            parkingLotId = parkingLotId,
                            name = name.trim(),
                            deviceKey = deviceKey.trim(),
                            direction = direction,
                            onSuccess = onCreated
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    enabled = name.isNotBlank() && deviceKey.isNotBlank()
                ) {
                    Text(
                        text = "게이트 등록",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
