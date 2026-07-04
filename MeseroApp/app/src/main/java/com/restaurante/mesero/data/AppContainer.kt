package com.restaurante.mesero.data

import android.content.Context
import com.restaurante.mesero.data.local.AppDatabase
import com.restaurante.mesero.data.repository.ConfiguracionRepository
import com.restaurante.mesero.data.repository.MenuRepository
import com.restaurante.mesero.data.repository.MesaRepository
import com.restaurante.mesero.data.repository.PedidoRepository

/**
 * Contenedor simple de dependencias. Se evita Hilt/Dagger para mantener el proyecto
 * ligero y fácil de compilar sin configuración adicional, cumpliendo igualmente MVVM.
 */
class AppContainer(context: Context) {

    private val database = AppDatabase.getInstance(context)

    val mesaRepository: MesaRepository by lazy {
        MesaRepository(database.mesaDao())
    }

    val menuRepository: MenuRepository by lazy {
        MenuRepository(database.categoriaDao(), database.productoDao())
    }

    val pedidoRepository: PedidoRepository by lazy {
        PedidoRepository(database.pedidoDao(), database.itemPedidoDao(), database.productoDao())
    }

    val configuracionRepository: ConfiguracionRepository by lazy {
        ConfiguracionRepository(database.configuracionDao())
    }

    companion object {
        @Volatile
        private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppContainer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
