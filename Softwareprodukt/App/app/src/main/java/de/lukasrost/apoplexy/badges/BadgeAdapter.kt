package de.lukasrost.apoplexy.badges

import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.TextView
import de.lukasrost.apoplexy.*

// Dialog zu Anzeige der verfügbaren/abgeschlossenen Quests bzw. Badges
class BadgeAdapter(context: Context): CursorAdapter(context,null,0) {
    private val inflater = LayoutInflater.from(context)
    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        // Icon, Titel, Beschreibung des Badges aus Datenbank bestimmen
        val icon = cursor!!.getInt(cursor.getColumnIndex(QUEST_ICON))
        val title = cursor.getString(cursor.getColumnIndex(QUEST_TITLE))
        val description = cursor.getString(cursor.getColumnIndex(QUEST_DESCRIPTION))

        // entsprechende Views damit befüllen
        view!!.findViewById<ImageView>(R.id.badge_icon).setImageResource(getIconRes(icon))
        view.findViewById<TextView>(R.id.text_badge_title).text = title
        view.findViewById<TextView>(R.id.text_badge_description).text = description
    }

    // Views aus Layout erstellen
    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return inflater.inflate(R.layout.badge_row, parent, false)
    }
}