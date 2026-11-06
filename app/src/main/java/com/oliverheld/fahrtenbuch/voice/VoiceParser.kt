package com.oliverheld.fahrtenbuch.voice
object VoiceParser {
    data class Parsed(val start: String = "", val ziel: String = "", val km: Double = 0.0, val anlass: String = "")
    fun parse(text: String): Parsed {
        var start = ""
        var ziel = ""
        var km = 0.0
        val vonNach = Regex("""von\s+(.+?)\s+nach\s+(.+?)(\s+\d+|\s|$)""", RegexOption.IGNORE_CASE)
        vonNach.find(text)?.let {
            start = it.groupValues[1].trim()
            ziel = it.groupValues[2].trim()
        }
        val kmRegex = Regex("""(\d+[.,]?\d*)\s*(km|kilometer)""", RegexOption.IGNORE_CASE)
        kmRegex.find(text)?.let {
            km = it.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0
        }
        return Parsed(start, ziel, km, text)
    }
}
