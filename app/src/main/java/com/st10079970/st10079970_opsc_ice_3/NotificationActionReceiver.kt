package com.st10079970.st10079970_opsc_ice_3

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.content.ClipData
import android.content.ClipboardManager

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "COPY_TOKEN") {
            val token = intent.getStringExtra("token")
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Firebase Token", token)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}