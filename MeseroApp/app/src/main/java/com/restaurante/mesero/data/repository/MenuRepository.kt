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

    /** Snapshot actual del menú completo, para exportarlo a un archivo. */
    suspend fun obtenerMenuCompleto(): Pair<List<CategoriaEntity>, List<ProductoEntity>> {
        return categoriaDao.obtenerCategorias() to productoDao.obtenerProductos()
    }

    /**
     * Importa un menú (categorías + productos) fusionándolo con el existente:
     * - Las categorías se relacionan por nombre (sin distinguir mayúsculas). Si ya
     *   existe una con ese nombre, se reutiliza en vez de crear una duplicada.
     * - Los productos se omiten si ya existe uno con el mismo nombre en la misma
     *   categoría, para evitar duplicar el menú si se importa el mismo archivo
     *   dos veces por error.
     * No se borra ni modifica nada existente: solo se agrega lo nuevo.
     */
    suspend fun importarMenu(importado: MenuImportado): ResultadoImportacionMenu {
        val categoriasActuales = categoriaDao.obtenerCategorias().toMutableList()
        val productosActuales = productoDao.obtenerProductos()

        var categoriasCreadas = 0
        var productosCreados = 0
        var productosOmitidos = 0

        fun idCategoriaPorNombre(nombre: String?): Long? {
            if (nombre.isNullOrBlank()) return null
            return categoriasActuales.firstOrNull { it.nombre.equals(nombre, ignoreCase = true) }?.id
        }

        importado.categorias.forEach { cat ->
            val existente = categoriasActuales.firstOrNull { it.nombre.equals(cat.nombre, ignoreCase = true) }
            if (existente == null) {
                val nuevoId = agregarCategoria(cat.nombre, cat.icono)
                categoriasActuales.add(CategoriaEntity(id = nuevoId, nombre = cat.nombre, icono = cat.icono))
                categoriasCreadas++
            }
        }

        importado.productos.forEach { prod ->
            val categoriaId = idCategoriaPorNombre(prod.categoria)
            val yaExiste = productosActuales.any {
                it.nombre.equals(prod.nombre, ignoreCase = true) && it.categoriaId == categoriaId
            }
            if (yaExiste) {
                productosOmitidos++
            } else {
                productoDao.insertar(
                    ProductoEntity(
                        nombre = prod.nombre,
                        precio = prod.precio,
                        categoriaId = categoriaId,
                        descripcion = prod.descripcion,
                        disponible = prod.disponible
                    )
                )
                productosCreados++
            }
        }

        return ResultadoImportacionMenu(categoriasCreadas, productosCreados, productosOmitidos)
    }
}

data class CategoriaImportada(val nombre: String, val icono: String = "Restaurant")

data class ProductoImportado(
    val nombre: String,
    val precio: Double,
    val categoria: String?,
    val descripcion: String?,
    val disponible: Boolean
)

data class MenuImportado(
    val categorias: List<CategoriaImportada>,
    val productos: List<ProductoImportado>
)

data class ResultadoImportacionMenu(
    val categoriasCreadas: Int,
    val productosCreados: Int,
    val productosOmitidos: Int
)
