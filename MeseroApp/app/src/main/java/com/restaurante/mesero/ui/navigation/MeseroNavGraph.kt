package com.restaurante.mesero.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.restaurante.mesero.data.AppContainer
import com.restaurante.mesero.ui.screens.bill.BillScreen
import com.restaurante.mesero.ui.screens.history.HistoryScreen
import com.restaurante.mesero.ui.screens.menu.MenuAdminScreen
import com.restaurante.mesero.ui.screens.order.OrderScreen
import com.restaurante.mesero.ui.screens.settings.SettingsScreen
import com.restaurante.mesero.ui.screens.stats.StatsScreen
import com.restaurante.mesero.ui.screens.tables.TablesScreen
import com.restaurante.mesero.ui.screens.welcome.WelcomeScreen
import com.restaurante.mesero.ui.viewmodel.ViewModelFactory

/** Duración estándar de transición: fluida y perceptible sin sentirse lenta. */
private const val DURACION_TRANSICION_MS = 280

@Composable
fun MeseroNavGraph(
    container: AppContainer,
    navController: NavHostController = rememberNavController()
) {
    val factory = ViewModelFactory(container)
    val configState by container.configuracionRepository.observar().collectAsState(
        initial = com.restaurante.mesero.data.local.entity.ConfiguracionEntity()
    )

    NavHost(
        navController = navController,
        startDestination = Rutas.BIENVENIDA,
        enterTransition = {
            slideInHorizontally(
                animationSpec = tween(DURACION_TRANSICION_MS), initialOffsetX = { it / 6 }
            ) + fadeIn(animationSpec = tween(DURACION_TRANSICION_MS))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(DURACION_TRANSICION_MS))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(DURACION_TRANSICION_MS))
        },
        popExitTransition = {
            slideOutHorizontally(
                animationSpec = tween(DURACION_TRANSICION_MS), targetOffsetX = { it / 6 }
            ) + fadeOut(animationSpec = tween(DURACION_TRANSICION_MS))
        }
    ) {

        composable(Rutas.BIENVENIDA) {
            WelcomeScreen(
                factory = factory,
                onContinuar = { nombreMesero ->
                    val nombreCodificado = android.net.Uri.encode(nombreMesero)
                    navController.navigate("${Rutas.MESAS}?mesero=$nombreCodificado") {
                        popUpTo(Rutas.BIENVENIDA) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Rutas.MESAS}?mesero={mesero}",
            arguments = listOf(navArgument("mesero") { type = NavType.StringType; defaultValue = "Mesero" })
        ) { backStackEntry ->
            val nombreMesero = backStackEntry.arguments?.getString("mesero") ?: "Mesero"
            TablesScreen(
                factory = factory,
                nombreMesero = nombreMesero,
                moneda = configState.moneda,
                onAbrirMesa = { mesaId ->
                    val nombreCodificado = android.net.Uri.encode(nombreMesero)
                    navController.navigate(Rutas.pedido(mesaId) + "?mesero=$nombreCodificado")
                },
                onIrAMenuAdmin = { navController.navigate(Rutas.MENU_ADMIN) },
                onIrAHistorial = { navController.navigate(Rutas.HISTORIAL) },
                onIrAEstadisticas = { navController.navigate(Rutas.ESTADISTICAS) },
                onIrAConfiguracion = { navController.navigate(Rutas.CONFIGURACION) }
            )
        }

        composable(
            route = "${Rutas.PEDIDO}?mesero={mesero}",
            arguments = listOf(
                navArgument("mesaId") { type = NavType.LongType },
                navArgument("mesero") { type = NavType.StringType; defaultValue = "Mesero" }
            )
        ) { backStackEntry ->
            val mesaId = backStackEntry.arguments?.getLong("mesaId") ?: -1L
            val nombreMesero = backStackEntry.arguments?.getString("mesero") ?: "Mesero"
            OrderScreen(
                factory = factory,
                mesaId = mesaId,
                nombreMesero = nombreMesero,
                onVolver = { navController.popBackStack() },
                onIrACuenta = { pedidoId ->
                    navController.navigate(Rutas.cuenta(pedidoId))
                }
            )
        }

        composable(
            route = Rutas.CUENTA,
            arguments = listOf(navArgument("pedidoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val pedidoId = backStackEntry.arguments?.getLong("pedidoId") ?: -1L
            BillScreen(
                factory = factory,
                pedidoId = pedidoId,
                nombreRestaurante = configState.nombreRestaurante,
                onVolver = { navController.popBackStack() },
                onCuentaCerrada = {
                    navController.popBackStack(route = "${Rutas.MESAS}?mesero={mesero}", inclusive = false)
                }
            )
        }

        composable(Rutas.MENU_ADMIN) {
            MenuAdminScreen(
                factory = factory,
                moneda = configState.moneda,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.HISTORIAL) {
            HistoryScreen(
                factory = factory,
                moneda = configState.moneda,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.ESTADISTICAS) {
            StatsScreen(
                factory = factory,
                moneda = configState.moneda,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.CONFIGURACION) {
            SettingsScreen(
                factory = factory,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
