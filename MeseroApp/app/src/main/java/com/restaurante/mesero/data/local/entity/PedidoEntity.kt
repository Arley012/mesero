package com.restaurante.mesero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EstadoPedido {
    ABIERTO,    // en curso, mesa ocupada
    CERRADO,    // cuenta pagada, pasa a historial
    CANCELADO
}

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mesaId: Long,
    val numeroMesa: Int,            // se guarda redundante para historial aunque la mesa se borre
    val nombreMesa: String? = null, // nombre personalizado de la mesa al momento del pedido (si tenía)
    val nombreMesero: String,
    val fechaApertura: Long,        // epoch millis
    val fechaCierre: Long? = null,
    val estado: EstadoPedido = EstadoPedido.ABIERTO,
    val subtotal: Double = 0.0,
    val impuesto: Double = 0.0,
    val propina: Double = 0.0,
    val total: Double = 0.0,
    val notaGeneral: String? = null
)

/** Nombre a mostrar para este pedido: el nombre de mesa vigente cuando se creó, o "Mesa {N}". */
val PedidoEntity.nombreMesaVisible: String
    get() = nombreMesa?.takeIf { it.isNotBlank() } ?: "Mesa $numeroMesa"
