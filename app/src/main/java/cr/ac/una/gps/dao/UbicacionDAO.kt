package cr.ac.una.gps.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cr.ac.una.gps.entity.Poligono
import cr.ac.una.gps.entity.Ubicacion
@Dao
interface UbicacionDao {
    @Insert
    fun insert(entity: Ubicacion)

    @Query("SELECT * FROM ubicacion")
    fun getAll(): List<Ubicacion?>?

    @Query("SELECT * FROM ubicacion WHERE fecha = :fecha")
    fun filtroPorFecha(fecha: Long): List<Ubicacion?>?
}