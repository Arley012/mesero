package com.restaurante.mesero.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "productos",
    foreignKeys = [
        ForeignKey(
            entity = CategoriaEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoriaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoriaId")]
)
data class ProductoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val precio: Double,
    val categoriaId: Long?,
    val descripcion: String? = null,
    val disponible: Boolean = true,
    val esFavorito: Boolean = false,
    val orden: Int = 0,
    val vecesVendido: Int = 0   // contador acumulado para estadísticas de "más vendido"
)
