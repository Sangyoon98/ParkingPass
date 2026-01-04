package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.sangyoon.parkingpass.auth.KakaoLoginResult
import com.sangyoon.parkingpass.auth.rememberKakaoLoginLauncher
import com.sangyoon.parkingpass.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val kakaoLauncher = rememberKakaoLoginLauncher()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Parking Pass", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = viewModel::updateEmail,
            label = { Text("이메일") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = viewModel::updatePassword,
            label = { Text("비밀번호") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::updateName,
            label = { Text("이름 (회원가입)") },
            modifier = Modifier.fillMaxWidth()
        )

        uiState.error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.login() },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp))
            } else {
                Text("로그인")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { viewModel.register() },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("회원가입")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                if (uiState.isLoading) return@Button
                viewModel.beginKakaoLogin()
                coroutineScope.launch {
                    val result = runCatching { kakaoLauncher.launch() }
                        .getOrElse {
                            viewModel.onKakaoLoginError(it.message ?: "카카오 로그인에 실패했습니다.")
                            return@launch
                        }
                    when (result) {
                        is KakaoLoginResult.Success -> viewModel.onKakaoAccessToken(result.accessToken)
                        KakaoLoginResult.Cancelled -> viewModel.onKakaoLoginCancelled()
                        is KakaoLoginResult.Failure -> viewModel.onKakaoLoginError(result.message)
                    }
                }
            },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("카카오 로그인")
        }
    }
}
