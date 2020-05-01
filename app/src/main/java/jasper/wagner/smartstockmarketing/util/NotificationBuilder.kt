package jasper.wagner.smartstockmarketing.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.graphics.Color
import android.media.AudioAttributes
import android.os.Build
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat.CATEGORY_EVENT
import android.graphics.BitmapFactory
import androidx.core.content.ContextCompat
import jasper.wagner.smartstockmarketing.R


class NotificationBuilder {

// Create the NotificationChannel, but only on API 26+ because
// the NotificationChannel class is new and not in the support library

    fun createNotification(context: Context, stockName: String, stockGrowthRate: Double) {

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val customVibrationPattern =
                longArrayOf(0,10, 2, 10, 2, 80, 2, 10, 2, 10, 2, 80, 2, 10, 2, 10, 2, 10, 2, 10)
//                longArrayOf(0, 10, 2, 10, 2, 80)
            //build the actual notification channel, giving it a unique ID and name
            val channel = NotificationChannel(workout_notification_channel_ID, workout_notification_channel_name, importance)

            channel.apply {
                lightColor = Color.MAGENTA
                setSound(soundUri, audioAttributes)
                vibrationPattern = customVibrationPattern
                enableVibration(true)
            }

            //build the notification
            val notificationBuilder = Notification.Builder(
                context,
                workout_notification_channel_ID
            )
                .setStyle(
                    bigTextStyle(stockName)
                )
                .setSmallIcon(R.drawable.stock_market_icon)
                .setContentTitle("Crucial development alert!")
                .setContentText("Stock market growth rate of the last hour: $stockGrowthRate %")
//                .setContentIntent(pendingIntent())
                .setAutoCancel(true)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.stock_market_icon
                    )
                )
                .setCategory(CATEGORY_EVENT)
                .setVisibility(Notification.VISIBILITY_PUBLIC)

                .setColor(
                    ContextCompat.getColor(
                        context.applicationContext,
                        R.color.colorAccent
                    )
                )

            notificationManager.createNotificationChannel(channel)
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun bigPictureStyle(context: Context): Notification.BigPictureStyle {

        val title = "Stock alert"
        val summary =
            ""

        val icon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.stock_market_icon
        )

        return Notification.BigPictureStyle()
            .bigPicture(icon)
            .setBigContentTitle(title)
            .setSummaryText(summary)
    }

    private fun bigTextStyle(stockName: String): Notification.BigTextStyle {
        return Notification.BigTextStyle()
            .setBigContentTitle(stockName)
            .setSummaryText("Crucial development alert!")
    }

    companion object {
        const val workout_notification_channel_ID = "1"
        const val NOTIFICATION_ID = 100
        const val workout_notification_channel_name = "Stock market alert channel"
    }
}