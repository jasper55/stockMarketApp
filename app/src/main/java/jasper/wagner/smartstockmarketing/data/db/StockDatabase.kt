package jasper.wagner.smartstockmarketing.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import jasper.wagner.smartstockmarketing.domain.model.Stock

@Database(entities = arrayOf(Stock::class), version = 1)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
}