package com.restaurante.mesero.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// MeseroApp — Sistema de color "Pastel Premium"
// Paleta suave, cálida y de alto contraste donde importa (texto, iconos),
// pensada para lecturas rápidas de pie en un restaurante sin perder elegancia.
// ============================================================================

// --- Neutros base ---
val GrisPerla = Color(0xFFF6F7F9)          // Fondo general de la app
val Blanco = Color(0xFFFFFFFF)             // Superficies (tarjetas, hojas, diálogos)
val GrisNiebla = Color(0xFFF1F2F5)         // Superficie secundaria / variantes sutiles
val GrafitoSuave = Color(0xFF23262B)       // Texto principal (negro cálido, no puro)
val GrisTexto = Color(0xFF6B7280)          // Texto secundario / etiquetas
val GrisLinea = Color(0xFFE7E9EE)          // Bordes y divisores, casi invisibles

// --- Azul cielo (acento principal / marca) ---
val AzulPastel = Color(0xFFDCEEFF)
val AzulProfundo = Color(0xFF3E7CB1)
val AzulTextoContenedor = Color(0xFF17436A)

// --- Verde menta (éxito / mesa libre) ---
val VerdeMenta = Color(0xFFDFF7EA)
val VerdeProfundo = Color(0xFF2F9E6E)
val VerdeTextoContenedor = Color(0xFF115C3E)

// --- Lavanda (secundario / acento decorativo) ---
val Lavanda = Color(0xFFEEE7FF)
val LavandaProfunda = Color(0xFF7C6FC4)
val LavandaTextoContenedor = Color(0xFF362B66)

// --- Rosa pastel (atención suave / esperando cuenta) ---
val RosaPastel = Color(0xFFFCE7EF)
val RosaProfundo = Color(0xFFC2477B)
val RosaTextoContenedor = Color(0xFF6B1D40)

// --- Amarillo crema (ocupada / advertencia suave) ---
val AmarilloCrema = Color(0xFFFFF6D6)
val AmarilloProfundo = Color(0xFFB98900)
val AmarilloTextoContenedor = Color(0xFF4A3800)

// --- Error (rojo suavizado, coherente con la paleta pastel) ---
val RojoPastel = Color(0xFFFCE4E4)
val RojoProfundo = Color(0xFFC1473F)

// ============================================================================
// Roles Material3 — Tema claro
// ============================================================================
val PrimaryLight = AzulProfundo
val OnPrimaryLight = Blanco
val PrimaryContainerLight = AzulPastel
val OnPrimaryContainerLight = AzulTextoContenedor

val SecondaryLight = LavandaProfunda
val OnSecondaryLight = Blanco
val SecondaryContainerLight = Lavanda
val OnSecondaryContainerLight = LavandaTextoContenedor

val TertiaryLight = VerdeProfundo
val OnTertiaryLight = Blanco
val TertiaryContainerLight = VerdeMenta
val OnTertiaryContainerLight = VerdeTextoContenedor

val BackgroundLight = GrisPerla
val OnBackgroundLight = GrafitoSuave
val SurfaceLight = Blanco
val OnSurfaceLight = GrafitoSuave
val SurfaceVariantLight = GrisNiebla
val OnSurfaceVariantLight = GrisTexto
val OutlineLight = GrisLinea
val OutlineVariantLight = GrisNiebla

val ErrorLight = RojoProfundo
val OnErrorLight = Blanco
val ErrorContainerLight = RojoPastel
val OnErrorContainerLight = Color(0xFF5C120E)

// Estados de mesa (colores semánticos, usados directo en las tarjetas)
val MesaLibre = VerdeProfundo
val MesaLibreContainer = VerdeMenta
val MesaOcupada = AmarilloProfundo
val MesaOcupadaContainer = AmarilloCrema
val MesaEsperandoPedido = AzulProfundo
val MesaEsperandoPedidoContainer = AzulPastel
val MesaEsperandoCuenta = RosaProfundo
val MesaEsperandoCuentaContainer = RosaPastel

// ============================================================================
// Roles Material3 — Tema oscuro
// (mismos acentos pastel llevados a superficies grafito, para mantener
// identidad de marca incluso de noche, sin saturar ni perder la calma)
// ============================================================================
val FondoOscuro = Color(0xFF15171A)
val SuperficieOscura = Color(0xFF1D1F23)
val SuperficieOscuraVariante = Color(0xFF2A2D32)
val TextoOscuroPrincipal = Color(0xFFECEDEF)
val TextoOscuroSecundario = Color(0xFFA6ACB5)
val LineaOscura = Color(0xFF34373D)

val PrimaryDark = Color(0xFF9CC6EA)
val OnPrimaryDark = Color(0xFF0B2338)
val PrimaryContainerDark = Color(0xFF244D6C)
val OnPrimaryContainerDark = AzulPastel

val SecondaryDark = Color(0xFFC9BFEF)
val OnSecondaryDark = Color(0xFF2C2352)
val SecondaryContainerDark = Color(0xFF453A7A)
val OnSecondaryContainerDark = Lavanda

val TertiaryDark = Color(0xFF8FD6B4)
val OnTertiaryDark = Color(0xFF06301F)
val TertiaryContainerDark = Color(0xFF1E5A40)
val OnTertiaryContainerDark = VerdeMenta

val BackgroundDark = FondoOscuro
val OnBackgroundDark = TextoOscuroPrincipal
val SurfaceDark = SuperficieOscura
val OnSurfaceDark = TextoOscuroPrincipal
val SurfaceVariantDark = SuperficieOscuraVariante
val OnSurfaceVariantDark = TextoOscuroSecundario
val OutlineDark = LineaOscura
val OutlineVariantDark = SuperficieOscuraVariante

val ErrorDark = Color(0xFFE6928C)
val OnErrorDark = Color(0xFF4A0E0A)
val ErrorContainerDark = Color(0xFF6B221D)
val OnErrorContainerDark = RojoPastel
