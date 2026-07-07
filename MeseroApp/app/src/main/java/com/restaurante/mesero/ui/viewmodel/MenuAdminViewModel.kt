package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import android.net.Uri
import com.restaurante.mesero.data.local.entity.CategoriaEntity
import com.restaurante.mesero.data.local.entity.ProductoEntity
import com.restaurante.mesero.data.repository.MenuRepository
import com.restaurante.mesero.data.repository.ResultadoImportacionMenu
import com.restaurante.mesero.util.ExportadorMenu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class MenuAdminUiState(
    val categorias: List<CategoriaEntity> = emptyList(),
    val productos: List<ProductoEntity> = emptyList(),
    val categoriaFiltroId: Long? = null
) {
    val productosFiltrados: List<ProductoEntity>
        get() = if (categoriaFiltroId == null) productos else productos.filter { it.categoriaId == categoriaFiltroId }
}

class MenuAdminViewModel(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _categoriaFiltroId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<MenuAdminUiState> = combine(
        menuRepository.observarCategorias(),
        menuRepository.observarProductos(),
        _categoriaFiltroId
    ) { categorias, productos, filtroId ->
        MenuAdminUiState(categorias = categorias, productos = productos, categoriaFiltroId = filtroId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MenuAdminUiState())

    fun filtrarPorCategoria(categoriaId: Long?) {
        _categoriaFiltroId.value = categoriaId
    }

    fun agregarCategoria(nombre: String, icono: String = "Restaurant") {
        viewModelScope.launch { menuRepository.agregarCategoria(nombre, icono) }
    }

    fun editarCategoria(categoria: CategoriaEntity, nuevoNombre: String) {
        viewModelScope.launch {
            menuRepository.actualizarCategoria(categoria.copy(nombre = nuevoNombre))
        }
    }

    fun eliminarCategoria(categoria: CategoriaEntity) {
        viewModelScope.launch {
            menuRepository.eliminarCategoria(categoria)
            // Los productos de esta categoría quedan sin categoría (SET_NULL), así que
            // si el filtro activo era justo esta categoría, lo reseteamos para no
            // dejar la lista de productos vacía por error.
            if (_categoriaFiltroId.value == categoria.id) {
                _categoriaFiltroId.value = null
            }
        }
    }

    fun guardarProducto(
        id: Long,
        nombre: String,
        precio: Double,
        categoriaId: Long?,
        descripcion: String?,
        disponible: Boolean
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                menuRepository.agregarProducto(
                    ProductoEntity(
                        nombre = nombre,
                        precio = precio,
                        categoriaId = categoriaId,
                        descripcion = descripcion,
                        disponible = disponible
                    )
                )
            } else {
                val existente = menuRepository.obtenerProducto(id) ?: return@launch
                menuRepository.actualizarProducto(
                    existente.copy(
                        nombre = nombre,
                        precio = precio,
                        categoriaId = categoriaId,
                        descripcion = descripcion,
                        disponible = disponible
                    )
                )
            }
        }
    }

    fun eliminarProducto(producto: ProductoEntity) {
        viewModelScope.launch { menuRepository.eliminarProducto(producto) }
    }

    fun alternarFavorito(producto: ProductoEntity) {
        viewModelScope.launch { menuRepository.alternarFavorito(producto) }
    }

    fun alternarDisponible(producto: ProductoEntity) {
        viewModelScope.launch {
            menuRepository.actualizarProducto(producto.copy(disponible = !producto.disponible))
        }
    }

    /** Exporta el menú actual a un archivo .json y lo entrega vía callback para compartir. */
    fun exportarMenu(context: Context, onListo: (File) -> Unit) {
        viewModelScope.launch {
            val (categorias, productos) = menuRepository.obtenerMenuCompleto()
            val archivo = ExportadorMenu.exportarArchivo(context, categorias, productos)
            onListo(archivo)
        }
    }

    /** Importa un menú desde un archivo .json elegido por el usuario, fusionándolo con el actual. */
    fun importarMenu(
        context: Context,
        uri: Uri,
        onResultado: (Result<ResultadoImportacionMenu>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val importado = ExportadorMenu.importarDesdeUri(context, uri)
                val resultado = menuRepository.importarMenu(importado)
                onResultado(Result.success(resultado))
            } catch (e: Exception) {
                onResultado(Result.failure(e))
            }
        }
    }
}
