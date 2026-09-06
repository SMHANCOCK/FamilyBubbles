package com.familybubbles.widget.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import java.io.File

object ImageUtils {
    fun loadBitmap(path: String?, maxSize: Int = 512): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists()) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / sample > maxSize * 2 || bounds.outHeight / sample > maxSize * 2) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeFile(path, options)
    }

    fun circleCrop(source: Bitmap, size: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val srcRatio = source.width.toFloat() / source.height
        val target = RectF(0f, 0f, size.toFloat(), size.toFloat())
        val srcRect = if (srcRatio > 1f) {
            val cropWidth = source.height
            val left = (source.width - cropWidth) / 2
            Rect(left, 0, left + cropWidth, source.height)
        } else {
            val cropHeight = source.width
            val top = (source.height - cropHeight) / 2
            Rect(0, top, source.width, top + cropHeight)
        }

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(source, srcRect, target, paint)
        paint.xfermode = null
        return output
    }

    fun placeholder(size: Int, initials: String): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFDDE7F2.toInt() }
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2364AA.toInt()
            textSize = size * 0.36f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bg)
        val baseline = size / 2f - (text.descent() + text.ascent()) / 2f
        canvas.drawText(initials.take(2).uppercase(), size / 2f, baseline, text)
        return bitmap
    }
}
