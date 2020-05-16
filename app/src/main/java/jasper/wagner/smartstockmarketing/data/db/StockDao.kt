package jasper.wagner.smartstockmarketing.data.db

import androidx.room.*
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_TABLE
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_VALUES_TABLE
import jasper.wagner.smartstockmarketing.domain.model.Stock
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeries
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance

@Dao
interface StockDao {

    @Transaction
    @Query("SELECT * FROM $STOCK_TABLE")
    fun loadAllStocks(): List<Stock>

    @Query("SELECT stockSymbol FROM $STOCK_TABLE")
    fun getStoredStockSymbols(): List<String>

    @Query("SELECT * FROM $STOCK_TABLE WHERE stockUID = :stockUID")
    fun getStock(stockUID: Int): Stock

    @Query("SELECT * FROM $STOCK_TABLE WHERE stockSymbol = :stockSymbol")
    fun getStockBySymbol(stockSymbol: String): Stock

    @Delete
    fun deleteStock(stock: Stock)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addStock(stock: Stock)


//    @Query("SELECT * FROM $STOCK_VALUES_TABLE WHERE stockUID = :stockUID")
//    fun getStockValuesForStock(stockUID: Long): List<StockTimeSeriesInstance>


//    @Query("SELECT * FROM stock WHERE first_name LIKE :first AND " +
//            "last_name LIKE :last LIMIT 1")
//    fun findByName(first: String, last: String): Stock


}