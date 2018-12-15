package de.lukasrost.apoplexy.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.preference.PreferenceManager
import de.lukasrost.apoplexy.*

// Verbindung zur Gamification-Datenbank
class GamificationDBHelper(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME,null, DATABASE_VERSION) {
    // Tabelle erstellen, Standard-Quests einfügen
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE_GAMIFICATION)
        questOne(db!!)
        questTwo(db)
        questThree(db)
        questFour(db)
        questFive(db)
    }

    // Upgrade der Tabelle bei neuer Datenbankversion
    override fun onUpgrade(db: SQLiteDatabase?, old: Int, new: Int) {
        db?.execSQL(DROP_TABLE_GAMIFICATION)
        onCreate(db)
    }

    // alle erledigten Quests / erreichten Badges zurückgeben
    fun getCompletedQuests() : Cursor{
        return readableDatabase.query(TABLE_NAME_GAMIFICATION,null,"$QUEST_COMPLETED = 1",null,null,null,"$QUEST_ID ASC")
    }

    // alle gerade durchführbaren Quests zurückgeben
    fun getAvailableQuests() : Cursor{
        val xp = PreferenceManager.getDefaultSharedPreferences(context).getInt(PREFS_POINTS,0)
        return readableDatabase.query(TABLE_NAME_GAMIFICATION,null,"$QUEST_REQ_XP <= ? AND $QUEST_COMPLETED = 0", arrayOf(xp.toString()),
                null,null,"$QUEST_REQ_XP ASC")
    }

    // Quest auf erledigt setzen
    fun setQuestCompleted(id: Int){
        val values = ContentValues()
        values.put(QUEST_COMPLETED,1)
        writableDatabase.update(TABLE_NAME_GAMIFICATION,values,"$QUEST_ID = ?", arrayOf(id.toString()))
        writableDatabase.close()
    }

    // die folgenden Funktionen fügen die Standard-Quests in die Datenbank ein
    fun questOne(db: SQLiteDatabase) {
        val values = ContentValues()
        values.put(QUEST_TITLE,"Tausend")
        values.put(QUEST_DESCRIPTION,"Erreiche 1000 XP und du erhältst dieses Badge sowie 8000 XP.")
        values.put(QUEST_ICON, ICON_ONE)
        values.put(QUEST_COMPLETED,0)
        values.put(QUEST_REQ_XP,0)
        values.put(QUEST_FIN_XP,200)
        values.put(QUEST_EARN_XP,8000)
        values.put(QUEST_MIN_PERC,0)
        values.put(QUEST_OVER_PERC,0)
        values.put(QUEST_TIME_OVER_PERC,0)
        db.insert(TABLE_NAME_GAMIFICATION,null,values)
    }

    fun questTwo(db: SQLiteDatabase) {
        val values = ContentValues()
        values.put(QUEST_TITLE,"Die Antwort auf die Frage nach dem Leben, ...")
        values.put(QUEST_DESCRIPTION,"Erreiche während einer Übung einen Wert von mindestens 42 % und du erhältst dieses Badge sowie 10000 XP.")
        values.put(QUEST_ICON, ICON_TWO)
        values.put(QUEST_COMPLETED,0)
        values.put(QUEST_REQ_XP,0)
        values.put(QUEST_FIN_XP,0)
        values.put(QUEST_EARN_XP,10000)
        values.put(QUEST_MIN_PERC,42)
        values.put(QUEST_OVER_PERC,0)
        values.put(QUEST_TIME_OVER_PERC,0)
        db.insert(TABLE_NAME_GAMIFICATION,null,values)
    }

    fun questThree(db: SQLiteDatabase) {
        val values = ContentValues()
        values.put(QUEST_TITLE,"Das Problem mit der Ausdauer")
        values.put(QUEST_DESCRIPTION,"Halte 20 Sekunden lang einen Wert von mindestens 50 %. Als Belohnung gibt's zusätzlich 6000 XP.")
        values.put(QUEST_ICON, ICON_THREE)
        values.put(QUEST_COMPLETED,0)
        values.put(QUEST_REQ_XP,15000)
        values.put(QUEST_FIN_XP,0)
        values.put(QUEST_EARN_XP,6000)
        values.put(QUEST_MIN_PERC,0)
        values.put(QUEST_OVER_PERC,50)
        values.put(QUEST_TIME_OVER_PERC,20)
        db.insert(TABLE_NAME_GAMIFICATION,null,values)
    }

    fun questFour(db: SQLiteDatabase) {
        val values = ContentValues()
        values.put(QUEST_TITLE,"Was ihr wollt")
        values.put(QUEST_DESCRIPTION,"Diese komplett sinnlose Mischung besteht aus: 100000 XP erhalten, einmal über 61 % haben und 20 Sekunden über 39 % bleiben. Du bekommst 11000 XP")
        values.put(QUEST_ICON, ICON_FOUR)
        values.put(QUEST_COMPLETED,0)
        values.put(QUEST_REQ_XP,0)
        values.put(QUEST_FIN_XP,100000)
        values.put(QUEST_EARN_XP,11000)
        values.put(QUEST_MIN_PERC,61)
        values.put(QUEST_OVER_PERC,39)
        values.put(QUEST_TIME_OVER_PERC,20)
        db.insert(TABLE_NAME_GAMIFICATION,null,values)
    }

    fun questFive(db: SQLiteDatabase) {
        val values = ContentValues()
        values.put(QUEST_TITLE,"Viel zu viel")
        values.put(QUEST_DESCRIPTION,"Eine Million XP und einmal über 80 % erreichen. Schaffst du das?")
        values.put(QUEST_ICON, ICON_FIVE)
        values.put(QUEST_COMPLETED,0)
        values.put(QUEST_REQ_XP,20000)
        values.put(QUEST_FIN_XP,1000000)
        values.put(QUEST_EARN_XP,20000)
        values.put(QUEST_MIN_PERC,80)
        values.put(QUEST_OVER_PERC,0)
        values.put(QUEST_TIME_OVER_PERC,0)
        db.insert(TABLE_NAME_GAMIFICATION,null,values)
    }
}