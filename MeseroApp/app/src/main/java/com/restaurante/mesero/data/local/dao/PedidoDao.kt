package com.restaurante.mesero.data.local.dao

import androidx.room.*
import com.restaurante.mesero.data.local.entity.EstadoPedido
import com.restaurante.mesero.data.local.entity.PedidoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Query("SELECT * FROM pedidos WHERE mesaId = :mesaId AND estado = 'ABIERTO' LIMIT 1")
    suspend fun obtenerPedidoAbiertoDeMesa(mesaId: Long): PedidoEntity?

    @Query("SELECT * FROM pedidos WHERE estado = 'ABIERTO'")
    fun observarTodosLosPedidosAbiertos(): Flow<List<PedidoEntity>>

    @Query("SELECT * FROM pedidos WHERE mesaId = :mesaId AND estado = 'ABIERTO' LIMIT 1")
    fun observarPedidoAbiertoDeMesa(mesaId: Long): Flow<PedidoEntity?>

    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    suspend fun obtenerPorId(pedidoId: Long): PedidoEntity?

    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    fun observarPorId(pedidoId: Long): Flow<PedidoEntity?>

    @Insert
    suspend fun insertar(pedido: PedidoEntity): Long

    @Update
    suspend fun actualizar(pedido: PedidoEntity)

    @Delete
    suspend fun eliminar(pedido: PedidoEntity)

    // ---------- Historial ----------

    @Query("""
        SELECT * FROM pedidos 
        WHERE estado = 'CERRADO' 
        ORDER BY fechaCierre DESC
    """)
    fun observarHistorial(): Flow<List<PedidoEntity>>

    @Query("""
        SELECT * FROM pedidos 
        WHERE estado = 'CERRADO' 
        AND fechaCierre BETWEEN :inicio AND :fin
        ORDER BY fechaCierre DESC
    """)
    fun observarHistorialPorFecha(inicio: Long, fin: Long): Flow<List<PedidoEntity>>

    // ---------- Estadísticas del día ----------

    @Query("""
        SELECT COUNT(*) FROM pedidos 
        WHERE estado = 'CERRADO' AND fechaCierre BETWEEN :inicio AND :fin
    """)
    suspend fun contarPedidosCerrados(inicio: Long, fin: Long): Int

    @Query("""
        SELECT COALESCE(SUM(total), 0) FROM pedidos 
        WHERE estado = 'CERRADO' AND fechaCierre BETWEEN :inicio AND :fin
    """)
    suspend fun sumaVentas(inicio: Long, fin: Long): Double

    @Query("""
        SELECT COUNT(DISTINCT mesaId) FROM pedidos 
        WHERE estado = 'CERRADO' AND fechaCierre BETWEEN :inicio AND :fin
    """)
    suspend fun contarMesasAtendidas(inicio: Long, fin: Long): Int

    @Query("""
        SELECT COALESCE(AVG(total), 0) FROM pedidos 
        WHERE estado = 'CERRADO' AND fechaCierre BETWEEN :inicio AND :fin
    """)
    suspend fun promedioVentaPorMesa(inicio: Long, fin: Long): Double
}
