package jasper.wagner.smartstockmarketing.util

import android.content.Context
import androidx.annotation.NonNull
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.workoutreminder.data.network.USStockMarketApi
import jasper.wagner.cryptotracking.common.Common.getWorkTag
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockGrowthRate
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockData
import jasper.wagner.smartstockmarketing.util.NotificationBuilder.Companion.NOTIFICATION_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlin.math.abs


class NotifyWorker(@NonNull context: Context, @NonNull params: WorkerParameters) :
    Worker(context, params) {

    val context = context

    @NonNull
    override fun doWork(): Result {
        val paramsString = inputData.getString(API_CALL_PARAMS)
        val growthMargin = inputData.getDouble(GROWTH_MARGIN,1.0)
        val channelID = inputData.getInt(NOTIFICATION_ID,100)
        val apiParams = SerializeHelper.deserializeFromJson(paramsString!!) as StockApiCallParams

        val usStockMarketApi = USStockMarketApi()
        CoroutineScope(IO).launch {
            val stockList = usStockMarketApi.fetchStockMarketData(apiParams)

            val stockGrowthRate = getStockGrowthRate(stockList)
            if (abs(stockGrowthRate) >= growthMargin) {
                createNotification(context, stockList, stockGrowthRate, channelID)
            }
        }

        return Result.success()
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }



    private fun createNotification(
        context: Context,
        stockList: ArrayList<StockData>,
        stockGrowthRate: Double,
        channelID: Int
    ) {
        NotificationBuilder().createNotification(
            context,
            stockList.last().stockName,
            stockGrowthRate,
            channelID
        )
    }

    companion object {
        const val PERIODIC_WORK_TAG = "periodic stock market analyzer"
        const val API_CALL_PARAMS = "API call_params"
        const val STOCK_DATA_AVAILABLE = "Stock data available"
        const val GROWTH_MARGIN = "growth margin"
    }
}