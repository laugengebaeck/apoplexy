package de.lukasrost.apoplexy

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

//Einstellungen
class SettingsActivity : AppCompatActivity() {

    //Erstellung: lade das SettingsFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().replace(android.R.id.content,SettingsFragment()).commit()
    }
}
