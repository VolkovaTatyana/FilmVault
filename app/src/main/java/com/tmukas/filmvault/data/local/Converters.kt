package com.tmukas.filmvault.data.local

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromGenreIds(value: List<Int>): String = value.joinToString(",")

    @TypeConverter
    fun toGenreIds(value: String): List<Int> =
        if (value.isBlank()) emptyList() else value.split(",").mapNotNull { it.toIntOrNull() }
}