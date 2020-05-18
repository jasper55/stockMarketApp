package jasper.wagner.smartstockmarketing.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import jasper.wagner.smartstockmarketing.common.Constants
import jasper.wagner.smartstockmarketing.domain.model.Stock
import jasper.wagner.smartstockmarketing.domain.model.StockInfo
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance
import jasper.wagner.smartstockmarketing.util.SingletonHolder

@Database(entities = arrayOf(Stock::class, StockTimeSeriesInstance::class, StockInfo::class), version = 1)
abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun stockValuesDao(): StockTimeSeriesInstanceDao
    abstract fun stockInfoDao(): StockInfoDao

    companion object : SingletonHolder<StockDatabase, Context>({
        Room.databaseBuilder(it.applicationContext,
            StockDatabase::class.java, Constants.DB.STOCK_DB_NAME
        )
            .build()
    })
}