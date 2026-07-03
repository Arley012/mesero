# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep data classes used by Room
-keepclassmembers class com.restaurante.mesero.data.local.entity.** { *; }
