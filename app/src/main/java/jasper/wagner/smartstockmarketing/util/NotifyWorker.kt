package jasper.wagner.smartstockmarketing.util

import android.content.Context
import androidx.annotation.NonNull
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlin.math.abs


class NotifyWorker(@NonNull context: Context, @NonNull params: WorkerParameters) :
    Worker(context, params) {

    val context = context

    @NonNull
    override fun doWork(): Result {
        // Method to trigger an instant notification
        val stockName = inputData.getString(STOCK_NAME)
        val stockGrowthRate = inputData.getDouble(STOCK_GROWTH_RATE,0.0)
        val criticalGrowthRate = inputData.getDouble(CRITICAL_GROWTH_RATE, 1.0)

        if (abs(stockGrowthRate) >= criticalGrowthRate)
        triggerNotification(context,stockName!!,stockGrowthRate)

        return Result.success()
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    private fun triggerNotification(
        context: Context,
        stockName: String,
        stockGrowthRate: Double
    ) {
        NotificationBuilder().createNotification(context, stockName, stockGrowthRate)
    }

    companion object {
        const val STOCK_NAME = "stock name"
        const val STOCK_GROWTH_RATE = "stock growth rate"
        const val CRITICAL_GROWTH_RATE = "critical growth rate"
        const val PERIODIC_WORK_TAG = "periodic notification workout work"
    }
}