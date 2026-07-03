package com.restaurante.mesero.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.restaurante.mesero.ui.viewmodel.OBSERVACIONES_RAPIDAS

@Composable
fun DialogoObservaciones(
    titulo: String,
    observacionInicial: String = "",
    onConfirmar: (String) -> Unit,
    onCancelar: () -> Unit
) {
    var seleccionadas by remember {
        mutableStateOf(
            observacionInicial.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
        )
    }
    var notaPersonalizada by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = {
            Column {
                Text("Observaciones rápidas:", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(OBSERVACIONES_RAPIDAS) { obs ->
                        FilterChip(
                            selected = seleccionadas.contains(obs),
                            onClick = {
                                seleccionadas = if (seleccionadas.contains(obs)) {
                                    seleccionadas - obs
                                } else {
                                    seleccionadas + obs
                                }
                            },
                            label = { Text(obs) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = notaPersonalizada,
                    onValueChange = { notaPersonalizada = it },
                    label = { Text("Nota personalizada") },
                    placeholder = { Text("Ej. Para llevar, alergia a frutos secos...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val todas = (seleccionadas + listOfNotNull(notaPersonalizada.takeIf { it.isNotBlank() }))
                    .joinToString(", ")
                onConfirmar(todas)
            }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text("Cancelar") }
        }
    )
}
