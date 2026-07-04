package com.restaurante.mesero.ui.screens.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.restaurante.mesero.ui.viewmodel.ViewModelFactory
import com.restaurante.mesero.ui.viewmodel.WelcomeViewModel

@Composable
fun WelcomeScreen(
    factory: ViewModelFactory,
    onContinuar: (nombreMesero: String) -> Unit
) {
    val viewModel: WelcomeViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()
    var nombre by remember { mutableStateOf("") }
    var yaPrellenado by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.nombreMeseroRecordado) {
        if (!yaPrellenado && uiState.nombreMeseroRecordado != null) {
            nombre = uiState.nombreMeseroRecordado.orEmpty()
            yaPrellenado = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = uiState.nombreRestaurante,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Gestión de pedidos para meseros",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Tu nombre (opcional)") },
                placeholder = { Text("Ej. Carlos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val nombreFinal = nombre.ifBlank { "Mesero" }
                    viewModel.guardarNombreMesero(nombreFinal)
                    onContinuar(nombreFinal)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Comenzar turno", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
