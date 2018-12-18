package de.lukasrost.apoplexy

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

import de.lukasrost.apoplexy.helpers.GamificationDBHelper
import de.lukasrost.apoplexy.helpers.GamificationGraderHelper
import de.lukasrost.apoplexy.badges.BadgeAdapter

import kotlinx.android.synthetic.main.activity_exercise.*
import kotlinx.android.synthetic.main.app_bar_exercise.*
import kotlinx.android.synthetic.main.content_exercise.*

class ExerciseActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    //für Verbindung mit Bluetooth-Service nötige Variablen
    private var exerciseRunning = false
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
    private lateinit var prefs : SharedPreferences
    private lateinit var graderHelper : GamificationGraderHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        // Layout und Navigationsmenü
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)
        setSupportActionBar(toolbar)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        graderHelper = GamificationGraderHelper(this)

        intenty = Intent(this,BluetoothNoService::class.java)
        registerReceiver(discoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        // Floating-Action-Buttons rechts unten
        // 1) Start und Stop
        fab_start_stop.setOnClickListener { startExercise() }
        // 2) Quest-Anzeige
        fab_quests.setOnClickListener {
            val listView = ListView(this)
            val ca = BadgeAdapter(this)
            val helper = GamificationDBHelper(this)
            ca.changeCursor(helper.getAvailableQuests())
            listView.adapter = ca
            val dialog = Dialog(this)
            dialog.setContentView(listView)
            dialog.show()
            helper.close()}
    }

    // Reaktion auf Drücken des Zurück-Knopfs
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            moveTaskToBack(true)
            super.onBackPressed()
        }
    }


    // Navigationsmenü-Auswahl -> zur entsprechenden Activity weiterleiten
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Home-Seite
            R.id.nav_home -> {
                startActivity(Intent(this,HomeNavActivity::class.java))
            }
            // Übungen -> nichts tun
            R.id.nav_exercise -> {}
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

    // Verbindung des Services herstellen
    fun createNoService(device: BluetoothDevice){
        bluetoothNoService.establishConnection(device,this)
    }

    // Thread mit Schleife zur Aktualisierung der UI-Elemente
    private val updateUIRunnable = Runnable {
        bluetoothNoService.startReading()

        // Diagrammdaten von vorheriger Benutzung laden
        val prefsOldData = prefs.getString(PREFS_OLD_DATA,"0")
        val oldDataList = prefsOldData!!.split("|")
        val oldEntries = mutableListOf(Entry(0f,0f))
        val dataSetOld = LineDataSet(oldEntries,"Deine vorherige Leistung")
        dataSetOld.color = Color.GREEN

        // Diagramm vorbereiten
        val entries = mutableListOf(Entry(0f,0f))
        val dataset = LineDataSet(entries,"Deine Leistung")
        dataset.color = Color.BLUE
        val linedata = LineData(dataset,dataSetOld)
        runOnUiThread {
            graph.data = linedata
            graph.invalidate()
        }

        var xval = 1
        val currentData = mutableListOf<Float>()

        // solange Übung läuft
        while (exerciseRunning){
            // aktuellen Bluetooth-Wert speichern
            val perc = bluetoothNoService.getCurrentValuePercent()
            currentData.add(perc)

            // Diagramm ändern
            dataset.addEntry(Entry(xval.toFloat(), if (perc > 0) perc else 0f))
            if (xval < oldDataList.size) {
                dataSetOld.addEntry(Entry(xval.toFloat(), oldDataList[xval - 1].toFloat()))
            }

            runOnUiThread {
                // Tacho-Element ändern
                velocimeter.setValue(perc, true)
                linedata.notifyDataChanged()
                graph.notifyDataSetChanged()
                graph.invalidate()
            }
            xval++

            // 1 Sekunde warten
            Thread.sleep(1000)
        }
        bluetoothNoService.stopReading()

        // Daten abspeichern
        prefs.edit().putString(PREFS_OLD_DATA,currentData.joinToString(separator = "|")).apply()

        // Bewertung, entsprechend Dialog anzeigen
        graderHelper.gradeForExercise(currentData)
        val fragment = graderHelper.checkBadgesForCompletion(currentData)
        runOnUiThread { fragment?.show(this@ExerciseActivity.supportFragmentManager,"badgecompleted") }
    }

    // Übung starten
    private fun startExercise(){
        // Start-Button wird zu Stop-Button
        fab_start_stop.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.ic_pause))
        fab_start_stop.setOnClickListener { stopExercise() }

        // Thread starten
        exerciseRunning = true
        val t = Thread(updateUIRunnable)
        t.start()
    }

    // Übung stoppen
    private fun stopExercise(){
        // Thread stoppen
        exerciseRunning = false
        // Stop-Button wird zu Start-Button
        fab_start_stop.setOnClickListener { startExercise() }
        fab_start_stop.setImageDrawable(ContextCompat.getDrawable(applicationContext,R.drawable.ic_play_arrow))
    }

    override fun onPause() {
        stopExercise()
        super.onPause()
    }

    override fun onDestroy() {
        unregisterReceiver(discoveryReceiver)
        super.onDestroy()
    }
}
