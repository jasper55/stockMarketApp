package jasper.wagner.smartstockmarketing.util

import android.content.Context
import androidx.room.Room
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_DB_NAME
import jasper.wagner.smartstockmarketing.data.db.StockDatabase

open class SingletonHolder<T, A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }

    companion object : SingletonHolder<StockDatabase, Context>({
        Room.databaseBuilder(it.applicationContext, StockDatabase::class.java, STOCK_DB_NAME).build()
    })
}