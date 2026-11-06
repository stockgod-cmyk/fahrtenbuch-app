package com.oliverheld.fahrtenbuch.location
import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
data class Ort(val name: String, val lat: Double, val lng: Double)
class LocationHelper(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)
    @SuppressLint("MissingPermission")
    suspend fun currentOrt(): Ort? {
        try {
            val loc = client.lastLocation.await() ?: return null
            val url = java.net.URL("https://nominatim.openstreetmap.org/reverse?lat=${loc.latitude}&lon=${loc.longitude}&format=json")
            val conn = url.openConnection()
            conn.setRequestProperty("User-Agent", "FahrtenbuchApp/3.0")
            val text = conn.getInputStream().bufferedReader().readText()
            val json = org.json.JSONObject(text)
            val display = json.optString("display_name", "Aktueller Standort").split(",").take(3).joinToString(",")
            return Ort(display.ifBlank { "Aktueller Standort" }, loc.latitude, loc.longitude)
        } catch (e: Exception) {
            return null
        }
    }
}
