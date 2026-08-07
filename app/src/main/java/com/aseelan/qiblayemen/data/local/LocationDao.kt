package com.aseelan.qiblayemen.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>)

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun count(): Int

    @Query("SELECT DISTINCT governorate FROM locations ORDER BY governorate")
    suspend fun getGovernorates(): List<String>

    @Query("SELECT * FROM locations WHERE governorate = :governorate ORDER BY district")
    suspend fun getDistrictsForGovernorate(governorate: String): List<LocationEntity>

    @Query("SELECT * FROM locations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): LocationEntity?

    @Query("SELECT * FROM locations")
    suspend fun getAll(): List<LocationEntity>
}
