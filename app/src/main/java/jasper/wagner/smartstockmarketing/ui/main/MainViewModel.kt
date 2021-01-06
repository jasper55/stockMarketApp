package jasper.wagner.smartstockmarketing.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import androidx.work.*
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.smartstockmarketing.common.Constants
import jasper.wagner.smartstockmarketing.common.StockOperations
import jasper.wagner.smartstockmarketing.data.Repository
import jasper.wagner.smartstockmarketing.domain.model.Stock
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockDisplayItem
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance
import jasper.wagner.smartstockmarketing.ui.customview.StockLineChartView
import jasper.wagner.smartstockmarketing.util.NotifyWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MainViewModel(application: Application) : AndroidViewModel(application) {


    val context: Context = application.applicationContext
    private val repository = Repository.getInstance(context)
    private var _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _stockList: MutableLiveData<List<Stock>> = MutableLiveData()
    val stockList: LiveData<List<Stock>>
        get() = _stockList

    private val _stockItemList: MutableLiveData<List<StockDisplayItem>> = MutableLiveData()
    val stockItemList: LiveData<List<StockDisplayItem>>
        get() = _stockItemList

    private var _newStockItem: MutableLiveData<StockDisplayItem> = MutableLiveData()
    val newStockItem: LiveData<StockDisplayItem>
        get() = _newStockItem

    private var apiParams: MutableLiveData<StockApiCallParams> = MutableLiveData()
    private var updatedStocks = MutableLiveData<Int>(0)
    private var lastUpdate = MutableLiveData(0L)

    init {
        refreshData()
    }

    private fun creatStockItemWithlastValues(
        stockValuesList: List<StockTimeSeriesInstance>,
        stock: Stock
    ): StockDisplayItem {
        val lastValues = stockValuesList.last()
        val stockLineChartView =
            StockLineChartView(stockValuesList, context).getLineChart()
        return StockDisplayItem(
            stockName = stock.stockName,
            stockSymbol = stock.stockSymbol,
            growthLastHour = StockOperations.getStockGrowthRate(stockValuesList),
            open = lastValues.open,
            close = lastValues.close,
            high = lastValues.high,
            low = lastValues.low,
            volume = lastValues.volume,
            lineChart = stockLineChartView
        )
    }

    private fun updateLastTimeStamp(storedStock: Stock, lastTimeStamp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            storedStock.lastTimeStamp = lastTimeStamp
            repository.updateLastTimeStamp(storedStock)
        }
    }

    fun updateStockData() {
        if ((System.currentTimeMillis() - lastUpdate.value!!) <= 1000 * 60) {
            return
        } else {
            CoroutineScope(Dispatchers.IO).launch {

                _isLoading.postValue(true)
                val stockList = repository.loadAllStocks()

                if (stockList.isNotEmpty()) {
                    val size = stockList.size
                    var updatedStocks = updatedStocks.value!!
                    val startIndex = updatedStocks

                    while (startIndex + 5 >= updatedStocks && updatedStocks < size) {
                        val stock = stockList[updatedStocks]
                        apiParams.postValue(StockApiCallParams(
                            stock.stockSymbol,
                            function = Common.Function.intraDay,
                            interval = Common.Interval.min1,
                            outputSize = Common.OutputSize.full
                        ))
                        val newValues = fetchNewDataFromApi(stock)
                        if (newValues.isNotEmpty()) {
                            storeNewDataToDb(newValues, stock)
                            updateLastTimeStamp(stock, newValues.last().timeStamp)
                        }
                        updatedStocks++
                    }

                    if (updatedStocks == size) {
                        updatedStocks = 0
                    }
                }
                _isLoading.postValue(false)
                lastUpdate.postValue(System.currentTimeMillis())
//                updateView() TODO()
            }
        }
    }


    private fun schedulePeriodicStockAnalyzes(
        stockUID: Long,
        stockSymbol: String,
        growthMargin: Double
    ) {

        val repeatInterval = 15L
        val timeUnit = TimeUnit.MINUTES

        val data = Data.Builder()
            .putDouble(Constants.WorkManager.GROWTH_MARGIN, growthMargin)
            .putLong(Constants.WorkManager.STOCK_UID, stockUID)
            .build()

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(NotifyWorker::class.java, repeatInterval, timeUnit)
                .addTag(Common.getWorkTag(stockSymbol))
                .setConstraints(constraints)
                .setInputData(data)
                .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                Common.getWorkTag(stockSymbol),
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )
    }


    private suspend fun fetchNewDataFromApi(
        storedStock: Stock
    ): ArrayList<StockTimeSeriesInstance>  {
        val params = StockApiCallParams(
                storedStock.stockSymbol,
                Common.Function.intraDay,
                Common.Interval.min1,
                Common.OutputSize.compact
            )
        apiParams.postValue(params)
            return repository.fetchStockValuesList(storedStock.stockUID, params)
    }

    private fun storeNewDataToDb(
        stockDataList: ArrayList<StockTimeSeriesInstance>,
        storedStock: Stock
    ) {
        for (stockData in stockDataList) {
            stockData.stockRelationUID = storedStock.stockUID
            viewModelScope.launch(Dispatchers.IO) {
                repository.addStockValues(stockData)
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            val stockList = repository.loadAllStocks()
//            _stockList.postValue(stockList)
            _isLoading.postValue(true)

            if (stockList.isEmpty()) {
                _isLoading.postValue(false)
            } else {
                withContext(Dispatchers.IO) {
                    for (stock in stockList) {
                        val valuesList =
                            repository.getAllByStockUID(stock.stockUID)
                        if (valuesList.isNotEmpty()) {
                            _newStockItem.value = creatStockItemWithlastValues(valuesList, stock)
//                            _stockItemList.value.a

                        }
                    }
                    _isLoading.postValue(false)
                }
            }
        }
    }
}
