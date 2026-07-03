package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.restaurante.mesero.data.AppContainer

/**
 * Factory simple que construye cualquier ViewModel pasándole el AppContainer.
 * Evita boilerplate de una Factory por cada pantalla.
 */
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            TablesViewModel::class.java -> TablesViewModel(
                container.mesaRepository,
                container.pedidoRepository
            ) as T
            OrderViewModel::class.java -> OrderViewModel(
                container.mesaRepository,
                container.menuRepository,
                container.pedidoRepository,
                container.configuracionRepository
            ) as T
            MenuAdminViewModel::class.java -> MenuAdminViewModel(
                container.menuRepository
            ) as T
            BillViewModel::class.java -> BillViewModel(
                container.pedidoRepository,
                container.mesaRepository,
                container.configuracionRepository
            ) as T
            HistoryViewModel::class.java -> HistoryViewModel(
                container.pedidoRepository
            ) as T
            StatsViewModel::class.java -> StatsViewModel(
                container.pedidoRepository
            ) as T
            SettingsViewModel::class.java -> SettingsViewModel(
                container.configuracionRepository,
                container.mesaRepository
            ) as T
            WelcomeViewModel::class.java -> WelcomeViewModel(
                container.configuracionRepository
            ) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}
