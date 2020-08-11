package jasper.wagner.smartstockmarketing.data.network

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.smartstockmarketing.data.db.StockDatabase
import jasper.wagner.smartstockmarketing.domain.model.StockInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import kotlin.random.Random

interface SearchApi {
    suspend fun performSearch(query: String): List<String>
}

class SearchRepository(
    private val context: Context,
    private val maxResult: Int = DEFAULT_RESULT_MAX_SIZE
) : SearchApi {

    internal lateinit var client: OkHttpClient
    internal lateinit var request: Request

    companion object {
        private const val DEFAULT_RESULT_MAX_SIZE = 250
        private const val RANDOM_ERROR_THRESHOLD = 0.75
    }

    override suspend fun performSearch(keywords: String): List<String> =
        withContext(Dispatchers.IO) {

            initApiCall(keywords)

            val response = client.newCall(request)
                .execute()
            return@withContext getSearchResultList(response)
        }

    private fun getSearchResultList(response: Response): List<String> {
        val resultList = ArrayList<String>()
        var newItems = ArrayList<StockInfo>()

        val body = response.body!!.string()
        val jsonResponse = JSONObject(body)
        Log.d("API Search response", body)

        if (jsonResponse.has("bestMatches")) {
            val data = jsonResponse.getJSONArray("bestMatches")
            val gson = Gson()
            newItems = gson.fromJson<ArrayList<StockInfo>>(data.toString(),
                object : TypeToken<ArrayList<StockInfo>>() {}.type
            )
        }
        val items = newItems.distinct()

        for (item in items) {
            resultList.add(item.stockName)
            StockDatabase.getInstance(context).stockInfoDao().addStockInfo(item)
        }
        return resultList
    }

    private fun initApiCall(keywords: String) {

        val url = Common.createApiSearchQuery(keywords)

        client = OkHttpClient()
        request = Request.Builder()
            .url(url)
            .build()
    }
}