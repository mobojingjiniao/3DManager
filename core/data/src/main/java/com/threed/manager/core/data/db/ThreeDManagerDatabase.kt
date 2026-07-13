package com.threed.manager.core.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import android.content.Context

/**
 * App-wide Room database. Single instance owned by ThreeDManagerDatabase.
 *
 * Entities: avatar, capture_session.
 * Future: edit_log, gaussian_group, splat_metadata.
 */
@Database(
    entities = [AvatarEntity::class, CaptureSessionEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ThreeDManagerDatabase : RoomDatabase() {
    abstract fun avatarDao(): AvatarDao
    abstract fun captureSessionDao(): CaptureSessionDao

    companion object {
        @Volatile private var instance: ThreeDManagerDatabase? = null

        fun get(context: Context): ThreeDManagerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ThreeDManagerDatabase::class.java,
                    "3dmanager.db",
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}

/** Type converters for primitives Room can't natively persist. */
class Converters {
    @TypeConverter fun stringFromList(list: List<String>?): String = list?.joinToString(",") ?: ""
    @TypeConverter fun listFromString(value: String?): List<String> =
        value?.takeIf { it.isNotEmpty() }?.split(",") ?: emptyList()
}