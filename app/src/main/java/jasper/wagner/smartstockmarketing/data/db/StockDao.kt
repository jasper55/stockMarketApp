package jasper.wagner.smartstockmarketing.data.db

import androidx.room.*
import jasper.wagner.smartstockmarketing.domain.model.Stock
import jasper.wagner.smartstockmarketing.domain.model.StockData
import jasper.wagner.smartstockmarketing.domain.model.StockItem

@Dao
interface StockDao {

    @Transaction
    @Query("SELECT * FROM Stock")
    fun getAllStocks(): ArrayList<StockData>

    @Query("SELECT stockSymbol FROM Stock")
    fun getStock(stockSymbol: String): ArrayList<StockItem>

    @Insert
    fun insertStock(stock: Stock)

    @Query("SELECT * FROM stock")
    fun getAll(): List<Stock>

    @Query("SELECT * FROM stock WHERE stockUID IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<Stock>

    @Query("SELECT * FROM stock WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): Stock

    @Insert
    fun insertAll(vararg users: Stock)

    @Delete
    fun delete(user: Stock)
}