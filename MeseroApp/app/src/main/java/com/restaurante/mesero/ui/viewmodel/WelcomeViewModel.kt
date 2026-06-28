package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurante.mesero.data.repository.ConfiguracionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WelcomeUiState(
    val nombreRestaurante: String = "Mi Restaurante",
    val nombreMeseroRecordado: String? = null,
    val cargando: Boolean = true
)

class WelcomeViewModel(
    private val configuracionRepository: ConfiguracionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val config = configuracionRepository.obtener()
            _uiState.value = WelcomeUiState(
                nombreRestaurante = config.nombreRestaurante,
                nombreMeseroRecordado = config.nombreMeseroRecordado,
                cargando = false
            )
        }
    }

    fun guardarNombreMesero(nombre: String) {
        viewModelScope.launch {
            configuracionRepository.guardarNombreMesero(nombre)
        }
    }
}
