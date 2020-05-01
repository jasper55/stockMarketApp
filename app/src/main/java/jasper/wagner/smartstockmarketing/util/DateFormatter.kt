package jasper.wagner.smartstockmarketing.util

import android.os.Build
import androidx.annotation.RequiresApi
import jasper.wagner.cryptotracking.common.MathOperation.round
import jasper.wagner.smartstockmarketing.util.DateFormatter.length
import java.lang.Math.abs
import java.lang.Math.log10
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateFormatter {

    fun getDate(timeStamp: String): String {
        return timeStamp.substringBefore(" ")
    }

    fun getTime(after: String, timeStamp: String): String {
        return timeStamp.substringAfterLast(after)
    }

    fun getHour(time: String): Int {
        var hour = time.substringBefore(":")
        hour = hour.replace(" ", "")
        if (hour.startsWith("0")) {
            hour = hour.replace("0", "")
        }
        return hour.toInt()
    }

    fun getMinute(time: String, interval: String): Int {
        val sep = interval.substringBefore("min").toInt()

        val hour = time.substringBefore(":")
        val minSec = time.substringAfterLast("${hour}:")

        var min = minSec.substringBefore(":")

        if (min.startsWith("0")) {
            min = min.replace("0", "")
        }
        return (round((min.toInt() / sep).toDouble()) * sep).toInt()
    }

    fun Int.length() = when (this) {
        0 -> 1
        else -> log10(abs(toDouble())).toInt() + 1
    }

}