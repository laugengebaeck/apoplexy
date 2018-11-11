package de.lukasrost.apoplexy.game

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import de.lukasrost.apoplexy.BluetoothNoService
import de.lukasrost.apoplexy.R
import de.lukasrost.apoplexy.helpers.GamificationGraderHelper
import java.util.*

// eigene View, beinhaltet das Minispiel
// Flugzeug-Icon von https://icon-icons.com/de/symbol/Flugzeug-rechts-rot/26360
class PlaneGameView : View {
    // Konstruktoren: zeichnet sich nicht selbst
    constructor(context : Context) : super(context){
        setWillNotDraw(false)
    }
    constructor(context: Context, attributeSet: AttributeSet) : super(context,attributeSet){
        setWillNotDraw(false)
    }

    // Bluetooth- und Gamification-Variablen
    private lateinit var bluetoothNoService : BluetoothNoService
    private var bluetoothPercList = mutableListOf<Float>()
    private var inGame = false
    private val graderHelper = GamificationGraderHelper(context)

    // Höhe und Breite der View
    private var effWidth = width - (paddingLeft + paddingRight)
    private var effHeight = height - (paddingTop + paddingBottom)

    // Zufällige Berge als Queue
    private val random = Random()
    private var randomHills = mutableListOf<Int>()

    // Gesten-Detektor für Klicks
    private val gestureListener =  object : GestureDetector.SimpleOnGestureListener(){
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }
    }
    private val gestureDetector = GestureDetector(context,gestureListener)

    // Texte
    private val pausedText = "Um das Spiel zu beginnen, berühre den Bildschirm."
    private val lostText = "Du bist gegen einen Berg gestoßen. Du verlierst!"

    // Hintergrund-Rechteck
    private var backgroundRect = Rect(paddingLeft,paddingTop,effWidth,effHeight)
    // Farben, Stile, Textgrößen
    private val backgroundPaint = Paint(0).apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }
    private val hillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textSize = 60f
        color = Color.BLACK
    }
    private val planePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Flugzeug-Bild (nur halb so groß wie Originalbild)
    private var opts = BitmapFactory.Options().apply {
        inSampleSize = 2
    }
    private val planeBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_airplane,opts)

    // Zeichnen der View
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if(inGame) {
            // im Spiel
            canvas?.apply {
                bluetoothPercList.add(bluetoothNoService.getCurrentValuePercent())
                // Verschiebung des Flugzeugs nach oben
                val verschieb = 20
                // obere linke Ecke des Flugzeugs durch  *Magie* bestimmen
                val top = paddingTop + effHeight - ( effHeight * (bluetoothPercList[bluetoothPercList.size-1] + verschieb) / 100 )
                val left = paddingLeft + effWidth/2 - planeBitmap.width

                // Kollision mit Hügel vor Flugzeug -> verloren
                if(top + planeBitmap.height >= paddingTop + effHeight - randomHills[randomHills.size/2]){
                    drawText(lostText,50f,(effHeight/2).toFloat(),textPaint)
                    Thread.sleep(2500)
                    // Spiel beenden
                    handleTap()
                } else {
                    // Spiellandschaft (Flugzeug, Himmel, Berge) zeichnen
                    drawRect(backgroundRect, backgroundPaint)
                    drawHills(this)
                    drawBitmap(planeBitmap, left.toFloat(), top, planePaint)
                }
            }
        } else {
            // außerhalb des Spiels -> Text anzeigen
            canvas?.apply {
                drawText(pausedText,50f,(effHeight/2).toFloat(),textPaint)
            }
        }
    }

    // Berge zeichen
    private fun drawHills(canvas: Canvas){
        // Höhe des nächsten Bergs zufällig bestimmen
        val k = random.nextInt( effHeight) - effHeight / 5
        randomHills.add(if (k < 0) effHeight / 4 else k)
        randomHills.removeAt(0)

        // Berge im Abstand von 100 Pixel zeichnen
        val p = Path()
        canvas.apply {
            var offset = 0
            for (hill in randomHills){
                p.lineTo((paddingLeft+offset).toFloat(),(paddingTop+ effHeight - hill).toFloat())
                offset += 100
            }
            p.lineTo((paddingLeft + effWidth).toFloat(),(paddingTop+effHeight).toFloat())
            p.lineTo(paddingLeft.toFloat(),(paddingTop+effHeight).toFloat())
            p.close()
            drawPath(p,hillPaint)
        }
    }

    // Bildschirmgröße verändert -> abhängige Werte anpassen
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        effWidth = width - (paddingLeft + paddingRight)
        effHeight = height - (paddingTop + paddingBottom)
        backgroundRect = Rect(paddingLeft,paddingTop,effWidth,effHeight)
        genRandomHills()
        super.onSizeChanged(w, h, oldw, oldh)
    }

    // neue initiale Berge-Liste zufällig generieren
    private fun genRandomHills(){
        randomHills = mutableListOf<Int>().apply {
            for (i in 0..effWidth/100){
                val k = random.nextInt( effHeight) - effHeight / 5
                add(if (k < 0) effHeight / 4 else k)
            }
        }
    }

    // diese und nächste Funktion setzen Höhe und Breite der View bei Anforderung durch System
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        setMeasuredDimension(measureDimension(desiredWidth, widthMeasureSpec),
                measureDimension(desiredHeight, heightMeasureSpec))
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    // bei Klick Spiel starten bzw. beenden
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event).let {
            if(it){
                handleTap()
                true
            } else false
        }
    }

    // Spiel starten bzw. beenden
    private fun handleTap(){
        inGame = !inGame
        if(!inGame) {
            // Beenden
            // Gamification durchführen
            graderHelper.gradeForGame(bluetoothPercList.size, bluetoothPercList)
            val fragment = graderHelper.checkBadgesForCompletion(bluetoothPercList)
            val activity = context as FragmentActivity
            activity.runOnUiThread { fragment?.show(activity.supportFragmentManager,"badgecompleted") }

            // Bluetooth- und Berge-Listen leeren
            bluetoothPercList = mutableListOf()
            randomHills = mutableListOf()
        } else {
            // Starten -> Berge generieren
            genRandomHills()
        }
        // View neu zeichnen
        invalidate()
    }

    // Bluetooth-Service setzen
    fun setBluetoothNoService(bb : BluetoothNoService){
        bluetoothNoService = bb
        bluetoothNoService.startReading()
    }
}