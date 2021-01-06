package jasper.wagner.smartstockmarketing.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_VALUES_TABLE
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance

@Dao
interface StockTimeSeriesInstanceDao {

    @Query("SELECT * FROM $STOCK_VALUES_TABLE WHERE stockRelationUID = :stockRelationUID")
    suspend fun getAllByStockUID(stockRelationUID: Long): List<StockTimeSeriesInstance>

    @Query("SELECT * FROM $STOCK_VALUES_TABLE")
    suspend fun getAll(): List<StockTimeSeriesInstance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStockValues(stockValues: StockTimeSeriesInstance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addList(stockValues: List<StockTimeSeriesInstance>)
}

