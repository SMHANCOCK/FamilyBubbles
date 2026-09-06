package com.familybubbles.widget.ui

import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun AppCompatActivity.applyFamilyBubblesSystemInsets(root: View) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT

    WindowCompat.getInsetsController(window, window.decorView).apply {
        isAppearanceLightStatusBars = true
        isAppearanceLightNavigationBars = true
    }

    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val safeInsets = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.setPadding(
            safeInsets.left,
            safeInsets.top,
            safeInsets.right,
            safeInsets.bottom
        )
        insets
    }
    ViewCompat.requestApplyInsets(root)
}
