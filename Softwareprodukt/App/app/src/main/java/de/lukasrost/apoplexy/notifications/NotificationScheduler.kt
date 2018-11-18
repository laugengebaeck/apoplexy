package de.lukasrost.apoplexy.notifications

import android.content.Context
import android.preference.PreferenceManager
import java.util.*
import android.content.pm.PackageManager
import android.content.ComponentName
import android.app.AlarmManager
import android.content.Context.ALARM_SERVICE
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.app.NotificationManager
import android.support.v4.content.ContextCompat.getSystemService
import android.app.NotificationChannel
import android.os.Build
import android.support.v4.app.NotificationManagerCompat
import de.lukasrost.apoplexy.*

// Funktionen zur Benachrichtigungsverwaltung

// Benachrichtigungszeitpunkt neu setzen
fun setReminder(ctx: Context){
    val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

    // wenn Benachrichtigungen aktiviert
    if (prefs.getBoolean(PREFS_ALARM_ENABLED,false)) {

        // Zeit aus Einstellungen lesen
        val time = prefs.getString(PREFS_ALARM_TIME, "00:00")?.split(":")!!
        val hour = time[0].toInt()
        val minute = time[1].toInt()
        val calendar = Calendar.getInstance()
        val setcalendar = Calendar.getInstance()

        // Zeit setzen
        setcalendar.set(Calendar.HOUR_OF_DAY, hour)
        setcalendar.set(Calendar.MINUTE, minute)
        setcalendar.set(Calendar.SECOND, 0)

        // bisherige Reminder löschen
        cancelReminder(ctx)

        // wenn Zeit heute schon vergangen -> für morgen setzen
        if (setcalendar.before(calendar)) {
            setcalendar.add(Calendar.DATE, 1)
        }

        // Receiver des Alarms setzen
        val receiver = ComponentName(ctx, NotificationReceiver::class.java)
        ctx.packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
        val intent1 = Intent(ctx, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(ctx,
                DAILY_REMINDER_CODE, intent1,
                PendingIntent.FLAG_UPDATE_CURRENT)

        // Benachrichtigung dem AlarmManager übergeben
        val am = ctx.getSystemService(ALARM_SERVICE) as AlarmManager
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, setcalendar.timeInMillis,
                AlarmManager.INTERVAL_DAY, pendingIntent)
    } else {
        // wenn Benachrichtigung deaktiviert -> Erinnerung löschen
        cancelReminder(ctx)
    }
}

// Alarm des AlarmManagers mithilfe von *Magie* löschen
fun cancelReminder(context: Context){
    val receiver = ComponentName(context, NotificationReceiver::class.java)
    val pm = context.packageManager
    pm.setComponentEnabledSetting(receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP)

    val intent1 = Intent(context, NotificationReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context,
            DAILY_REMINDER_CODE, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
    val am = context.getSystemService(ALARM_SERVICE) as AlarmManager
    am.cancel(pendingIntent)
    pendingIntent.cancel()
}

// Benachrichtigung anzeigen
fun showNotification(context: Context){

    // Activity, die geöffnet werden soll
    val intent = Intent(context, HomeNavActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

    // Aussehen der Benachrichtigung
    val builder = NotificationCompat.Builder(context, DAILY_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_minigame_icon)
            .setContentTitle("Zeit zum Üben!")
            .setContentText("Hast du Lust, deine Schlaganfall-Übungen mit Apoplexy durchzuführen?")
            .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("Hast du Lust, deine Schlaganfall-Übungen mit Apoplexy durchzuführen?"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // Set the intent that will fire when the user taps the notification
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

    // Benachrichtigung senden
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(DAILY_REMINDER_CODE,builder.build())
}

// Benachrichtigungskanal auf unterstützten Geräten erstellen
fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Erinnerungen"
        val description = "Apoplexys Übungserinnerungen"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(DAILY_REMINDER_CHANNEL_ID, name, importance)
        channel.description = description

        val notificationManager = getSystemService(context,NotificationManager::class.java)
        notificationManager!!.createNotificationChannel(channel)
    }
}