package com.restaurante.mesero.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import com.restaurante.mesero.data.local.entity.PedidoEntity
import com.restaurante.mesero.data.local.entity.nombreMesaVisible
import java.io.File
import java.io.FileOutputStream

/**
 * Genera un PDF simple de la cuenta (tamaño A4 escalado) y lo deja listo
 * para compartir con un Intent.ACTION_SEND vía FileProvider.
 */
object PdfCuentaGenerator {

    fun generar(
        context: Context,
        nombreRestaurante: String,
        pedido: PedidoEntity,
        items: List<ItemPedidoEntity>,
        moneda: String
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 a 72dpi
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintTitulo = Paint().apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintNormal = Paint().apply { textSize = 12f }
        val paintBold = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var y = 50f
        val xMargen = 40f

        canvas.drawText(nombreRestaurante, xMargen, y, paintTitulo)
        y += 30f
        canvas.drawText("Mesa: ${pedido.nombreMesaVisible}", xMargen, y, paintNormal)
        y += 18f
        canvas.drawText("Mesero: ${pedido.nombreMesero}", xMargen, y, paintNormal)
        y += 18f
        canvas.drawText("Fecha: ${Formato.fechaHora(pedido.fechaCierre ?: pedido.fechaApertura)}", xMargen, y, paintNormal)
        y += 30f

        canvas.drawLine(xMargen, y, 555f, y, paintNormal)
        y += 20f
        canvas.drawText("Producto", xMargen, y, paintBold)
        canvas.drawText("Cant.", 350f, y, paintBold)
        canvas.drawText("Subtotal", 450f, y, paintBold)
        y += 10f
        canvas.drawLine(xMargen, y, 555f, y, paintNormal)
        y += 20f

        items.forEach { item ->
            canvas.drawText(item.nombreProducto.take(35), xMargen, y, paintNormal)
            canvas.drawText("${item.cantidad}", 350f, y, paintNormal)
            canvas.drawText(Formato.moneda(item.subtotal, moneda), 450f, y, paintNormal)
            y += 18f
            if (!item.observaciones.isNullOrBlank()) {
                canvas.drawText("  Nota: ${item.observaciones}", xMargen, y, paintNormal)
                y += 16f
            }
        }

        y += 10f
        canvas.drawLine(xMargen, y, 555f, y, paintNormal)
        y += 24f
        canvas.drawText("Subtotal: ${Formato.moneda(pedido.subtotal, moneda)}", 350f, y, paintNormal)
        y += 18f
        if (pedido.impuesto > 0) {
            canvas.drawText("Impuesto: ${Formato.moneda(pedido.impuesto, moneda)}", 350f, y, paintNormal)
            y += 18f
        }
        if (pedido.propina > 0) {
            canvas.drawText("Propina: ${Formato.moneda(pedido.propina, moneda)}", 350f, y, paintNormal)
            y += 18f
        }
        canvas.drawText("TOTAL: ${Formato.moneda(pedido.total, moneda)}", 350f, y, paintTitulo)

        document.finishPage(page)

        val carpeta = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val archivo = File(carpeta, "cuenta_mesa${pedido.numeroMesa}_${pedido.id}.pdf")
        FileOutputStream(archivo).use { document.writeTo(it) }
        document.close()
        return archivo
    }

    fun obtenerUriParaCompartir(context: Context, archivo: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
}
