package jasper.wagner.smartstockmarketing.ui.stockinfo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import jasper.wagner.smartstockmarketing.R
import jasper.wagner.smartstockmarketing.common.Constants.Bundle.STOCK_SYMBOL
import jasper.wagner.smartstockmarketing.common.StockOperations.getStockGrowthRate
import jasper.wagner.smartstockmarketing.data.local.StockDatabase
import jasper.wagner.smartstockmarketing.databinding.StockInfoFragmentBinding
import jasper.wagner.smartstockmarketing.domain.model.StockDisplayItem
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance
import kotlinx.android.synthetic.main.stock_data_item.stock_development_last_hour
import kotlinx.android.synthetic.main.stock_info_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import lecho.lib.hellocharts.model.*

class StockInfoFragment : Fragment() {

    private lateinit var binding: StockInfoFragmentBinding
    private lateinit var viewModel: StockInfoViewModel
    private lateinit var stockDatabase: StockDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = StockInfoFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StockInfoViewModel::class.java)

        stockDatabase = StockDatabase.getInstance(requireActivity().applicationContext)

        val stockSymbol = arguments?.getSerializable(STOCK_SYMBOL) as String

        CoroutineScope(Dispatchers.IO).launch {
            loadDataFromDB(stockSymbol)
        }
    }

    private suspend fun loadDataFromDB(stockSymbol: String) {
        val stock = stockDatabase.stockDao().getStockBySymbol(stockSymbol)
        val list = stockDatabase.stockValuesDao().getAllByStockUID(stock.stockUID!!)

        withContext(Dispatchers.Main) {
            showLineChart(list)
            val growth = getStockGrowthRate(list)
            val stockItem = StockDisplayItem(
                stockSymbol = stock.stockSymbol,
                stockName = stock.stockName,
                close = list.last().close,
                open = list.last().open,
                high = list.last().high,
                low = list.last().low,
                volume = list.last().volume,
                growthLastHour = growth,
                lineChart = null
            )
            updateView(stockItem)
            showDifferenceToOneHour(growth)
        }
    }

    private fun updateView(stockValues: StockDisplayItem) {
        binding.progressBar.visibility = View.GONE
        binding.stockName.text = "${stockValues.stockName}"
        binding.open.text = "open: ${stockValues.open}"
        binding.close.text = "close: ${stockValues.close}"
        binding.high.text = "high: ${stockValues.high}"
        binding.low.text = "low: ${stockValues.low}"
        binding.volume.text = "volume: ${stockValues.volume}"
    }

    private suspend fun showDifferenceToOneHour(stockGrowthRate: Double) = withContext(Main) {
        if (stockGrowthRate >= 0)
            stock_development_last_hour.setTextColor(Color.GREEN)
        else stock_development_last_hour.setTextColor(Color.RED)
        stock_development_last_hour.text = "growth rate: $stockGrowthRate %"
        stock_development_last_hour.visibility = View.VISIBLE
    }

    private suspend fun showLineChart(stockList: List<StockTimeSeriesInstance>) =
        withContext(Main) {
            val yAxisValuesRelativeDiff = ArrayList<PointValue>()
            val yAxisValuesVolume = ArrayList<PointValue>()
            val yAxisValuesDiffOpenClose = ArrayList<PointValue>()
            val yAxisValuesDiffLowHigh = ArrayList<PointValue>()
            val axisValues = ArrayList<AxisValue>()

            val size = stockList.size
            val list = stockList.reversed()

            var i = 0
            while (i < size) {
                axisValues.add(i, AxisValue(i.toFloat()).setLabel(list[i].time))
                val low = list[i].low
                val high = list[i].high
                val open = list[i].open
                val close = list[i].close
                val volume = list[i].volume
                val data = (high-low)/volume
//                yAxisValues.add(i, PointValue(i.toFloat(), high.toFloat()))
//                yAxisValues.add(i, PointValue(i.toFloat(), low.toFloat()))
                yAxisValuesRelativeDiff.add(i, PointValue(i.toFloat(), data.toFloat()))
                yAxisValuesDiffOpenClose.add(i, PointValue(i.toFloat(), (open-close).toFloat()))
                yAxisValuesDiffLowHigh.add(i, PointValue(i.toFloat(), (high-low).toFloat()))
                i += 1
            }

            val relativeDiffLine = Line(yAxisValuesRelativeDiff).setColor(requireActivity().resources.getColor(R.color.colorPrimary))
            relativeDiffLine.pointRadius = 0
            relativeDiffLine.strokeWidth = 2

            val diffOpenClose = Line(yAxisValuesRelativeDiff).setColor(requireActivity().resources.getColor(R.color.colorPrimaryDark))
            diffOpenClose.pointRadius = 0
            diffOpenClose.strokeWidth = 2

            val diffLowHigh = Line(yAxisValuesRelativeDiff).setColor(requireActivity().resources.getColor(R.color.colorAccent))
            diffLowHigh.pointRadius = 0
            diffLowHigh.strokeWidth = 2

            val lines = ArrayList<Line>()
            lines.add(relativeDiffLine)
            lines.add(diffOpenClose)
            lines.add(diffLowHigh)

            val axis = Axis()
            val yAxis = Axis()
            axis.values = axisValues
            axis.textSize = 12
            axis.textColor = Color.parseColor("#03A9F4")
            yAxis.textColor = Color.parseColor("#03A9F4")
            yAxis.textSize = 12

            val data = LineChartData()
            data.lines = lines

            data.axisXBottom = axis
            data.axisYLeft = yAxis

            line_chart.lineChartData = data
//        val viewport = Viewport(line_chart.maximumViewport)
//        viewport.top = 110f
//        line_chart.maximumViewport = viewport
//        line_chart.currentViewport = viewport

            line_chart.visibility = View.VISIBLE
        }


    companion object {
        fun newInstance(): StockInfoFragment {
            return StockInfoFragment()
        }
    }
}