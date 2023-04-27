package cr.ac.una.gps.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cr.ac.una.gps.entity.Poligono


@Dao
interface PoligonoDao {
    @Insert
    fun insert(entity: Poligono)

    @Query("SELECT * FROM poligono")
    fun getAll(): List<Poligono?>?
}