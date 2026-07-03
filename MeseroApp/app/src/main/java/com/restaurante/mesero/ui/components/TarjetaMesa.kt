package com.restaurante.mesero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.restaurante.mesero.ui.theme.*
import com.restaurante.mesero.util.Formato

data class EstiloEstadoMesa(
    val color: Color,
    val colorContainer: Color,
    val etiqueta: String,
    val emoji: String
)

fun estiloDe(estado: EstadoMesa): EstiloEstadoMesa = when (estado) {
    EstadoMesa.LIBRE -> EstiloEstadoMesa(MesaLibre, MesaLibreContainer, "Libre", "🟢")
    EstadoMesa.OCUPADA -> EstiloEstadoMesa(MesaOcupada, MesaOcupadaContainer, "Ocupada", "🟡")
    EstadoMesa.ESPERANDO_PEDIDO -> EstiloEstadoMesa(MesaEsperandoPedido, MesaEsperandoPedidoContainer, "Esperando pedido", "🔵")
    EstadoMesa.ESPERANDO_CUENTA -> EstiloEstadoMesa(MesaEsperandoCuenta, MesaEsperandoCuentaContainer, "Esperando cuenta", "🟠")
}

@Composable
fun TarjetaMesa(
    mesa: MesaEntity,
    total: Double,
    moneda: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estilo = estiloDe(mesa.estado)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = estilo.colorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mesa ${mesa.numero}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(estilo.color.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${estilo.emoji} ${estilo.etiqueta}",
                        style = MaterialTheme.typography.labelMedium,
                        color = estilo.color,
                        fontWeight = FontWeight.SemiBold
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
                        style = MaterialTheme.typography.bodySmall
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
                        Text(text = mesa.meseroAsignado, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                Text(
                    text = "Capacidad: ${mesa.capacidad} personas",
                    style = MaterialTheme.typography.bodySmall
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
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
