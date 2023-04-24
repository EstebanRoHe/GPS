package cr.ac.una.gps.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


@Entity
data class Ubicacion(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val latitud: Double,
    val longitud: Double,
    val fecha: Date
)