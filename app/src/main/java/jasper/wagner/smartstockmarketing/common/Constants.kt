package jasper.wagner.smartstockmarketing.common

object Constants {


    object DB {
        const val STOCK_DB_NAME = "stock_database"
        const val STOCK_VALUES_TABLE = "stock_values_table"
        const val STOCK_TABLE = "stock_table"
    }

    object Bundle {
        const val STOCK_SYMBOL = "stock symbol"
    }

    object WorkManager {
        const val PERIODIC_WORK_TAG = "periodic stock market analyzer"
        const val API_CALL_PARAMS = "API call_params"
        const val STOCK_DATA_AVAILABLE = "Stock data available"
        const val GROWTH_MARGIN = "growth margin"
        const val STOCK_UID = "stockUID"
    }
}