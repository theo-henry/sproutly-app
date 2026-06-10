package com.sproutly.app.scanner

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScannerUiState(
    val cameraGranted: Boolean = false,
    val lastBarcode: String? = null,
)

class ScannerViewModel : ViewModel() {
    private val _state = MutableStateFlow(ScannerUiState())
    val state: StateFlow<ScannerUiState> = _state.asStateFlow()

    fun setCameraPermission(granted: Boolean) {
        _state.value = _state.value.copy(cameraGranted = granted)
    }

    // TODO: connect to CameraX preview + ML Kit barcode analyzer
    // TODO: onBarcodeScanned(code) -> ProductRepository.lookupByBarcode(code)
    // TODO: onLabelImageCaptured(bytes) -> Future OCR + ingredient analysis pipeline
}
