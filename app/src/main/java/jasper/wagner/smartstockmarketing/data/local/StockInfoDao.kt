package jasper.wagner.smartstockmarketing.data.local

import androidx.room.*
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_SEARCH_RESULT_TABLE
import jasper.wagner.smartstockmarketing.domain.model.StockInfo

@Dao
interface StockInfoDao {

//    @Transaction
//    @Query("SELECT * FROM $STOCK_INFO_TABLE")
//    fun loadAllStocks(): List<Stock>

//    @Query("SELECT stockSymbol FROM $STOCK_TABLE")
//    fun getStoredStockSymbols(): List<String>
//
//    @Query("SELECT * FROM $STOCK_TABLE WHERE stockUID = :stockUID")
//    fun getStock(stockUID: Long): Stock
//
    @Query("SELECT * FROM $STOCK_SEARCH_RESULT_TABLE WHERE stockName = :stockName")
    suspend fun getStockInfoForStockName(stockName: String): StockInfo
//
//    @Delete
//    fun deleteStockInfo(stockInfo: StockInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStockInfo(stockInfo: StockInfo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStockInfo(stockInfo: StockInfo) {

    }


//    @Query("SELECT * FROM $STOCK_VALUES_TABLE WHERE stockUID = :stockUID")
//    fun getStockValuesForStock(stockUID: Long): List<StockTimeSeriesInstance>


//    @Query("SELECT * FROM stock WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): Stock


}