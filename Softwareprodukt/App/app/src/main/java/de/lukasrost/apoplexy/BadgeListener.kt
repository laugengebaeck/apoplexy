package de.lukasrost.apoplexy

import android.view.View
import de.lukasrost.apoplexy.badges.BadgeDialogFragment

interface BadgeListener {
    fun callback(view: View, fragment: BadgeDialogFragment?)
}