package jasper.wagner.smartstockmarketing.domain.model

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import lecho.lib.hellocharts.view.LineChartView
import java.io.Serializable

data class StockDisplayItem(
    @SerializedName("stock_name") val stockName: String,
    @SerializedName("stockSymbol") val stockSymbol: String,
    @SerializedName("1. open") val open: Double,
    @SerializedName("2. high") val high: Double,
    @SerializedName("3. low") val low: Double,
    @SerializedName("4. close") val close: Double,
    @SerializedName("5. volume") val volume: Double,
    @SerializedName("growthLastHour") val growthLastHour: Double,
    @SerializedName("lineChart") val lineChart: LineChartView?
) : Serializable
