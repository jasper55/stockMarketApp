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
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainFragment : Fragment() {


    internal lateinit var client: OkHttpClient
    internal lateinit var request: Request


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
//        val view = inflater.inflate(R.layout.main_fragment, container, false)
//        openTv = view.findViewById(R.id.open)
//        closeTv = view.findViewById(R.id.close)
//        highTv = view.findViewById(R.id.high)
//        lowTv = view.findViewById(R.id.low)
//        progressBar = view.findViewById(R.id.progress_bar)
//        errorMessage = view.findViewById(R.id.error_message)

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
//
//
//        errorMessage.setOnClickListener {
        binding.progressBar.visibility = View.GONE
        binding.open.text = "open: ${stockData.open}"
        binding.close.text = "close: ${stockData.close}"
        binding.high.text = "high: ${stockData.high}"
        binding.low.text = "low: ${stockData.low}"
//        }
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

                            val date = getDate(lastRefreshed)
                            val time = getTime(date, lastRefreshed)
                            val hour = getHour(time)
                            val minute = getMinute(time,timeInterval)
                            Log.d("DATE", getDate(lastRefreshed))
                            Log.d("TIME", getTime(date,lastRefreshed))

                            val formattedTimestamp = "$date$hour:$minute:00"
                            Log.d("TIME_STAMP", formattedTimestamp)


                            if (jsonResponse.has("Time Series ($timeInterval)")) {
                                val data = jsonResponse.getJSONObject("Time Series ($timeInterval)")
                                val stockDataObject = data.getJSONObject(formattedTimestamp)
                                val open = stockDataObject.getString("1. open").toDouble()
                                val high = stockDataObject.getString("2. high").toDouble()
                                val low = stockDataObject.getString("3. low").toDouble()
                                val close = stockDataObject.getString("4. close").toDouble()
                                val volume = stockDataObject.getString("5. volume").toDouble()

                                val stockData = StockData(
                                    open = open,
                                    high = high,
                                    low = low,
                                    volume = volume,
                                    close = close
                                )

                                scopeMainThread.launch {
                                    updateView(stockData)
                                    Log.d("OPEN", stockData.toString())
                                }
                            }
                        }
                    }
                })
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
