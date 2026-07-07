package com.restaurante.mesero.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurante.mesero.data.repository.EstadisticasDia
import com.restaurante.mesero.ui.viewmodel.DetalleRapido
import com.restaurante.mesero.ui.viewmodel.StatsViewModel
import com.restaurante.mesero.ui.viewmodel.ViewModelFactory
import com.restaurante.mesero.util.Formato

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    factory: ViewModelFactory,
    moneda: String,
    detallesRapidosCsv: String,
    onVolver: () -> Unit
) {
    val viewModel: StatsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val detalles = remember(detallesRapidosCsv) { DetalleRapido.desdeCsv(detallesRapidosCsv) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estadísticas de hoy") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (cargando) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                detalles.chunked(2).forEach { fila ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        fila.forEach { detalle ->
                            TarjetaEstadistica(
                                icono = iconoPara(detalle),
                                etiqueta = detalle.etiqueta,
                                valor = valorPara(detalle, uiState, moneda),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (fila.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Producto más vendido", style = MaterialTheme.typography.labelLarge)
                            Text(
                                uiState.productoMasVendido ?: "Sin datos aún",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState.productoMasVendido != null) {
                                Text(
                                    "${uiState.cantidadProductoMasVendido} unidades vendidas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    onClick = { viewModel.cargarEstadisticasDeHoy() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Actualizar")
                }
            }
        }
    }
}

private fun iconoPara(detalle: DetalleRapido): ImageVector = when (detalle) {
    DetalleRapido.VENTAS_TOTALES -> Icons.Default.AttachMoney
    DetalleRapido.PEDIDOS_HOY -> Icons.Default.Receipt
    DetalleRapido.MESAS_ATENDIDAS -> Icons.Default.TableRestaurant
    DetalleRapido.PROMEDIO_MESA -> Icons.Default.TrendingUp
    DetalleRapido.PROPINAS_TOTALES -> Icons.Default.Savings
}

private fun valorPara(detalle: DetalleRapido, stats: EstadisticasDia, moneda: String): String = when (detalle) {
    DetalleRapido.VENTAS_TOTALES -> Formato.moneda(stats.ventasTotales, moneda)
    DetalleRapido.PEDIDOS_HOY -> stats.numeroPedidos.toString()
    DetalleRapido.MESAS_ATENDIDAS -> stats.mesasAtendidas.toString()
    DetalleRapido.PROMEDIO_MESA -> Formato.moneda(stats.promedioPorMesa, moneda)
    DetalleRapido.PROPINAS_TOTALES -> Formato.moneda(stats.propinasTotales, moneda)
}

@Composable
private fun TarjetaEstadistica(
    icono: ImageVector,
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
