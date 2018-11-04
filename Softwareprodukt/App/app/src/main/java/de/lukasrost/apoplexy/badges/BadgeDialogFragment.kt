package de.lukasrost.apoplexy.badges

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.widget.LinearLayout
import de.lukasrost.apoplexy.R
import de.lukasrost.apoplexy.getIconRes
import kotlinx.android.synthetic.main.badge_completed_dialog.*

// Dialog, wenn ein Badge freigeschaltet wurde
class BadgeDialogFragment : DialogFragment() {
    private var icon = 0
    private var title = "Beispielabzeichen"
    private var earnedXP = 0

    // Dialog entsprechend Icon, Titel, XP erstellen
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)

        builder.setTitle("Abzeichen freigeschaltet!").setView(activity!!.layoutInflater.inflate(R.layout.badge_completed_dialog, LinearLayout(context), false))
        builder.setNeutralButton("Schließen"){ _, _ -> this@BadgeDialogFragment.dialog.cancel()}
        imageIconBadge.setImageDrawable(ContextCompat.getDrawable(context!!, getIconRes(icon)))

        val text = "Herzlichen Glückwunsch! Du hast eine Quest gelöst und damit das/die Abzeichen $title freigeschaltet! Zusätzlich erhältst du dafür $earnedXP Erfahrungspunkte."
        textBadgeCompleted.text = text
        return builder.create()
    }

    // nötige Informationen übergeben
    fun setDialogInformation(icon : Int, title: String, earnedXP: Int) : BadgeDialogFragment{
        this.icon = icon
        this.title = title
        this.earnedXP = earnedXP
        return this
    }
}