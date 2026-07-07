package com.restaurante.mesero.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.restaurante.mesero.data.local.entity.ConfiguracionEntity
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.repository.ConfiguracionRepository
import com.restaurante.mesero.data.repository.EstadisticasDia
import com.restaurante.mesero.data.repository.MesaRepository
import com.restaurante.mesero.data.repository.PedidoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val config: ConfiguracionEntity = ConfiguracionEntity(),
    val mesas: List<MesaEntity> = emptyList()
)

/** Resumen del turno activo, para mostrar antes de confirmarlo como cerrado. */
data class ResumenTurno(
    val inicio: Long,
    val stats: EstadisticasDia
)

class SettingsViewModel(
    private val configuracionRepository: ConfiguracionRepository,
    private val mesaRepository: MesaRepository,
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        configuracionRepository.observar(),
        mesaRepository.observarMesas()
    ) { config, mesas ->
        SettingsUiState(config = config, mesas = mesas)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun actualizarNombreRestaurante(nombre: String) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(nombreRestaurante = nombre))
        }
    }

    fun actualizarLogo(uri: String?) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(logoUri = uri))
        }
    }

    fun actualizarMoneda(moneda: String) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(moneda = moneda))
        }
    }

    fun actualizarImpuesto(porcentaje: Double) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(porcentajeImpuesto = porcentaje))
        }
    }

    fun alternarModoOscuro(activado: Boolean) {
        viewModelScope.launch {
            val actual = configuracionRepository.obtener()
            configuracionRepository.actualizar(actual.copy(modoOscuro = activado))
        }
    }

    fun agregarMesa(capacidad: Int = 4) {
        viewModelScope.launch { mesaRepository.agregarMesa(capacidad = capacidad) }
    }

    fun eliminarMesa(mesa: MesaEntity) {
        viewModelScope.launch { mesaRepository.eliminarMesa(mesa) }
    }

    /** Cambia el nombre del mesero guardado (el que se usa al iniciar turno). */
    fun cambiarNombreMesero(nombre: String) {
        viewModelScope.launch { configuracionRepository.guardarNombreMesero(nombre) }
    }

    /** Activa o desactiva un tipo de tarjeta en "Detalles rápidos" de Estadísticas. */
    fun alternarDetalleRapido(detalle: DetalleRapido, activado: Boolean) {
        viewModelScope.launch {
            val actuales = DetalleRapido.desdeCsv(configuracionRepository.obtener().detallesRapidos).toMutableList()
            if (activado) {
                if (detalle !in actuales) actuales.add(detalle)
            } else {
                actuales.remove(detalle)
            }
            // Siempre debe quedar al menos una tarjeta visible.
            val nuevaLista = actuales.ifEmpty { listOf(detalle) }
            configuracionRepository.guardarDetallesRapidos(nuevaLista.map { it.name })
        }
    }

    /** Trae el resumen del turno activo (si hay uno) para mostrarlo antes de cerrarlo. */
    fun obtenerResumenTurno(onListo: (ResumenTurno?) -> Unit) {
        viewModelScope.launch {
            val config = configuracionRepository.obtener()
            val inicio = config.turnoInicio
            if (inicio == null) {
                onListo(null)
            } else {
                val stats = pedidoRepository.estadisticasDelDia(inicio, System.currentTimeMillis())
                onListo(ResumenTurno(inicio, stats))
            }
        }
    }

    /** Cierra el turno activo (el próximo ingreso a la app pedirá "Iniciar turno" de nuevo). */
    fun cerrarTurno(onListo: () -> Unit) {
        viewModelScope.launch {
            configuracionRepository.cerrarTurno()
            onListo()
        }
    }
}
