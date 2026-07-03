package com.restaurante.mesero.ui.navigation

object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val MESAS = "mesas"
    const val PEDIDO = "pedido/{mesaId}"
    const val MENU_ADMIN = "menu_admin"
    const val CUENTA = "cuenta/{pedidoId}"
    const val HISTORIAL = "historial"
    const val HISTORIAL_DETALLE = "historial_detalle/{pedidoId}"
    const val ESTADISTICAS = "estadisticas"
    const val CONFIGURACION = "configuracion"

    fun pedido(mesaId: Long) = "pedido/$mesaId"
    fun cuenta(pedidoId: Long) = "cuenta/$pedidoId"
    fun historialDetalle(pedidoId: Long) = "historial_detalle/$pedidoId"
}
