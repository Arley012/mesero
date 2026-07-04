package com.restaurante.mesero.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.local.entity.nombreVisible
import com.restaurante.mesero.ui.viewmodel.SettingsViewModel
import com.restaurante.mesero.ui.viewmodel.ViewModelFactory
import com.restaurante.mesero.util.BackupManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    factory: ViewModelFactory,
    onVolver: () -> Unit
) {
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var nombreRestaurante by remember(uiState.config.nombreRestaurante) { mutableStateOf(uiState.config.nombreRestaurante) }
    var moneda by remember(uiState.config.moneda) { mutableStateOf(uiState.config.moneda) }
    var impuestoTexto by remember(uiState.config.porcentajeImpuesto) { mutableStateOf(uiState.config.porcentajeImpuesto.toString()) }
    var mesaAEliminar by remember { mutableStateOf<MesaEntity?>(null) }
    var mensajeBackup by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SeccionTitulo("Restaurante")
                OutlinedTextField(
                    value = nombreRestaurante,
                    onValueChange = {
                        nombreRestaurante = it
                        viewModel.actualizarNombreRestaurante(it)
                    },
                    label = { Text("Nombre del restaurante") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = moneda,
                    onValueChange = {
                        moneda = it
                        viewModel.actualizarMoneda(it)
                    },
                    label = { Text("Símbolo de moneda (ej. Bs, $, S/.)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = impuestoTexto,
                    onValueChange = {
                        impuestoTexto = it
                        it.toDoubleOrNull()?.let { valor -> viewModel.actualizarImpuesto(valor) }
                    },
                    label = { Text("Impuesto (%)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                SeccionTitulo("Apariencia")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Modo oscuro")
                    Switch(
                        checked = uiState.config.modoOscuro,
                        onCheckedChange = { viewModel.alternarModoOscuro(it) }
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SeccionTitulo("Mesas (${uiState.mesas.size})")
                    IconButton(onClick = { viewModel.agregarMesa() }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar mesa")
                    }
                }
            }

            items(uiState.mesas, key = { it.id }) { mesa ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${mesa.nombreVisible} · Capacidad ${mesa.capacidad}")
                    IconButton(onClick = { mesaAEliminar = mesa }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            item {
                SeccionTitulo("Copia de seguridad")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val archivo = BackupManager.crearBackup(context)
                            mensajeBackup = "Backup creado: ${archivo.name}"
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear backup")
                    }
                    OutlinedButton(
                        onClick = {
                            val backups = BackupManager.listarBackups(context)
                            if (backups.isNotEmpty()) {
                                BackupManager.restaurarBackup(context, backups.first())
                                mensajeBackup = "Backup restaurado. Reinicia la app para aplicar los cambios."
                            } else {
                                mensajeBackup = "No hay copias de seguridad disponibles."
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restaurar")
                    }
                }
                Text(
                    "Los backups se guardan localmente en el almacenamiento del dispositivo, sin conexión a Internet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    mesaAEliminar?.let { mesa ->
        AlertDialog(
            onDismissRequest = { mesaAEliminar = null },
            title = { Text("Eliminar ${mesa.nombreVisible}") },
            text = { Text("¿Seguro que deseas eliminar esta mesa?") },
            confirmButton = {
                TextButton(onClick = { viewModel.eliminarMesa(mesa); mesaAEliminar = null }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mesaAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    mensajeBackup?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { mensajeBackup = null },
            title = { Text("Copia de seguridad") },
            text = { Text(mensaje) },
            confirmButton = { TextButton(onClick = { mensajeBackup = null }) { Text("OK") } }
        )
    }
}

@Composable
private fun SeccionTitulo(texto: String) {
    Text(texto, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}
