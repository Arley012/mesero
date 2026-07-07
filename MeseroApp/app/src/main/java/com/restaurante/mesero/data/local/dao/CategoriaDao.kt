package com.restaurante.mesero.data.local.dao

import androidx.room.*
import com.restaurante.mesero.data.local.entity.CategoriaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Query("SELECT * FROM categorias ORDER BY orden ASC")
    fun observarCategorias(): Flow<List<CategoriaEntity>>

    @Query("SELECT * FROM categorias ORDER BY orden ASC")
    suspend fun obtenerCategorias(): List<CategoriaEntity>

    @Insert
    suspend fun insertar(categoria: CategoriaEntity): Long

    @Insert
    suspend fun insertarTodas(categorias: List<CategoriaEntity>)

    @Update
    suspend fun actualizar(categoria: CategoriaEntity)

    @Delete
    suspend fun eliminar(categoria: CategoriaEntity)

    @Query("SELECT COUNT(*) FROM categorias")
    suspend fun contar(): Int
}
