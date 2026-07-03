package com.restaurante.mesero.data.local.dao

import androidx.room.*
import com.restaurante.mesero.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos ORDER BY orden ASC, nombre ASC")
    fun observarProductos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE categoriaId = :categoriaId ORDER BY orden ASC, nombre ASC")
    fun observarPorCategoria(categoriaId: Long): Flow<List<ProductoEntity>>

    @Query("""
        SELECT * FROM productos 
        WHERE nombre LIKE '%' || :query || '%' 
        ORDER BY nombre ASC
    """)
    fun buscarProductos(query: String): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE esFavorito = 1 ORDER BY nombre ASC")
    fun observarFavoritos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): ProductoEntity?

    @Insert
    suspend fun insertar(producto: ProductoEntity): Long

    @Insert
    suspend fun insertarTodos(productos: List<ProductoEntity>)

    @Update
    suspend fun actualizar(producto: ProductoEntity)

    @Delete
    suspend fun eliminar(producto: ProductoEntity)

    @Query("UPDATE productos SET vecesVendido = vecesVendido + :cantidad WHERE id = :productoId")
    suspend fun incrementarVendidos(productoId: Long, cantidad: Int)

    @Query("SELECT * FROM productos ORDER BY vecesVendido DESC LIMIT :limite")
    suspend fun masVendidos(limite: Int = 5): List<ProductoEntity>

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contar(): Int
}
