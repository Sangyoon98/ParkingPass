package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.domain.model.MemberRole
import com.sangyoon.parkingpass.domain.model.MemberStatus
import com.sangyoon.parkingpass.domain.model.ParkingLotMember
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotMemberScreen(
    viewModel: ParkingLotMemberViewModel,
    parkingLotId: Long,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(parkingLotId) {
        viewModel.setParkingLotId(parkingLotId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("멤버 관리") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
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
                value = uiState.inviteEmail,
                onValueChange = {
                    viewModel.updateInviteEmail(it)
                    viewModel.clearMessage()
                },
                label = { Text("초대 이메일") },
                modifier = Modifier.fillMaxWidth()
            )
            RoleSelector(
                selected = uiState.inviteRole,
                onSelect = {
                    viewModel.updateInviteRole(it)
                    viewModel.clearMessage()
                }
            )
            Button(
                onClick = { viewModel.inviteMember() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("초대 보내기")
            }
            uiState.successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            uiState.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.members) { member ->
                    MemberCard(
                        member = member,
                        onApprove = { viewModel.approveMember(member.userId) },
                        onReject = { viewModel.rejectMember(member.userId) },
                        onToggleRole = {
                            val newRole = if (member.role == MemberRole.ADMIN) MemberRole.MEMBER else MemberRole.ADMIN
                            viewModel.changeRole(member.userId, newRole)
                        },
                        onRemove = { viewModel.removeMember(member.userId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleSelector(
    selected: MemberRole,
    onSelect: (MemberRole) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        MemberRole.values().forEach { role ->
            val isSelected = selected == role
            Button(
                onClick = { onSelect(role) },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = role.name,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: ParkingLotMember,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onToggleRole: () -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(member.email, style = MaterialTheme.typography.titleMedium)
            member.name?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Text("역할: ${member.role}", style = MaterialTheme.typography.bodySmall)
            Text("상태: ${member.status}", style = MaterialTheme.typography.bodySmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                when (member.status) {
                    MemberStatus.PENDING -> {
                        Button(
                            onClick = onApprove,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("승인")
                        }
                        Button(
                            onClick = onReject,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("거절")
                        }
                    }
                    MemberStatus.APPROVED -> {
                        if (member.role != MemberRole.OWNER) {
                            Button(
                                onClick = onToggleRole,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("역할 변경")
                            }
                            Button(
                                onClick = onRemove,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("제거")
                            }
                        } else {
                            Text("소유자", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    MemberStatus.REJECTED -> {
                        Text("거절됨", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
