package com.restaurante.mesero.data.repository

import com.restaurante.mesero.data.local.dao.ItemPedidoDao
import com.restaurante.mesero.data.local.dao.PedidoDao
import com.restaurante.mesero.data.local.dao.ProductoDao
import com.restaurante.mesero.data.local.entity.EstadoPedido
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.local.entity.PedidoEntity
import com.restaurante.mesero.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

class PedidoRepository(
    private val pedidoDao: PedidoDao,
    private val itemPedidoDao: ItemPedidoDao,
    private val productoDao: ProductoDao
) {

    suspend fun obtenerOCrearPedidoAbierto(mesa: MesaEntity, nombreMesero: String): PedidoEntity {
        val existente = pedidoDao.obtenerPedidoAbiertoDeMesa(mesa.id)
        if (existente != null) return existente

        val nuevo = PedidoEntity(
            mesaId = mesa.id,
            numeroMesa = mesa.numero,
            nombreMesero = nombreMesero,
            fechaApertura = System.currentTimeMillis()
        )
        val id = pedidoDao.insertar(nuevo)
        return nuevo.copy(id = id)
    }

    fun observarPedido(pedidoId: Long): Flow<PedidoEntity?> = pedidoDao.observarPorId(pedidoId)

    /** Útil para que la pantalla de mesas muestre el total en curso de cada mesa sin N consultas. */
    fun observarTodosLosPedidosAbiertos(): Flow<List<PedidoEntity>> =
        pedidoDao.observarTodosLosPedidosAbiertos()

    fun observarItems(pedidoId: Long): Flow<List<ItemPedidoEntity>> =
        itemPedidoDao.observarItemsDePedido(pedidoId)

    suspend fun agregarProducto(
        pedido: PedidoEntity,
        producto: ProductoEntity,
        cantidad: Int = 1,
        observaciones: String? = null
    ) {
        // Si no hay observaciones, intenta agrupar con un item existente del mismo producto
        if (observaciones.isNullOrBlank()) {
            val existente = itemPedidoDao.obtenerItemSinObservacion(pedido.id, producto.id)
            if (existente != null) {
                itemPedidoDao.actualizar(existente.copy(cantidad = existente.cantidad + cantidad))
                recalcularTotales(pedido.id)
                return
            }
        }
        itemPedidoDao.insertar(
            ItemPedidoEntity(
                pedidoId = pedido.id,
                productoId = producto.id,
                nombreProducto = producto.nombre,
                precioUnitario = producto.precio,
                cantidad = cantidad,
                observaciones = observaciones?.takeIf { it.isNotBlank() }
            )
        )
        recalcularTotales(pedido.id)
    }

    suspend fun cambiarCantidad(item: ItemPedidoEntity, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            itemPedidoDao.eliminar(item)
        } else {
            itemPedidoDao.actualizar(item.copy(cantidad = nuevaCantidad))
        }
        recalcularTotales(item.pedidoId)
    }

    suspend fun actualizarObservaciones(item: ItemPedidoEntity, observaciones: String?) {
        itemPedidoDao.actualizar(item.copy(observaciones = observaciones?.takeIf { it.isNotBlank() }))
    }

    suspend fun eliminarItem(item: ItemPedidoEntity) {
        itemPedidoDao.eliminar(item)
        recalcularTotales(item.pedidoId)
    }

    suspend fun duplicarItem(item: ItemPedidoEntity) {
        itemPedidoDao.insertar(item.copy(id = 0))
        recalcularTotales(item.pedidoId)
    }

    /** Duplica TODO el pedido de una mesa cerrada/abierta hacia un pedido nuevo (otra mesa o repetir consumo). */
    suspend fun duplicarPedidoHaciaMesa(pedidoOrigenId: Long, mesaDestino: MesaEntity, nombreMesero: String): Long {
        val origenItems = itemPedidoDao.obtenerItemsDePedido(pedidoOrigenId)
        val nuevoPedido = obtenerOCrearPedidoAbierto(mesaDestino, nombreMesero)
        origenItems.forEach { item ->
            itemPedidoDao.insertar(item.copy(id = 0, pedidoId = nuevoPedido.id))
        }
        recalcularTotales(nuevoPedido.id)
        return nuevoPedido.id
    }

    private suspend fun recalcularTotales(pedidoId: Long, porcentajeImpuesto: Double = 0.0) {
        val pedido = pedidoDao.obtenerPorId(pedidoId) ?: return
        val items = itemPedidoDao.obtenerItemsDePedido(pedidoId)
        val subtotal = items.sumOf { it.subtotal }
        val impuesto = subtotal * (porcentajeImpuesto / 100.0)
        val total = subtotal + impuesto + pedido.propina
        pedidoDao.actualizar(pedido.copy(subtotal = subtotal, impuesto = impuesto, total = total))
    }

    suspend fun aplicarImpuestoYPropina(pedidoId: Long, porcentajeImpuesto: Double, propina: Double) {
        val pedido = pedidoDao.obtenerPorId(pedidoId) ?: return
        val items = itemPedidoDao.obtenerItemsDePedido(pedidoId)
        val subtotal = items.sumOf { it.subtotal }
        val impuesto = subtotal * (porcentajeImpuesto / 100.0)
        val total = subtotal + impuesto + propina
        pedidoDao.actualizar(
            pedido.copy(subtotal = subtotal, impuesto = impuesto, propina = propina, total = total)
        )
    }

    /** Cierra el pedido: marca CERRADO, guarda fecha de cierre y actualiza contador de "más vendido". */
    suspend fun cerrarPedido(pedidoId: Long) {
        val pedido = pedidoDao.obtenerPorId(pedidoId) ?: return
        val items = itemPedidoDao.obtenerItemsDePedido(pedidoId)
        items.forEach { item ->
            productoDao.incrementarVendidos(item.productoId, item.cantidad)
        }
        pedidoDao.actualizar(
            pedido.copy(estado = EstadoPedido.CERRADO, fechaCierre = System.currentTimeMillis())
        )
    }

    suspend fun cancelarPedido(pedidoId: Long) {
        val pedido = pedidoDao.obtenerPorId(pedidoId) ?: return
        pedidoDao.actualizar(pedido.copy(estado = EstadoPedido.CANCELADO, fechaCierre = System.currentTimeMillis()))
    }

    // ---------- Historial ----------

    fun observarHistorial(): Flow<List<PedidoEntity>> = pedidoDao.observarHistorial()

    fun observarHistorialPorRango(inicio: Long, fin: Long): Flow<List<PedidoEntity>> =
        pedidoDao.observarHistorialPorFecha(inicio, fin)

    suspend fun obtenerItemsDePedido(pedidoId: Long): List<ItemPedidoEntity> =
        itemPedidoDao.obtenerItemsDePedido(pedidoId)

    // ---------- Estadísticas ----------

    suspend fun estadisticasDelDia(inicio: Long, fin: Long): EstadisticasDia {
        val numPedidos = pedidoDao.contarPedidosCerrados(inicio, fin)
        val ventas = pedidoDao.sumaVentas(inicio, fin)
        val mesasAtendidas = pedidoDao.contarMesasAtendidas(inicio, fin)
        val promedio = pedidoDao.promedioVentaPorMesa(inicio, fin)
        val productoTop = itemPedidoDao.productoMasVendidoEntreFechas(inicio, fin)
        return EstadisticasDia(
            numeroPedidos = numPedidos,
            ventasTotales = ventas,
            mesasAtendidas = mesasAtendidas,
            promedioPorMesa = promedio,
            productoMasVendido = productoTop?.nombreProducto,
            cantidadProductoMasVendido = productoTop?.totalVendido ?: 0
        )
    }
}

data class EstadisticasDia(
    val numeroPedidos: Int,
    val ventasTotales: Double,
    val mesasAtendidas: Int,
    val promedioPorMesa: Double,
    val productoMasVendido: String?,
    val cantidadProductoMasVendido: Int
)
