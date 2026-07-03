package com.restaurante.mesero.util

import android.content.Context
import com.restaurante.mesero.data.local.AppDatabase
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Copia/restaura el archivo físico de la base de datos Room (SQLite).
 * Antes de restaurar es necesario cerrar todas las conexiones abiertas a la BD
 * (en la práctica, se recomienda reiniciar la app después de restaurar).
 */
object BackupManager {

    fun crearBackup(context: Context): File {
        val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
        val carpetaBackup = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(java.util.Date())
        val destino = File(carpetaBackup, "mesero_backup_$timestamp.db")
        dbFile.copyTo(destino, overwrite = true)

        // Copiar también los archivos -wal y -shm si existen (modo WAL de SQLite)
        listOf("-wal", "-shm").forEach { sufijo ->
            val extra = File(dbFile.absolutePath + sufijo)
            if (extra.exists()) {
                extra.copyTo(File(destino.absolutePath + sufijo), overwrite = true)
            }
        }
        return destino
    }

    /**
     * Restaura un backup. La app debe reiniciarse después de llamar esta función
     * para que Room vuelva a abrir el archivo restaurado desde cero.
     */
    fun restaurarBackup(context: Context, archivoBackup: File): Boolean {
        return try {
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            archivoBackup.copyTo(dbFile, overwrite = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun listarBackups(context: Context): List<File> {
        val carpeta = File(context.getExternalFilesDir(null), "backups")
        if (!carpeta.exists()) return emptyList()
        return carpeta.listFiles { f -> f.extension == "db" }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
