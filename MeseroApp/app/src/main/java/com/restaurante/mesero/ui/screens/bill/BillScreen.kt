package com.restaurante.mesero.ui.screens.bill

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurante.mesero.data.local.entity.nombreVisible
import com.restaurante.mesero.ui.components.DialogoSeleccionarImpresora
import com.restaurante.mesero.ui.viewmodel.BillViewModel
import com.restaurante.mesero.ui.viewmodel.TipoPropina
import com.restaurante.mesero.ui.viewmodel.ViewModelFactory
import com.restaurante.mesero.util.AnchoPapel
import com.restaurante.mesero.util.Formato
import com.restaurante.mesero.util.ImpresoraTermica
import com.restaurante.mesero.util.PdfCuentaGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillScreen(
    factory: ViewModelFactory,
    pedidoId: Long,
    nombreRestaurante: String,
    onVolver: () -> Unit,
    onCuentaCerrada: () -> Unit
) {
    val viewModel: BillViewModel = viewModel(factory = factory)
    LaunchedEffect(pedidoId) { viewModel.inicializar(pedidoId) }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mostrarDialogoImpresora by remember { mutableStateOf(false) }
    var anchoPapel by remember { mutableStateOf(AnchoPapel.MM_80) }
    var imprimiendo by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    val permisoBluetoothLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            mostrarDialogoImpresora = true
        } else {
            mensajeError = "Se necesita el permiso de Bluetooth para imprimir la cuenta."
        }
    }

    fun solicitarImpresion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val concedido = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            if (concedido) {
                mostrarDialogoImpresora = true
            } else {
                permisoBluetoothLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            mostrarDialogoImpresora = true
        }
    }

    LaunchedEffect(uiState.cerrado) {
        if (uiState.cerrado) onCuentaCerrada()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cuenta · ${uiState.mesa?.nombreVisible ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val pedido = uiState.pedido
                        if (pedido != null) {
                            val archivo = PdfCuentaGenerator.generar(
                                context, nombreRestaurante, pedido, uiState.items, uiState.config.moneda
                            )
                            val uri = PdfCuentaGenerator.obtenerUriParaCompartir(context, archivo)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir cuenta"))
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir PDF")
                    }
                    IconButton(onClick = { solicitarImpresion() }) {
                        Icon(Icons.Default.Print, contentDescription = "Imprimir")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { viewModel.cerrarCuentaYLiberarMesa() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Cerrar cuenta y liberar mesa")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(nombreRestaurante, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Mesero: ${uiState.pedido?.nombreMesero ?: ""}")
                        Text("Fecha: ${Formato.fechaHora(uiState.pedido?.fechaApertura ?: System.currentTimeMillis())}")
                    }
                }
            }

            item {
                Text("Productos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(uiState.items, key = { it.id }) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${item.cantidad}x ${item.nombreProducto}")
                    Text(Formato.moneda(item.subtotal, uiState.config.moneda))
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            item {
                FilaTotal("Subtotal", uiState.subtotal, uiState.config.moneda)
                if (uiState.impuesto > 0) {
                    FilaTotal("Impuesto (${uiState.config.porcentajeImpuesto}%)", uiState.impuesto, uiState.config.moneda)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Propina", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                SelectorPropina(
                    tipoSeleccionado = uiState.tipoPropina,
                    propinaPersonalizada = uiState.propinaPersonalizada,
                    onSeleccionar = { viewModel.seleccionarPropina(it) },
                    onPersonalizada = { viewModel.establecerPropinaPersonalizada(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                FilaTotal("Propina", uiState.propina, uiState.config.moneda)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(
                        Formato.moneda(uiState.total, uiState.config.moneda),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Dividir cuenta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                SelectorNumeroPersonas(
                    numeroPersonas = uiState.numeroPersonas,
                    onCambiar = { viewModel.establecerNumeroPersonas(it) }
                )
                if (uiState.numeroPersonas > 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Cada persona paga: ${Formato.moneda(uiState.totalPorPersona, uiState.config.moneda)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (mostrarDialogoImpresora) {
        val adapter = remember { BluetoothAdapter.getDefaultAdapter() }
        val dispositivos = remember { adapter?.let { ImpresoraTermica.dispositivosEmparejados(it) } ?: emptyList() }

        DialogoSeleccionarImpresora(
            dispositivos = dispositivos,
            anchoSeleccionado = anchoPapel,
            onCambiarAncho = { anchoPapel = it },
            onSeleccionar = { dispositivo: BluetoothDevice ->
                val pedido = uiState.pedido
                if (pedido != null) {
                    imprimiendo = true
                    scope.launch {
                        val resultado = ImpresoraTermica.imprimirCuenta(
                            dispositivo, anchoPapel, nombreRestaurante, pedido, uiState.items, uiState.config.moneda
                        )
                        imprimiendo = false
                        mostrarDialogoImpresora = false
                        resultado.onFailure { e -> mensajeError = "No se pudo imprimir: ${e.message}" }
                    }
                }
            },
            onCancelar = { mostrarDialogoImpresora = false }
        )
    }

    mensajeError?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { mensajeError = null },
            title = { Text("Error de impresión") },
            text = { Text(mensaje) },
            confirmButton = { TextButton(onClick = { mensajeError = null }) { Text("Entendido") } }
        )
    }

    if (imprimiendo) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun FilaTotal(etiqueta: String, valor: Double, moneda: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodyLarge)
        Text(Formato.moneda(valor, moneda), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SelectorPropina(
    tipoSeleccionado: TipoPropina,
    propinaPersonalizada: Double,
    onSeleccionar: (TipoPropina) -> Unit,
    onPersonalizada: (Double) -> Unit
) {
    var textoPersonalizado by remember { mutableStateOf(if (propinaPersonalizada > 0) propinaPersonalizada.toString() else "") }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = tipoSeleccionado == TipoPropina.NINGUNA, onClick = { onSeleccionar(TipoPropina.NINGUNA) }, label = { Text("Ninguna") })
        FilterChip(selected = tipoSeleccionado == TipoPropina.P5, onClick = { onSeleccionar(TipoPropina.P5) }, label = { Text("5%") })
        FilterChip(selected = tipoSeleccionado == TipoPropina.P10, onClick = { onSeleccionar(TipoPropina.P10) }, label = { Text("10%") })
        FilterChip(selected = tipoSeleccionado == TipoPropina.P15, onClick = { onSeleccionar(TipoPropina.P15) }, label = { Text("15%") })
    }
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = textoPersonalizado,
        onValueChange = {
            textoPersonalizado = it
            it.toDoubleOrNull()?.let { monto -> onPersonalizada(monto) }
        },
        label = { Text("Propina personalizada") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SelectorNumeroPersonas(numeroPersonas: Int, onCambiar: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedButton(onClick = { onCambiar((numeroPersonas - 1).coerceAtLeast(1)) }) { Text("-") }
        Text(
            "$numeroPersonas ${if (numeroPersonas == 1) "persona" else "personas"}",
            style = MaterialTheme.typography.titleMedium
        )
        OutlinedButton(onClick = { onCambiar(numeroPersonas + 1) }) { Text("+") }
    }
}
