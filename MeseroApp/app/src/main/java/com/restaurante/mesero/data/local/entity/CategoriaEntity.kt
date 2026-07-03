package com.restaurante.mesero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categorias")
data class CategoriaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,        // Entradas, Sopas, Platos fuertes, Parrillas, Hamburguesas, Pizzas, Bebidas, Postres
    val orden: Int = 0,
    val icono: String = "Restaurant" // nombre simbólico del icono Material
)
