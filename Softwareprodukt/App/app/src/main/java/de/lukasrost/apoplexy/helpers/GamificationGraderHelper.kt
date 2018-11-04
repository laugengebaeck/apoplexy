package de.lukasrost.apoplexy.helpers

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager
import android.widget.Toast
import de.lukasrost.apoplexy.*
import de.lukasrost.apoplexy.badges.*
import kotlin.math.roundToInt

//Bewertungshelfer für Gamification-Funktionen
class GamificationGraderHelper(val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val activity = context as Activity
    private lateinit var dbHelper : GamificationDBHelper

    // XP für Übungen vergeben
    fun gradeForExercise(data : MutableList<Float>){
        // Minimal-, Maximal- und Durchschnittswert der Liste bestimmen
        val min = data.min()?:0f
        val max = data.max()?:0f
        val avg = if (data.average().isNaN()) 0.0 else data.average()

        // Bewertungsfunktion
        var points = ((avg + min + max) / 3 )
        points *= 100
        val pointsInt = if(!points.isNaN() && points.roundToInt() > 0) points.roundToInt() else 0

        // neue XP zu bisherigen XP hinzufügen
        val pointsBefore = prefs.getInt(PREFS_POINTS,0)
        prefs.edit().putInt(PREFS_POINTS,pointsBefore + pointsInt).apply()
        activity.runOnUiThread { Toast.makeText(context,"Du hast gerade $pointsInt XP erhalten!",Toast.LENGTH_LONG).show() }
    }

    // XP für Minispiel vergeben
    fun gradeForGame(distanceUntilCrash: Int, data: MutableList<Float>){
        // Minimal-, Maximal- und Durchschnittswert der Liste bestimmen
        val min = data.min()?:0f
        val max = data.max()?:0f
        val avg = if (data.average().isNaN()) 0.0 else data.average()

        // Bewertungsfunktion
        var points = ((avg + min + max) / 3 ) + distanceUntilCrash * 2
        points *= 100
        val pointsInt = if(!points.isNaN() && points.roundToInt() > 0) points.roundToInt() else 0

        // neue XP zu bisherigen XP hinzufügen
        val pointsBefore = prefs.getInt(PREFS_POINTS,0)
        prefs.edit().putInt(PREFS_POINTS,pointsBefore + pointsInt).apply()
        activity.runOnUiThread { Toast.makeText(context,"Du hast gerade $pointsInt XP erhalten!",Toast.LENGTH_LONG).show() }
    }

    // Freischaltung von Badges überprüfen
    fun checkBadgesForCompletion(data: MutableList<Float>) : BadgeDialogFragment?{
        dbHelper = GamificationDBHelper(context)
        val cursor = dbHelper.getAvailableQuests()
        var shouldShowDialog = false
        var icon = 0
        var title = ""
        var earnedXP = 0

        // durch alle vefügbaren Quests iterieren
        while (cursor.moveToNext()){
            val neededXP = cursor.getInt(cursor.getColumnIndex(QUEST_FIN_XP))
            val minPerc = cursor.getInt(cursor.getColumnIndex(QUEST_MIN_PERC))
            val isMinPercCompleted = data.any { it >= minPerc.toFloat() }
            val isOverPercCompleted = checkOverPercCondition(data,cursor.getInt(cursor.getColumnIndex(QUEST_OVER_PERC)),cursor.getInt(cursor.getColumnIndex(QUEST_TIME_OVER_PERC)))

            // spezifische Bedingungen überprüfen
            if (neededXP <= prefs.getInt(PREFS_POINTS,0) && isMinPercCompleted && isOverPercCompleted){

                // Quest fertig -> Badge freigeschaltet
                dbHelper.setQuestCompleted(cursor.getInt(cursor.getColumnIndex(QUEST_ID)))

                // entsprechend XP vergeben
                val pointsBefore = prefs.getInt(PREFS_POINTS,0)
                prefs.edit().putInt(PREFS_POINTS, pointsBefore + cursor.getInt(cursor.getColumnIndex(QUEST_EARN_XP))).apply()

                // Dialog soll angezeigt werden
                icon = cursor.getInt(cursor.getColumnIndex(QUEST_ICON))
                title += "\"" + cursor.getString(cursor.getColumnIndex(QUEST_TITLE)) + "\", "
                earnedXP += cursor.getInt(cursor.getColumnIndex(QUEST_EARN_XP))
                shouldShowDialog = true
            }
        }
        cursor.close()
        dbHelper.close()

        // Dialog an Aufrufer zurückgeben
        if (shouldShowDialog){
            title = title.substring(0,title.length-2)
            return BadgeDialogFragment().setDialogInformation(icon, title, earnedXP)
        }
        return null
    }

    // Überprüfen der Bedingung, dass man bestimmte Zeit über bestimmtem Prozentwert war
    private fun checkOverPercCondition(data: MutableList<Float>, overPerc :Int, timeOverPerc: Int): Boolean{
        var count = 0;
        for (el in data){
            if (el >= overPerc){
                count++
            } else {
                count = 0
            }
            if (count >= timeOverPerc){
                return true
            }
        }
        return false
    }
}