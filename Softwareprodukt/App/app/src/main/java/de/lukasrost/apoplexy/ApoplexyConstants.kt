package de.lukasrost.apoplexy

// Request-IDs und System-Codes
const val REQUEST_ENABLE_BT = 42
const val PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 21
const val PERMISSIONS_REQUEST_EXT_STOR = 788
const val DAILY_REMINDER_CODE = 4242
const val DAILY_REMINDER_CHANNEL_ID = "de.lukasrost.apoplexy Notifications"

// EMG-Konstanten
const val MAX_VOLTAGE_EMG = 3.3
const val MIN_VOLTAGE_EMG = 1.5

// Preference-Namen
const val PREFS_POINTS = "points"
const val PREFS_NAME = "userName"
const val PREFS_ALARM_ENABLED = "alarmEnabled"
const val PREFS_ALARM_TIME = "alarmTime"
const val PREFS_OLD_DATA = "oldDiagramData"

// Tabellenspalten
const val QUEST_ID = "_id"
const val QUEST_DESCRIPTION = "description"
const val QUEST_TITLE = "title"
const val QUEST_ICON = "icon"
const val QUEST_REQ_XP = "requiredXP"
const val QUEST_FIN_XP = "finishedXP"
const val QUEST_EARN_XP = "earnedXP"
const val QUEST_COMPLETED = "isCompleted"
const val QUEST_OVER_PERC = "overPercentage"
const val QUEST_TIME_OVER_PERC = "timeOverPercentage"
const val QUEST_MIN_PERC = "minimumPercentage"

// Badge-Icons
const val ICON_ONE = 1
const val ICON_TWO = 2
const val ICON_THREE = 3
const val ICON_FOUR = 4
const val ICON_FIVE = 5

// Datenbank
const val DATABASE_NAME = "apoplexy.db"
const val DATABASE_VERSION = 3
const val TABLE_NAME_GAMIFICATION = "quests"
const val DROP_TABLE_GAMIFICATION = "DROP TABLE IF EXISTS $TABLE_NAME_GAMIFICATION"
const val CREATE_TABLE_GAMIFICATION = "CREATE TABLE $TABLE_NAME_GAMIFICATION ($QUEST_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
        "$QUEST_TITLE VARCHAR(50), $QUEST_DESCRIPTION VARCHAR(150), $QUEST_ICON INTEGER, $QUEST_REQ_XP INTEGER, " +
        "$QUEST_FIN_XP INTEGER, $QUEST_EARN_XP INTEGER, $QUEST_COMPLETED INTEGER, $QUEST_OVER_PERC INTEGER, " +
        "$QUEST_TIME_OVER_PERC INTEGER, $QUEST_MIN_PERC INTEGER);"

// Badge-Icons (Zuordnung Zahl -> Icon)
// Copyright: Icons made by Roundicons from www.flaticon.com is licensed by CC 3.0 BY
fun getIconRes(icon : Int) : Int{
    return when(icon){
        ICON_ONE -> R.drawable.ic_trophy
        ICON_TWO -> R.drawable.ic_shield
        ICON_THREE -> R.drawable.ic_flag
        ICON_FOUR -> R.drawable.ic_medal_normal
        ICON_FIVE -> R.drawable.ic_medal_soviet
        else -> R.drawable.ic_trophy
    }
}