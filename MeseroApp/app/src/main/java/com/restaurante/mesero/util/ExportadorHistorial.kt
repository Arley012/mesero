package com.restaurante.mesero.util

import android.content.Context
import com.restaurante.mesero.data.local.entity.PedidoEntity
import com.restaurante.mesero.data.local.entity.nombreMesaVisible
import java.io.File
import java.io.FileWriter

/**
 * Exporta el historial a CSV. Los archivos .csv se abren de forma nativa en Excel,
 * Google Sheets y cualquier hoja de cálculo, sin depender de librerías externas.
 */
object ExportadorHistorial {

    fun exportarCsv(context: Context, pedidos: List<PedidoEntity>, moneda: String): File {
        val carpeta = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val archivo = File(carpeta, "historial_${System.currentTimeMillis()}.csv")

        FileWriter(archivo).use { writer ->
            writer.append("Fecha,Mesa,Mesero,Subtotal,Impuesto,Propina,Total,Estado\n")
            pedidos.forEach { p ->
                val fecha = Formato.fechaHora(p.fechaCierre ?: p.fechaApertura)
                writer.append("$fecha,${p.nombreMesaVisible},${p.nombreMesero},")
                writer.append("${p.subtotal},${p.impuesto},${p.propina},${p.total},${p.estado}\n")
            }
        }
        return archivo
    }
}
