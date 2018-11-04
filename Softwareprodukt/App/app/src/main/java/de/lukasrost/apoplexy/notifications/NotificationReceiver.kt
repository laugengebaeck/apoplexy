package de.lukasrost.apoplexy.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// wird gestartet, wenn AlarmManager des Systems Erinnerung sendet
class NotificationReceiver : BroadcastReceiver() {

    // Benachrichtigung zeigen
    override fun onReceive(context: Context, intent: Intent) {
        showNotification(context)
    }
}
