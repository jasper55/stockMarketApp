package jasper.wagner.smartstockmarketing.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import jasper.wagner.smartstockmarketing.common.Constants.DB.STOCK_TABLE
import java.io.Serializable

@Entity(tableName = STOCK_TABLE)
data class Stock(
    @ColumnInfo(name = "stockSymbol") var stockSymbol: String,
    @ColumnInfo(name = "stockName") var stockName: String,
    @ColumnInfo(name = "lastTimeStamp") var lastTimeStamp: String
): Serializable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "stockUID") var stockUID: Int = 1
}