package de.lukasrost.apoplexy

import android.Manifest
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ListView
import de.lukasrost.apoplexy.helpers.GamificationDBHelper
import de.lukasrost.apoplexy.badges.*
import de.lukasrost.apoplexy.notifications.createNotificationChannel
import kotlinx.android.synthetic.main.activity_home_nav.*
import kotlinx.android.synthetic.main.app_bar_home_nav.*
import kotlinx.android.synthetic.main.content_home_nav.*

// Hauptseite der App
class HomeNavActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var prefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // Benutzeroberfläche initialisieren
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_nav)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        nav_view.setNavigationItemSelectedListener(this)

        // Aktionen bei Button-Klick bestimmen
        button_exercise_start.setOnClickListener {startActivity(Intent(this,ExerciseActivity::class.java))}
        button_minigame_start.setOnClickListener {startActivity(Intent(this,GameActivity::class.java))}

        // XP anzeigen
        val text = "Hallo ${prefs.getString(PREFS_NAME,"")}, du hast ${prefs.getInt(PREFS_POINTS,0)} XP erreicht!"
        results_gamification.text = text

        // Badges anzeigen
        button_results_show.setOnClickListener {
            val listView = ListView(this)
            val ca = BadgeAdapter(this)
            val helper = GamificationDBHelper(this)
            ca.changeCursor(helper.getCompletedQuests())
            listView.adapter = ca
            val dialog = Dialog(this)
            dialog.setContentView(listView)
            dialog.show()
            helper.close()
        }

        // Benachrichtigungskanal erstellen
        createNotificationChannel(this)
    }

    // Berechtigung zum Schreibzugriff auf SD-Karte verlangen
    override fun onResume() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_EXT_STOR)
        }
        super.onResume()
    }

    // Reaktion bei Klicks auf Zurück-Taste
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            moveTaskToBack(true)
            super.onBackPressed()
        }
    }

    // Reaktion bei Klicks im Navigationsmenü
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Startseite -> nichts tun
            R.id.nav_home -> {}
            // Übungen
            R.id.nav_exercise -> {
                startActivity(Intent(this,ExerciseActivity::class.java))
            }
            // Minispiel
            R.id.nav_minigame -> {
                startActivity(Intent(this,GameActivity::class.java))
            }
            // Einstellungen
            R.id.nav_settings -> {
                startActivity(Intent(this,SettingsActivity::class.java))
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
