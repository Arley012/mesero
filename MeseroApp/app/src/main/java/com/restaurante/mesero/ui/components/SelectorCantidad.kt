package com.restaurante.mesero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SelectorCantidad(
    cantidad: Int,
    onDecrementar: () -> Unit,
    onIncrementar: () -> Unit,
    modifier: Modifier = Modifier,
    tamanoBoton: androidx.compose.ui.unit.Dp = 36.dp
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDecrementar, modifier = Modifier.size(tamanoBoton)) {
            Icon(Icons.Default.Remove, contentDescription = "Disminuir")
        }
        Text(
            text = cantidad.toString(),
            modifier = Modifier.padding(horizontal = 8.dp),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(onClick = onIncrementar, modifier = Modifier.size(tamanoBoton)) {
            Icon(Icons.Default.Add, contentDescription = "Aumentar")
        }
    }
}
