package jasper.wagner.smartstockmarketing.ui.main

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
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min


class MainFragment : Fragment() {

    internal lateinit var client: OkHttpClient
    internal lateinit var request: Request

    private var stockList = listOf<StockData>()
    private lateinit var binding: MainFragmentBinding

    private val parentJob = Job()
    private val coroutineExceptionHandler: CoroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            coroutineScope.launch(Dispatchers.Main) {
                binding.errorMessage.visibility = View.VISIBLE
                binding.errorMessage.text = throwable.message
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
            binding.errorMessage.visibility = View.VISIBLE

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


                                val formattedTimestamp =
                                    getFormattedTimeStamp(minute, hour, date)
                                Log.d("TIME_STAMP", formattedTimestamp)

                                timeStampAvailable = data.has(formattedTimestamp)
                                if (timeStampAvailable) {
                                    data.getJSONObject(formattedTimestamp).apply {

                                        val stockData = StockData(
                                            open = getString("1. open").toDouble(),
                                            high = getString("2. high").toDouble(),
                                            low = getString("3. low").toDouble(),
                                            close = getString("4. close").toDouble(),
                                            volume = getString("5. volume").toDouble()
                                        )

                                        stockList.plus(stockData)

                                        scopeMainThread.launch {
                                            updateView(stockData)
                                            Log.d("OPEN", stockData.toString())
                                        }
                                    }
                                    if (minute > 1) {
                                        minute -= 1
                                    } else {
                                        minute = 0
                                        hour -= 1
                                    }
                                }

                            }
                        }
                    }
                }
            })
    }

    private fun getFormattedTimeStamp(
        minute: Int,
        hour: Int,
        date: String
    ): String {
        if (minute.length() == 1 && hour.length() == 1) {
            return "$date 0$hour:0$minute:00"

        } else if (minute.length() == 2 && hour.length() == 1) {
            return "$date 0$hour:$minute:00"

        } else if (minute.length() == 1 && hour.length() == 2) {
            return "$date $hour:0$minute:00"
        } else {
            return "$date $hour:$minute:00"
        }
    }


    fun getPrice(text: String): Float {
        if (text.trim().length == 0 || ("Invalid" in text)) return -1.0f
        val message = text
        val json = JSONObject(message)
        val today = Date()
        var DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd")
        var dateKey = DATE_FORMAT.format(today) as String
// get price from today
        val todaysStockPrices =
            ((json["Time Series (Daily)"] as JSONObject)[dateKey] as JSONObject)
                .getString("4. close")
        return todaysStockPrices.toFloat()
    }


    companion object {
        fun newInstance() = MainFragment()
    }
}
