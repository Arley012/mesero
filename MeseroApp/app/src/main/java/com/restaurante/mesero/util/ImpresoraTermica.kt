package com.restaurante.mesero.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import com.restaurante.mesero.data.local.entity.PedidoEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.util.UUID

/**
 * Impresión térmica vía Bluetooth usando el protocolo ESC/POS, compatible con la
 * gran mayoría de impresoras térmicas de 58mm y 80mm (Xprinter, Goojprt, etc.)
 * que emulan un puerto serie SPP (Serial Port Profile).
 *
 * No requiere SDK propietario: usa BluetoothSocket estándar de Android.
 */
enum class AnchoPapel(val caracteresPorLinea: Int) {
    MM_58(32),
    MM_80(48)
}

object ImpresoraTermica {

    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Comandos ESC/POS básicos
    private val ESC = 0x1B
    private val GS = 0x1D

    private fun cmdInicializar() = byteArrayOf(ESC.toByte(), '@'.code.toByte())
    private fun cmdCentrar() = byteArrayOf(ESC.toByte(), 'a'.code.toByte(), 1)
    private fun cmdIzquierda() = byteArrayOf(ESC.toByte(), 'a'.code.toByte(), 0)
    private fun cmdNegritaOn() = byteArrayOf(ESC.toByte(), 'E'.code.toByte(), 1)
    private fun cmdNegritaOff() = byteArrayOf(ESC.toByte(), 'E'.code.toByte(), 0)
    private fun cmdDobleAltoOn() = byteArrayOf(GS.toByte(), '!'.code.toByte(), 0x11)
    private fun cmdDobleAltoOff() = byteArrayOf(GS.toByte(), '!'.code.toByte(), 0x00)
    private fun cmdCorte() = byteArrayOf(GS.toByte(), 'V'.code.toByte(), 1)
    private fun salto(n: Int = 1) = "\n".repeat(n).toByteArray()

    @SuppressLint("MissingPermission")
    suspend fun imprimirCuenta(
        dispositivo: BluetoothDevice,
        ancho: AnchoPapel,
        nombreRestaurante: String,
        pedido: PedidoEntity,
        items: List<ItemPedidoEntity>,
        moneda: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        var socket: BluetoothSocket? = null
        try {
            socket = dispositivo.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            val out: OutputStream = socket.outputStream

            val cols = ancho.caracteresPorLinea

            out.write(cmdInicializar())
            out.write(cmdCentrar())
            out.write(cmdDobleAltoOn())
            out.write(cmdNegritaOn())
            out.write(nombreRestaurante.toByteArray())
            out.write(salto())
            out.write(cmdDobleAltoOff())
            out.write(cmdNegritaOff())
            out.write("-".repeat(cols).toByteArray())
            out.write(salto())

            out.write(cmdIzquierda())
            out.write("Mesa: ${pedido.numeroMesa}".toByteArray())
            out.write(salto())
            out.write("Mesero: ${pedido.nombreMesero}".toByteArray())
            out.write(salto())
            out.write("Fecha: ${Formato.fechaHora(pedido.fechaCierre ?: pedido.fechaApertura)}".toByteArray())
            out.write(salto(2))

            items.forEach { item ->
                val linea = formatearLineaProducto(item, cols, moneda)
                out.write(linea.toByteArray())
                out.write(salto())
                if (!item.observaciones.isNullOrBlank()) {
                    out.write("  * ${item.observaciones}".toByteArray())
                    out.write(salto())
                }
            }

            out.write("-".repeat(cols).toByteArray())
            out.write(salto())
            out.write(formatearTotal("Subtotal", pedido.subtotal, cols, moneda).toByteArray())
            out.write(salto())
            if (pedido.impuesto > 0) {
                out.write(formatearTotal("Impuesto", pedido.impuesto, cols, moneda).toByteArray())
                out.write(salto())
            }
            if (pedido.propina > 0) {
                out.write(formatearTotal("Propina", pedido.propina, cols, moneda).toByteArray())
                out.write(salto())
            }
            out.write(cmdNegritaOn())
            out.write(formatearTotal("TOTAL", pedido.total, cols, moneda).toByteArray())
            out.write(cmdNegritaOff())
            out.write(salto(2))
            out.write(cmdCentrar())
            out.write("¡Gracias por su visita!".toByteArray())
            out.write(salto(3))
            out.write(cmdCorte())

            out.flush()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun formatearLineaProducto(item: ItemPedidoEntity, cols: Int, moneda: String): String {
        val precioTxt = Formato.moneda(item.subtotal, moneda)
        val nombreTxt = "${item.cantidad}x ${item.nombreProducto}"
        val espacios = (cols - nombreTxt.length - precioTxt.length).coerceAtLeast(1)
        return if (nombreTxt.length + precioTxt.length + 1 > cols) {
            nombreTxt.take(cols)
        } else {
            nombreTxt + " ".repeat(espacios) + precioTxt
        }
    }

    private fun formatearTotal(etiqueta: String, valor: Double, cols: Int, moneda: String): String {
        val valorTxt = Formato.moneda(valor, moneda)
        val espacios = (cols - etiqueta.length - valorTxt.length).coerceAtLeast(1)
        return etiqueta + " ".repeat(espacios) + valorTxt
    }

    @SuppressLint("MissingPermission")
    fun dispositivosEmparejados(adapter: BluetoothAdapter): List<BluetoothDevice> {
        return adapter.bondedDevices?.toList() ?: emptyList()
    }
}
