package jasper.wagner.smartstockmarketing.util

import android.content.Context
import androidx.annotation.NonNull
import androidx.work.Worker
import androidx.work.WorkerParameters
import jasper.wagner.smartstockmarketing.common.Constants.WorkManager.API_CALL_PARAMS
import jasper.wagner.smartstockmarketing.common.Constants.WorkManager.GROWTH_MARGIN
import jasper.wagner.smartstockmarketing.common.Constants.WorkManager.STOCK_UID
import jasper.wagner.smartstockmarketing.data.network.USStockMarketApi
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockGrowthRate
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance
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
        val stockUID = inputData.getLong(STOCK_UID,0)
        val growthMargin = inputData.getDouble(GROWTH_MARGIN,1.0)
        val channelID = inputData.getInt(NOTIFICATION_ID,100)
        val apiParams = SerializeHelper.deserializeFromJson(paramsString!!) as StockApiCallParams

        val usStockMarketApi = USStockMarketApi()
        CoroutineScope(IO).launch {
            val stockList = usStockMarketApi.fetchStockValuesList(stockUID,apiParams)

            val stockGrowthRate = getStockGrowthRate(stockList)
            if (abs(stockGrowthRate) >= growthMargin) {
                createNotification(context, stockList, apiParams.stockSymbol,stockGrowthRate, channelID)
            }
        }

        return Result.success()
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }



    private fun createNotification(
        context: Context,
        stockList: ArrayList<StockTimeSeriesInstance>,
        name: String,
        stockGrowthRate: Double,
        channelID: Int
    ) {
        NotificationBuilder().createNotification(
            context,
            name,
            stockGrowthRate,
            channelID
        )
    }
}