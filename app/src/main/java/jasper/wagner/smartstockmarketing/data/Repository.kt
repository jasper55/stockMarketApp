package jasper.wagner.smartstockmarketing.data

import android.content.Context
import jasper.wagner.smartstockmarketing.data.local.StockDatabase
import jasper.wagner.smartstockmarketing.data.remote.USStockMarketApi
import jasper.wagner.smartstockmarketing.domain.model.Stock
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance

class Repository constructor(
    private val context: Context
) {


    val database = StockDatabase.getInstance(context)
    val stockDao = database.stockDao()
    val stockInfoDao = database.stockInfoDao()
    val stockValuesDao = database.stockValuesDao()
    private val usStockMarketApi = USStockMarketApi()



    suspend fun updateLastTimeStamp(storedStock: Stock) {
        stockDao.updateLastTimeStamp(storedStock)
    }

    suspend fun loadAllStocks(): List<Stock> {
        return stockDao.loadAllStocks()
    }

    suspend fun getAllByStockUID(stockUID: Long): List<StockTimeSeriesInstance> {
        return stockValuesDao.getAllByStockUID(stockUID)
    }

    suspend fun addStockValues(stockData: StockTimeSeriesInstance) {
        stockValuesDao.addStockValues(stockData)
    }

    suspend fun fetchStockValuesList(stockUID: Long, apiParams: StockApiCallParams): ArrayList<StockTimeSeriesInstance> {
        return usStockMarketApi.fetchStockValuesList(stockUID, apiParams)
    }

    companion object {
        private var instance: Repository? = null
        fun getInstance(context: Context): Repository {
            if (instance == null) {
                instance = Repository(context)
            }
            return instance as Repository
        }
    }
}