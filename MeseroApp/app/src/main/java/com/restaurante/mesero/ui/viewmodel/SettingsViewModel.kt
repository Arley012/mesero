package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurante.mesero.data.local.entity.ConfiguracionEntity
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.repository.ConfiguracionRepository
import com.restaurante.mesero.data.repository.MesaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val config: ConfiguracionEntity = ConfiguracionEntity(),
    val mesas: List<MesaEntity> = emptyList()
)

class SettingsViewModel(
    private val configuracionRepository: ConfiguracionRepository,
    private val mesaRepository: MesaRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        configuracionRepository.observar(),
        mesaRepository.observarMesas()
    ) { config, mesas ->
        SettingsUiState(config = config, mesas = mesas)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun actualizarNombreRestaurante(nombre: String) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(nombreRestaurante = nombre))
        }
    }

    fun actualizarLogo(uri: String?) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(logoUri = uri))
        }
    }

    fun actualizarMoneda(moneda: String) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(moneda = moneda))
        }
    }

    fun actualizarImpuesto(porcentaje: Double) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(porcentajeImpuesto = porcentaje))
        }
    }

    fun alternarModoOscuro(activado: Boolean) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(modoOscuro = activado))
        }
    }

    fun agregarMesa(capacidad: Int = 4) {
        viewModelScope.launch { mesaRepository.agregarMesa(capacidad = capacidad) }
    }

    fun eliminarMesa(mesa: MesaEntity) {
        viewModelScope.launch { mesaRepository.eliminarMesa(mesa) }
    }
}
