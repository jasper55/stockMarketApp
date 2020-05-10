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
import com.example.workoutreminder.data.network.USStockMarketApi
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockGrowthRate
import jasper.wagner.smartstockmarketing.databinding.MainFragmentBinding
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockData
import jasper.wagner.smartstockmarketing.ui.adapter.StockItemAdapter
import jasper.wagner.smartstockmarketing.ui.stockinfo.StockInfoFragment
import jasper.wagner.smartstockmarketing.util.NotifyWorker
import jasper.wagner.smartstockmarketing.util.NotifyWorker.Companion.API_CALL_PARAMS
import jasper.wagner.smartstockmarketing.util.NotifyWorker.Companion.GROWTH_MARGIN
import jasper.wagner.smartstockmarketing.util.NotifyWorker.Companion.PERIODIC_WORK_TAG
import jasper.wagner.smartstockmarketing.util.SerializeHelper
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainFragment : Fragment(), StockItemAdapter.ListItemClickListener {

    private lateinit var binding: MainFragmentBinding

    private val itemList = ArrayList<StockData>()
    private val nameList = ArrayList<String>()
    private lateinit var apiParams: StockApiCallParams
    private lateinit var itemAdapter : StockItemAdapter

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
            val usStockMarketApi = USStockMarketApi()
            nameList.clear()
            val stockName = "IBM"
            nameList.add("IBM")
            nameList.add("BAC")
            nameList.add("BABA")
//            nameList.add("GOLD")
//            nameList.add("BIDU")
//            nameList.add("BAYRY")    //not working
//            nameList.add("BLDP")
//            nameList.add("BHC")
//            nameList.add("BK")

            for (name in nameList) {
                apiParams = StockApiCallParams(
                    name,
                    Common.Function.intraDay,
                    Common.Interval.min1,
                    Common.OutputSize.compact
                )


                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.VISIBLE
                    var growth = 0.0

                    var stockDataList = ArrayList<StockData>()
                    withContext(Dispatchers.IO) {
                        stockDataList = usStockMarketApi.fetchStockMarketData(apiParams)
                        growth = getStockGrowthRate(stockDataList)
                    }

                    withContext(Dispatchers.Main) {
                        addToList(
                            stockDataList.last()
                                .copy(growth = growth)
                        )

                    }
                }
            }
            schedulePeriodicStockAnalyzes(apiParams, 0.01)

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
                putSerializable("API_PARAMS",apiParams as Serializable)
            }

            val stockInfoFragment = StockInfoFragment.newInstance()
            stockInfoFragment.arguments = bundle

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(jasper.wagner.smartstockmarketing.R.id.container, stockInfoFragment)
                .addToBackStack(null)
                .commit()
        }

    }

    private fun addToList(stockData: StockData){
        itemList.add(stockData)
//        (0..Random().nextInt(100)).mapTo(itemList) { stockData }
    }

    private fun updateView(){
        itemAdapter.submitList(itemList)
    }

    private fun schedulePeriodicStockAnalyzes(
        apiParams: StockApiCallParams,
        growthMargin: Double
    ) {

        val repeatInterval = 15L
        val timeUnit = TimeUnit.MINUTES

        val paramsString = SerializeHelper.serializeToJson(apiParams)
        val data = Data.Builder()
            .putString(API_CALL_PARAMS, paramsString)
            .putDouble(GROWTH_MARGIN, growthMargin)
            .build()

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(NotifyWorker::class.java, repeatInterval, timeUnit)
                .addTag(PERIODIC_WORK_TAG)
                .setConstraints(constraints)
                .setInputData(data)
                .build()

        WorkManager.getInstance(requireContext().applicationContext)
            .enqueueUniquePeriodicWork(
                PERIODIC_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                periodicWorkRequest
            )

    }

    companion object {
        fun newInstance() = MainFragment()
        const val MAIN_FRAG_TAG = "MainFragment"
    }

    override fun onItemClick(item: StockData, position: Int) {
        val name = item.stockName
        val apiParams = StockApiCallParams(
        name,
        Common.Function.intraDay,
        Common.Interval.min1,
        Common.OutputSize.compact
        )
        val bundle = Bundle().apply {
            putSerializable("API_PARAMS",apiParams as Serializable)
        }

        val stockInfoFragment = StockInfoFragment.newInstance()
        stockInfoFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(jasper.wagner.smartstockmarketing.R.id.container, stockInfoFragment)
            .addToBackStack(null)
            .commit()
    }
}
