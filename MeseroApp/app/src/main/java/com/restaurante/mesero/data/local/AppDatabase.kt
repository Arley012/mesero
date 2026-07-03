package com.restaurante.mesero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.restaurante.mesero.data.local.converter.Converters
import com.restaurante.mesero.data.local.dao.CategoriaDao
import com.restaurante.mesero.data.local.dao.ConfiguracionDao
import com.restaurante.mesero.data.local.dao.ItemPedidoDao
import com.restaurante.mesero.data.local.dao.MesaDao
import com.restaurante.mesero.data.local.dao.PedidoDao
import com.restaurante.mesero.data.local.dao.ProductoDao
import com.restaurante.mesero.data.local.entity.CategoriaEntity
import com.restaurante.mesero.data.local.entity.ConfiguracionEntity
import com.restaurante.mesero.data.local.entity.ItemPedidoEntity
import com.restaurante.mesero.data.local.entity.MesaEntity
import com.restaurante.mesero.data.local.entity.PedidoEntity
import com.restaurante.mesero.data.local.entity.ProductoEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MesaEntity::class,
        CategoriaEntity::class,
        ProductoEntity::class,
        PedidoEntity::class,
        ItemPedidoEntity::class,
        ConfiguracionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mesaDao(): MesaDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun productoDao(): ProductoDao
    abstract fun pedidoDao(): PedidoDao
    abstract fun itemPedidoDao(): ItemPedidoDao
    abstract fun configuracionDao(): ConfiguracionDao

    companion object {
        const val DATABASE_NAME = "mesero_app.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context).also { INSTANCE = it }
            }
        }

        private fun build(context: Context): AppDatabase {
            val instanciaDeferida = kotlinx.coroutines.CompletableDeferred<AppDatabase>()
            val database = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(SeedCallback { instanciaDeferida.await() })
                .build()
            instanciaDeferida.complete(database)
            return database
        }
    }

    /**
     * Pobla datos iniciales (categorías y mesas de ejemplo) la primera vez que se crea la BD,
     * para que la app no abra completamente vacía.
     *
     * Recibe la instancia mediante una función suspendida diferida (en vez de llamar a
     * getInstance() de nuevo) para evitar un posible deadlock: onCreate() se dispara durante
     * la construcción del propio Room.databaseBuilder().build(), momento en el que INSTANCE
     * todavía no ha sido asignada. El CompletableDeferred garantiza visibilidad segura entre
     * hilos del valor una vez que build() termina de construir la base de datos.
     */
    private class SeedCallback(private val obtenerInstancia: suspend () -> AppDatabase) : RoomDatabase.Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val database = obtenerInstancia()
                seedCategoriasYProductos(database)
                seedMesas(database)
                seedConfiguracion(database)
            }
        }

        private suspend fun seedConfiguracion(db: AppDatabase) {
            db.configuracionDao().guardar(ConfiguracionEntity())
        }

        private suspend fun seedMesas(db: AppDatabase) {
            val mesaDao = db.mesaDao()
            val mesasIniciales = (1..8).map { numero ->
                MesaEntity(numero = numero, orden = numero)
            }
            mesasIniciales.forEach { mesaDao.insertar(it) }
        }

        private suspend fun seedCategoriasYProductos(db: AppDatabase) {
            val categoriaDao = db.categoriaDao()
            val productoDao = db.productoDao()

            val categorias = listOf(
                CategoriaEntity(nombre = "Entradas", orden = 1, icono = "Tapas"),
                CategoriaEntity(nombre = "Sopas", orden = 2, icono = "SoupKitchen"),
                CategoriaEntity(nombre = "Platos fuertes", orden = 3, icono = "DinnerDining"),
                CategoriaEntity(nombre = "Parrillas", orden = 4, icono = "OutdoorGrill"),
                CategoriaEntity(nombre = "Hamburguesas", orden = 5, icono = "LunchDining"),
                CategoriaEntity(nombre = "Pizzas", orden = 6, icono = "LocalPizza"),
                CategoriaEntity(nombre = "Bebidas", orden = 7, icono = "LocalBar"),
                CategoriaEntity(nombre = "Postres", orden = 8, icono = "Cake")
            )
            val ids = categorias.map { categoriaDao.insertar(it) }

            val productosEjemplo = listOf(
                ProductoEntity(nombre = "Tequeños", precio = 25.0, categoriaId = ids[0], descripcion = "8 unidades con salsa"),
                ProductoEntity(nombre = "Alitas BBQ", precio = 35.0, categoriaId = ids[0]),
                ProductoEntity(nombre = "Sopa de maní", precio = 20.0, categoriaId = ids[1]),
                ProductoEntity(nombre = "Crema de verduras", precio = 18.0, categoriaId = ids[1]),
                ProductoEntity(nombre = "Pique macho", precio = 45.0, categoriaId = ids[2]),
                ProductoEntity(nombre = "Silpancho", precio = 38.0, categoriaId = ids[2]),
                ProductoEntity(nombre = "Parrillada mixta", precio = 80.0, categoriaId = ids[3], descripcion = "Para 2 personas"),
                ProductoEntity(nombre = "Churrasco", precio = 55.0, categoriaId = ids[3]),
                ProductoEntity(nombre = "Hamburguesa clásica", precio = 30.0, categoriaId = ids[4]),
                ProductoEntity(nombre = "Hamburguesa doble queso", precio = 38.0, categoriaId = ids[4]),
                ProductoEntity(nombre = "Pizza margarita", precio = 40.0, categoriaId = ids[5]),
                ProductoEntity(nombre = "Pizza pepperoni", precio = 45.0, categoriaId = ids[5]),
                ProductoEntity(nombre = "Coca-Cola", precio = 8.0, categoriaId = ids[6]),
                ProductoEntity(nombre = "Limonada", precio = 10.0, categoriaId = ids[6]),
                ProductoEntity(nombre = "Cerveza", precio = 15.0, categoriaId = ids[6]),
                ProductoEntity(nombre = "Flan", precio = 12.0, categoriaId = ids[7]),
                ProductoEntity(nombre = "Helado", precio = 14.0, categoriaId = ids[7])
            )
            productoDao.insertarTodos(productosEjemplo)
        }
    }
}
