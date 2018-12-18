package de.lukasrost.apoplexy

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.widget.Toast
import java.nio.charset.StandardCharsets
import java.util.*

// Steuerung der Bluetooth-Verbindung
class BluetoothNoService {
    // Bluetooth-Gerät und Socket
    private lateinit var device: BluetoothDevice
    private lateinit var activity: Activity
    private var bluetoothSocket: BluetoothSocket? = null

    // Queue der Messwerte
    private val btQueue = mutableListOf<Double>()

    // Thread zum Updaten der Queue
    private var keepRunning = false
    private val updateQueueRunnable = Runnable {
        var read = 0
        val data = ByteArray(1024)

        // wenn verbunden
        if (bluetoothSocket != null) {

            // solange nicht gestoppt und Datenempfang vorhanden
            while (this@BluetoothNoService.keepRunning && bluetoothSocket != null && ((bluetoothSocket!!.inputStream.read(data).let { read = it; it != -1 }))) {

                // Einlesen der Bluetooth-Daten
                val readdata = Arrays.copyOf(data, read)
                val value = String(readdata, StandardCharsets.UTF_8)

                // Daten der Queue hinzufügen
                for (number in value.split("\r\n")) {
                    if (btQueue.size == 4) {
                        btQueue.removeAt(0)
                    }
                    val num = number.toDoubleOrNull()
                    num?.let { if( num < 1.5) btQueue.add(1.5) else btQueue.add(it) } // nur Werte > 1.5
                }
            }
        }
        activity.runOnUiThread { Toast.makeText(activity,"Bluetooth-Verbindung beendet!", Toast.LENGTH_LONG).show() }
    }

    // Verbindung beginnen
    fun establishConnection(device: BluetoothDevice, activity: Activity){
        this.device = device
        this.activity = activity
    }

    // mit Bluetooth-Gerät verbinden und Thread starten
    fun startReading(){
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            bluetoothSocket?.connect()
            keepRunning = true
            Thread(updateQueueRunnable).start()
            activity.runOnUiThread { Toast.makeText(activity,"Erfolgreich verbunden!", Toast.LENGTH_LONG).show() }
        } catch (e: Exception){
            activity.runOnUiThread { Toast.makeText(activity,"Fehler beim Verbinden über Bluetooth!", Toast.LENGTH_LONG).show() }
        }
    }

    // Verbindung beenden, Thread stoppen
    fun stopReading(){
        keepRunning = false
        Thread.sleep(1000)
        bluetoothSocket?.close()
    }

    // aktuellen Durchschnittswert der Queue in Prozent des Maximalwerts berechnen
    fun getCurrentValuePercent() : Float = 100 * ((btQueue.average() - MIN_VOLTAGE_EMG) / (MAX_VOLTAGE_EMG - MIN_VOLTAGE_EMG)).toFloat()
}