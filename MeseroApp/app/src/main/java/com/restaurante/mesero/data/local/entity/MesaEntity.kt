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

/**
 * Nombre que debe mostrarse en toda la app para identificar esta mesa: el nombre
 * personalizado si el mesero lo asignó (ej. "P1", "INT 1"), o "Mesa {numero}" si no.
 * Úsese en todas las pantallas (tarjetas, pedido, cuenta, configuración) para que
 * el identificador sea siempre consistente.
 */
val MesaEntity.nombreVisible: String
    get() = nombre?.takeIf { it.isNotBlank() } ?: "Mesa $numero"
