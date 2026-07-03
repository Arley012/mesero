package com.restaurante.mesero.ui.screens.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import com.restaurante.mesero.data.local.entity.ProductoEntity
import com.restaurante.mesero.ui.components.DialogoObservaciones
import com.restaurante.mesero.ui.components.SelectorCantidad
import com.restaurante.mesero.ui.viewmodel.OrderViewModel
import com.restaurante.mesero.ui.viewmodel.ViewModelFactory
import com.restaurante.mesero.util.Formato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    factory: ViewModelFactory,
    mesaId: Long,
    nombreMesero: String,
    onVolver: () -> Unit,
    onIrACuenta: (pedidoId: Long) -> Unit
) {
    val viewModel: OrderViewModel = viewModel(factory = factory)
    LaunchedEffect(mesaId) { viewModel.inicializar(mesaId, nombreMesero) }

    val uiState by viewModel.uiState.collectAsState()
    var productoParaObservacion by remember { mutableStateOf<ProductoEntity?>(null) }
    var itemParaEditarObservacion by remember { mutableStateOf<ItemPedidoEntity?>(null) }
    var mostrarResumen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mesa ${uiState.mesa?.numero ?: ""}")
                        Text(
                            text = "Subtotal: ${Formato.moneda(uiState.subtotal, uiState.moneda)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (uiState.items.isNotEmpty()) {
                                Badge { Text(uiState.items.sumOf { it.cantidad }.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = { mostrarResumen = true }) {
                            Icon(Icons.Default.Receipt, contentDescription = "Ver resumen del pedido")
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = {
                        val pedido = uiState.pedido
                        if (pedido != null) {
                            viewModel.marcarEsperandoCuenta()
                            onIrACuenta(pedido.id)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    enabled = uiState.items.isNotEmpty()
                ) {
                    Text("Ir a cuenta · ${Formato.moneda(uiState.subtotal, uiState.moneda)}")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Buscador
            OutlinedTextField(
                value = uiState.textoBusqueda,
                onValueChange = { viewModel.buscar(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.textoBusqueda.isNotEmpty()) {
                        IconButton(onClick = { viewModel.buscar("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )

            // Categorías
            if (uiState.textoBusqueda.isBlank()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = uiState.categoriaSeleccionadaId == null,
                            onClick = { viewModel.seleccionarCategoria(null) },
                            label = { Text("Todo") }
                        )
                    }
                    items(uiState.categorias, key = { it.id }) { categoria ->
                        FilterChip(
                            selected = uiState.categoriaSeleccionadaId == categoria.id,
                            onClick = { viewModel.seleccionarCategoria(categoria.id) },
                            label = { Text(categoria.nombre) }
                        )
                    }
                }
            }

            // Lista de productos
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.productosFiltrados.filter { it.disponible }, key = { it.id }) { producto ->
                    TarjetaProductoMenu(
                        producto = producto,
                        moneda = uiState.moneda,
                        onAgregarRapido = { viewModel.agregarProducto(producto) },
                        onAgregarConNota = { productoParaObservacion = producto }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    // Diálogo: agregar producto con observación
    productoParaObservacion?.let { producto ->
        DialogoObservaciones(
            titulo = "Observaciones para ${producto.nombre}",
            onConfirmar = { obs ->
                viewModel.agregarProducto(producto, observaciones = obs)
                productoParaObservacion = null
            },
            onCancelar = { productoParaObservacion = null }
        )
    }

    // Diálogo: editar observación de un ítem ya agregado
    itemParaEditarObservacion?.let { item ->
        DialogoObservaciones(
            titulo = "Editar observaciones",
            observacionInicial = item.observaciones.orEmpty(),
            onConfirmar = { obs ->
                viewModel.actualizarObservaciones(item, obs)
                itemParaEditarObservacion = null
            },
            onCancelar = { itemParaEditarObservacion = null }
        )
    }

    // Hoja inferior: resumen del pedido
    if (mostrarResumen) {
        ModalBottomSheet(onDismissRequest = { mostrarResumen = false }) {
            ResumenPedidoContenido(
                items = uiState.items,
                moneda = uiState.moneda,
                subtotal = uiState.subtotal,
                onCambiarCantidad = { item, nueva -> viewModel.cambiarCantidad(item, nueva) },
                onEliminar = { viewModel.eliminarItem(it) },
                onEditarObservacion = { itemParaEditarObservacion = it; mostrarResumen = false },
                onDuplicar = { viewModel.duplicarItem(it) }
            )
        }
    }
}

@Composable
private fun TarjetaProductoMenu(
    producto: ProductoEntity,
    moneda: String,
    onAgregarRapido: () -> Unit,
    onAgregarConNota: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onAgregarRapido
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (!producto.descripcion.isNullOrBlank()) {
                    Text(
                        producto.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    Formato.moneda(producto.precio, moneda),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            OutlinedButton(onClick = onAgregarConNota) {
                Text("+ Nota")
            }
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(onClick = onAgregarRapido) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    }
}

@Composable
private fun ResumenPedidoContenido(
    items: List<ItemPedidoEntity>,
    moneda: String,
    subtotal: Double,
    onCambiarCantidad: (ItemPedidoEntity, Int) -> Unit,
    onEliminar: (ItemPedidoEntity) -> Unit,
    onEditarObservacion: (ItemPedidoEntity) -> Unit,
    onDuplicar: (ItemPedidoEntity) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Resumen del pedido", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            Text("Aún no has agregado productos.", modifier = Modifier.padding(vertical = 24.dp))
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ItemPedidoFila(
                        item = item,
                        moneda = moneda,
                        onCambiarCantidad = { nueva -> onCambiarCantidad(item, nueva) },
                        onEliminar = { onEliminar(item) },
                        onEditarObservacion = { onEditarObservacion(item) },
                        onDuplicar = { onDuplicar(item) }
                    )
                    HorizontalDivider()
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", style = MaterialTheme.typography.titleMedium)
                Text(
                    Formato.moneda(subtotal, moneda),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ItemPedidoFila(
    item: ItemPedidoEntity,
    moneda: String,
    onCambiarCantidad: (Int) -> Unit,
    onEliminar: () -> Unit,
    onEditarObservacion: () -> Unit,
    onDuplicar: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.nombreProducto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                Formato.moneda(item.precioUnitario, moneda) + " c/u",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!item.observaciones.isNullOrBlank()) {
                Text(
                    "📝 ${item.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Text(
                Formato.moneda(item.subtotal, moneda),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        SelectorCantidad(
            cantidad = item.cantidad,
            onDecrementar = { onCambiarCantidad(item.cantidad - 1) },
            onIncrementar = { onCambiarCantidad(item.cantidad + 1) }
        )
        IconButton(onClick = onEditarObservacion) {
            Icon(Icons.Default.Edit, contentDescription = "Editar observación")
        }
        IconButton(onClick = onDuplicar) {
            Icon(Icons.Default.FileCopy, contentDescription = "Duplicar")
        }
        IconButton(onClick = onEliminar) {
            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
        }
    }
}
