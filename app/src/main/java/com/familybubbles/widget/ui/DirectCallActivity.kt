package com.familybubbles.widget.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class DirectCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val phone = intent.getStringExtra(EXTRA_PHONE).orEmpty()
        if (phone.isBlank()) {
            finish()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Open FamilyBubbles and allow phone calls first.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
            return
        }

        runCatching {
            startActivity(
                Intent(Intent.ACTION_CALL).apply {
                    data = Uri.fromParts("tel", phone, null)
                }
            )
        }.onFailure {
            Toast.makeText(this, "Couldn't place the call on this device.", Toast.LENGTH_LONG).show()
        }
        finish()
    }

    companion object {
        const val EXTRA_PHONE = "phone"
    }
}
