package com.restaurante.mesero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.restaurante.mesero.data.local.entity.EstadoMesa
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.local.entity.nombreVisible
import com.restaurante.mesero.ui.theme.*
import com.restaurante.mesero.util.Formato

data class EstiloEstadoMesa(
    val color: Color,
    val colorContainer: Color,
    val etiqueta: String
)

fun estiloDe(estado: EstadoMesa): EstiloEstadoMesa = when (estado) {
    EstadoMesa.LIBRE -> EstiloEstadoMesa(MesaLibre, MesaLibreContainer, "Libre")
    EstadoMesa.OCUPADA -> EstiloEstadoMesa(MesaOcupada, MesaOcupadaContainer, "Ocupada")
    EstadoMesa.ESPERANDO_PEDIDO -> EstiloEstadoMesa(MesaEsperandoPedido, MesaEsperandoPedidoContainer, "Esperando pedido")
    EstadoMesa.ESPERANDO_CUENTA -> EstiloEstadoMesa(MesaEsperandoCuenta, MesaEsperandoCuentaContainer, "Esperando cuenta")
}

@Composable
fun TarjetaMesa(
    mesa: MesaEntity,
    total: Double,
    moneda: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTitleClick: (() -> Unit)? = null
) {
    val estilo = estiloDe(mesa.estado)
    // Las tarjetas de mesa siempre usan fondos pastel claros (por diseño, incluso
    // en modo oscuro), así que fijamos un color de texto oscuro constante en vez
    // de heredar el color de contenido ambiental del tema, que en modo oscuro es
    // claro y quedaría invisible sobre estos fondos claros.
    val colorTexto = GrafitoSuave

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(176.dp),
        shape = RadioTarjeta,
        colors = CardDefaults.cardColors(
            containerColor = estilo.colorContainer,
            contentColor = colorTexto
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Reservamos espacio a la derecha para el botón de "⋮" que se
                // dibuja encima de la tarjeta, así el badge de estado nunca lo tapa.
                .padding(start = 18.dp, top = 18.dp, end = 44.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = mesa.nombreVisible,
                    style = MaterialTheme.typography.headlineSmall,
                    color = estilo.color,
                    maxLines = 1,
                    modifier = if (onTitleClick != null) {
                        Modifier.clickable(onClick = onTitleClick)
                    } else {
                        Modifier
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Blanco.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = estilo.etiqueta,
                        style = MaterialTheme.typography.labelSmall,
                        color = estilo.color
                    )
                }
            }

            if (mesa.horaApertura != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = estilo.color
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${Formato.hora(mesa.horaApertura)} · ${Formato.tiempoTranscurrido(mesa.horaApertura)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorTexto
                    )
                }
                if (!mesa.meseroAsignado.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = estilo.color
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mesa.meseroAsignado,
                            style = MaterialTheme.typography.bodySmall,
                            color = colorTexto
                        )
                    }
                }
            } else {
                Text(
                    text = "Capacidad: ${mesa.capacidad} personas",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorTexto
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = estilo.color
                )
                Text(
                    text = Formato.moneda(total, moneda),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorTexto
                )
            }
        }
    }
}
