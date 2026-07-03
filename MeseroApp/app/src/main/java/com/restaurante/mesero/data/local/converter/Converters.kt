package com.restaurante.mesero.data.local.converter

import androidx.room.TypeConverter
import com.restaurante.mesero.data.local.entity.EstadoMesa
import com.restaurante.mesero.data.local.entity.EstadoPedido

class Converters {

    @TypeConverter
    fun estadoMesaToString(estado: EstadoMesa): String = estado.name

    @TypeConverter
    fun stringToEstadoMesa(valor: String): EstadoMesa = EstadoMesa.valueOf(valor)

    @TypeConverter
    fun estadoPedidoToString(estado: EstadoPedido): String = estado.name

    @TypeConverter
    fun stringToEstadoPedido(valor: String): EstadoPedido = EstadoPedido.valueOf(valor)
}
