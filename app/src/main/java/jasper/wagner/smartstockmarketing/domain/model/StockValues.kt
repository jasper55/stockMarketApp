package jasper.wagner.smartstockmarketing.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class StockValues(
    @PrimaryKey val timeStamp: String,
    @ColumnInfo(name = "time") val time: String,
    @ColumnInfo(name = "1. open") val open: Double,
    @ColumnInfo(name = "2. high") val high: Double,
    @ColumnInfo(name = "3. low") val low: Double,
    @ColumnInfo(name = "4. close") val close: Double,
    @ColumnInfo(name = "5. volume") val volume: Double
): Serializable