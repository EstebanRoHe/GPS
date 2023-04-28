package cr.ac.una.gps.converter
/*
import androidx.room.TypeConverter
import java.util.*


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

}*/

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.*
class Converters {
    private val formato = SimpleDateFormat("dd/MM/yyyy")

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromFormattedDate(value: String?): Date? {
        return value?.let { formato.parse(it) }
    }

    @TypeConverter
    fun dateToFormattedDate(date: Date?): String? {
        return date?.let { formato.format(it) }
    }
}