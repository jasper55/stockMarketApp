package jasper.wagner.smartstockmarketing.ui.main

import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
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
                binding.stockDevelopmentLastHour.visibility = View.VISIBLE
                binding.stockDevelopmentLastHour.text = throwable.message
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

                                        val stockData = StockData(
                                            stockName = stockName,
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
                                    } else if (minute == 0)  {
                                        minute = 59
                                        hour -= 1
                                    }

                                }

                            }
                        }
                    }
                    scopeMainThread.launch {
                        updateView(stockList[0])
                        showDifferenceToOneHour(stockList)
                    }
                }
            })
    }

    private fun showDifferenceToOneHour(stockList: List<StockData>) {
        val size = stockList.size
        var stockGrowthRate: Double
        if (size >= 59) {
            stockGrowthRate = ((stockList[0].close/stockList[59].close)*100)-100
        } else {
            stockGrowthRate = ((stockList[0].close/stockList[size-1].close)*100)-100
        }
        stockGrowthRate = round(stockGrowthRate)
        if (stockGrowthRate >= 0 )
            stockDevelopmentLastHour.setTextColor(Color.GREEN)
        else stockDevelopmentLastHour.setTextColor(Color.RED)
        stockDevelopmentLastHour.text = "$stockGrowthRate %"
        stockDevelopmentLastHour.visibility = View.VISIBLE

        if (abs(stockGrowthRate) >= 0.01){
            val notificationBuilder = NotificationBuilder()
            notificationBuilder.createNotification(requireContext(),
                stockList[0].stockName,stockGrowthRate)
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

    companion object {
        fun newInstance() = MainFragment()
    }
}
