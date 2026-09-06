package com.familybubbles.widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.familybubbles.widget.R
import com.familybubbles.widget.data.FamilyRepository
import com.familybubbles.widget.ui.DirectCallActivity
import com.familybubbles.widget.ui.ImageUtils
import com.familybubbles.widget.ui.MainActivity

class FamilyWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId -> updateWidget(context, appWidgetManager, widgetId) }
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

            if (people.isEmpty()) {
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
                root.setViewVisibility(R.id.emptyState, View.GONE)
                people.chunked(PEOPLE_PER_ROW).forEachIndexed { rowIndex, rowPeople ->
                    val row = RemoteViews(context.packageName, R.layout.widget_row)
                    row.removeAllViews(R.id.rowContainer)

                    rowPeople.forEachIndexed { columnIndex, person ->
                        val item = RemoteViews(context.packageName, R.layout.widget_person)
                        item.setTextViewText(R.id.personName, person.name)
                        val raw = ImageUtils.loadBitmap(person.photoPath, 256)
                        val face = if (raw != null) {
                            ImageUtils.circleCrop(raw, 220)
                        } else {
                            ImageUtils.placeholder(220, person.name.take(2))
                        }
                        item.setImageViewBitmap(R.id.personPhoto, face)

                        val requestCode = widgetId * 100 + rowIndex * 10 + columnIndex
                        val callIntent = Intent(context, DirectCallActivity::class.java).apply {
                            putExtra(DirectCallActivity.EXTRA_PHONE, person.phone)
                            putExtra("person_id", person.id)
                        }
                        val pendingCall = PendingIntent.getActivity(
                            context,
                            requestCode,
                            callIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
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
