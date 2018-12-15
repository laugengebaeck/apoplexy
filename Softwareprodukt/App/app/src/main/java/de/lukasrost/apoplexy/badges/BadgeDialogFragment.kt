package de.lukasrost.apoplexy.badges

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import de.lukasrost.apoplexy.R
import de.lukasrost.apoplexy.getIconRes

// Dialog, wenn ein Badge freigeschaltet wurde
class BadgeDialogFragment : DialogFragment() {
    // Dialog entsprechend Icon, Titel, XP erstellen
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        val icon = arguments?.getInt("icon") ?: 1
        val title = arguments?.getString("title") ?: "Beispielbadge"
        val earnedXP = arguments?.getInt("earnedXP") ?: 0

        val view = activity!!.layoutInflater.inflate(R.layout.badge_completed_dialog, LinearLayout(context), false)
        view.findViewById<ImageView>(R.id.imageIconBadge).setImageResource(getIconRes(icon))
        val text = "Herzlichen Glückwunsch! Du hast eine Quest gelöst und damit das/die Abzeichen $title freigeschaltet! Zusätzlich erhältst du dafür $earnedXP Erfahrungspunkte."
        view.findViewById<TextView>(R.id.textBadgeCompleted).text = text

        builder.setTitle("Abzeichen freigeschaltet!").setView(view)
        builder.setNeutralButton("Schließen"){ _, _ -> this@BadgeDialogFragment.dialog.dismiss()}
        return builder.create()
    }
}

// nötige Informationen übergeben
fun newBadgeInstance(icon : Int, title: String, earnedXP: Int) : BadgeDialogFragment{
    val df = BadgeDialogFragment()
    val bundle = Bundle()
    bundle.putInt("icon",icon)
    bundle.putString("title",title)
    bundle.putInt("earnedXP",earnedXP)
    df.arguments = bundle
    return df
}