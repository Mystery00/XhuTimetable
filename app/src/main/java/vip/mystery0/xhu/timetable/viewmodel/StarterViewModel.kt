package vip.mystery0.xhu.timetable.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel

class StarterViewModel : ViewModel() {
    companion object {
        const val WORK_DURATION = 2000L
    }

    private val initTime = SystemClock.uptimeMillis()

    fun isDataReady() = SystemClock.uptimeMillis() - initTime > WORK_DURATION
}