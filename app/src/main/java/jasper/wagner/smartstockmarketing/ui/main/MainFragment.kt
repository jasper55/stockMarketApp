package jasper.wagner.smartstockmarketing.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import jasper.wagner.smartstockmarketing.data.network.USStockMarketApi
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.cryptotracking.common.Common.getWorkTag
import jasper.wagner.smartstockmarketing.common.Constants.Bundle.STOCK_SYMBOL
import jasper.wagner.smartstockmarketing.common.Constants.WorkManager.GROWTH_MARGIN
import jasper.wagner.smartstockmarketing.common.Constants.WorkManager.STOCK_UID
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockGrowthRate
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockNameFromSymbol
import jasper.wagner.smartstockmarketing.data.db.StockDatabase
import jasper.wagner.smartstockmarketing.databinding.MainFragmentBinding
import jasper.wagner.smartstockmarketing.domain.model.*
import jasper.wagner.smartstockmarketing.ui.adapter.StockItemAdapter
import jasper.wagner.smartstockmarketing.ui.customview.StockLineChartView
import jasper.wagner.smartstockmarketing.ui.search.SearchFragment
import jasper.wagner.smartstockmarketing.ui.stockinfo.StockInfoFragment
import jasper.wagner.smartstockmarketing.util.NotifyWorker
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainFragment : Fragment(), StockItemAdapter.ListItemClickListener {

    private lateinit var binding: MainFragmentBinding

    private val itemList = ArrayList<StockDisplayItem>()
    private val nameList = ArrayList<String>()
    private lateinit var apiParams: StockApiCallParams
    private lateinit var itemAdapter: StockItemAdapter
    private var updatedStocks = 0
    private var lastUpdate = 0L

//    private val parentJob = Job()
//    private val coroutineExceptionHandler: CoroutineExceptionHandler =
//        CoroutineExceptionHandler { _, throwable ->
//            coroutineScope.launch(Dispatchers.Main) {
////                binding.errorContainer.visibility = View.VISIBLE
////                binding.errorContainer.text = throwable.message
//            }
//            GlobalScope.launch { println("Caught $throwable") }
//        }
//    private val coroutineScope =
//        CoroutineScope(Dispatchers.Main + parentJob + coroutineExceptionHandler)
//    private val scopeMainThread =
//        CoroutineScope(parentJob + Dispatchers.Main + coroutineExceptionHandler)

    private lateinit var viewModel: MainViewModel

    private lateinit var stockDatabase: StockDatabase

    private lateinit var usStockMarketApi: USStockMarketApi

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        stockDatabase =
            StockDatabase.getInstance(requireActivity().applicationContext)

        itemAdapter = StockItemAdapter(this)
        stock_list.layoutManager = LinearLayoutManager(requireContext())
        stock_list.itemAnimator = DefaultItemAnimator()
        stock_list.adapter = itemAdapter

        usStockMarketApi = USStockMarketApi()
        initAddButton()
        initRefreshButton()
    }


    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {

            showProgressbar()
            itemList.clear()

            withContext(Dispatchers.IO) {
                val stockList = stockDatabase.stockDao().loadAllStocks()

                if (stockList.isEmpty()) {
                    hideProgressbar()
                    return@withContext
                } else {
                    withContext(Dispatchers.IO) {
                        for (stock in stockList) {
                            val valuesList =
                                stockDatabase.stockValuesDao()
                                    .getAllByListStockUID(stock.stockUID!!)
                            if (valuesList.isNotEmpty()) {
//                                stock_name_list.text = (stock_name_list.text).plus()

                                    val stockItem = creatStockItemWithlastValues(valuesList, stock)
                                    displayItemOnList(stockItem)
                            }
                        }
                        hideProgressbar()
                    }
                }
            }
        }
    }

    private suspend fun displayItemOnList(stockItem: StockDisplayItem) {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
            addToItemList(stockItem)
            updateView()
        }
    }

    private fun updateLastTimeStamp(storedStock: Stock, lastTimeStamp: String) {
        storedStock.lastTimeStamp = lastTimeStamp
        stockDatabase.stockDao().updateLastTimeStamp(storedStock)

    }

    private suspend fun creatStockItemWithlastValues(
        stockValuesList: List<StockTimeSeriesInstance>,
        stock: Stock
    ): StockDisplayItem = withContext(Dispatchers.Main) {
        val lastValues = stockValuesList.last()
        val stockLineChartView =
            StockLineChartView(stockValuesList, requireContext()).getLineChart()
        return@withContext StockDisplayItem(
            stockName = stock.stockName,
            stockSymbol = stock.stockSymbol,
            growthLastHour = getStockGrowthRate(stockValuesList),
            open = lastValues.open,
            close = lastValues.close,
            high = lastValues.high,
            low = lastValues.low,
            volume = lastValues.volume,
            lineChart = stockLineChartView
        )
    }

    private suspend fun fetchNewDataFromApi(
        storedStock: Stock
    ): ArrayList<StockTimeSeriesInstance> {
        apiParams = StockApiCallParams(
            storedStock.stockSymbol,
            Common.Function.intraDay,
            Common.Interval.min1,
            Common.OutputSize.compact
        )
        return usStockMarketApi.fetchStockValuesList(storedStock.stockUID!!, apiParams)
    }

    private fun storeNewDataToDb(
        stockValuesList: ArrayList<StockTimeSeriesInstance>,
        storedStock: Stock
    ) {
        for (values in stockValuesList) {
            values.stockRelationUID = storedStock.stockUID!!
            stockDatabase.stockValuesDao().addStockValues(values)
        }
    }


    private fun initAddButton() {
        add_stock.setOnClickListener {
            val searchFragment = SearchFragment.newInstance()

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(jasper.wagner.smartstockmarketing.R.id.container, searchFragment)
                .addToBackStack(null)
                .commit()
        }

    }

    private fun initRefreshButton() {
        refresh_button.setOnClickListener {
            updateStockData()
        }
    }

    private fun updateStockData() {
        if ((System.currentTimeMillis() - lastUpdate) <= 1000 * 60) {
            return
        } else {
            CoroutineScope(Dispatchers.IO).launch {

                showProgressbar()
                val stockList = stockDatabase.stockDao().loadAllStocks()

                if (stockList.isNotEmpty()) {
                    val size = stockList.size
                    val startIndex = updatedStocks

                    while (startIndex + 5 >= updatedStocks && updatedStocks <= size) {
                        val stock = stockList[updatedStocks-1]
                        apiParams = StockApiCallParams(
                            stock.stockSymbol,
                            function = Common.Function.intraDay,
                            interval = Common.Interval.min1,
                            outputSize = Common.OutputSize.compact
                        )
                        val newValues = fetchNewDataFromApi(stock)
                        if (newValues.isNotEmpty()) {
                            storeNewDataToDb(newValues, stock)
                            updateLastTimeStamp(stock, newValues.last().timeStamp)
                        }
                        updatedStocks++
                    }
                    if (updatedStocks == size){
                        updatedStocks = 0
                    }
                }
                hideProgressbar()
                lastUpdate = System.currentTimeMillis()
                updateView()
            }
        }
    }

    private suspend fun showProgressbar() {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private suspend fun hideProgressbar() {
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
        }
    }


    private fun addToItemList(stockDisplayItem: StockDisplayItem) {
        itemList.add(stockDisplayItem)
    }

    private fun updateView() {
        itemAdapter.submitList(itemList)
    }

    private fun schedulePeriodicStockAnalyzes(
        stockUID: Long,
        stockSymbol: String,
        growthMargin: Double
    ) {

        val repeatInterval = 15L
        val timeUnit = TimeUnit.MINUTES

        val data = Data.Builder()
            .putDouble(GROWTH_MARGIN, growthMargin)
            .putLong(STOCK_UID, stockUID)
            .build()

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(NotifyWorker::class.java, repeatInterval, timeUnit)
                .addTag(getWorkTag(stockSymbol))
                .setConstraints(constraints)
                .setInputData(data)
                .build()

        WorkManager.getInstance(requireContext().applicationContext)
            .enqueueUniquePeriodicWork(
                getWorkTag(stockSymbol),
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )

    }

    companion object {
        fun newInstance() = MainFragment()
        const val MAIN_FRAG_TAG = "MainFragment"
    }

    override fun onItemClick(item: StockDisplayItem, position: Int) {

        val bundle = Bundle().apply {
            putString(STOCK_SYMBOL, item.stockSymbol)
        }

        val stockInfoFragment = StockInfoFragment.newInstance()
        stockInfoFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(jasper.wagner.smartstockmarketing.R.id.container, stockInfoFragment)
            .addToBackStack(null)
            .commit()
    }
}
