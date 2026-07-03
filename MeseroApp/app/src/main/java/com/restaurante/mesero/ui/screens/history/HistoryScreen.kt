package com.restaurante.mesero.ui.screens.history

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import com.restaurante.mesero.data.local.entity.PedidoEntity
import com.restaurante.mesero.ui.viewmodel.HistoryViewModel
import com.restaurante.mesero.ui.viewmodel.ViewModelFactory
import com.restaurante.mesero.util.ExportadorHistorial
import com.restaurante.mesero.util.Formato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    factory: ViewModelFactory,
    moneda: String,
    onVolver: () -> Unit
) {
    val viewModel: HistoryViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mostrarDatePicker by remember { mutableStateOf(false) }
    var pedidoExpandido by remember { mutableStateOf<Long?>(null) }
    var itemsDelPedido by remember { mutableStateOf<List<ItemPedidoEntity>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Filtrar por fecha")
                    }
                    if (uiState.fechaFiltro != null) {
                        IconButton(onClick = { viewModel.limpiarFiltro() }) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar filtro")
                        }
                    }
                    IconButton(onClick = {
                        val archivo = ExportadorHistorial.exportarCsv(context, uiState.pedidos, moneda)
                        val uri = FileProvider.getUriForFile(
                            context, "${context.packageName}.fileprovider", archivo
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Exportar historial"))
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Exportar a Excel/CSV")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.pedidos.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no hay pedidos en el historial", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.pedidos, key = { it.id }) { pedido ->
                    TarjetaPedidoHistorial(
                        pedido = pedido,
                        moneda = moneda,
                        expandido = pedidoExpandido == pedido.id,
                        items = if (pedidoExpandido == pedido.id) itemsDelPedido else emptyList(),
                        onToggle = {
                            pedidoExpandido = if (pedidoExpandido == pedido.id) null else pedido.id
                        }
                    )
                }
            }
        }
    }

    LaunchedEffect(pedidoExpandido) {
        val id = pedidoExpandido
        itemsDelPedido = if (id != null) viewModel.obtenerItemsDePedido(id) else emptyList()
    }

    if (mostrarDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.filtrarPorFecha(it) }
                    mostrarDatePicker = false
                }) { Text("Aplicar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TarjetaPedidoHistorial(
    pedido: PedidoEntity,
    moneda: String,
    expandido: Boolean,
    items: List<ItemPedidoEntity>,
    onToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onToggle) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Mesa ${pedido.numeroMesa}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        Formato.fechaHora(pedido.fechaCierre ?: pedido.fechaApertura),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Mesero: ${pedido.nombreMesero}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    Formato.moneda(pedido.total, moneda),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (expandido) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.cantidad}x ${item.nombreProducto}", style = MaterialTheme.typography.bodyMedium)
                        Text(Formato.moneda(item.subtotal, moneda), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
