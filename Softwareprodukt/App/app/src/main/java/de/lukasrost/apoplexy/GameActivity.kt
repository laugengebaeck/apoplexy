package de.lukasrost.apoplexy

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import de.lukasrost.apoplexy.badges.BadgeDialogFragment
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.app_bar_game.*
import kotlinx.android.synthetic.main.content_game.*

// Bildschirmseite für das Minispiel
class GameActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, BadgeListener {
    // alle 500 Millisekunden Spiel-View neu zeichnen
    private var stopThread = false
    private val invalidateRunnable = Runnable {
        while (!stopThread){
            plane_game.invalidate()
            Thread.sleep(500)
        }
    }

    // für Verbindung mit Bluetooth-Service nötige Variablen
    private val bluetoothNoService = BluetoothNoService()
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var intenty : Intent

    // wenn HC-05 (Bluetooth-Gerät) entdeckt -> Verbindung herstellen
    private val discoveryReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (BluetoothDevice.ACTION_FOUND == intent?.action){
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                if (device.name == "HC-05"){
                    createNoService(device)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Benutzeroberfläche bereitstellen und Thread starten
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        registerReceiver(discoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        intenty = Intent(this,BluetoothNoService::class.java)

        nav_view.setNavigationItemSelectedListener(this)
        plane_game.setBadgeListener(this)

        val t = Thread(invalidateRunnable)
        t.start()
    }

    // Reaktion auf Drücken des Zurück-Knopfs
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // Reaktion auf Klick im Navigationsmenü
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Home-Seite
            R.id.nav_home -> {
                startActivity(Intent(this,HomeNavActivity::class.java))
            }
            // Übungen
            R.id.nav_exercise -> {
                startActivity(Intent(this,ExerciseActivity::class.java))
            }
            // Minispiel -> nichts tun
            R.id.nav_minigame -> {}
            // Einstellungen
            R.id.nav_settings -> {
                startActivity(Intent(this,SettingsActivity::class.java))
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // Bluetooth-Berechtigungen einfordern, danach Bluetooth einschalten
    override fun onResume() {
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
        } else {
            performBluetoothEnable()
        }
        super.onResume()
    }

    // wenn Berechtigung gewährt -> Bluetooth einschalten
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if ((requestCode == PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) &&
                (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
            performBluetoothEnable()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Bluetooth einschalten, wenn nicht schon geschehen
    private fun performBluetoothEnable(){
        val tempBTAdapter = BluetoothAdapter.getDefaultAdapter()
        if (tempBTAdapter != null){
            bluetoothAdapter = tempBTAdapter
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                performBluetoothSetup()
            }
        } else {
            // Fehlermeldung anzeigen, wenn kein Bluetooth vorhanden
            Toast.makeText(applicationContext,R.string.no_bluetooth, Toast.LENGTH_LONG).show()
        }
    }

    // wenn Bluetooth eingeschalten -> Gerät suchen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        if (requestCode == REQUEST_ENABLE_BT){
            when(resultCode){
                Activity.RESULT_OK -> {
                    performBluetoothSetup()
                }
                else -> {
                    // Fehlermeldung, wenn nicht eingeschalten
                    Toast.makeText(applicationContext,"Bluetooth wurde nicht aktiviert. Bitte App neu starten!", Toast.LENGTH_LONG).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    // Gerät unter gepairten Geräten finden
    private fun performBluetoothSetup(){
        val pairedDevices = bluetoothAdapter.bondedDevices
        var hc05Adress = ""
        var hc05 : BluetoothDevice? = null
        if (pairedDevices.size > 0){
            for (device in pairedDevices){
                if (device.name.contains("HC-05")){
                    hc05Adress = device.address
                    hc05 = device
                }
            }
        }
        // Gerät noch nicht gepairt -> Discovery starten
        if (hc05Adress == "") {
            bluetoothAdapter.startDiscovery()
        } else {
            // falls Device schon gepairt: Kontrolle zum Verbinden an Service übergeben
            createNoService(hc05!!)
        }
    }

    // Verbindung des Services herstellen und der Spiel-View übergeben
    fun createNoService(device: BluetoothDevice){
        bluetoothNoService.establishConnection(device,this)
        plane_game.setBluetoothNoService(bluetoothNoService)
    }

    override fun callback(view: View, fragment: BadgeDialogFragment?) {
        if(fragment != null) {
            supportFragmentManager.beginTransaction().add(fragment, "badgecompleted").addToBackStack(null).commit()
        }
    }

    // Aktualisierungs-Thread beenden usw.
    override fun onDestroy() {
        stopThread = true
        unregisterReceiver(discoveryReceiver)
        super.onDestroy()
    }
}
