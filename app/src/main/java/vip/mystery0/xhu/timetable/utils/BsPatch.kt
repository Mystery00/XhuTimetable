package vip.mystery0.xhu.timetable.utils

object BsPatch {
    init {
        System.loadLibrary("bspatch")
    }

    external fun patch(oldApk: String, newApk: String, patch: String): Int
}
