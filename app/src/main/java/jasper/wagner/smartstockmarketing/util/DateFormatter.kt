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

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun getRoundedTime(interval: String, timeStamp: String) {
//        val time = timeStamp.substringAfterLast("2020-04-30 ")
//        val pattern = "yyyy-MM-dd HH:mm:ss"
//        val simpleDateFormat = SimpleDateFormat(pattern)
//        String date = simpleDateFormat. format (new Date ());
//        System.out.println(date);
//
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
//        val formatted = date.format(formatter)
//        timeStamp.format(formatter)
//    }

    fun getDate(timeStamp: String): String {
        return timeStamp.substringBefore(" ")
    }

    fun getTime(after: String, timeStamp: String): String {
        return timeStamp.substringAfterLast(after)
    }

    fun getHour(time: String): String {
        return time.substringBefore(":")
    }
    fun getMinute(time: String, interval: String): String {
        val sep = interval.substringBefore("min").toInt()

        val hour = time.substringBefore(":")
        val minSec = time.substringAfterLast("${hour}:")

        val min = minSec.substringBefore(":").toInt()

        val minute = (round((min / sep).toDouble()) * sep).toInt()
        if (minute.length() == 1) {
            return "0$minute"
        } else {
            return "$minute"
        }
    }

    fun Int.length() = when (this) {
        0 -> 1
        else -> log10(abs(toDouble())).toInt() + 1
    }

}