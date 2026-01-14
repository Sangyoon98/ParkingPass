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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.sangyoon.parkingpass.domain.model.Session
import com.sangyoon.parkingpass.domain.model.SessionStatus
import com.sangyoon.parkingpass.domain.model.VehicleCategory
import com.sangyoon.parkingpass.presentation.ui.components.FilterChipRow
import com.sangyoon.parkingpass.presentation.ui.components.StatusBadge
import com.sangyoon.parkingpass.presentation.ui.components.VehicleTypeIcon
import com.sangyoon.parkingpass.presentation.ui.theme.PrimaryBlue
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary
import com.sangyoon.parkingpass.presentation.viewmodel.SessionViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionListScreen(
    viewModel: SessionViewModel,
    parkingLotId: Long,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedFilter by rememberSaveable { mutableStateOf<VehicleCategory?>(null) }

    LaunchedEffect(parkingLotId) {
        viewModel.loadSessions(parkingLotId)
    }

    // Filter current sessions by vehicle category
    val filteredOpenSessions = if (selectedFilter == null) {
        uiState.openSessions
    } else {
        uiState.openSessions.filter { it.vehicleCategory == selectedFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "입출차 기록",
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
                        Button(onClick = { viewModel.loadSessions(parkingLotId) }) {
                            Text("다시 시도")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Current Parking Section
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "현재 주차 중",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            // Filter Chips
                            val allCategories = listOf(null) + VehicleCategory.entries
                            FilterChipRow(
                                filters = allCategories,
                                selectedFilter = selectedFilter,
                                onFilterSelected = { selectedFilter = it },
                                filterLabel = { category ->
                                    when (category) {
                                        null -> "전체"
                                        VehicleCategory.SEDAN -> "승용차"
                                        VehicleCategory.SUV -> "SUV"
                                        VehicleCategory.ELECTRIC_CAR -> "전기차"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Count and Last Updated
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "총 ${filteredOpenSessions.size}대 주차 중",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                                Text(
                                    text = "업데이트: ${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    // Current Parking List
                    if (filteredOpenSessions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedFilter == null) {
                                        "주차 중인 차량이 없습니다"
                                    } else {
                                        "해당 차종의 주차 중인 차량이 없습니다"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(filteredOpenSessions, key = { it.id }) { session ->
                            SessionCard(session = session, isOpen = true)
                        }
                    }

                    // History Section
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "입출차 기록",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        DateSelector(
                            selected = uiState.selectedDate,
                            onSelect = { date ->
                                viewModel.loadSessions(parkingLotId, date)
                            }
                        )
                    }

                    // History List
                    if (uiState.history.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "조회된 기록이 없습니다",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        items(uiState.history, key = { it.id }) { session ->
                            SessionCard(session = session, isOpen = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: Session,
    isOpen: Boolean
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
            // Vehicle Type Icon
            session.vehicleCategory?.let { category ->
                VehicleTypeIcon(
                    category = category,
                    size = 48.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            // Session Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = session.plateNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Status Badge
                    StatusBadge(
                        isEntry = session.status == SessionStatus.OPEN,
                        label = if (session.status == SessionStatus.OPEN) "입차중" else "출차완료"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                session.vehicleLabel?.let { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Text(
                    text = "입차: ${formatDateTime(session.enteredAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                session.exitedAt?.let { exitTime ->
                    Text(
                        text = "출차: ${formatDateTime(exitTime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                // Parking Duration
                if (isOpen) {
                    val duration = calculateDuration(session.enteredAt)
                    Text(
                        text = "주차 시간: $duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    session.exitedAt?.let { exitTime ->
                        val duration = calculateDuration(session.enteredAt, exitTime)
                        Text(
                            text = "주차 시간: $duration",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelector(
    selected: String?,
    onSelect: (String) -> Unit
) {
    var input by remember { mutableStateOf(selected ?: "") }

    LaunchedEffect(selected) {
        input = selected ?: ""
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            placeholder = { Text("YYYY-MM-DD", color = TextSecondary) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true
        )

        Button(
            onClick = { onSelect(input) },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ),
            enabled = input.isNotBlank()
        ) {
            Text(
                text = "조회",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Helper functions
private fun formatDateTime(dateTime: String): String {
    // Format: "2024-01-15T14:30:00" -> "01/15 14:30"
    return try {
        val parts = dateTime.split("T")
        if (parts.size == 2) {
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")
            if (dateParts.size >= 3 && timeParts.size >= 2) {
                "${dateParts[1]}/${dateParts[2]} ${timeParts[0]}:${timeParts[1]}"
            } else {
                dateTime
            }
        } else {
            dateTime
        }
    } catch (e: Exception) {
        dateTime
    }
}

private fun calculateDuration(enteredAt: String, exitedAt: String? = null): String {
    // Simplified duration calculation
    // In a real app, you'd use proper date/time parsing
    return if (exitedAt != null) {
        "2시간 30분" // Placeholder
    } else {
        "1시간 15분" // Placeholder for ongoing
    }
}
