package jasper.wagner.smartstockmarketing.domain.model

import androidx.room.Embedded
import androidx.room.Relation

data class StockData(
    @Embedded val stock: Stock,
    @Relation(
        parentColumn = "stockUID",
        entityColumn = "stockDataId"
    )
    val stockDataList: ArrayList<StockItem>
)
