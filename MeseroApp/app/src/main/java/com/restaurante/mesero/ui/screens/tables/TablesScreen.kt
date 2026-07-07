package com.restaurante.mesero.ui.screens.tables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurante.mesero.data.local.entity.EstadoMesa
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.local.entity.nombreVisible
import com.restaurante.mesero.ui.components.DialogoEditarMesa
import com.restaurante.mesero.ui.components.DialogoNuevaMesa
import com.restaurante.mesero.ui.components.TarjetaMesa
import com.restaurante.mesero.ui.theme.GrafitoSuave
import com.restaurante.mesero.ui.viewmodel.FiltroMesa
import com.restaurante.mesero.ui.viewmodel.TablesViewModel
import com.restaurante.mesero.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablesScreen(
    factory: ViewModelFactory,
    nombreMesero: String,
    moneda: String,
    onAbrirMesa: (Long) -> Unit,
    onIrAMenuAdmin: () -> Unit,
    onIrAHistorial: () -> Unit,
    onIrAEstadisticas: () -> Unit,
    onIrAConfiguracion: () -> Unit
) {
    val viewModel: TablesViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    var mostrarDialogoEliminar by remember { mutableStateOf<MesaEntity?>(null) }
    var mostrarDialogoEditar by remember { mutableStateOf<MesaEntity?>(null) }
    var mostrarDialogoCerrarForzado by remember { mutableStateOf<MesaEntity?>(null) }
    var mostrarDialogoNuevaMesa by remember { mutableStateOf(false) }
    var menuAbierto by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hola, $nombreMesero 👋") },
                actions = {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Más opciones")
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        DropdownMenuItem(text = { Text("Menú / Productos") }, onClick = { menuAbierto = false; onIrAMenuAdmin() })
                        DropdownMenuItem(text = { Text("Historial") }, onClick = { menuAbierto = false; onIrAHistorial() })
                        DropdownMenuItem(text = { Text("Estadísticas") }, onClick = { menuAbierto = false; onIrAEstadisticas() })
                        DropdownMenuItem(text = { Text("Configuración") }, onClick = { menuAbierto = false; onIrAConfiguracion() })
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoNuevaMesa = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar mesa")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Filtros rápidos
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.filtro == FiltroMesa.TODAS,
                    onClick = { viewModel.cambiarFiltro(FiltroMesa.TODAS) },
                    label = { Text("Todas") }
                )
                FilterChip(
                    selected = uiState.filtro == FiltroMesa.LIBRES,
                    onClick = { viewModel.cambiarFiltro(FiltroMesa.LIBRES) },
                    label = { Text("🟢 Libres") }
                )
                FilterChip(
                    selected = uiState.filtro == FiltroMesa.OCUPADAS,
                    onClick = { viewModel.cambiarFiltro(FiltroMesa.OCUPADAS) },
                    label = { Text("Ocupadas") }
                )
            }

            if (uiState.mesas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay mesas para mostrar", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.mesas, key = { it.id }) { mesa ->
                        TarjetaMesaConAcciones(
                            mesa = mesa,
                            total = uiState.totalesPorMesa[mesa.id] ?: 0.0,
                            moneda = moneda,
                            onClick = { onAbrirMesa(mesa.id) },
                            onEliminar = { mostrarDialogoEliminar = mesa },
                            onMarcarEsperandoCuenta = { viewModel.marcarEsperandoCuenta(mesa) },
                            onEditar = { mostrarDialogoEditar = mesa },
                            onCerrarForzado = { mostrarDialogoCerrarForzado = mesa }
                        )
                    }
                }
            }
        }
    }

    if (mostrarDialogoNuevaMesa) {
        DialogoNuevaMesa(
            onConfirmar = { nombre, capacidad ->
                viewModel.agregarMesa(capacidad = capacidad, nombre = nombre)
                mostrarDialogoNuevaMesa = false
            },
            onCancelar = { mostrarDialogoNuevaMesa = false }
        )
    }

    mostrarDialogoEliminar?.let { mesa ->
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = null },
            title = { Text("Eliminar ${mesa.nombreVisible}") },
            text = { Text("¿Seguro que deseas eliminar esta mesa? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarMesa(mesa)
                    mostrarDialogoEliminar = null
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    mostrarDialogoEditar?.let { mesa ->
        DialogoEditarMesa(
            mesa = mesa,
            onConfirmar = { nombre, capacidad ->
                viewModel.editarMesa(mesa, nombre, capacidad)
                mostrarDialogoEditar = null
            },
            onCancelar = { mostrarDialogoEditar = null }
        )
    }

    mostrarDialogoCerrarForzado?.let { mesa ->
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarForzado = null },
            title = { Text("Cerrar ${mesa.nombreVisible}") },
            text = {
                Text(
                    "Esta mesa está abierta. Si la abriste por error, puedes cerrarla ahora: " +
                        "se cancelará el pedido en curso (si existe) y la mesa quedará libre. " +
                        "Esta acción no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cerrarMesaForzado(mesa)
                    mostrarDialogoCerrarForzado = null
                }) { Text("Cerrar mesa") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarForzado = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun TarjetaMesaConAcciones(
    mesa: MesaEntity,
    total: Double,
    moneda: String,
    onClick: () -> Unit,
    onEliminar: () -> Unit,
    onMarcarEsperandoCuenta: () -> Unit,
    onEditar: () -> Unit,
    onCerrarForzado: () -> Unit
) {
    var menuContextual by remember { mutableStateOf(false) }

    Box {
        TarjetaMesa(
            mesa = mesa,
            total = total,
            moneda = moneda,
            onClick = onClick,
            onTitleClick = onEditar
        )
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp)) {
            IconButton(onClick = { menuContextual = true }, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Más opciones de ${mesa.nombreVisible}",
                    tint = GrafitoSuave
                )
            }
            DropdownMenu(expanded = menuContextual, onDismissRequest = { menuContextual = false }) {
                DropdownMenuItem(
                    text = { Text("Personalizar mesa") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = { menuContextual = false; onEditar() }
                )
                if (mesa.estado == EstadoMesa.OCUPADA) {
                    DropdownMenuItem(
                        text = { Text("Marcar esperando cuenta") },
                        onClick = { menuContextual = false; onMarcarEsperandoCuenta() }
                    )
                }
                if (mesa.estado != EstadoMesa.LIBRE) {
                    DropdownMenuItem(
                        text = { Text("Cerrar mesa") },
                        leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null) },
                        onClick = { menuContextual = false; onCerrarForzado() }
                    )
                }
                if (mesa.estado == EstadoMesa.LIBRE) {
                    DropdownMenuItem(
                        text = { Text("Eliminar mesa") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = { menuContextual = false; onEliminar() }
                    )
                }
            }
        }
    }
}
