package vip.mystery0.xhu.timetable

import android.content.Intent
import android.os.Build
import android.util.Log
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import vip.mystery0.xhu.timetable.base.appName
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.ui.activity.ErrorReportActivity
import vip.mystery0.xhu.timetable.utils.chinaDateTime
import vip.mystery0.xhu.timetable.utils.now
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter

private const val TAG = "ApplicationExceptionCat"

actual fun handleGlobalException(throwable: Throwable) {
    if (GlobalConfigStore.alwaysCrash) {
        throw throwable
    }
    val logFile = dumpExceptionToFile(throwable)
    showErrorReport(throwable, logFile)
}

private fun dumpExceptionToFile(throwable: Throwable): File {
    val dir = File(context.externalCacheDir, "crash")
    if (!dir.exists() && !dir.mkdirs()) {
        Log.w(TAG, "dumpExceptionToFile: log dump dir not exist")
        return dir
    }
    val now = LocalDateTime.now()
    val time = now.format(LocalDateTime.Format {
        year()
        monthNumber()
        day()
        hour()
        minute()
        second()
    })
    val file = File(dir, "${appName()}$time.log")
    try {
        PrintWriter(BufferedWriter(FileWriter(file))).use { printWriter ->
            //导出时间
            printWriter.println(now.format(chinaDateTime))
            //导出手机信息
            printWriter.println("===================================")
            printWriter.println("应用版本: ${appVersionName()}")
            printWriter.println("Android版本: ${Build.VERSION.RELEASE}_${Build.VERSION.SDK_INT}")
            printWriter.println("厂商: ${Build.MANUFACTURER}")
            printWriter.println("型号: ${Build.MODEL}")
            printWriter.println("===================================")
            printWriter.println()
            throwable.printStackTrace(printWriter)
        }
    } catch (e: Exception) {
        Log.e(TAG, "dumpExceptionToFile: exception dump failed ", e)
    }
    return file
}

private fun showErrorReport(e: Throwable, logFile: File) {
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    e.printStackTrace(pw)
    val stackTraceString = sw.toString()

    val intent = Intent(context, ErrorReportActivity::class.java)
    intent.putExtra(ErrorReportActivity.EXTRA_LOG_FILE, logFile.absolutePath)
    intent.putExtra(ErrorReportActivity.EXTRA_STACK_TRACE, stackTraceString)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}