package com.restaurante.mesero.data.local.dao

import androidx.room.*
import com.restaurante.mesero.data.local.entity.MesaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MesaDao {

    @Query("SELECT * FROM mesas ORDER BY orden ASC, numero ASC")
    fun observarMesas(): Flow<List<MesaEntity>>

    @Query("SELECT * FROM mesas WHERE id = :mesaId")
    suspend fun obtenerPorId(mesaId: Long): MesaEntity?

    @Query("SELECT * FROM mesas WHERE id = :mesaId")
    fun observarPorId(mesaId: Long): Flow<MesaEntity?>

    @Query("SELECT COALESCE(MAX(numero), 0) FROM mesas")
    suspend fun obtenerMaxNumero(): Int

    @Query("SELECT COALESCE(MAX(orden), 0) FROM mesas")
    suspend fun obtenerMaxOrden(): Int

    @Insert
    suspend fun insertar(mesa: MesaEntity): Long

    @Update
    suspend fun actualizar(mesa: MesaEntity)

    @Delete
    suspend fun eliminar(mesa: MesaEntity)

    @Query("DELETE FROM mesas WHERE id = :mesaId")
    suspend fun eliminarPorId(mesaId: Long)

    @Query("SELECT COUNT(*) FROM mesas")
    suspend fun contarMesas(): Int
}
