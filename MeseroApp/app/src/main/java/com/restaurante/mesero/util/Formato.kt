package com.restaurante.mesero.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Formato {

    fun moneda(valor: Double, simbolo: String = "Bs"): String {
        return "$simbolo ${String.format(Locale.getDefault(), "%.2f", valor)}"
    }

    fun fechaHora(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun fecha(millis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun hora(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    /** Devuelve el tiempo transcurrido desde [desde] en formato "Xh Ym" o "Ym". */
    fun tiempoTranscurrido(desde: Long, ahora: Long = System.currentTimeMillis()): String {
        val diffMillis = (ahora - desde).coerceAtLeast(0)
        val horas = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val minutos = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
        return if (horas > 0) "${horas}h ${minutos}m" else "${minutos}m"
    }
}
