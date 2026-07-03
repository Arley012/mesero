package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurante.mesero.data.local.entity.EstadoMesa
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.repository.MesaRepository
import com.restaurante.mesero.data.repository.PedidoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FiltroMesa { TODAS, LIBRES, OCUPADAS }

data class TablesUiState(
    val mesas: List<MesaEntity> = emptyList(),
    val totalesPorMesa: Map<Long, Double> = emptyMap(),
    val filtro: FiltroMesa = FiltroMesa.TODAS,
    val tickRelojMillis: Long = System.currentTimeMillis() // fuerza recomposición del tiempo transcurrido
)

class TablesViewModel(
    private val mesaRepository: MesaRepository,
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroMesa.TODAS)
    private val _tick = MutableStateFlow(System.currentTimeMillis())

    val uiState: StateFlow<TablesUiState> = combine(
        mesaRepository.observarMesas(),
        pedidoRepository.observarTodosLosPedidosAbiertos(),
        _filtro,
        _tick
    ) { mesas, pedidosAbiertos, filtro, tick ->
        val totales = pedidosAbiertos.associate { it.mesaId to it.total }
        val filtradas = when (filtro) {
            FiltroMesa.TODAS -> mesas
            FiltroMesa.LIBRES -> mesas.filter { it.estado == EstadoMesa.LIBRE }
            FiltroMesa.OCUPADAS -> mesas.filter { it.estado != EstadoMesa.LIBRE }
        }
        TablesUiState(mesas = filtradas, totalesPorMesa = totales, filtro = filtro, tickRelojMillis = tick)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TablesUiState())

    init {
        // Refresca cada 30s para actualizar "tiempo transcurrido" en las tarjetas
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                _tick.value = System.currentTimeMillis()
            }
        }
    }

    fun cambiarFiltro(filtro: FiltroMesa) {
        _filtro.value = filtro
    }

    fun agregarMesa(capacidad: Int = 4) {
        viewModelScope.launch {
            mesaRepository.agregarMesa(capacidad = capacidad)
        }
    }

    fun eliminarMesa(mesa: MesaEntity) {
        viewModelScope.launch {
            mesaRepository.eliminarMesa(mesa)
        }
    }

    fun marcarEsperandoCuenta(mesa: MesaEntity) {
        viewModelScope.launch {
            mesaRepository.actualizarEstado(mesa, EstadoMesa.ESPERANDO_CUENTA)
        }
    }
}

