package com.threed.manager.core.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AvatarDao {
    @Query("SELECT * FROM avatars ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<AvatarEntity>>

    @Query("SELECT * FROM avatars WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): AvatarEntity?

    @Query("SELECT * FROM avatars WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<AvatarEntity?>

    @Query("SELECT * FROM avatars WHERE favorite = 1 ORDER BY updatedAt DESC")
    fun observeFavorites(): Flow<List<AvatarEntity>>

    @Query("SELECT * FROM avatars WHERE locationLat IS NOT NULL AND locationLng IS NOT NULL")
    fun observePinned(): Flow<List<AvatarEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(avatar: AvatarEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(avatars: List<AvatarEntity>)

    @Update
    suspend fun update(avatar: AvatarEntity)

    @Query("UPDATE avatars SET favorite = NOT favorite, updatedAt = :now WHERE id = :id")
    suspend fun toggleFavorite(id: String, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM avatars WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM avatars")
    suspend fun count(): Int
}

@Dao
interface CaptureSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: CaptureSessionEntity)

    @Update
    suspend fun update(session: CaptureSessionEntity)

    @Query("SELECT * FROM capture_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CaptureSessionEntity?

    @Query("SELECT * FROM capture_sessions WHERE id = :id LIMIT 1")
    fun observe(id: String): Flow<CaptureSessionEntity?>

    @Query("UPDATE capture_sessions SET framePaths = :paths, progress = :progress, state = :state WHERE id = :id")
    suspend fun updateFrames(id: String, paths: String, progress: Float, state: String)
}