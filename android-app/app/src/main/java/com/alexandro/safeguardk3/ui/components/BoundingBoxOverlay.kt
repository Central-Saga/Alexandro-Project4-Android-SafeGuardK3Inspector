package com.alexandro.safeguardk3.ui.components

import android.graphics.*
import com.alexandro.safeguardk3.data.model.DetectionItem

/**
 * Utilitas menggambar kotak deteksi (Bounding Box) dan label objek pada gambar.
 * Menggunakan palet aksen K3 Safety Field Log:
 * - Pekerja: Navy K3 (#17324D)
 * - Helm: Hijau Aman (#198754)
 * - Rompi: Safety Amber (#E9A826)
 * - Masker: Sky Blue (#0284C7)
 * - Sarung Tangan: Teal (#0D9488)
 * - Sepatu Safety: Ungu (#7C3AED)
 */
object BoundingBoxOverlay {

    fun normalizeLabel(className: String): String {
        return when (className) {
            "Person" -> "Pekerja"
            "Hard_hat" -> "Helm Pengaman"
            "Vest" -> "Rompi Safety"
            "Mask" -> "Masker"
            "Gloves" -> "Sarung Tangan"
            "Safety_boots" -> "Sepatu Safety"
            else -> className
        }
    }

    fun getColorForClass(className: String): Int {
        return when (className) {
            "Person" -> Color.rgb(23, 50, 77)       // Navy K3
            "Hard_hat" -> Color.rgb(25, 135, 84)    // Hijau Aman
            "Vest" -> Color.rgb(233, 168, 38)      // Safety Amber
            "Mask" -> Color.rgb(2, 132, 199)       // Sky Blue
            "Gloves" -> Color.rgb(13, 148, 136)     // Teal
            "Safety_boots" -> Color.rgb(124, 58, 237) // Ungu
            else -> Color.rgb(107, 114, 128)        // Abu Sekunder
        }
    }

    fun draw(source: Bitmap, detections: List<DetectionItem>): Bitmap {
        val mutableBitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val strokeWidth = (mutableBitmap.width * 0.005f).coerceAtLeast(4f)
        val textSize = (mutableBitmap.width * 0.026f).coerceIn(22f, 44f)

        val paintBox = Paint().apply {
            style = Paint.Style.STROKE
            this.strokeWidth = strokeWidth
            isAntiAlias = true
        }

        val paintTextBg = Paint().apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val paintText = Paint().apply {
            color = Color.WHITE
            this.textSize = textSize
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        detections.forEach { det ->
            if (det.bbox.size >= 4) {
                val x1 = det.bbox[0]
                val y1 = det.bbox[1]
                val x2 = det.bbox[2]
                val y2 = det.bbox[3]

                val colorInt = getColorForClass(det.className)
                paintBox.color = colorInt
                paintTextBg.color = colorInt

                // Gambar bounding box
                canvas.drawRect(x1, y1, x2, y2, paintBox)

                // Terjemahkan nama kelas untuk label
                val displayName = normalizeLabel(det.className)
                val label = "$displayName ${(det.confidence * 100).toInt()}%"

                val textBounds = Rect()
                paintText.getTextBounds(label, 0, label.length, textBounds)
                val bgHeight = textBounds.height() + 14f
                val bgWidth = textBounds.width() + 16f

                // Gambar latar belakang teks
                canvas.drawRect(x1, (y1 - bgHeight).coerceAtLeast(0f), x1 + bgWidth, y1.coerceAtLeast(bgHeight), paintTextBg)
                // Gambar teks label
                canvas.drawText(label, x1 + 8f, y1.coerceAtLeast(bgHeight) - 8f, paintText)
            }
        }

        return mutableBitmap
    }
}
