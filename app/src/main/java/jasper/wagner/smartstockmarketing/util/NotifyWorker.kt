package jasper.wagner.smartstockmarketing.util

import android.content.Context
import androidx.annotation.NonNull
import androidx.work.Worker
import androidx.work.WorkerParameters
import jasper.wagner.smartstockmarketing.common.Constants.WorkManager.GROWTH_MARGIN
import jasper.wagner.smartstockmarketing.common.Constants.WorkManager.STOCK_UID
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockGrowthRate
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockNameFromSymbol
import jasper.wagner.smartstockmarketing.data.local.StockDatabase
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlin.math.abs


class NotifyWorker(@NonNull context: Context, @NonNull params: WorkerParameters) :
    Worker(context, params) {

    val context = context

    @NonNull
    override fun doWork(): Result {
        val stockUID = inputData.getLong(STOCK_UID,0)
        val growthMargin = inputData.getDouble(GROWTH_MARGIN,1.0)

        CoroutineScope(IO).launch {
        val stockList = StockDatabase.getInstance(context).stockValuesDao().getAllByStockUID(stockUID)
            val stock = StockDatabase.getInstance(context).stockDao().getStock(stockUID)
            val stockGrowthRate = getStockGrowthRate(stockList)
            if (abs(stockGrowthRate) >= growthMargin) {
                createNotification(context, stockList,
                    stockName = getStockNameFromSymbol(stock.stockSymbol),
                    stockGrowthRate = getStockGrowthRate(stockList),
                    channelID = stock.stockUID!!)
            }
        }

        return Result.success()
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }



    private fun createNotification(
        context: Context,
        stockList: List<StockTimeSeriesInstance>,
        stockName: String,
        stockGrowthRate: Double,
        channelID: Long
    ) {
        NotificationBuilder().createNotification(
            context,
            stockName,
            stockGrowthRate,
            channelID.toInt()
        )
    }
}