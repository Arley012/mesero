package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import com.restaurante.mesero.data.local.entity.PedidoEntity
import com.restaurante.mesero.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class HistoryUiState(
    val pedidos: List<PedidoEntity> = emptyList(),
    val fechaFiltro: Long? = null // null = todo el historial
)

class HistoryViewModel(
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    private val _fechaFiltro = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<HistoryUiState> = _fechaFiltro.flatMapLatest { fecha ->
        val pedidosFlow = if (fecha == null) {
            pedidoRepository.observarHistorial()
        } else {
            val (inicio, fin) = rangoDelDia(fecha)
            pedidoRepository.observarHistorialPorRango(inicio, fin)
        }
        pedidosFlow.map { pedidos -> HistoryUiState(pedidos = pedidos, fechaFiltro = fecha) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    fun filtrarPorFecha(fechaMillis: Long?) {
        _fechaFiltro.value = fechaMillis
    }

    fun limpiarFiltro() {
        _fechaFiltro.value = null
    }

    suspend fun obtenerItemsDePedido(pedidoId: Long): List<ItemPedidoEntity> =
        pedidoRepository.obtenerItemsDePedido(pedidoId)

    private fun rangoDelDia(fechaMillis: Long): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.timeInMillis = fechaMillis
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
