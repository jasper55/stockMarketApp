package jasper.wagner.smartstockmarketing.domain.model

import androidx.room.Embedded
import androidx.room.Relation




data class StockTimeSeries(
    @Embedded val stock: Stock,
    @Relation(
        parentColumn = "stockUID",
        entityColumn = "stockRelationUID"
    )
    val stockTimeSeries: List<StockTimeSeriesInstance>
)
