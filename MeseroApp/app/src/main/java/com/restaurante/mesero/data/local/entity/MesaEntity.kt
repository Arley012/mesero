package com.restaurante.mesero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Estado de una mesa dentro del salón.
 */
enum class EstadoMesa {
    LIBRE,              // 🟢 Libre
    OCUPADA,            // 🟡 Ocupada
    ESPERANDO_PEDIDO,   // 🔵 Esperando pedido (cliente sentado, sin pedir aún)
    ESPERANDO_CUENTA    // 🟠 Esperando cuenta
}

@Entity(tableName = "mesas")
data class MesaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numero: Int,
    val nombre: String? = null,        // Opcional: "Terraza 1", "VIP", etc.
    val estado: EstadoMesa = EstadoMesa.LIBRE,
    val horaApertura: Long? = null,    // timestamp epoch millis, null si está libre
    val meseroAsignado: String? = null,
    val capacidad: Int = 4,
    val orden: Int = 0                 // para reordenar tarjetas manualmente
)
