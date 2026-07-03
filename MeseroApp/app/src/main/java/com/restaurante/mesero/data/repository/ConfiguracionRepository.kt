package com.restaurante.mesero.data.repository

import com.restaurante.mesero.data.local.dao.ConfiguracionDao
import com.restaurante.mesero.data.local.entity.ConfiguracionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ConfiguracionRepository(private val configuracionDao: ConfiguracionDao) {

    fun observar(): Flow<ConfiguracionEntity> =
        configuracionDao.observar().map { it ?: ConfiguracionEntity() }

    suspend fun obtener(): ConfiguracionEntity =
        configuracionDao.obtener() ?: ConfiguracionEntity()

    suspend fun actualizar(config: ConfiguracionEntity) = configuracionDao.guardar(config)

    suspend fun guardarNombreMesero(nombre: String) {
        val actual = obtener()
        configuracionDao.guardar(actual.copy(nombreMeseroRecordado = nombre))
    }
}
