package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurante.mesero.data.local.entity.CategoriaEntity
import com.restaurante.mesero.data.local.entity.EstadoMesa
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.local.entity.PedidoEntity
import com.restaurante.mesero.data.local.entity.ProductoEntity
import com.restaurante.mesero.data.repository.ConfiguracionRepository
import com.restaurante.mesero.data.repository.MenuRepository
import com.restaurante.mesero.data.repository.MesaRepository
import com.restaurante.mesero.data.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

data class OrderUiState(
    val mesa: MesaEntity? = null,
    val pedido: PedidoEntity? = null,
    val items: List<ItemPedidoEntity> = emptyList(),
    val categorias: List<CategoriaEntity> = emptyList(),
    val productosFiltrados: List<ProductoEntity> = emptyList(),
    val categoriaSeleccionadaId: Long? = null,
    val textoBusqueda: String = "",
    val cargando: Boolean = true,
    val moneda: String = "Bs"
) {
    val subtotal: Double get() = items.sumOf { it.subtotal }
}

/** Observaciones rápidas predefinidas, reutilizables en toda la app */
val OBSERVACIONES_RAPIDAS = listOf(
    "Sin cebolla", "Sin picante", "Poco aceite", "Extra queso", "Bien cocido", "Término medio"
)

class OrderViewModel(
    private val mesaRepository: MesaRepository,
    private val menuRepository: MenuRepository,
    private val pedidoRepository: PedidoRepository,
    private val configuracionRepository: ConfiguracionRepository
) : ViewModel() {

    private var mesaId: Long = -1L
    private var nombreMeseroActual: String = ""

    private val _categoriaSeleccionada = MutableStateFlow<Long?>(null)
    private val _textoBusqueda = MutableStateFlow("")
    private val _pedidoId = MutableStateFlow<Long?>(null)
    private val _cargando = MutableStateFlow(true)

    private val _uiState = MutableStateFlow(OrderUiState())
    val uiState: StateFlow<OrderUiState> = _uiState

    fun inicializar(mesaId: Long, nombreMesero: String) {
        if (this.mesaId == mesaId) return // ya inicializado
        this.mesaId = mesaId
        this.nombreMeseroActual = nombreMesero

        viewModelScope.launch {
            val mesa = mesaRepository.obtenerMesa(mesaId) ?: return@launch
            val pedido = pedidoRepository.obtenerOCrearPedidoAbierto(mesa, nombreMesero)
            if (mesa.estado == EstadoMesa.LIBRE || mesa.estado == EstadoMesa.ESPERANDO_PEDIDO) {
                mesaRepository.abrirMesa(mesa, nombreMesero)
            }
            _pedidoId.value = pedido.id
            _cargando.value = false
        }

        val productosFlow = combine(_categoriaSeleccionada, _textoBusqueda) { catId, texto -> catId to texto }
            .flatMapLatest { (catId, texto) ->
                when {
                    texto.isNotBlank() -> menuRepository.buscarProductos(texto)
                    catId != null -> menuRepository.observarProductosPorCategoria(catId)
                    else -> menuRepository.observarProductos()
                }
            }

        val itemsFlow = _pedidoId.flatMapLatest { id ->
            if (id == null) {
                kotlinx.coroutines.flow.flowOf<List<ItemPedidoEntity>>(emptyList())
            } else {
                pedidoRepository.observarItems(id)
            }
        }

        val pedidoFlow = _pedidoId.flatMapLatest { id ->
            if (id == null) {
                kotlinx.coroutines.flow.flowOf<PedidoEntity?>(null)
            } else {
                pedidoRepository.observarPedido(id)
            }
        }

        // Combina los datos "base" del pedido (mesa, pedido, items, categorías, productos)
        data class BaseDatos(
            val mesa: MesaEntity?,
            val pedido: PedidoEntity?,
            val items: List<ItemPedidoEntity>,
            val categorias: List<CategoriaEntity>,
            val productos: List<ProductoEntity>
        )

        val baseFlow = combine(
            mesaRepository.observarMesa(mesaId),
            pedidoFlow,
            itemsFlow,
            menuRepository.observarCategorias(),
            productosFlow
        ) { mesa, pedido, items, categorias, productos ->
            BaseDatos(mesa, pedido, items, categorias, productos)
        }

        // Combina los datos de "filtros/estado de UI"
        data class FiltrosUi(
            val categoriaId: Long?,
            val texto: String,
            val cargando: Boolean,
            val moneda: String
        )

        val filtrosFlow = combine(
            _categoriaSeleccionada,
            _textoBusqueda,
            _cargando,
            configuracionRepository.observar()
        ) { catId, texto, loading, config ->
            FiltrosUi(catId, texto, loading, config.moneda)
        }

        viewModelScope.launch {
            combine(baseFlow, filtrosFlow) { base, filtros ->
                OrderUiState(
                    mesa = base.mesa,
                    pedido = base.pedido,
                    items = base.items,
                    categorias = base.categorias,
                    productosFiltrados = base.productos,
                    categoriaSeleccionadaId = filtros.categoriaId,
                    textoBusqueda = filtros.texto,
                    cargando = filtros.cargando,
                    moneda = filtros.moneda
                )
            }.collect { estado ->
                _uiState.value = estado
            }
        }
    }

    fun seleccionarCategoria(categoriaId: Long?) {
        _categoriaSeleccionada.value = categoriaId
        _textoBusqueda.value = ""
    }

    fun buscar(texto: String) {
        _textoBusqueda.value = texto
        if (texto.isNotBlank()) _categoriaSeleccionada.value = null
    }

    fun agregarProducto(producto: ProductoEntity, cantidad: Int = 1, observaciones: String? = null) {
        val pedido = uiState.value.pedido ?: return
        viewModelScope.launch {
            pedidoRepository.agregarProducto(pedido, producto, cantidad, observaciones)
            val mesa = uiState.value.mesa
            if (mesa != null && mesa.estado == EstadoMesa.ESPERANDO_PEDIDO) {
                mesaRepository.actualizarEstado(mesa, EstadoMesa.OCUPADA)
            }
        }
    }

    fun cambiarCantidad(item: ItemPedidoEntity, nuevaCantidad: Int) {
        viewModelScope.launch {
            pedidoRepository.cambiarCantidad(item, nuevaCantidad)
        }
    }

    fun actualizarObservaciones(item: ItemPedidoEntity, observaciones: String) {
        viewModelScope.launch {
            pedidoRepository.actualizarObservaciones(item, observaciones)
        }
    }

    fun eliminarItem(item: ItemPedidoEntity) {
        viewModelScope.launch {
            pedidoRepository.eliminarItem(item)
        }
    }

    fun duplicarItem(item: ItemPedidoEntity) {
        viewModelScope.launch {
            pedidoRepository.duplicarItem(item)
        }
    }

    fun marcarEsperandoCuenta() {
        val mesa = uiState.value.mesa ?: return
        viewModelScope.launch {
            mesaRepository.actualizarEstado(mesa, EstadoMesa.ESPERANDO_CUENTA)
        }
    }
}
