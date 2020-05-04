package jasper.wagner.smartstockmarketing.util

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.workoutreminder.data.network.USStockMarketApi
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockData
import kotlinx.android.synthetic.main.main_fragment.*
import kotlin.math.abs


class NotifyWorker(@NonNull context: Context, @NonNull params: WorkerParameters) :
    CoroutineWorker(context, params) {

    val context = context
//    private var stockList = ArrayList<StockData>()

    @NonNull
    override suspend fun doWork(): Result {
        // Method to trigger an instant notification
        val paramsString = inputData.getString(API_CALL_PARAMS)
        val criticalGrowthRate = inputData.getDouble(CRITICAL_GROWTH_RATE,0.01)

        val params = SerializeHelper.deserializeFromJson(paramsString!!) as StockApiCallParams


        val usStockMarketApi = USStockMarketApi()
        val stockList = usStockMarketApi.fetchStockMarketData(params)
        Log.d("NOTIFYWORKER", stockList.toString())
        val stockGrowthRate = calculateStockGrowthRate(stockList)

        triggerNotification(context, stockList[0].stockName, stockGrowthRate)


        if (abs(stockGrowthRate) >= criticalGrowthRate)
            triggerNotification(context, stockList[0].stockName, stockGrowthRate)

        return Result.success(createOutputData(stockList,stockGrowthRate))
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

    private fun createOutputData(stockList: ArrayList<StockData>, stockGrowthRate: Double): Data {

        val stockListString = SerializeHelper.serializeToJson(stockList)
        return Data.Builder()
            .putString(STOCK_LIST, stockListString)
            .putDouble(STOCK_GROWTH_RATE, stockGrowthRate)
            .build()
    }

    private fun calculateStockGrowthRate(stockList: ArrayList<StockData>): Double {
        val size = stockList.size
        var stockGrowthRate: Double
        if (size >= 59) {
            stockGrowthRate = ((stockList[0].close / stockList[59].close) * 100) - 100
        } else {
            stockGrowthRate = ((stockList[0].close / stockList[size - 1].close) * 100) - 100
        }
        return MathOperation.round(stockGrowthRate)
    }

    companion object {
        const val STOCK_NAME = "stock name"
        const val STOCK_GROWTH_RATE = "stock growth rate"
        const val CRITICAL_GROWTH_RATE = "critical growth rate"
        const val PERIODIC_WORK_TAG = "periodic notification workout work"
        const val API_CALL_PARAMS = "API call_params"
        const val STOCK_LIST = "stock list"
    }
}