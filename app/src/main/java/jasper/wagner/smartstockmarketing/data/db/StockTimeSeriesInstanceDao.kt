package jasper.wagner.smartstockmarketing.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_VALUES_TABLE
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance

@Dao
interface StockTimeSeriesInstanceDao {

    @Query("SELECT * FROM $STOCK_VALUES_TABLE WHERE stockRelationUID = :stockRelationUID")
    fun getAllByListStockUID(stockRelationUID: Long): List<StockTimeSeriesInstance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addStockValues(stockValues: StockTimeSeriesInstance)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addList(stockValues: List<StockTimeSeriesInstance>)
}

