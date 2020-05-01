package jasper.wagner.smartstockmarketing.model

import com.google.gson.annotations.SerializedName

data class StockData(
    @SerializedName("stockName") val stockName: String,
    @SerializedName("1. open") val open: Double,
    @SerializedName("2. high") val high: Double,
    @SerializedName("3. low") val low: Double,
    @SerializedName("4. close") val close: Double,
    @SerializedName("5. volume") val volume: Double
)