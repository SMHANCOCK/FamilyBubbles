package com.familybubbles.widget.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.appcompat.content.res.AppCompatResources
import com.familybubbles.widget.R
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

    /**
     * Creates the complete child-facing profile bubble as one bitmap so RemoteViews can
     * reliably reproduce the mock-up without unsupported custom views.
     */
    fun contactBubble(
        context: Context,
        photoPath: String?,
        initials: String,
        borderColor: Int,
        accentStyle: Int,
        size: Int = 320
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val scale = size / 320f

        val cx = 160f * scale
        val cy = 143f * scale
        val outerRadius = 101f * scale
        val innerRadius = 88f * scale

        drawAccentRays(canvas, borderColor, accentStyle, scale)

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x24000000 }
        canvas.drawCircle(cx, cy + 5f * scale, outerRadius + 5f * scale, shadowPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = borderColor }
        canvas.drawCircle(cx, cy, outerRadius, borderPaint)

        val raw = loadBitmap(photoPath, 640)
        if (raw != null) {
            val cropped = circleCrop(raw, (innerRadius * 2).toInt())
            canvas.drawBitmap(cropped, cx - innerRadius, cy - innerRadius, null)
        } else {
            val fill = lighten(borderColor, 0.73f)
            canvas.drawCircle(cx, cy, innerRadius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill })
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(31, 42, 68)
                textSize = 69f * scale
                textAlign = Paint.Align.CENTER
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            val label = initials.trim().take(1).uppercase().ifBlank { "?" }
            val baseline = cy - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(label, cx, baseline, textPaint)
        }

        val badgeCx = cx
        val badgeCy = 232f * scale
        val badgeRadius = 36f * scale
        canvas.drawCircle(
            badgeCx,
            badgeCy + 4f * scale,
            badgeRadius + 2f * scale,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x25000000 }
        )
        canvas.drawCircle(
            badgeCx,
            badgeCy,
            badgeRadius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        )

        AppCompatResources.getDrawable(context, R.drawable.ic_phone_green)?.let { drawable ->
            val half = (19f * scale).toInt()
            drawable.setBounds(
                badgeCx.toInt() - half,
                badgeCy.toInt() - half,
                badgeCx.toInt() + half,
                badgeCy.toInt() + half
            )
            drawable.draw(canvas)
        }

        return bitmap
    }

    private fun drawAccentRays(canvas: Canvas, color: Int, accentStyle: Int, scale: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            strokeWidth = 9f * scale
            strokeCap = Paint.Cap.ROUND
        }

        when (accentStyle % 3) {
            0 -> {
                canvas.drawLine(55f * scale, 52f * scale, 39f * scale, 38f * scale, paint)
                canvas.drawLine(73f * scale, 36f * scale, 66f * scale, 17f * scale, paint)
                canvas.drawLine(91f * scale, 34f * scale, 95f * scale, 14f * scale, paint)
            }
            1 -> {
                canvas.drawLine(137f * scale, 30f * scale, 132f * scale, 9f * scale, paint)
                canvas.drawLine(160f * scale, 25f * scale, 160f * scale, 3f * scale, paint)
                canvas.drawLine(183f * scale, 30f * scale, 190f * scale, 10f * scale, paint)
            }
            else -> {
                canvas.drawLine(229f * scale, 34f * scale, 225f * scale, 14f * scale, paint)
                canvas.drawLine(247f * scale, 37f * scale, 255f * scale, 18f * scale, paint)
                canvas.drawLine(264f * scale, 52f * scale, 281f * scale, 39f * scale, paint)
            }
        }
    }

    fun circleCrop(source: Bitmap, size: Int): Bitmap {
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val target = RectF(0f, 0f, size.toFloat(), size.toFloat())

        val srcRatio = source.width.toFloat() / source.height
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
            color = 0xFF1F2A44.toInt()
            textSize = size * 0.36f
            textAlign = Paint.Align.CENTER
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, bg)
        val baseline = size / 2f - (text.descent() + text.ascent()) / 2f
        canvas.drawText(initials.take(2).uppercase(), size / 2f, baseline, text)
        return bitmap
    }

    private fun lighten(color: Int, amount: Float): Int {
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.rgb(
            (r + (255 - r) * amount).toInt().coerceIn(0, 255),
            (g + (255 - g) * amount).toInt().coerceIn(0, 255),
            (b + (255 - b) * amount).toInt().coerceIn(0, 255)
        )
    }
}
