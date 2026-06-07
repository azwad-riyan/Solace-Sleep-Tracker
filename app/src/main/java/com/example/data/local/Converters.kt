package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.DetectionSensitivity
import com.example.domain.model.SessionSource
import com.example.domain.model.SessionType

class Converters {

    @TypeConverter
    fun fromDetectionSensitivity(value: DetectionSensitivity): String {
        return value.name
    }

    @TypeConverter
    fun toDetectionSensitivity(value: String): DetectionSensitivity {
        return DetectionSensitivity.valueOf(value)
    }

    @TypeConverter
    fun fromSessionType(value: SessionType): String {
        return value.name
    }

    @TypeConverter
    fun toSessionType(value: String): SessionType {
        return SessionType.valueOf(value)
    }

    @TypeConverter
    fun fromSessionSource(value: SessionSource): String {
        return value.name
    }

    @TypeConverter
    fun toSessionSource(value: String): SessionSource {
        return SessionSource.valueOf(value)
    }
}
