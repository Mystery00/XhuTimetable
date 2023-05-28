package vip.mystery0.xhu.timetable.config

import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.ui.activity.ErrorReportActivity
import vip.mystery0.xhu.timetable.utils.finishAllActivity
import vip.mystery0.xhu.timetable.utils.formatChinaDateTime
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ApplicationExceptionCatcher : Thread.UncaughtExceptionHandler {
    companion object {
        private const val TAG = "ApplicationExceptionCat"
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (e is ServerError) {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            return
        }
        if (e is ServerNeedLoginException) {
            Toast.makeText(context, "请刷新页面", Toast.LENGTH_LONG).show()
            return
        }
        val logFile = dumpExceptionToFile(e)
        finishAllActivity()
        showErrorReport(e, logFile)
    }

    private fun dumpExceptionToFile(throwable: Throwable): File {
        val dir = File(context.externalCacheDir, "crash")
        if (!dir.exists() && !dir.mkdirs()) {
            Log.w(TAG, "dumpExceptionToFile: log dump dir not exist")
            return dir
        }
        val now = LocalDateTime.now()
        val time = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(now)
        val file = File(dir, "${appName}$time.log")
        try {
            PrintWriter(BufferedWriter(FileWriter(file))).use { printWriter ->
                //导出时间
                printWriter.println(now.formatChinaDateTime())
                //导出手机信息
                printWriter.println("===================================")
                printWriter.println("应用版本: $appVersionName")
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
}