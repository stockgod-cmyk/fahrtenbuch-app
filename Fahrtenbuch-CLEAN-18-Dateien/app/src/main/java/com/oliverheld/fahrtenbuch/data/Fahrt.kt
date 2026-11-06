
package com.oliverheld.fahrtenbuch.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity
data class Fahrt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lfdNr: Int = 0,
    val datum: String,
    val quartal: Int,
    val jahr: Int,
    val start: String,
    val ziel: String,
    val startLat: Double? = null,
    val startLng: Double? = null,
    val zielLat: Double? = null,
    val zielLng: Double? = null,
    val anlass: String,
    val km: Double,
    val kmBerechnetPerGps: Boolean = false,
    val notiz: String = ""
)
