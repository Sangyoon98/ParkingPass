package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.sangyoon.parkingpass.domain.model.MemberRole
import com.sangyoon.parkingpass.domain.model.MemberStatus
import com.sangyoon.parkingpass.domain.model.ParkingLotMember
import com.sangyoon.parkingpass.presentation.ui.theme.PrimaryBlue
import com.sangyoon.parkingpass.presentation.ui.theme.StatusEntry
import com.sangyoon.parkingpass.presentation.ui.theme.StatusExit
import com.sangyoon.parkingpass.presentation.ui.theme.TextSecondary
import com.sangyoon.parkingpass.presentation.viewmodel.ParkingLotMemberViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingLotMemberScreen(
    viewModel: ParkingLotMemberViewModel,
    parkingLotId: Long,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showInviteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(parkingLotId) {
        viewModel.setParkingLotId(parkingLotId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "멤버 관리",
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
                        Button(onClick = { /* Retry */ }) {
                            Text("다시 시도")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hero Section
                    item {
                        HeroSection(onInviteClick = { showInviteDialog = true })
                    }

                    // Success/Error Messages
                    if (uiState.successMessage != null) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = StatusEntry.copy(alpha = 0.1f)
                                )
                            ) {
                                Text(
                                    text = uiState.successMessage ?: "",
                                    color = StatusEntry,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }

                    // Member List Header
                    item {
                        Text(
                            text = "멤버 목록 (${uiState.members.size}명)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Member Cards
                    items(uiState.members, key = { it.userId }) { member ->
                        MemberCard(
                            member = member,
                            onApprove = { viewModel.approveMember(member.userId) },
                            onReject = { viewModel.rejectMember(member.userId) },
                            onChangeRole = { newRole -> viewModel.changeRole(member.userId, newRole) },
                            onRemove = { viewModel.removeMember(member.userId) },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    // Bottom Spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Invite Dialog
    if (showInviteDialog) {
        InviteDialog(
            email = uiState.inviteEmail,
            onEmailChange = { viewModel.updateInviteEmail(it) },
            selectedRole = uiState.inviteRole,
            onRoleChange = { viewModel.updateInviteRole(it) },
            onConfirm = {
                viewModel.inviteMember()
                showInviteDialog = false
            },
            onDismiss = {
                showInviteDialog = false
                viewModel.clearMessage()
            }
        )
    }
}

@Composable
private fun HeroSection(onInviteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryBlue.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = PrimaryBlue
            )

            Text(
                text = "멤버를 초대하세요",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "이메일로 멤버를 초대하고\n주차장을 함께 관리하세요",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Button(
                onClick = onInviteClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "멤버 초대하기",
                    fontWeight = FontWeight.SemiBold
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
    onChangeRole: (MemberRole) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(PrimaryBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = PrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Member Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        member.name?.let { name ->
                            Text(
                                text = name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Role Badge
                        RoleBadge(role = member.role)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = member.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    // Status
                    if (member.status != MemberStatus.APPROVED) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (member.status) {
                                MemberStatus.PENDING -> "승인 대기중"
                                MemberStatus.REJECTED -> "거절됨"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = when (member.status) {
                                MemberStatus.PENDING -> StatusExit
                                MemberStatus.REJECTED -> MaterialTheme.colorScheme.error
                                else -> TextSecondary
                            }
                        )
                    }
                }

                // Action Menu
                if (member.role != MemberRole.OWNER) {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "메뉴",
                                tint = TextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            when (member.status) {
                                MemberStatus.PENDING -> {
                                    DropdownMenuItem(
                                        text = { Text("승인") },
                                        onClick = {
                                            onApprove()
                                            showMenu = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("거절") },
                                        onClick = {
                                            onReject()
                                            showMenu = false
                                        }
                                    )
                                }
                                MemberStatus.APPROVED -> {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (member.role == MemberRole.ADMIN) "일반 멤버로 변경"
                                                else "관리자로 변경"
                                            )
                                        },
                                        onClick = {
                                            val newRole = if (member.role == MemberRole.ADMIN) {
                                                MemberRole.MEMBER
                                            } else {
                                                MemberRole.ADMIN
                                            }
                                            onChangeRole(newRole)
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("멤버 제거", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            onRemove()
                                            showMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                }
                                MemberStatus.REJECTED -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleBadge(role: MemberRole) {
    val (text, color) = when (role) {
        MemberRole.OWNER -> "소유자" to PrimaryBlue
        MemberRole.ADMIN -> "관리자" to StatusEntry
        MemberRole.MEMBER -> "멤버" to TextSecondary
    }

    FilterChip(
        selected = false,
        onClick = { },
        enabled = false,
        label = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            disabledContainerColor = color.copy(alpha = 0.2f),
            disabledLabelColor = color
        ),
        border = null,
        modifier = Modifier.height(24.dp)
    )
}

@Composable
private fun InviteDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    selectedRole: MemberRole,
    onRoleChange: (MemberRole) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "멤버 초대",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("이메일") },
                    placeholder = { Text("user@example.com", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "역할",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(MemberRole.ADMIN, MemberRole.MEMBER).forEach { role ->
                            val isSelected = selectedRole == role
                            FilterChip(
                                selected = isSelected,
                                onClick = { onRoleChange(role) },
                                label = {
                                    Text(
                                        text = when (role) {
                                            MemberRole.ADMIN -> "관리자"
                                            MemberRole.MEMBER -> "멤버"
                                            else -> role.name
                                        },
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f),
                                    selectedLabelColor = PrimaryBlue
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("취소")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        enabled = email.isNotBlank()
                    ) {
                        Text(
                            text = "초대",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
