package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurante.mesero.data.local.entity.ConfiguracionEntity
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.local.entity.PedidoEntity
import com.restaurante.mesero.data.repository.ConfiguracionRepository
import com.restaurante.mesero.data.repository.MesaRepository
import com.restaurante.mesero.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TipoPropina { NINGUNA, P5, P10, P15, PERSONALIZADA }

data class BillUiState(
    val pedido: PedidoEntity? = null,
    val items: List<ItemPedidoEntity> = emptyList(),
    val mesa: MesaEntity? = null,
    val config: ConfiguracionEntity = ConfiguracionEntity(),
    val tipoPropina: TipoPropina = TipoPropina.NINGUNA,
    val propinaPersonalizada: Double = 0.0,
    val numeroPersonas: Int = 1,
    val cerrado: Boolean = false
) {
    val subtotal: Double get() = items.sumOf { it.subtotal }
    val impuesto: Double get() = subtotal * (config.porcentajeImpuesto / 100.0)
    val propina: Double
        get() = when (tipoPropina) {
            TipoPropina.NINGUNA -> 0.0
            TipoPropina.P5 -> subtotal * 0.05
            TipoPropina.P10 -> subtotal * 0.10
            TipoPropina.P15 -> subtotal * 0.15
            TipoPropina.PERSONALIZADA -> propinaPersonalizada
        }
    val total: Double get() = subtotal + impuesto + propina
    val totalPorPersona: Double get() = if (numeroPersonas > 0) total / numeroPersonas else total
}

class BillViewModel(
    private val pedidoRepository: PedidoRepository,
    private val mesaRepository: MesaRepository,
    private val configuracionRepository: ConfiguracionRepository
) : ViewModel() {

    private var pedidoId: Long = -1L
    private val _tipoPropina = MutableStateFlow(TipoPropina.NINGUNA)
    private val _propinaPersonalizada = MutableStateFlow(0.0)
    private val _numeroPersonas = MutableStateFlow(1)
    private val _cerrado = MutableStateFlow(false)

    private lateinit var uiStateInternal: StateFlow<BillUiState>
    val uiState: StateFlow<BillUiState> get() = uiStateInternal

    fun inicializar(pedidoId: Long) {
        if (this.pedidoId == pedidoId) return
        this.pedidoId = pedidoId

        val pedidoFlow = pedidoRepository.observarPedido(pedidoId)
        val itemsFlow = pedidoRepository.observarItems(pedidoId)
        val mesaFlow = pedidoFlow.distinctUntilChanged().flatMapLatest { pedido ->
            if (pedido == null) flowOf<MesaEntity?>(null) else mesaRepository.observarMesa(pedido.mesaId)
        }

        data class Extras(
            val tipo: TipoPropina,
            val personalizada: Double,
            val personas: Int,
            val cerrado: Boolean
        )

        val extrasFlow = combine(
            _tipoPropina, _propinaPersonalizada, _numeroPersonas, _cerrado
        ) { tipo, personalizada, personas, cerrado ->
            Extras(tipo, personalizada, personas, cerrado)
        }

        uiStateInternal = combine(
            pedidoFlow, itemsFlow, mesaFlow, configuracionRepository.observar(), extrasFlow
        ) { pedido, items, mesa, config, extras ->
            BillUiState(
                pedido = pedido,
                items = items,
                mesa = mesa,
                config = config,
                tipoPropina = extras.tipo,
                propinaPersonalizada = extras.personalizada,
                numeroPersonas = extras.personas,
                cerrado = extras.cerrado
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BillUiState())
    }

    fun seleccionarPropina(tipo: TipoPropina) {
        _tipoPropina.value = tipo
    }

    fun establecerPropinaPersonalizada(monto: Double) {
        _propinaPersonalizada.value = monto
        _tipoPropina.value = TipoPropina.PERSONALIZADA
    }

    fun establecerNumeroPersonas(numero: Int) {
        _numeroPersonas.value = numero.coerceAtLeast(1)
    }

    /** Cierra el pedido, libera la mesa y marca el flujo como completado. */
    fun cerrarCuentaYLiberarMesa() {
        val estado = uiState.value
        val pedido = estado.pedido ?: return
        val mesa = estado.mesa
        viewModelScope.launch {
            pedidoRepository.aplicarImpuestoYPropina(
                pedido.id,
                estado.config.porcentajeImpuesto,
                estado.propina
            )
            pedidoRepository.cerrarPedido(pedido.id)
            if (mesa != null) {
                mesaRepository.liberarMesa(mesa)
            }
            _cerrado.value = true
        }
    }
}
