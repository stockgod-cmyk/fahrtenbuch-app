
package com.oliverheld.fahrtenbuch.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface FahrtDao {
    @Query("SELECT * FROM Fahrt ORDER BY jahr DESC, lfdNr DESC")
    fun alle(): Flow<List<Fahrt>>
    @Query("SELECT * FROM Fahrt WHERE jahr = :jahr AND quartal = :quartal ORDER BY lfdNr ASC")
    suspend fun fuerQuartalSync(jahr: Int, quartal: Int): List<Fahrt>
    @Query("SELECT COALESCE(MAX(lfdNr),0) FROM Fahrt WHERE jahr = :jahr")
    suspend fun maxLfdNrFuerJahr(jahr: Int): Int
    @Insert
    suspend fun insert(f: Fahrt): Long
    @Delete
    suspend fun delete(f: Fahrt)
}
