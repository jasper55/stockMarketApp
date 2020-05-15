package jasper.wagner.smartstockmarketing.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.work.*
import jasper.wagner.smartstockmarketing.data.network.USStockMarketApi
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.cryptotracking.common.Common.getWorkTag
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_DB_NAME
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockGrowthRate
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockNameFromSymbol
import jasper.wagner.smartstockmarketing.data.db.StockDatabase
import jasper.wagner.smartstockmarketing.databinding.MainFragmentBinding
import jasper.wagner.smartstockmarketing.domain.model.*
import jasper.wagner.smartstockmarketing.ui.adapter.StockItemAdapter
import jasper.wagner.smartstockmarketing.ui.stockinfo.StockInfoFragment
import jasper.wagner.smartstockmarketing.util.NotificationBuilder.Companion.NOTIFICATION_ID
import jasper.wagner.smartstockmarketing.util.NotifyWorker
import jasper.wagner.smartstockmarketing.util.NotifyWorker.Companion.API_CALL_PARAMS
import jasper.wagner.smartstockmarketing.util.NotifyWorker.Companion.GROWTH_MARGIN
import jasper.wagner.smartstockmarketing.util.SerializeHelper
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import java.io.Serializable
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainFragment : Fragment(), StockItemAdapter.ListItemClickListener {

    private lateinit var binding: MainFragmentBinding

    private val itemList = ArrayList<StockDisplayItem>()
    private val nameList = ArrayList<String>()
    private lateinit var apiParams: StockApiCallParams
    private lateinit var itemAdapter: StockItemAdapter

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(layoutInflater)
//        itemAdapter = StockItemAdapter(this)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        stockDatabase =
            StockDatabase.getInstance(requireActivity().applicationContext)

//        stock_list.layoutManager = LinearLayoutManager(context)
//        stock_list.itemAnimator = DefaultItemAnimator()
//        stock_list.adapter = itemAdapter

        itemAdapter = StockItemAdapter(this)
        stock_list.layoutManager = LinearLayoutManager(requireContext())
        stock_list.itemAnimator = DefaultItemAnimator()
        stock_list.adapter = itemAdapter


//        initAddButton(apiParams)
    }

    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.IO).launch {

            nameList.clear()
            nameList.add("IBM")
//            nameList.add("BAC")
//            nameList.add("BABA")
//            nameList.add("GOLD")
//            nameList.add("BIDU")
//            nameList.add("BLDP")
//            nameList.add("BHC")
//            nameList.add("BK")
//                        nameList.add("BAYRY")    //not working


            /**
            // 1. get all StockValues

            // 2. show stockdata as item on list
            // 2.1 get growth from last hour - needed : List<StockValues>

            // 3. get all data for detail view
            // 3.1 StockItem
            // 3.2 List<StockValues>


            // 4. save data to DB:
            // 4.1. StockValues
            // 4.2. StockData(stock, List<StockValues>)


            neeed: List<StockValues>, List<StockValues>.last() + growth
             **/

            for (symbol in nameList) {

                withContext(Dispatchers.Main){
                    binding.progressBar.visibility = View.VISIBLE
                }

                apiParams = StockApiCallParams(
                    symbol,
                    Common.Function.intraDay,
                    Common.Interval.min1,
                    Common.OutputSize.compact
                )
                val usStockMarketApi = USStockMarketApi()

                // 1. get StockValues List
                val stockValuesList = usStockMarketApi.fetchStockValuesList(apiParams)


                ///-----------------------

                // 2.
                val lastValues = stockValuesList.last()
                val stockItem = StockDisplayItem(
                    stockName = getStockNameFromSymbol(apiParams.stockSymbol),
                    stockSymbol = apiParams.stockSymbol,
                    growthLastHour = getStockGrowthRate(stockValuesList),
                    open = lastValues.open,
                    close = lastValues.close,
                    high = lastValues.high,
                    low = lastValues.low,
                    volume = lastValues.volume
                )

                withContext(Dispatchers.Main) {
                    addToItemList(stockItem)
                }

                ///-----------------------

                // 3.

                ///-----------------------

                // 4.1
                val lastTimeStamp = usStockMarketApi.getLastTimeStamp(apiParams)
                val stock = Stock(
                    stockSymbol = symbol,
                    stockName = getStockNameFromSymbol(symbol),
                    lastTimeStamp = lastTimeStamp
                )
                stockDatabase.stockDao().addStock(stock)

                // 4.2
                stockDatabase.stockValuesDao().addToStockValuesList(stockValuesList)

                // 5. schedule analyzes
                schedulePeriodicStockAnalyzes(apiParams, 0.01, itemList.size + 1)
            }

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                updateView()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        parentJob.cancel()
    }

    private fun initAddButton(apiParams: StockApiCallParams) {
        add_stock.setOnClickListener {

            val bundle = Bundle().apply {
                putSerializable("API_PARAMS", apiParams as Serializable)
            }

            val stockInfoFragment = StockInfoFragment.newInstance()
            stockInfoFragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(jasper.wagner.smartstockmarketing.R.id.container, stockInfoFragment)
                .addToBackStack(null)
                .commit()
        }

    }

    private fun addToItemList(stockDisplayItem: StockDisplayItem) {
        itemList.add(stockDisplayItem)
    }

    private fun updateView() {
        itemAdapter.submitList(itemList)
    }

    private fun schedulePeriodicStockAnalyzes(
        apiParams: StockApiCallParams,
        growthMargin: Double,
        channelId: Int
    ) {

        val repeatInterval = 15L
        val timeUnit = TimeUnit.MINUTES

        val paramsString = SerializeHelper.serializeToJson(apiParams)
        val data = Data.Builder()
            .putString(API_CALL_PARAMS, paramsString)
            .putDouble(GROWTH_MARGIN, growthMargin)
            .putInt(NOTIFICATION_ID, channelId)
            .build()

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(NotifyWorker::class.java, repeatInterval, timeUnit)
                .addTag(getWorkTag(apiParams))
                .setConstraints(constraints)
                .setInputData(data)
                .build()

        WorkManager.getInstance(requireContext().applicationContext)
            .enqueueUniquePeriodicWork(
                getWorkTag(apiParams),
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )

    }

    private fun initDb() {
        val db = Room.databaseBuilder(
            requireContext().applicationContext,
            StockDatabase::class.java, STOCK_DB_NAME
        ).build()
    }

    companion object {
        fun newInstance() = MainFragment()
        const val MAIN_FRAG_TAG = "MainFragment"
    }

    override fun onItemClick(item: StockDisplayItem, position: Int) {
        val name = item.stockSymbol
        val apiParams = StockApiCallParams(
            name,
            Common.Function.intraDay,
            Common.Interval.min1,
            Common.OutputSize.compact
        )
        val bundle = Bundle().apply {
            putSerializable("API_PARAMS", apiParams as Serializable)
        }

//        val bundle = Bundle().apply {
//            putString("STOCK_NAME",item.stockName)
//        }

        val stockInfoFragment = StockInfoFragment.newInstance()
        stockInfoFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(jasper.wagner.smartstockmarketing.R.id.container, stockInfoFragment)
            .addToBackStack(null)
            .commit()
    }
}
