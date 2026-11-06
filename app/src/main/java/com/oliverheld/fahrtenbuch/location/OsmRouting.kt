
package com.oliverheld.fahrtenbuch.location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
object OsmRouting {
    data class RouteResult(val km: Double, val durationMin: Int, val source: String)
    suspend fun geocode(query: String): Ort? = withContext(Dispatchers.IO) {
        try {
            val q = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://nominatim.openstreetmap.org/search?q=$q&format=json&limit=1&countrycodes=de")
            val conn = url.openConnection()
            conn.setRequestProperty("User-Agent", "FahrtenbuchApp/3.0")
            val text = conn.getInputStream().bufferedReader().readText()
            val arr = org.json.JSONArray(text)
            if(arr.length()==0) return@withContext null
            val obj = arr.getJSONObject(0)
            return@withContext Ort(query, obj.getString("lat").toDouble(), obj.getString("lon").toDouble())
        } catch(e: Exception) { null }
    }
    suspend fun getRealDistanceKm(origin: Ort, destination: Ort): RouteResult? = withContext(Dispatchers.IO) {
        try {
            val urlStr = "https://router.project-osrm.org/route/v1/driving/${origin.lng},${origin.lat};${destination.lng},${destination.lat}?overview=false"
            val conn = URL(urlStr).openConnection()
            conn.setRequestProperty("User-Agent", "FahrtenbuchApp/3.0")
            val text = conn.getInputStream().bufferedReader().readText()
            val json = JSONObject(text)
            if(json.getString("code") != "Ok") return@withContext null
            val route = json.getJSONArray("routes").getJSONObject(0)
            return@withContext RouteResult(route.getDouble("distance")/1000.0, (route.getDouble("duration")/60).toInt(), "OSRM")
        } catch(e: Exception) { null }
    }
    suspend fun getRealDistanceByNames(startName: String, zielName: String): RouteResult? {
        val s = geocode(startName) ?: return null
        val z = geocode(zielName) ?: return null
        return getRealDistanceKm(s, z)
    }
}
