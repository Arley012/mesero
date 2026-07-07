package com.restaurante.mesero.util

import android.content.Context
import android.net.Uri
import com.restaurante.mesero.data.local.entity.CategoriaEntity
import com.restaurante.mesero.data.local.entity.ProductoEntity
import com.restaurante.mesero.data.repository.CategoriaImportada
import com.restaurante.mesero.data.repository.MenuImportado
import com.restaurante.mesero.data.repository.ProductoImportado
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Exporta/importa el menú (categorías + productos) como un archivo .json legible,
 * pensado para pasar el menú entre dispositivos o guardar una copia de respaldo.
 * Usa org.json (incluido en Android) para no depender de librerías externas.
 */
object ExportadorMenu {

    private const val VERSION_FORMATO = 1

    fun exportarArchivo(
        context: Context,
        categorias: List<CategoriaEntity>,
        productos: List<ProductoEntity>
    ): File {
        val categoriasPorId = categorias.associateBy { it.id }

        val json = JSONObject().apply {
            put("version", VERSION_FORMATO)
            put("categorias", JSONArray().apply {
                categorias.forEach { cat ->
                    put(JSONObject().apply {
                        put("nombre", cat.nombre)
                        put("icono", cat.icono)
                        put("orden", cat.orden)
                    })
                }
            })
            put("productos", JSONArray().apply {
                productos.forEach { prod ->
                    put(JSONObject().apply {
                        put("nombre", prod.nombre)
                        put("precio", prod.precio)
                        put("categoria", prod.categoriaId?.let { categoriasPorId[it]?.nombre })
                        put("descripcion", prod.descripcion)
                        put("disponible", prod.disponible)
                    })
                }
            })
        }

        val carpeta = File(context.getExternalFilesDir(null), "exports").apply { mkdirs() }
        val archivo = File(carpeta, "menu_${System.currentTimeMillis()}.json")
        archivo.writeText(json.toString(2))
        return archivo
    }

    fun importarDesdeUri(context: Context, uri: Uri): MenuImportado {
        val texto = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
            ?: throw IllegalArgumentException("No se pudo leer el archivo seleccionado.")

        val json = JSONObject(texto)

        val categorias = mutableListOf<CategoriaImportada>()
        json.optJSONArray("categorias")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val nombre = obj.optString("nombre").trim()
                if (nombre.isNotEmpty()) {
                    categorias.add(
                        CategoriaImportada(
                            nombre = nombre,
                            icono = obj.optString("icono", "Restaurant").ifBlank { "Restaurant" }
                        )
                    )
                }
            }
        }

        val productos = mutableListOf<ProductoImportado>()
        json.optJSONArray("productos")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val nombre = obj.optString("nombre").trim()
                if (nombre.isNotEmpty()) {
                    productos.add(
                        ProductoImportado(
                            nombre = nombre,
                            precio = obj.optDouble("precio", 0.0),
                            categoria = obj.optString("categoria", "").ifBlank { null },
                            descripcion = obj.optString("descripcion", "").ifBlank { null },
                            disponible = obj.optBoolean("disponible", true)
                        )
                    )
                }
            }
        }

        if (categorias.isEmpty() && productos.isEmpty()) {
            throw IllegalArgumentException("El archivo no tiene un formato de menú reconocible.")
        }

        return MenuImportado(categorias, productos)
    }
}
