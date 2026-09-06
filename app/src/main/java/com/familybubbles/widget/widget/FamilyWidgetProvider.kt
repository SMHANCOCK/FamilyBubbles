package com.familybubbles.widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.familybubbles.widget.R
import com.familybubbles.widget.data.FamilyRepository
import com.familybubbles.widget.ui.DirectCallActivity
import com.familybubbles.widget.ui.ImageUtils
import com.familybubbles.widget.ui.MainActivity

class FamilyWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId -> updateWidget(context, appWidgetManager, widgetId) }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId)
    }

    companion object {
        private const val MAX_PEOPLE = 6
        private const val PEOPLE_PER_ROW = 3

        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, FamilyWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val people = FamilyRepository(context).getPeople().take(MAX_PEOPLE)
            val root = RemoteViews(context.packageName, R.layout.widget_family)
            root.removeAllViews(R.id.faceContainer)

            val options = manager.getAppWidgetOptions(widgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 150)
            val twoRows = people.size > PEOPLE_PER_ROW

            val itemLayout = when {
                twoRows && minHeight < 185 -> R.layout.widget_person_tiny
                twoRows || minWidth < 285 -> R.layout.widget_person_compact
                else -> R.layout.widget_person
            }
            val renderSize = when (itemLayout) {
                R.layout.widget_person_tiny -> 240
                R.layout.widget_person_compact -> 280
                else -> 320
            }
            root.setTextViewTextSize(
                R.id.widgetTitle,
                TypedValue.COMPLEX_UNIT_SP,
                if (minWidth < 245) 16f else 20f
            )

            if (people.isEmpty()) {
                root.setViewVisibility(R.id.faceContainer, View.GONE)
                root.setViewVisibility(R.id.emptyState, View.VISIBLE)
                root.setOnClickPendingIntent(
                    R.id.emptyState,
                    PendingIntent.getActivity(
                        context,
                        widgetId,
                        Intent(context, MainActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
            } else {
                root.setViewVisibility(R.id.faceContainer, View.VISIBLE)
                root.setViewVisibility(R.id.emptyState, View.GONE)

                val palette = intArrayOf(
                    R.color.family_pink,
                    R.color.family_yellow,
                    R.color.family_blue,
                    R.color.family_green,
                    R.color.family_purple,
                    R.color.family_orange
                )

                people.chunked(PEOPLE_PER_ROW).forEachIndexed { rowIndex, rowPeople ->
                    val row = RemoteViews(context.packageName, R.layout.widget_row)
                    row.removeAllViews(R.id.rowContainer)

                    rowPeople.forEachIndexed { columnIndex, person ->
                        val absoluteIndex = rowIndex * PEOPLE_PER_ROW + columnIndex
                        val item = RemoteViews(context.packageName, itemLayout)
                        item.setTextViewText(R.id.personName, person.name)

                        val accentColor = ContextCompat.getColor(
                            context,
                            palette[absoluteIndex % palette.size]
                        )
                        val bubble = ImageUtils.contactBubble(
                            context = context,
                            photoPath = person.photoPath,
                            initials = person.name,
                            borderColor = accentColor,
                            accentStyle = absoluteIndex,
                            size = renderSize
                        )
                        item.setImageViewBitmap(R.id.personPhoto, bubble)

                        val requestCode = 31 * widgetId + person.id.hashCode()
                        val callIntent = Intent(context, DirectCallActivity::class.java).apply {
                            action = "com.familybubbles.widget.CALL.${person.id}"
                            putExtra(DirectCallActivity.EXTRA_PERSON_ID, person.id)
                        }
                        val pendingCall = PendingIntent.getActivity(
                            context,
                            requestCode,
                            callIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )

                        // The whole family tile is deliberately a large child-friendly target.
                        item.setOnClickPendingIntent(R.id.personRoot, pendingCall)
                        item.setOnClickPendingIntent(R.id.personPhoto, pendingCall)
                        item.setOnClickPendingIntent(R.id.personName, pendingCall)
                        row.addView(R.id.rowContainer, item)
                    }
                    root.addView(R.id.faceContainer, row)
                }
            }

            manager.updateAppWidget(widgetId, root)
        }
    }
}
