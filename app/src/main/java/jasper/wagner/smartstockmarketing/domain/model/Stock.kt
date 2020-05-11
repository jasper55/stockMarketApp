package jasper.wagner.smartstockmarketing.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Stock(
    @PrimaryKey val stockUID: Int,
    @ColumnInfo(name = "stockSymbol") val stockSymbol: String,
    @ColumnInfo(name = "stockName") val stockName: String,
    @ColumnInfo(name = "lastTimeStamp") val lastTimeStamp: String
): Serializable