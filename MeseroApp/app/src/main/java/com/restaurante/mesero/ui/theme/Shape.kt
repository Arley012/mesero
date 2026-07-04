package com.restaurante.mesero.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================================
// MeseroApp — Escala de formas
// Bordes redondeados consistentes (16–24dp) para transmitir suavidad y
// modernidad sin caer en formas exageradamente circulares.
// ============================================================================
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),   // chips pequeños, badges
    small = RoundedCornerShape(14.dp),        // campos de texto, botones pequeños
    medium = RoundedCornerShape(18.dp),       // tarjetas estándar
    large = RoundedCornerShape(22.dp),        // tarjetas grandes, hojas inferiores
    extraLarge = RoundedCornerShape(26.dp)    // diálogos, contenedores destacados
)

// Radios sueltos para usos puntuales fuera del sistema de Shapes de M3
val RadioTarjeta = RoundedCornerShape(20.dp)
val RadioDialogo = RoundedCornerShape(24.dp)
val RadioChip = RoundedCornerShape(16.dp)
val RadioBoton = RoundedCornerShape(16.dp)
