package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.Session
import com.sangyoon.parkingpass.domain.model.SessionStatus
import com.sangyoon.parkingpass.domain.model.Vehicle
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.presentation.state.VehicleInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleInfoBottomSheet(
    vehicleInfo: VehicleInfo,
    onEnter: () -> Unit,
    onExit: () -> Unit,
    onRegister: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 번호판 표시
            Text(
                text = vehicleInfo.plateNumber,
                style = MaterialTheme.typography.headlineMedium
            )

            HorizontalDivider()

            if (vehicleInfo.vehicle != null) {
                // 등록된 차량 정보
                RegisteredVehicleContent(
                    vehicle = vehicleInfo.vehicle,
                    currentSession = vehicleInfo.currentSession,
                    onEnter = onEnter,
                    onExit = onExit
                )
            } else {
                // 미등록 차량
                UnregisteredVehicleContent(
                    plateNumber = vehicleInfo.plateNumber,
                    onRegister = onRegister
                )
            }

            // 다시 인식 버튼
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다시 인식")
            }
        }
    }
}

@Composable
private fun RegisteredVehicleContent(
    vehicle: Vehicle,
    currentSession: Session?,
    onEnter: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 차량 정보
        InfoRow("명칭", vehicle.label)
        InfoRow("분류", getCategoryDisplayName(vehicle.category))
        vehicle.memo?.let {
            InfoRow("메모", it)
        }

        HorizontalDivider()

        // 현재 상태
        val statusText = if (currentSession != null && currentSession.status == SessionStatus.OPEN) {
            "주차 중 (입차: ${formatTime(currentSession.enteredAt)})"
        } else {
            "주차장 외부"
        }
        InfoRow("현재 상태", statusText)

        HorizontalDivider()

        // 입출차 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (currentSession?.status != SessionStatus.OPEN) {
                // 입차 가능
                Button(
                    onClick = onEnter,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("입차 처리")
                }
            } else {
                // 출차 가능
                Button(
                    onClick = onExit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("출차 처리")
                }
            }
        }
    }
}

@Composable
private fun UnregisteredVehicleContent(
    plateNumber: String,
    onRegister: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "등록되지 않은 차량입니다",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("차량 등록 후 입차")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatTime(dateTimeString: String): String {
    // ISO8601 형식에서 시간만 추출
    return try {
        dateTimeString.split("T")[1].split(".")[0].substring(0, 5) // HH:mm
    } catch (e: Exception) {
        dateTimeString
    }
}

private fun getCategoryDisplayName(category: VehicleCategory): String {
    return when (category) {
        VehicleCategory.RESIDENT -> "거주자"
        VehicleCategory.EMPLOYEE -> "직원"
        VehicleCategory.VISITOR -> "방문자"
    }
}

