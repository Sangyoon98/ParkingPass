package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.ParkingLot
import com.sangyoon.parkingpass.presentation.state.ParkingLotFilter
import com.sangyoon.parkingpass.presentation.ui.components.FilterChipRow
import com.sangyoon.parkingpass.presentation.ui.components.OccupancyProgressBar
import com.sangyoon.parkingpass.presentation.ui.components.RoundedSearchBar
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotListScreen(
    viewModel: ParkingLotViewModel,
    onParkingLotClick: (Long) -> Unit,
    onCreateClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("주차장") },
                actions = {
                    IconButton(onClick = { /* TODO: 알림 */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "알림"
                        )
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
                    contentDescription = "주차장 생성"
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadParkingLots() }) {
                            Text("다시 시도")
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // Search Bar
                    RoundedSearchBar(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = "주차장 검색",
                        onSearch = onSearchClick
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filter Chips
                    FilterChipRow(
                        filters = listOf(ParkingLotFilter.ALL, ParkingLotFilter.OPERATING, ParkingLotFilter.FULL),
                        selectedFilter = uiState.selectedFilter,
                        onFilterSelected = { filter ->
                            viewModel.selectFilter(filter)
                        },
                        filterLabel = { filter ->
                            when (filter) {
                                ParkingLotFilter.ALL -> "전체"
                                ParkingLotFilter.OPERATING -> "운영중"
                                ParkingLotFilter.FULL -> "만차"
                            }
                        }
                    )

                    // Parking Lot List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (uiState.myParkingLots.isNotEmpty()) {
                            item {
                                Text(
                                    text = "내 주차장",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(
                                items = filterParkingLots(
                                    uiState.myParkingLots,
                                    uiState.selectedFilter
                                ),
                                key = { "my-${it.id}" }
                            ) { lot ->
                                ParkingLotCard(
                                    parkingLot = lot,
                                    onClick = { onParkingLotClick(lot.id) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        item {
                            Text(
                                text = "전체 주차장",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        items(
                            items = filterParkingLots(
                                uiState.publicParkingLots,
                                uiState.selectedFilter
                            ),
                            key = { it.id }
                        ) { parkingLot ->
                            ParkingLotCard(
                                parkingLot = parkingLot,
                                onClick = { onParkingLotClick(parkingLot.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParkingLotCard(
    parkingLot: ParkingLot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Name + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = parkingLot.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Status Badge (운영중/만차)
                if (!parkingLot.isPublic) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "비공개",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Location
            Text(
                text = parkingLot.location,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Occupancy Progress Bar
            OccupancyProgressBar(
                currentOccupancy = 0,  // Placeholder
                totalCapacity = 100, // Placeholder - capacity not in domain model yet
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun filterParkingLots(
    parkingLots: List<ParkingLot>,
    filter: ParkingLotFilter
): List<ParkingLot> {
    return when (filter) {
        ParkingLotFilter.ALL -> parkingLots
        ParkingLotFilter.OPERATING -> parkingLots // TODO: 운영중 필터 로직
        ParkingLotFilter.FULL -> parkingLots // TODO: 만차 필터 로직
    }
}
