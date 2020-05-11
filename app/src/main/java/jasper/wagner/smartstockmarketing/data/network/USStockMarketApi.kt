package jasper.wagner.smartstockmarketing.data.network

import android.util.Log
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockGrowthRate
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockNameFromSymbol
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockItem
import jasper.wagner.smartstockmarketing.domain.model.StockValues
import jasper.wagner.smartstockmarketing.util.DateFormatter
import jasper.wagner.smartstockmarketing.util.DateFormatter.length
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject

class USStockMarketApi {

    internal lateinit var client: OkHttpClient
    internal lateinit var request: Request

    private fun initApiCall(params: StockApiCallParams) {


        val url = Common.createApiLink(
            function = params.function,
            stockName = params.stockSymbol,
            interval = params.interval,
            outputSize = params.outputSize
        )

        client = OkHttpClient()
        request = Request.Builder()
            .url(url)
            .build()
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

    suspend fun fetchStockMarketData(apiParams: StockApiCallParams): StockItem =
        withContext(IO) {
            initApiCall(apiParams)

            val response = client.newCall(request)
                .execute()

            return@withContext getStockItemFromResponse(apiParams, response)
        }

    suspend fun fetchStockMarketData2(apiParams: StockApiCallParams): ArrayList<StockValues> =
        withContext(IO) {
            initApiCall(apiParams)

            val response = client.newCall(request)
                .execute()
//            .enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    Log.d("ERROR", e.toString())
//                }
//
//                override fun onResponse(call: Call, response: Response) {
            return@withContext getStockValuesFromResponse(response)
        }

    private fun getStockValuesFromResponse(
        response: Response
    ): ArrayList<StockValues> {
        val stockValuesList = ArrayList<StockValues>()

        val body = response.body!!.string()
        val jsonResponse = JSONObject(body)
        Log.d("API body response", body)

        if (jsonResponse.has("Meta Data")) {
            val metaData = jsonResponse.getJSONObject("Meta Data")

            val lastRefreshed = metaData.get("3. Last Refreshed").toString()
            val timeInterval = metaData.get("4. Interval").toString()

            if (jsonResponse.has("Time Series ($timeInterval)")) {
                val data = jsonResponse.getJSONObject("Time Series ($timeInterval)")
                val date = DateFormatter.getDate(lastRefreshed)
                val time = DateFormatter.getTime(date, lastRefreshed)
                var hour = DateFormatter.getHour(time)
                var minute = DateFormatter.getMinute(time, timeInterval)
                Log.d("DATE", DateFormatter.getDate(lastRefreshed))
                Log.d("TIME", DateFormatter.getTime(date, lastRefreshed))

                var timeStampAvailable = true
                while (timeStampAvailable) {

                    val formattedTimestamp = getFormattedTimeStamp(minute, hour, date)
                    Log.d("TIME_STAMP", formattedTimestamp)

                    timeStampAvailable = data.has(formattedTimestamp)
                    if (timeStampAvailable) {
                        val data = data.getJSONObject(formattedTimestamp)

                        var min = ""
                        min = if (minute == 0) {
                            "00"
                        } else {
                            minute.toString()
                        }

                        data.apply {
                            val stockValues = StockValues(
                                timeStamp = "$date $hour:$min",
                                time = "$hour:$min",
                                open = getString("1. open").toDouble(),
                                high = getString("2. high").toDouble(),
                                low = getString("3. low").toDouble(),
                                close = getString("4. close").toDouble(),
                                volume = getString("5. volume").toDouble()
                            )
                            stockValuesList.add(stockValues)
                        }
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
        return stockValuesList
    }

    private fun getStockItemFromResponse(
        apiParams: StockApiCallParams,
        response: Response
    ): StockItem {
        val stockValuesList = ArrayList<StockValues>()

        val body = response.body!!.string()
        val jsonResponse = JSONObject(body)
        Log.d("API body response", body)

        if (jsonResponse.has("Meta Data")) {
            val metaData = jsonResponse.getJSONObject("Meta Data")

            val lastRefreshed = metaData.get("3. Last Refreshed").toString()
            val timeInterval = metaData.get("4. Interval").toString()

            if (jsonResponse.has("Time Series ($timeInterval)")) {
                val data = jsonResponse.getJSONObject("Time Series ($timeInterval)")
                val date = DateFormatter.getDate(lastRefreshed)
                val time = DateFormatter.getTime(date, lastRefreshed)
                var hour = DateFormatter.getHour(time)
                var minute = DateFormatter.getMinute(time, timeInterval)
                Log.d("DATE", DateFormatter.getDate(lastRefreshed))
                Log.d("TIME", DateFormatter.getTime(date, lastRefreshed))

                var timeStampAvailable = true
                while (timeStampAvailable) {

                    val formattedTimestamp = getFormattedTimeStamp(minute, hour, date)
                    Log.d("TIME_STAMP", formattedTimestamp)

                    timeStampAvailable = data.has(formattedTimestamp)
                    if (timeStampAvailable) {
                        val data = data.getJSONObject(formattedTimestamp)

                        var min = ""
                        min = if (minute == 0) {
                            "00"
                        } else {
                            minute.toString()
                        }

                        data.apply {
                            val stockValues = StockValues(
                                timeStamp = "$date $hour:$min",
                                time = "$hour:$min",
                                open = getString("1. open").toDouble(),
                                high = getString("2. high").toDouble(),
                                low = getString("3. low").toDouble(),
                                close = getString("4. close").toDouble(),
                                volume = getString("5. volume").toDouble()
                            )
                            stockValuesList.add(stockValues)
                        }
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
        return createStockItem(stockValuesList, apiParams)
    }

    private fun createStockItem(stockValuesList: ArrayList<StockValues>, apiParams: StockApiCallParams): StockItem {
        val growth = getStockGrowthRate(stockValuesList)
        val lastValues = stockValuesList.last()
            val name = getStockNameFromSymbol(apiParams.stockSymbol)
            return StockItem(
                stockSymbol = apiParams.stockSymbol,
                stockName = name,
                open = lastValues.open,
                high = lastValues.high,
                low = lastValues.low,
                close = lastValues.close,
                volume = lastValues.volume,
                growthLastHour = growth
            )
    }

    fun isDataAvailable(params: StockApiCallParams): Boolean {
        initApiCall(params)

        val response = client.newCall(request)
            .execute()

        if (response.body != null) {
            val body = response.body!!.string()
            val jsonResponse = JSONObject(body)
            Log.d("API body response", body)

            return jsonResponse.has("Meta Data")
        } else return false
    }


}