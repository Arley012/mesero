package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurante.mesero.data.repository.EstadisticasDia
import com.restaurante.mesero.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class StatsViewModel(
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EstadisticasDia(0, 0.0, 0, 0.0, null, 0))
    val uiState: StateFlow<EstadisticasDia> = _uiState.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    init {
        cargarEstadisticasDeHoy()
    }

    fun cargarEstadisticasDeHoy() {
        viewModelScope.launch {
            _cargando.value = true
            val (inicio, fin) = rangoDeHoy()
            _uiState.value = pedidoRepository.estadisticasDelDia(inicio, fin)
            _cargando.value = false
        }
    }

    private fun rangoDeHoy(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val inicio = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val fin = cal.timeInMillis - 1
        return inicio to fin
    }
}
