
package com.oliverheld.fahrtenbuch.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.oliverheld.fahrtenbuch.data.Fahrt
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {
    fun create(context: Context, fahrten: List<Fahrt>): File {
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        var page = doc.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        var y = 60f

        fun drawText(text: String, size: Float = 12f, bold: Boolean = false) {
            paint.textSize = size
            paint.isFakeBoldText = bold
            if (y > 800) {
                doc.finishPage(page)
                page = doc.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }
            canvas.drawText(text, 40f, y, paint)
            y += size + 8
        }

        drawText("Fahrtenbuch - ${java.time.LocalDate.now()}", 18f, true)
        y += 10
        drawText("Anzahl Fahrten: ${fahrten.size} | Gesamt km: ${fahrten.sumOf { it.km }}", 11f)
        y += 15
        drawText("--------------------------------------------------------------------------------", 9f)
        
        fahrten.forEach { f ->
            drawText("${f.datum} | ${f.start} -> ${f.ziel} | ${f.km} km", 11f, true)
            drawText("Anlass: ${f.anlass} ${if(f.notiz.isNotBlank()) "- ${f.notiz}" else ""}", 10f)
            y += 5
        }

        doc.finishPage(page)
        val file = File(context.getExternalFilesDir(null), "Fahrtenbuch_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
