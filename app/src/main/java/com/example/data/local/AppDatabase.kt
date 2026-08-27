package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.ClaimEntity
import com.example.data.model.PolicyEntity
import com.example.data.model.ScannedItemEntity
import com.example.data.model.StationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PolicyDao {
    @Query("SELECT * FROM policies ORDER BY startTimeMillis DESC")
    fun getAllPolicies(): Flow<List<PolicyEntity>>

    @Query("SELECT * FROM policies WHERE status = 'ACTIVE' ORDER BY startTimeMillis DESC")
    fun getActivePolicies(): Flow<List<PolicyEntity>>

    @Query("SELECT * FROM policies WHERE policyId = :id LIMIT 1")
    suspend fun getPolicyById(id: String): PolicyEntity?

    @Query("SELECT * FROM policies WHERE policyNumber = :policyNumber LIMIT 1")
    suspend fun getPolicyByNumber(policyNumber: String): PolicyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicy(policy: PolicyEntity)

    @Update
    suspend fun updatePolicy(policy: PolicyEntity)

    @Query("DELETE FROM policies WHERE policyId = :id")
    suspend fun deletePolicy(id: String)
}

@Dao
interface ScanDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScannedItemEntity>>

    @Query("SELECT * FROM scan_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteScans(): Flow<List<ScannedItemEntity>>

    @Query("SELECT * FROM scan_history WHERE id = :id LIMIT 1")
    suspend fun getScanById(id: String): ScannedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(item: ScannedItemEntity)

    @Update
    suspend fun updateScan(item: ScannedItemEntity)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteScan(id: String)

    @Query("DELETE FROM scan_history")
    suspend fun clearHistory()
}

@Dao
interface StationDao {
    @Query("SELECT * FROM stations ORDER BY name ASC")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE code = :code LIMIT 1")
    suspend fun getStationByCode(code: String): StationEntity?

    @Query("SELECT * FROM stations WHERE line = :line ORDER BY name ASC")
    fun getStationsByLine(line: String): Flow<List<StationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStations(stations: List<StationEntity>)
}

@Dao
interface ClaimDao {
    @Query("SELECT * FROM claims ORDER BY timestamp DESC")
    fun getAllClaims(): Flow<List<ClaimEntity>>

    @Query("SELECT * FROM claims WHERE policyId = :policyId")
    fun getClaimsForPolicy(policyId: String): Flow<List<ClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaim(claim: ClaimEntity)
}

@Database(
    entities = [
        PolicyEntity::class,
        ScannedItemEntity::class,
        StationEntity::class,
        ClaimEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun policyDao(): PolicyDao
    abstract fun scanDao(): ScanDao
    abstract fun stationDao(): StationDao
    abstract fun claimDao(): ClaimDao
}
