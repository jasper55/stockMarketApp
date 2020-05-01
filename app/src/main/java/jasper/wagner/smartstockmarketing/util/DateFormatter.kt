package jasper.wagner.smartstockmarketing.util

import android.os.Build
import androidx.annotation.RequiresApi
import jasper.wagner.cryptotracking.common.MathOperation.round
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

    fun getTime(after: String, interval: String, timeStamp: String): String {
        val sep = interval.substringBefore("min").toInt()
        val time = timeStamp.substringAfterLast(after)
        val hour = time.substringBefore(":")
        val minSec = time.substringAfterLast("${hour}:")

        val min = minSec.substringBefore(":").toInt()

        val minute = (round((min / sep).toDouble()) * sep).toInt()
        if (minute.length() == 1) {
            return "$hour:0$minute:00"
        } else {
            return "$hour:$minute:00"
        }
    }

    fun Int.length() = when (this) {
        0 -> 1
        else -> log10(abs(toDouble())).toInt() + 1
    }

}