package com.restaurante.mesero.data.repository

import com.restaurante.mesero.data.local.dao.CategoriaDao
import com.restaurante.mesero.data.local.dao.ProductoDao
import com.restaurante.mesero.data.local.entity.CategoriaEntity
import com.restaurante.mesero.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

class MenuRepository(
    private val categoriaDao: CategoriaDao,
    private val productoDao: ProductoDao
) {

    fun observarCategorias(): Flow<List<CategoriaEntity>> = categoriaDao.observarCategorias()

    fun observarProductos(): Flow<List<ProductoEntity>> = productoDao.observarProductos()

    fun observarProductosPorCategoria(categoriaId: Long): Flow<List<ProductoEntity>> =
        productoDao.observarPorCategoria(categoriaId)

    fun buscarProductos(query: String): Flow<List<ProductoEntity>> =
        productoDao.buscarProductos(query)

    fun observarFavoritos(): Flow<List<ProductoEntity>> = productoDao.observarFavoritos()

    suspend fun agregarCategoria(nombre: String, icono: String = "Restaurant"): Long {
        val orden = categoriaDao.obtenerCategorias().size + 1
        return categoriaDao.insertar(CategoriaEntity(nombre = nombre, orden = orden, icono = icono))
    }

    suspend fun actualizarCategoria(categoria: CategoriaEntity) = categoriaDao.actualizar(categoria)

    suspend fun eliminarCategoria(categoria: CategoriaEntity) = categoriaDao.eliminar(categoria)

    suspend fun agregarProducto(producto: ProductoEntity): Long = productoDao.insertar(producto)

    suspend fun actualizarProducto(producto: ProductoEntity) = productoDao.actualizar(producto)

    suspend fun eliminarProducto(producto: ProductoEntity) = productoDao.eliminar(producto)

    suspend fun alternarFavorito(producto: ProductoEntity) {
        productoDao.actualizar(producto.copy(esFavorito = !producto.esFavorito))
    }

    suspend fun obtenerProducto(id: Long): ProductoEntity? = productoDao.obtenerPorId(id)

    suspend fun masVendidos(limite: Int = 5): List<ProductoEntity> = productoDao.masVendidos(limite)
}
