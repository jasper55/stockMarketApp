package jasper.wagner.smartstockmarketing.data.local

import androidx.room.*
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_TABLE
import jasper.wagner.smartstockmarketing.domain.model.Stock

@Dao
interface StockDao {

    @Transaction
    @Query("SELECT * FROM $STOCK_TABLE")
    suspend fun loadAllStocks(): List<Stock>

    @Query("SELECT stockSymbol FROM $STOCK_TABLE")
    suspend fun getStoredStockSymbols(): List<String>

    @Query("SELECT stockName FROM $STOCK_TABLE")
    suspend fun getStoredStockNames(): List<String>

    @Query("SELECT * FROM $STOCK_TABLE WHERE stockUID = :stockUID")
    suspend fun getStock(stockUID: Long): Stock

    @Query("SELECT * FROM $STOCK_TABLE WHERE stockSymbol = :stockSymbol")
    suspend fun getStockBySymbol(stockSymbol: String): Stock

    @Delete
    suspend fun deleteStock(stock: Stock)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addStock(stock: Stock)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateLastTimeStamp(stock: Stock)

    @Query("SELECT stockUID FROM $STOCK_TABLE")
    suspend  fun getStoredStockUIDs(): List<Long>




//    @Query("SELECT * FROM $STOCK_VALUES_TABLE WHERE stockUID = :stockUID")
//    fun getStockValuesForStock(stockUID: Long): List<StockTimeSeriesInstance>


//    @Query("SELECT * FROM stock WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): Stock


}