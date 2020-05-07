package jasper.wagner.smartstockmarketing.domain.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class StockApiCallParams(
    @SerializedName("stockName") val stockName: String,
    @SerializedName("function") val function: String,
    @SerializedName("interval") val interval: String,
    @SerializedName("outputSize") val outputSize: String
    ): Serializable
