package com.restaurante.mesero.ui.screens.menu

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurante.mesero.data.local.entity.CategoriaEntity
import com.restaurante.mesero.data.local.entity.ProductoEntity
import com.restaurante.mesero.data.repository.ResultadoImportacionMenu
import com.restaurante.mesero.ui.viewmodel.MenuAdminViewModel
import com.restaurante.mesero.ui.viewmodel.ViewModelFactory
import com.restaurante.mesero.util.Formato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuAdminScreen(
    factory: ViewModelFactory,
    moneda: String,
    onVolver: () -> Unit
) {
    val viewModel: MenuAdminViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var mostrarDialogoProducto by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<ProductoEntity?>(null) }
    var mostrarDialogoCategoria by remember { mutableStateOf(false) }
    var productoAEliminar by remember { mutableStateOf<ProductoEntity?>(null) }
    var resultadoImportacion by remember { mutableStateOf<Result<ResultadoImportacionMenu>?>(null) }

    val importarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importarMenu(context, uri) { resultado -> resultadoImportacion = resultado }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menú y productos") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { importarLauncher.launch(arrayOf("application/json")) }) {
                        Icon(Icons.Default.Upload, contentDescription = "Importar menú")
                    }
                    IconButton(onClick = {
                        viewModel.exportarMenu(context) { archivo ->
                            val uri = FileProvider.getUriForFile(
                                context, "${context.packageName}.fileprovider", archivo
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/json"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Exportar menú"))
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Exportar menú")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { productoEditando = null; mostrarDialogoProducto = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.categoriaFiltroId == null,
                        onClick = { viewModel.filtrarPorCategoria(null) },
                        label = { Text("Todas") }
                    )
                }
                items(uiState.categorias, key = { it.id }) { categoria ->
                    FilterChip(
                        selected = uiState.categoriaFiltroId == categoria.id,
                        onClick = { viewModel.filtrarPorCategoria(categoria.id) },
                        label = { Text(categoria.nombre) }
                    )
                }
                item {
                    AssistChip(
                        onClick = { mostrarDialogoCategoria = true },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        label = { Text("Categorías") }
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.productosFiltrados, key = { it.id }) { producto ->
                    val categoria = uiState.categorias.find { it.id == producto.categoriaId }
                    FilaProductoAdmin(
                        producto = producto,
                        nombreCategoria = categoria?.nombre ?: "Sin categoría",
                        moneda = moneda,
                        onEditar = { productoEditando = producto; mostrarDialogoProducto = true },
                        onEliminar = { productoAEliminar = producto },
                        onAlternarFavorito = { viewModel.alternarFavorito(producto) },
                        onAlternarDisponible = { viewModel.alternarDisponible(producto) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (mostrarDialogoProducto) {
        DialogoEditarProducto(
            producto = productoEditando,
            categorias = uiState.categorias,
            onGuardar = { nombre, precio, categoriaId, descripcion, disponible ->
                viewModel.guardarProducto(
                    productoEditando?.id ?: 0L, nombre, precio, categoriaId, descripcion, disponible
                )
                mostrarDialogoProducto = false
            },
            onCancelar = { mostrarDialogoProducto = false }
        )
    }

    if (mostrarDialogoCategoria) {
        DialogoGestionarCategorias(
            categorias = uiState.categorias,
            onAgregar = { nombre -> viewModel.agregarCategoria(nombre) },
            onRenombrar = { categoria, nuevoNombre -> viewModel.editarCategoria(categoria, nuevoNombre) },
            onEliminar = { categoria -> viewModel.eliminarCategoria(categoria) },
            onCerrar = { mostrarDialogoCategoria = false }
        )
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Eliminar \"${producto.nombre}\" del menú?") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminarProducto(producto); productoAEliminar = null }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    resultadoImportacion?.let { resultado ->
        AlertDialog(
            onDismissRequest = { resultadoImportacion = null },
            title = { Text(if (resultado.isSuccess) "Menú importado" else "No se pudo importar") },
            text = {
                resultado.fold(
                    onSuccess = { r ->
                        Text(
                            "Se agregaron ${r.categoriasCreadas} categoría(s) y ${r.productosCreados} " +
                                "producto(s) nuevo(s)." +
                                if (r.productosOmitidos > 0) {
                                    " Se omitieron ${r.productosOmitidos} producto(s) que ya existían."
                                } else {
                                    ""
                                }
                        )
                    },
                    onFailure = { e -> Text(e.message ?: "El archivo no tiene un formato válido.") }
                )
            },
            confirmButton = {
                TextButton(onClick = { resultadoImportacion = null }) { Text("Entendido") }
            }
        )
    }
}

@Composable
private fun FilaProductoAdmin(
    producto: ProductoEntity,
    nombreCategoria: String,
    moneda: String,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onAlternarFavorito: () -> Unit,
    onAlternarDisponible: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onAlternarFavorito) {
                Icon(
                    if (producto.esFavorito) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorito",
                    tint = if (producto.esFavorito) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(nombreCategoria, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    Formato.moneda(producto.precio, moneda),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Switch(checked = producto.disponible, onCheckedChange = { onAlternarDisponible() })
            IconButton(onClick = onEditar) { Icon(Icons.Default.Edit, contentDescription = "Editar") }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogoEditarProducto(
    producto: ProductoEntity?,
    categorias: List<CategoriaEntity>,
    onGuardar: (nombre: String, precio: Double, categoriaId: Long?, descripcion: String?, disponible: Boolean) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(producto?.nombre.orEmpty()) }
    var precioTexto by remember { mutableStateOf(producto?.precio?.toString().orEmpty()) }
    var descripcion by remember { mutableStateOf(producto?.descripcion.orEmpty()) }
    var categoriaSeleccionada by remember { mutableStateOf(producto?.categoriaId ?: categorias.firstOrNull()?.id) }
    var disponible by remember { mutableStateOf(producto?.disponible ?: true) }
    var expandedCategoria by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(if (producto == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = precioTexto, onValueChange = { precioTexto = it },
                    label = { Text("Precio") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion, onValueChange = { descripcion = it },
                    label = { Text("Descripción (opcional)") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedCategoria, onExpandedChange = { expandedCategoria = it }) {
                    OutlinedTextField(
                        value = categorias.find { it.id == categoriaSeleccionada }?.nombre ?: "Sin categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedCategoria, onDismissRequest = { expandedCategoria = false }) {
                        categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria.nombre) },
                                onClick = { categoriaSeleccionada = categoria.id; expandedCategoria = false }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = disponible, onCheckedChange = { disponible = it })
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disponible")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val precio = precioTexto.toDoubleOrNull() ?: 0.0
                if (nombre.isNotBlank()) {
                    onGuardar(nombre, precio, categoriaSeleccionada, descripcion.ifBlank { null }, disponible)
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoGestionarCategorias(
    categorias: List<CategoriaEntity>,
    onAgregar: (String) -> Unit,
    onRenombrar: (CategoriaEntity, String) -> Unit,
    onEliminar: (CategoriaEntity) -> Unit,
    onCerrar: () -> Unit
) {
    var nombreNueva by remember { mutableStateOf("") }
    var categoriaEditando by remember { mutableStateOf<CategoriaEntity?>(null) }
    var categoriaAEliminar by remember { mutableStateOf<CategoriaEntity?>(null) }

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Categorías") },
        text = {
            Column {
                if (categorias.isEmpty()) {
                    Text(
                        "Aún no hay categorías.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 260.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        categorias.forEach { categoria ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    categoria.nombre,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                IconButton(onClick = { categoriaEditando = categoria }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Renombrar ${categoria.nombre}")
                                }
                                IconButton(onClick = { categoriaAEliminar = categoria }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar ${categoria.nombre}",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = nombreNueva,
                        onValueChange = { nombreNueva = it },
                        label = { Text("Nueva categoría") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        if (nombreNueva.isNotBlank()) {
                            onAgregar(nombreNueva)
                            nombreNueva = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar categoría")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text("Listo") }
        }
    )

    categoriaEditando?.let { categoria ->
        DialogoRenombrarCategoria(
            categoria = categoria,
            onGuardar = { nuevoNombre ->
                onRenombrar(categoria, nuevoNombre)
                categoriaEditando = null
            },
            onCancelar = { categoriaEditando = null }
        )
    }

    categoriaAEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = { categoriaAEliminar = null },
            title = { Text("Eliminar categoría") },
            text = {
                Text(
                    "¿Eliminar \"${categoria.nombre}\"? Los productos de esta categoría no se " +
                        "borrarán, solo quedarán sin categoría asignada."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar(categoria)
                    categoriaAEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { categoriaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun DialogoRenombrarCategoria(
    categoria: CategoriaEntity,
    onGuardar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(categoria.nombre) }
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Renombrar categoría") },
        text = {
            OutlinedTextField(
                value = nombre, onValueChange = { nombre = it },
                label = { Text("Nombre") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (nombre.isNotBlank()) onGuardar(nombre) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar") } }
    )
}
