package com.familybubbles.widget.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.familybubbles.widget.data.FamilyRepository

class DirectCallActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The widget now passes only the internal person ID. The phone number stays in
        // encrypted app storage rather than being embedded inside a launcher PendingIntent.
        val personId = intent.getStringExtra(EXTRA_PERSON_ID)
        val savedPhone = personId?.let { FamilyRepository(this).getPerson(it)?.phone }
        val legacyPhone = intent.getStringExtra(EXTRA_PHONE)
        val phone = normalizeForDial(savedPhone ?: legacyPhone.orEmpty())

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
            Toast.makeText(this, "Couldn\'t place the call on this device.", Toast.LENGTH_LONG).show()
        }
        finish()
    }

    private fun normalizeForDial(raw: String): String = buildString {
        raw.forEach { char ->
            if (char.isDigit() || char == '+' || char == '*' || char == '#' || char == ',' || char == ';') {
                append(char)
            }
        }
    }

    companion object {
        const val EXTRA_PERSON_ID = "person_id"
        const val EXTRA_PHONE = "phone" // Kept only for backwards compatibility with old widget PendingIntents.
    }
}
