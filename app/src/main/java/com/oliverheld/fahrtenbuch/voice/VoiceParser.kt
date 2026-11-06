
package com.oliverheld.fahrtenbuch.voice

// Simple heuristic parser for German speech
// Example input: "Heute von München nach Augsburg 45 Kilometer Kundentermin"

object VoiceParser {
    data class Parsed(val start: String = "", val ziel: String = "", val km: Double = 0.0, val anlass: String = "", val notiz: String = "")

    fun parse(text: String): Parsed {
        val lower = text.lowercase()
        var start = ""
        var ziel = ""
        var km = 0.0
        var anlass = text

        // Regex: von X nach Y
        val vonNach = Regex("von\s+(.+?)\s+nach\s+(.+?)(\s+\d+|\s|$)", RegexOption.IGNORE_CASE)
        vonNach.find(text)?.let {
            start = it.groupValues[1].trim()
            ziel = it.groupValues[2].trim()
        }

        // km
        val kmRegex = Regex("(\d+[.,]?\d*)\s*(km|kilometer)", RegexOption.IGNORE_CASE)
        kmRegex.find(text)?.let {
            km = it.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0
        }

        // Anlass = rest without von/nach/km part
        return Parsed(start, ziel, km, anlass, "")
    }
}
