package com.restaurante.mesero.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.local.entity.nombreVisible

@Composable
fun DialogoEditarMesa(
    mesa: MesaEntity,
    onConfirmar: (nombre: String, capacidad: Int) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre by remember { mutableStateOf(mesa.nombre ?: "") }
    var capacidad by remember { mutableIntStateOf(mesa.capacidad) }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Personalizar ${mesa.nombreVisible}") },
        text = {
            Column {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la mesa") },
                    placeholder = { Text("Ej. Terraza 1, VIP...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text("Número de personas")
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (capacidad > 1) capacidad-- }) {
                        Icon(Icons.Default.Remove, contentDescription = "Reducir capacidad")
                    }
                    Text(
                        text = capacidad.toString(),
                        modifier = Modifier.width(40.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { capacidad++ }) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar capacidad")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(nombre, capacidad) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
