package de.lukasrost.apoplexy


import android.app.TimePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.widget.TimePicker
import android.widget.Toast
import de.lukasrost.apoplexy.notifications.setReminder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

//Einstellungsseite
class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, TimePickerDialog.OnTimeSetListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Einstellungen aus XML laden
        setPreferencesFromResource(R.xml.preferences,rootKey)
        // App-Version im Info-Bereich aktualisieren
        findPreference("pref_static_field_version").summary = BuildConfig.VERSION_NAME
        // Backup-Verzeichnis erstellen
        val direct = File("${Environment.getExternalStorageDirectory()}/ApoplexyBackup")
        if (!direct.exists()) {
            direct.mkdir()

        }
    }

    override fun onResume() {
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val buttonExport = findPreference("databaseExport")
        val buttonImport = findPreference("databaseImport")
        val alarmTime = findPreference("alarmTime")

        // Aktionen, die bei Klick auf Einstellung ausgeführt werden, steuern
        buttonExport.onPreferenceClickListener = Preference.OnPreferenceClickListener {exportDB()}
        buttonImport.onPreferenceClickListener = Preference.OnPreferenceClickListener {importDB()}
        alarmTime.onPreferenceClickListener = Preference.OnPreferenceClickListener { showTimePickerDialog() }
        super.onResume()
    }

    // Änderung der Benachrichtigungszeit in Einstellungen übernehmen
    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        preferenceManager.sharedPreferences.edit().putString("alarmTime","$p1:$p2").apply()
    }

    // Zeit-Auswahldialog anzeigen und mit bisheriger Einstellung befüllen
    private fun showTimePickerDialog() : Boolean{
        val time = preferenceManager.sharedPreferences.getString("alarmTime","18:00")!!.split(":")
        TimePickerDialog(activity,this,time[0].toInt(),time[1].toInt(),true).show()
        return true
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    // wenn Benachrichtigungseinstellungen geändert wurden -> Erinnerung neu erstellen
    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, changedPref: String?) {
        when(changedPref){
            PREFS_ALARM_ENABLED, PREFS_ALARM_TIME -> {
                setReminder(activity!!)
            }
        }
    }

    // Datenbank auf die SD-Karte in das Verzeichnis /ApoplexyBackup/ exportieren
    private fun exportDB() : Boolean{
        val sd = Environment.getExternalStorageDirectory()
        val data = Environment.getDataDirectory()

        if (sd.canWrite()) {
            val currentDBPath = ("//data//" + "de.lukasrost.apoplexy"
                    + "//databases//" + DATABASE_NAME)
            val backupDBPath = "/ApoplexyBackup/exported.db"
            val currentDB = File(data, currentDBPath)
            val backupDB = File(sd, backupDBPath)

            val src = FileInputStream(currentDB).channel
            val dst = FileOutputStream(backupDB).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()
            Toast.makeText(activity, "Datenbank exportiert!",
                    Toast.LENGTH_LONG).show()
        }
        return true
    }

    // Datenbank von der SD-Karte (Verzeichnis /ApoplexyBackup/) importieren
    private fun importDB() : Boolean{
        val sd = Environment.getExternalStorageDirectory()
        val data = Environment.getDataDirectory()

        if (sd.canWrite()) {
            val currentDBPath = ("//data//" + "de.lukasrost.apoplexy"
                    + "//databases//" + DATABASE_NAME)
            val backupDBPath = "/ApoplexyBackup/imported.db"
            val currentDB = File(data, currentDBPath)
            val backupDB = File(sd, backupDBPath)

            val src = FileInputStream(backupDB).channel
            val dst = FileOutputStream(currentDB).channel
            dst.transferFrom(src, 0, src.size())
            src.close()
            dst.close()
            Toast.makeText(activity, "Datenbank importiert!",
                    Toast.LENGTH_LONG).show()

        }
        return true
    }

}
