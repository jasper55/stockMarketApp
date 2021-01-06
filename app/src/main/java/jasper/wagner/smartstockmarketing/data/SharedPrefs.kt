package jasper.wagner.smartstockmarketing.data

import android.content.Context
import android.content.SharedPreferences
import jasper.wagner.smartstockmarketing.domain.model.StockDisplayItem
import jasper.wagner.smartstockmarketing.util.SerializeHelper

object SharedPrefs {

    const val STOCK_PREFS = "Stockdata"
    const val PRIVATE_MODE = 0

    fun saveToSharedPrefs(context: Context, stockList: ArrayList<StockDisplayItem>){
        val sharedPref: SharedPreferences = context.getSharedPreferences(STOCK_PREFS, PRIVATE_MODE)
        sharedPref.edit().putString(stockList.last().stockName,
            SerializeHelper.serializeToJson(stockList)
        )
            .apply()
    }

    fun getStockDataFromPrefs(context: Context, stockName: String): ArrayList<StockDisplayItem>{
        val sharedPref: SharedPreferences = context.getSharedPreferences(STOCK_PREFS, PRIVATE_MODE)
        val stockString = sharedPref.getString(stockName,"")
        return SerializeHelper.deserializeToArrayList(stockString!!)
    }

}