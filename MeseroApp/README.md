# Mesero App 🍽️

Aplicación Android nativa para meseros — gestión de mesas, pedidos y cuentas, **100% local**, sin conexión a Internet ni servidores externos.

## 🚀 Cómo abrir el proyecto

1. Abre **Android Studio** (versión Iguana / 2023.3.1 o más reciente recomendada).
2. Selecciona **Open** y elige la carpeta raíz `MeseroApp/` (la que contiene `settings.gradle.kts`).
3. Espera a que Android Studio sincronice Gradle automáticamente.
   - **Importante:** este proyecto fue generado fuera de Android Studio, así que el archivo
     `gradle/wrapper/gradle-wrapper.jar` (binario) no está incluido. Android Studio lo
     regenerará solo al detectar `gradle-wrapper.properties`; si no lo hace automáticamente,
     ejecuta desde el menú **File → Sync Project with Gradle Files**, o genera el wrapper
     manualmente con `gradle wrapper --gradle-version 8.7` si tienes Gradle instalado localmente.
4. Conecta un dispositivo Android (API 24+) o usa un emulador, y presiona **Run ▶**.

## 📋 Requisitos técnicos

- Android Studio Iguana o superior
- JDK 17+ (Android Studio lo trae integrado)
- Android SDK con compileSdk 34, minSdk 24
- Gradle 8.7 / Android Gradle Plugin 8.5.2 (definido en build.gradle.kts)

## 🧱 Arquitectura

- Kotlin + Jetpack Compose (Material Design 3)
- MVVM: ui/viewmodel conectado con data/repository conectado con data/local (Room/SQLite)
- Sin Hilt/Dagger: se usa un AppContainer (Service Locator manual) en
  data/AppContainer.kt para mantener el proyecto ligero y fácil de leer.
- Sin red: no hay Retrofit, OkHttp, ni llamadas a API REST. Toda la persistencia
  es local vía Room.

## 📂 Estructura del proyecto

app/src/main/java/com/restaurante/mesero/
- data/local         -> Room: entidades, DAOs, AppDatabase, converters
- data/repository    -> Lógica de negocio entre Room y ViewModels
- data/AppContainer.kt -> Inyección de dependencias manual
- ui/components      -> Composables reutilizables (tarjetas, diálogos, selectores)
- ui/navigation      -> NavHost y definición de rutas
- ui/screens         -> Una carpeta por pantalla (welcome, tables, order, bill, menu, history, stats, settings)
- ui/theme           -> Colores, tipografía y tema Material 3
- ui/viewmodel       -> Un ViewModel por pantalla + Factory manual
- util               -> Formato, generación de PDF, impresión Bluetooth, backup, exportación CSV
- MainActivity.kt
- MeseroApplication.kt

## ✅ Funcionalidades incluidas

- Pantalla de bienvenida con nombre de mesero recordado
- Gestión de mesas con estados visuales (Libre/Ocupada/Esperando pedido/Esperando cuenta), tiempo transcurrido, filtros
- Toma de pedidos: búsqueda, categorías, observaciones rápidas y personalizadas, botones +/- de cantidad
- Edición y eliminación de productos del pedido antes de cerrar la cuenta
- Menú editable: categorías y productos (crear, editar, eliminar), favoritos, disponibilidad
- Cuenta: propina (5%/10%/15%/personalizada), dividir entre N personas, compartir PDF,
  imprimir en impresora térmica Bluetooth (ESC/POS, 58mm y 80mm)
- Historial con filtro por fecha y exportación a CSV (compatible con Excel)
- Estadísticas del día: número de pedidos, ventas, producto más vendido, mesas atendidas, promedio por mesa
- Configuración: nombre del restaurante, moneda, impuesto, modo oscuro, gestión de mesas,
  backup/restauración de la base de datos

## ⚠️ Notas de implementación importantes

1. Permisos Bluetooth (Android 12+): la app solicita BLUETOOTH_CONNECT en tiempo de
   ejecución antes de listar impresoras emparejadas (ver BillScreen.kt). Empareja tu
   impresora térmica desde los ajustes de Bluetooth del sistema antes de usarla aquí.
2. Impresión térmica: usa comandos ESC/POS estándar sobre BluetoothSocket (perfil SPP),
   compatible con la mayoría de impresoras térmicas genéricas del mercado (Xprinter, Goojprt,
   etc.). No depende de ningún SDK propietario.
3. PDF y backups: se generan en el almacenamiento externo de la app (carpetas exports/ y
   backups/), completamente locales al dispositivo, sin conexión a Internet.
4. Sin compilación verificada por CI: este proyecto fue escrito y revisado manualmente,
   línea por línea, pero no pudo compilarse en un entorno con Android SDK real antes de la
   entrega. Se recomienda hacer un build completo (Run desde Android Studio o
   ./gradlew assembleDebug) y revisar cualquier advertencia menor del IDE antes de usarlo
   en producción.

## 🛠️ Personalización rápida

- Para cambiar los datos de ejemplo (mesas, categorías, productos iniciales), edita el
  SeedCallback en data/local/AppDatabase.kt.
- Para cambiar la paleta de colores, edita ui/theme/Color.kt.
