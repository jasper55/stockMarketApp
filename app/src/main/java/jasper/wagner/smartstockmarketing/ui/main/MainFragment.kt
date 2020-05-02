package jasper.wagner.smartstockmarketing.ui.main

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.smartstockmarketing.databinding.MainFragmentBinding
import jasper.wagner.smartstockmarketing.model.StockData
import jasper.wagner.smartstockmarketing.util.DateFormatter.getDate
import jasper.wagner.smartstockmarketing.util.DateFormatter.getHour
import jasper.wagner.smartstockmarketing.util.DateFormatter.getMinute
import jasper.wagner.smartstockmarketing.util.DateFormatter.getTime
import jasper.wagner.smartstockmarketing.util.DateFormatter.length
import jasper.wagner.smartstockmarketing.util.MathOperation.round
import jasper.wagner.smartstockmarketing.util.NotificationBuilder
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import lecho.lib.hellocharts.model.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import kotlin.math.abs


class MainFragment : Fragment() {

    internal lateinit var client: OkHttpClient
    internal lateinit var request: Request

    private var stockList = ArrayList<StockData>()
    private lateinit var binding: MainFragmentBinding

    private val parentJob = Job()
    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            coroutineScope.launch(Dispatchers.Main) {
//                binding.stockDevelopmentLastHour.visibility = View.VISIBLE
//                binding.stockDevelopmentLastHour.text = throwable.message
            }
            GlobalScope.launch { println("Caught $throwable") }
        }
    private val coroutineScope =
        CoroutineScope(Dispatchers.Main + parentJob + coroutineExceptionHandler)
    private val scopeMainThread =
        CoroutineScope(parentJob + Dispatchers.Main + coroutineExceptionHandler)

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        val stockName = "IBM"

        coroutineScope.launch(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE

            getLastStockData(
                Common.Function.intraDay,
                stockName,
                Common.Interval.min1,
                Common.OutputSize.full
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        parentJob.cancel()
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


    private suspend fun getLastStockData(
        function: String,
        stockName: String,
        interval: String,
        outputSize: String
    ) = withContext(Dispatchers.IO) {

        stockList.clear()
        val url = Common.createApiLink(
            function = function,
            stockName = stockName,
            interval = interval,
            outputSize = outputSize
        )

        client = OkHttpClient()
        request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d("ERROR", e.toString())
                }

                override fun onResponse(call: Call, response: Response) {
                    val body = response.body!!.string()
                    val jsonResponse = JSONObject(body)
                    Log.d("API body response", body)

                    if (jsonResponse.has("Meta Data")) {
                        val metaData = jsonResponse.getJSONObject("Meta Data")

                        val lastRefreshed = metaData.get("3. Last Refreshed").toString()
                        val timeInterval = metaData.get("4. Interval").toString()

                        if (jsonResponse.has("Time Series ($timeInterval)")) {
                            val data = jsonResponse.getJSONObject("Time Series ($timeInterval)")
                            val date = getDate(lastRefreshed)
                            val time = getTime(date, lastRefreshed)
                            var hour = getHour(time)
                            var minute = getMinute(time, timeInterval)
                            Log.d("DATE", getDate(lastRefreshed))
                            Log.d("TIME", getTime(date, lastRefreshed))

                            var timeStampAvailable = true
                            while (timeStampAvailable) {

                                val formattedTimestamp = getFormattedTimeStamp(minute, hour, date)
                                Log.d("TIME_STAMP", formattedTimestamp)

                                timeStampAvailable = data.has(formattedTimestamp)
                                if (timeStampAvailable) {
                                    data.getJSONObject(formattedTimestamp).apply {

                                        var min = ""
                                        min = if (minute == 0) {
                                            "00"
                                        } else {
                                            minute.toString()
                                        }

                                        val stockData = StockData(
                                            stockName = stockName,
                                            time = "$hour:$min",
                                            open = getString("1. open").toDouble(),
                                            high = getString("2. high").toDouble(),
                                            low = getString("3. low").toDouble(),
                                            close = getString("4. close").toDouble(),
                                            volume = getString("5. volume").toDouble()
                                        )

                                        stockList.add(stockData)
                                    }

                                    if (minute >= 1) {
                                        minute -= 1
                                    } else if (minute == 0) {
                                        minute = 59
                                        hour -= 1
                                    }
                                }
                            }
                        }
                    }
                    scopeMainThread.launch {
                        showLineChart()
                        updateView(stockList[0])
                        showDifferenceToOneHour()
                    }
                }
            })
    }

    private fun showDifferenceToOneHour() {
        val size = stockList.size
        var stockGrowthRate: Double
        if (size >= 59) {
            stockGrowthRate = ((stockList[0].close / stockList[59].close) * 100) - 100
        } else {
            stockGrowthRate = ((stockList[0].close / stockList[size - 1].close) * 100) - 100
        }
        stockGrowthRate = round(stockGrowthRate)
        if (stockGrowthRate >= 0)
            stock_development_last_hour.setTextColor(Color.GREEN)
        else stock_development_last_hour.setTextColor(Color.RED)
        stock_development_last_hour.text = "growth rate: $stockGrowthRate %"
        stock_development_last_hour.visibility = View.VISIBLE

        if (abs(stockGrowthRate) >= 0.01) {
            val notificationBuilder = NotificationBuilder()
            notificationBuilder.createNotification(
                requireContext(),
                stockList[0].stockName, stockGrowthRate
            )
        }
    }

    private fun getFormattedTimeStamp(
        minute: Int,
        hour: Int,
        date: String
    ): String {
        return if (minute.length() == 1 && hour.length() == 1) {
            "$date 0$hour:0$minute:00"

        } else if (minute.length() == 2 && hour.length() == 1) {
            "$date 0$hour:$minute:00"

        } else if (minute.length() == 1 && hour.length() == 2) {
            "$date $hour:0$minute:00"
        } else {
            "$date $hour:$minute:00"
        }
    }

    private fun showLineChart() {
        val yAxisValues = ArrayList<PointValue>()
        val axisValues = ArrayList<AxisValue>()

        val axisData = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "June", "July", "Aug", "Sept", "Oct", "Nov", "Dec"
        )

        val yAxisData = intArrayOf(50, 20, 15, 30, 20, 60, 15, 40, 45, 10, 90, 18)

        val line = Line(yAxisValues).setColor(Color.parseColor("#9C27B0"))
        line.pointRadius = 0
        line.strokeWidth = 2
        val size = stockList.size

        Log.d("SIZE", size.toString())
        val list = stockList.reversed()

        var i = 0
        while (i < size) {
            axisValues.add(i, AxisValue(i.toFloat()).setLabel(list[i].time))
            yAxisValues.add(i, PointValue(i.toFloat(), (list[i].close).toFloat()))
            i += 1
        }

        val lines = ArrayList<Line>()
        lines.add(line)

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

    companion object {
        fun newInstance() = MainFragment()
    }
}
