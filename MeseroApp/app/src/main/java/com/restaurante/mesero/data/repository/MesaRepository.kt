package com.restaurante.mesero.data.repository

import com.restaurante.mesero.data.local.dao.MesaDao
import com.restaurante.mesero.data.local.entity.EstadoMesa
import com.restaurante.mesero.data.local.entity.MesaEntity
import kotlinx.coroutines.flow.Flow

class MesaRepository(private val mesaDao: MesaDao) {

    fun observarMesas(): Flow<List<MesaEntity>> = mesaDao.observarMesas()

    fun observarMesa(mesaId: Long): Flow<MesaEntity?> = mesaDao.observarPorId(mesaId)

    suspend fun obtenerMesa(mesaId: Long): MesaEntity? = mesaDao.obtenerPorId(mesaId)

    suspend fun agregarMesa(numero: Int? = null, capacidad: Int = 4, nombre: String? = null): Long {
        val nuevoNumero = numero ?: (mesaDao.obtenerMaxNumero() + 1)
        val nuevoOrden = mesaDao.obtenerMaxOrden() + 1
        return mesaDao.insertar(
            MesaEntity(
                numero = nuevoNumero,
                nombre = nombre,
                capacidad = capacidad,
                orden = nuevoOrden
            )
        )
    }

    suspend fun eliminarMesa(mesa: MesaEntity) = mesaDao.eliminar(mesa)

    suspend fun actualizarEstado(mesa: MesaEntity, nuevoEstado: EstadoMesa) {
        val actualizada = when (nuevoEstado) {
            EstadoMesa.LIBRE -> mesa.copy(
                estado = EstadoMesa.LIBRE,
                horaApertura = null,
                meseroAsignado = null
            )
            EstadoMesa.ESPERANDO_PEDIDO -> mesa.copy(
                estado = EstadoMesa.ESPERANDO_PEDIDO,
                horaApertura = mesa.horaApertura ?: System.currentTimeMillis()
            )
            else -> mesa.copy(estado = nuevoEstado)
        }
        mesaDao.actualizar(actualizada)
    }

    suspend fun abrirMesa(mesa: MesaEntity, nombreMesero: String) {
        mesaDao.actualizar(
            mesa.copy(
                estado = EstadoMesa.ESPERANDO_PEDIDO,
                horaApertura = System.currentTimeMillis(),
                meseroAsignado = nombreMesero
            )
        )
    }

    suspend fun liberarMesa(mesa: MesaEntity) {
        mesaDao.actualizar(
            mesa.copy(
                estado = EstadoMesa.LIBRE,
                horaApertura = null,
                meseroAsignado = null
            )
        )
    }

    suspend fun actualizarMesa(mesa: MesaEntity) = mesaDao.actualizar(mesa)
}
