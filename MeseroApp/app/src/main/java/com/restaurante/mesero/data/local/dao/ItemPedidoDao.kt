package com.restaurante.mesero.data.local.dao

import androidx.room.*
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemPedidoDao {

    @Query("SELECT * FROM items_pedido WHERE pedidoId = :pedidoId ORDER BY id ASC")
    fun observarItemsDePedido(pedidoId: Long): Flow<List<ItemPedidoEntity>>

    @Query("SELECT * FROM items_pedido WHERE pedidoId = :pedidoId ORDER BY id ASC")
    suspend fun obtenerItemsDePedido(pedidoId: Long): List<ItemPedidoEntity>

    @Query("""
        SELECT * FROM items_pedido 
        WHERE pedidoId = :pedidoId AND productoId = :productoId AND observaciones IS NULL
        LIMIT 1
    """)
    suspend fun obtenerItemSinObservacion(pedidoId: Long, productoId: Long): ItemPedidoEntity?

    @Insert
    suspend fun insertar(item: ItemPedidoEntity): Long

    @Update
    suspend fun actualizar(item: ItemPedidoEntity)

    @Delete
    suspend fun eliminar(item: ItemPedidoEntity)

    @Query("DELETE FROM items_pedido WHERE id = :itemId")
    suspend fun eliminarPorId(itemId: Long)

    @Query("DELETE FROM items_pedido WHERE pedidoId = :pedidoId")
    suspend fun eliminarTodosDePedido(pedidoId: Long)

    @Query("""
        SELECT i.productoId, i.nombreProducto, SUM(i.cantidad) as totalVendido
        FROM items_pedido i
        INNER JOIN pedidos p ON p.id = i.pedidoId
        WHERE p.estado = 'CERRADO' AND p.fechaCierre BETWEEN :inicio AND :fin
        GROUP BY i.productoId
        ORDER BY totalVendido DESC
        LIMIT 1
    """)
    suspend fun productoMasVendidoEntreFechas(inicio: Long, fin: Long): ProductoVendidoResult?

    @Insert
    suspend fun insertarTodos(items: List<ItemPedidoEntity>)
}

data class ProductoVendidoResult(
    val productoId: Long,
    val nombreProducto: String,
    val totalVendido: Int
)
