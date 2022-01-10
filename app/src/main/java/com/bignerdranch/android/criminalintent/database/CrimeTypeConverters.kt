package com.bignerdranch.android.criminalintent.database

import androidx.room.TypeConverter
import java.util.*

class CrimeTypeConverters {
    @TypeConverter
    fun fromDate(date: Date?):Long?{
        return date?.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?):Date?{
        return millisSinceEpoch?.let{
            Date(it)
        }
    }

    @TypeConverter
    fun fromUUID(uuid: String?):UUID?{
        return UUID.fromString(uuid)
    }

    @TypeConverter
    fun toUUID(uuid: UUID?):String?{
        return uuid?.toString()
    }

}