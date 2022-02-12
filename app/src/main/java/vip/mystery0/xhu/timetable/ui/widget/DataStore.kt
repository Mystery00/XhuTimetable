package vip.mystery0.xhu.timetable.ui.widget

import android.content.Context
import androidx.datastore.dataStoreFile
import java.io.File

fun Context.widgetDataStoreFile(name: String): File =
    this.dataStoreFile("$name.widget.datastore")