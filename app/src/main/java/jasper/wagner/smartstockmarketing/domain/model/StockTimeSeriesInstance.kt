package jasper.wagner.smartstockmarketing.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_VALUES_TABLE
import java.io.Serializable


@Entity(tableName = STOCK_VALUES_TABLE)
data class StockTimeSeriesInstance(
    @PrimaryKey val timeStamp: String,
    @ColumnInfo(name = "stockRelationUID") var stockRelationUID: Long,
    @ColumnInfo(name = "time") var time: String,
    @ColumnInfo(name = "1. open") var open: Double,
    @ColumnInfo(name = "2. high") var high: Double,
    @ColumnInfo(name = "3. low") var low: Double,
    @ColumnInfo(name = "4. close") var close: Double,
    @ColumnInfo(name = "5. volume") var volume: Double
): Serializable

