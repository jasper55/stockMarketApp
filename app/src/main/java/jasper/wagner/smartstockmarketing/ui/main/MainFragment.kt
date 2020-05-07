package jasper.wagner.smartstockmarketing.ui.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.*
import com.example.workoutreminder.data.network.USStockMarketApi
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockGrowthRate
import jasper.wagner.smartstockmarketing.databinding.MainFragmentBinding
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockData
import jasper.wagner.smartstockmarketing.util.MathOperation
import jasper.wagner.smartstockmarketing.util.NotificationBuilder
import jasper.wagner.smartstockmarketing.util.NotifyWorker
import jasper.wagner.smartstockmarketing.util.NotifyWorker.Companion.API_CALL_PARAMS
import jasper.wagner.smartstockmarketing.util.NotifyWorker.Companion.GROWTH_MARGIN
import jasper.wagner.smartstockmarketing.util.NotifyWorker.Companion.PERIODIC_WORK_TAG
import jasper.wagner.smartstockmarketing.util.NotifyWorker.Companion.STOCK_DATA_AVAILABLE
import jasper.wagner.smartstockmarketing.util.SerializeHelper
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import lecho.lib.hellocharts.model.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class MainFragment : Fragment() {

    private lateinit var binding: MainFragmentBinding

    private val parentJob = Job()
    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            coroutineScope.launch(Dispatchers.Main) {
//                binding.errorContainer.visibility = View.VISIBLE
//                binding.errorContainer.text = throwable.message
            }
            GlobalScope.launch { println("Caught $throwable") }
        }
    private val coroutineScope =
        CoroutineScope(Dispatchers.Main + parentJob + coroutineExceptionHandler)
    private val scopeMainThread =
        CoroutineScope(parentJob + Dispatchers.Main + coroutineExceptionHandler)

    private lateinit var viewModel: MainViewModel

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

        initAddButton()
    }

    override fun onResume() {
        super.onResume()
        val stockName = "IBM"
        val apiParams = StockApiCallParams(
            stockName,
            Common.Function.intraDay,
            Common.Interval.min1,
            Common.OutputSize.compact
        )
        schedulePeriodicStockAnalyzes(apiParams, 0.01)
    }

    override fun onDestroy() {
        super.onDestroy()
        parentJob.cancel()
    }

    private fun initAddButton() {
        add_stock.setOnClickListener {
            CoroutineScope(IO).launch {

                withContext(Main) {
                    binding.progressBar.visibility = View.VISIBLE
                }

                var stockList = ArrayList<StockData>()
                withContext(IO) {
                    val stockName = "IBM"
                    val apiParams = StockApiCallParams(
                        stockName,
                        Common.Function.intraDay,
                        Common.Interval.min1,
                        Common.OutputSize.compact
                    )
                    val usStockMarketApi = USStockMarketApi()
                    stockList = usStockMarketApi.fetchStockMarketData(apiParams)
                }

                withContext(Main) {
                    updateView(stockList.last())
                    showDifferenceToOneHour(getStockGrowthRate(stockList))
                    showLineChart(stockList)
                }
            }
        }

    }


    private fun updateView(stockData: StockData) {
        binding.progressBar.visibility = View.GONE
        binding.stockName.text = "${stockData.stockName}"
        binding.open.text = "open: ${stockData.open}"
        binding.close.text = "close: ${stockData.close}"
        binding.high.text = "high: ${stockData.high}"
        binding.low.text = "low: ${stockData.low}"
        binding.volume.text = "volume: ${stockData.volume}"
    }

    private fun showDifferenceToOneHour(stockGrowthRate: Double) {
        if (stockGrowthRate >= 0)
            stock_development_last_hour.setTextColor(Color.GREEN)
        else stock_development_last_hour.setTextColor(Color.RED)
        stock_development_last_hour.text = "growth rate: $stockGrowthRate %"
        stock_development_last_hour.visibility = View.VISIBLE
    }

    private fun showLineChart(stockList: ArrayList<StockData>) {
        val yAxisValues = ArrayList<PointValue>()
        val axisValues = ArrayList<AxisValue>()

        val line = Line(yAxisValues).setColor(Color.parseColor("#9C27B0"))
        line.pointRadius = 0
        line.strokeWidth = 2

        val lines = ArrayList<Line>()
        lines.add(line)

        val size = stockList.size
        val list = stockList.reversed()

        var i = 0
        while (i < size) {
            axisValues.add(i, AxisValue(i.toFloat()).setLabel(list[i].time))
            yAxisValues.add(i, PointValue(i.toFloat(), (list[i].close).toFloat()))
            i += 1
        }

        val axis = Axis()
        val yAxis = Axis()
        axis.values = axisValues
        axis.textSize = 12
        axis.textColor = Color.parseColor("#03A9F4")
        yAxis.textColor = Color.parseColor("#03A9F4")
        yAxis.textSize = 12

        val data = LineChartData()
        data.lines = lines

        data.axisXBottom = axis
        data.axisYLeft = yAxis

        line_chart.lineChartData = data
//        val viewport = Viewport(line_chart.maximumViewport)
//        viewport.top = 110f
//        line_chart.maximumViewport = viewport
//        line_chart.currentViewport = viewport

        line_chart.visibility = View.VISIBLE
    }

    private fun schedulePeriodicStockAnalyzes(
        apiParams: StockApiCallParams,
        growthMargin: Double
    ) {

        val repeatInterval = 1L
        val timeUnit = TimeUnit.MINUTES

        val paramsString = SerializeHelper.serializeToJson(apiParams)
        val data = Data.Builder()
            .putString(API_CALL_PARAMS, paramsString)
            .putDouble(GROWTH_MARGIN, growthMargin)
            .build()

//        val constraints =
//            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val periodicWorkRequest =
            PeriodicWorkRequest.Builder(NotifyWorker::class.java, repeatInterval, timeUnit)
                .addTag(PERIODIC_WORK_TAG)
                .setInitialDelay(repeatInterval,timeUnit)
//                .setConstraints(constraints)
                .setInputData(data)
                .build()

        WorkManager.getInstance(requireContext().applicationContext)
            .enqueueUniquePeriodicWork(
                PERIODIC_WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )

    }

    companion object {
        fun newInstance() = MainFragment()
    }
}
