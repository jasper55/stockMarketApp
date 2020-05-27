package jasper.wagner.smartstockmarketing.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import jasper.wagner.smartstockmarketing.common.Constants
import java.io.Serializable

@Entity(tableName = Constants.DB.STOCK_SEARCH_RESULT_TABLE)
data class StockInfo(
    @PrimaryKey
    @SerializedName("1. symbol") val stockSymbol: String,
    @SerializedName("2. name") val stockName: String,
    @SerializedName("3. type") val stockType: String,
    @SerializedName("4. region") val region: String,
    @SerializedName("5. marketOpen") val marketOpen: String,
    @SerializedName("6. marketClose") val marketClose: String,
    @SerializedName("7. timezone") val timeZone: String,
    @SerializedName("8. currency") val currency: String,
    @SerializedName("9. matchScore") val matchScore: String
) : Serializable




