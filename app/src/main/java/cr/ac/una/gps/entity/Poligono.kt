package cr.ac.una.gps.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Poligono (
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val latitud: Double,
    val longitud: Double
    )
