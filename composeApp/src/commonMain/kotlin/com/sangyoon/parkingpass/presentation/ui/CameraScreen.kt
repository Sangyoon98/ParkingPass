package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.runtime.Composable
import com.sangyoon.parkingpass.camera.CameraImage
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel

@Composable
expect fun CameraScreen(
    viewModel: PlateDetectionViewModel,
    parkingLotId: Long,
    onBack: () -> Unit,
    onImageCaptured: (CameraImage) -> Unit
)

