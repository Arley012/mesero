package com.restaurante.mesero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.restaurante.mesero.data.local.entity.ConfiguracionEntity
import com.restaurante.mesero.ui.navigation.MeseroNavGraph
import com.restaurante.mesero.ui.theme.MeseroAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MeseroApplication).container

        setContent {
            val configState by container.configuracionRepository.observar()
                .collectAsState(initial = ConfiguracionEntity())

            MeseroAppTheme(darkTheme = configState.modoOscuro) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MeseroNavGraph(container = container)
                }
            }
        }
    }
}
