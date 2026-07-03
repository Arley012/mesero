package com.restaurante.mesero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configuracion")
data class ConfiguracionEntity(
    @PrimaryKey
    val id: Int = 1, // fila única
    val nombreRestaurante: String = "Mi Restaurante",
    val logoUri: String? = null,
    val moneda: String = "Bs",
    val porcentajeImpuesto: Double = 0.0,
    val modoOscuro: Boolean = false,
    val nombreMeseroRecordado: String? = null
)
