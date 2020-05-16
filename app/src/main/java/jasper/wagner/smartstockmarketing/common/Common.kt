package jasper.wagner.cryptotracking.common

import jasper.wagner.smartstockmarketing.common.Constants.WorkManager.PERIODIC_WORK_TAG
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams


object Common{
    const val imageUrl = "https://res.cloudinary.com/dxi90ksom/image/upload/"

    const val API_KEY = "H5BSS6SMAYI28YJE"

    const val API_URI = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest"
    const val API_URI_2 = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/?start=%d&limit=10"
    const val API_URI_INITIAL = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest"

    const val old = "https://api.coinmarketcap.com/v1/ticker/?start=%d&limit=10"

    /*
    https://www.alphavantage.co/query?function=
    // TIME_SERIES_INTRADAY&
    symbol=Microsoft&
    interval=5min&
    outputsize=compact&
    apikey=H5BSS6SMAYI28YJE
    */

    /*
    https://www.alphavantage.co/query?function=
    // TIME_SERIES_INTRADAY&
    // symbol=IBM&
    // interval=5min&
    // outputsize=full
    // &apikey=demo
     */


    /**
    compact: latest 100 data points in the intraday time series;
    full: full-length intraday time series.

    The "compact" option is recommended if you would like to reduce the data size of each API call."
    **/
    object OutputSize {
        const val compact = "compact"
        const val full = "full"
    }

    object Function {
        const val intraDay = "TIME_SERIES_INTRADAY"
    }

    object Interval {
        const val min1 = "1min"
        const val min5 = "5min"
        const val min15 = "15min"
        const val min30 = "30min"
        const val min60 = "60min"
    }

    fun createApiLink(function: String, stockSymbol: String, interval: String, outputSize: String): String {
        return "https://www.alphavantage.co/query?function="+
                function +
                "&symbol=" + stockSymbol +
                "&interval=" + interval +
                "&outputsize=" + outputSize +
                "&apikey=" + API_KEY
    }

    fun getWorkTag(apiCallParams: StockApiCallParams): String {
        return "${PERIODIC_WORK_TAG}_${apiCallParams.stockSymbol.toUpperCase()}"
    }

}