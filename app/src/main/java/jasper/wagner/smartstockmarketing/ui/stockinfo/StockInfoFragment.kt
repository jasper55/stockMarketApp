package jasper.wagner.smartstockmarketing.ui.stockinfo

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import jasper.wagner.smartstockmarketing.data.network.USStockMarketApi
import jasper.wagner.smartstockmarketing.databinding.StockInfoFragmentBinding
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockItem
import jasper.wagner.smartstockmarketing.domain.model.StockValues
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

        val apiParams = arguments?.getSerializable("API_PARAMS") as StockApiCallParams
//        val stockName = arguments?.getString("STOCK_NAME")
//        val stockList = SharedPrefs.getStockDataFromPrefs(requireContext().applicationContext,stockName!!)

        loadData(apiParams)
        CoroutineScope(Dispatchers.IO).launch {
            val usStockMarketApi = USStockMarketApi()
            val stockList = usStockMarketApi.fetchStockMarketData2(apiParams)
            showLineChart(stockList)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    private fun loadData(apiParams: StockApiCallParams) {
        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.VISIBLE
            }

            val usStockMarketApi = USStockMarketApi()
            withContext(Dispatchers.IO) {
                val stockItem = usStockMarketApi.fetchStockMarketData(apiParams)
                showDifferenceToOneHour(stockItem.growthLastHour)
                updateView(stockItem)
            }

            withContext(Dispatchers.Main) {
//                showLineChart(stockList) //TODO SQl Call
            }
        }
    }




    private fun updateView(stockValues: StockItem) {
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

    private suspend fun showLineChart(stockList: ArrayList<StockValues>) = withContext(Main) {
        val yAxisValues = ArrayList<PointValue>()
        val axisValues = ArrayList<AxisValue>()

        val line = Line(yAxisValues).setColor(Color.parseColor("#9C27B0"))
        line.pointRadius = 0
        line.strokeWidth = 2

        val lines = ArrayList<Line>()
        lines.add(line)

        val size = stockList.size
        val list = stockList.reversed()

        var i = 0
        while (i < size) {
            axisValues.add(i, AxisValue(i.toFloat()).setLabel(list[i].time))
            yAxisValues.add(i, PointValue(i.toFloat(), (list[i].close).toFloat()))
            i += 1
        }

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
            return StockInfoFragment();
        }
    }
}