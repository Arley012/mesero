package com.restaurante.mesero.data.local.dao

import androidx.room.*
import com.restaurante.mesero.data.local.entity.ConfiguracionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {

    @Query("SELECT * FROM configuracion WHERE id = 1")
    fun observar(): Flow<ConfiguracionEntity?>

    @Query("SELECT * FROM configuracion WHERE id = 1")
    suspend fun obtener(): ConfiguracionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(config: ConfiguracionEntity)
}
