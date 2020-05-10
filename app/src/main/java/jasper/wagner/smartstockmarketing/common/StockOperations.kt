package jasper.wagner.smartstockmarketing.common

import jasper.wagner.smartstockmarketing.domain.model.StockData
import jasper.wagner.smartstockmarketing.util.MathOperation

object StockOperations {

    fun getStockGrowthRate(stockList: List<StockData>): Double {
        val size = stockList.size
        var stockGrowthRate: Double
        if (size >= 60) {
            stockGrowthRate = ((stockList[0].close / stockList[59].close) * 100) - 100
        } else if (size in 1..59) {
            stockGrowthRate = ((stockList[0].close / stockList[size-1].close) * 100) - 100
        }
        else {
            stockGrowthRate = 0.0
        }
        return MathOperation.round(stockGrowthRate)
    }
}