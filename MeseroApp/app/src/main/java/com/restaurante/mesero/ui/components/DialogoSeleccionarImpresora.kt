package com.restaurante.mesero.ui.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.restaurante.mesero.util.AnchoPapel

@SuppressLint("MissingPermission")
@Composable
fun DialogoSeleccionarImpresora(
    dispositivos: List<BluetoothDevice>,
    anchoSeleccionado: AnchoPapel,
    onCambiarAncho: (AnchoPapel) -> Unit,
    onSeleccionar: (BluetoothDevice) -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Imprimir cuenta") },
        text = {
            Column {
                Text("Tamaño de papel:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = anchoSeleccionado == AnchoPapel.MM_58,
                        onClick = { onCambiarAncho(AnchoPapel.MM_58) },
                        label = { Text("58 mm") }
                    )
                    FilterChip(
                        selected = anchoSeleccionado == AnchoPapel.MM_80,
                        onClick = { onCambiarAncho(AnchoPapel.MM_80) },
                        label = { Text("80 mm") }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Impresoras emparejadas:", style = MaterialTheme.typography.labelLarge)
                if (dispositivos.isEmpty()) {
                    Text(
                        "No hay impresoras Bluetooth emparejadas. Empareja tu impresora térmica desde los ajustes de Bluetooth del sistema.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(dispositivos) { dispositivo ->
                            ListItem(
                                headlineContent = { Text(dispositivo.name ?: "Dispositivo desconocido") },
                                leadingContent = { Icon(Icons.Default.Print, contentDescription = null) },
                                modifier = Modifier.clickable { onSeleccionar(dispositivo) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCancelar) { Text("Cerrar") }
        }
    )
}
