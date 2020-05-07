package com.example.workoutreminder.data.network

import android.util.Log
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockData
import jasper.wagner.smartstockmarketing.util.DateFormatter
import jasper.wagner.smartstockmarketing.util.DateFormatter.length
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject

class USStockMarketApi {

    internal lateinit var client: OkHttpClient
    internal lateinit var request: Request

    private fun initApiCall(params: StockApiCallParams){


        val url = Common.createApiLink(
            function = params.function,
            stockName = params.stockName,
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

    suspend fun fetchStockMarketData(apiParams: StockApiCallParams): ArrayList<StockData>
    = withContext(IO) {
        initApiCall(apiParams)

        val stockList = ArrayList<StockData>()
        val response = client.newCall(request)
            .execute()
//            .enqueue(object : Callback {
//                override fun onFailure(call: Call, e: IOException) {
//                    Log.d("ERROR", e.toString())
//                }

//                override fun onResponse(call: Call, response: Response) {
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
                                    data.getJSONObject(formattedTimestamp).apply {

                                        var min = ""
                                        min = if (minute == 0) {
                                            "00"
                                        } else {
                                            minute.toString()
                                        }

                                        val stockData = StockData(
                                            stockName = apiParams.stockName,
                                            time = "$hour:$min",
                                            open = getString("1. open").toDouble(),
                                            high = getString("2. high").toDouble(),
                                            low = getString("3. low").toDouble(),
                                            close = getString("4. close").toDouble(),
                                            volume = getString("5. volume").toDouble(),
                                            growth = 0.0
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
//                }
//            })

        return@withContext stockList
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
        }
        else return false
    }


}