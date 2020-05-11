package jasper.wagner.smartstockmarketing.util

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken
import jasper.wagner.smartstockmarketing.domain.model.StockData

object SharedPrefs {

    const val STOCK_PREFS = "Stockdata"
    const val PRIVATE_MODE = 0

    fun saveToSharedPrefs(context: Context, stockList: ArrayList<StockData>){
        val sharedPref: SharedPreferences = context.getSharedPreferences(STOCK_PREFS, PRIVATE_MODE)
        sharedPref.edit().putString(stockList.last().stockName, SerializeHelper.serializeToJson(stockList))
            .apply()
    }

    fun getStockDataFromPrefs(context: Context, stockName: String): ArrayList<StockData>{
        val sharedPref: SharedPreferences = context.getSharedPreferences(STOCK_PREFS, PRIVATE_MODE)
        val stockString = sharedPref.getString(stockName,"")
        return SerializeHelper.deserializeToArrayList(stockString!!)
    }

}