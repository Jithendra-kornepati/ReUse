package uk.ac.tees.mad.reuse.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(list: List<String>?): String = list?.let { json.encodeToString(it) } ?: "[]"

    @TypeConverter
    fun toStringList(value: String?): List<String> = value?.let { json.decodeFromString(it) } ?: emptyList()
}
