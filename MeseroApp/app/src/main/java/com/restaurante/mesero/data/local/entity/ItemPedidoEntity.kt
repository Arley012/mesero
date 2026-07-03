package com.restaurante.mesero.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items_pedido",
    foreignKeys = [
        ForeignKey(
            entity = PedidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pedidoId"), Index("productoId")]
)
data class ItemPedidoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val pedidoId: Long,
    val productoId: Long,
    val nombreProducto: String,     // copiado al momento de agregar, por si el producto cambia luego
    val precioUnitario: Double,     // precio al momento de agregar
    val cantidad: Int = 1,
    val observaciones: String? = null, // "Sin cebolla, Término medio" concatenado o JSON simple
    val creadoEn: Long = System.currentTimeMillis()
) {
    val subtotal: Double get() = precioUnitario * cantidad
}
