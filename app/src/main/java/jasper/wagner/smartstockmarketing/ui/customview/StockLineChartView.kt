package jasper.wagner.smartstockmarketing.ui.customview

import android.content.Context
import android.graphics.Color
import android.graphics.Color.green
import android.view.View
import jasper.wagner.smartstockmarketing.R
import jasper.wagner.smartstockmarketing.domain.model.StockTimeSeriesInstance
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView

class StockLineChartView(stockList: List<StockTimeSeriesInstance>, context: Context) :
    LineChartView(context) {

    private val stockList = stockList

    fun getLineChart(): LineChartView {
        val yAxisValues = ArrayList<PointValue>()
        val axisValues = ArrayList<AxisValue>()

        val size = stockList.size
        val list = stockList.reversed()

        var i = 0
        while (i < size) {
            axisValues.add(i, AxisValue(i.toFloat()).setLabel(list[i].time))
            val close = list[i].close.toFloat()
            yAxisValues.add(i, PointValue(i.toFloat(), close))
            i += 1
        }

        val course = if (list.last().close > list[0].close) {
            Line(yAxisValues).setColor(context.resources.getColor(R.color.green))
        } else {
            Line(yAxisValues).setColor(context.resources.getColor(R.color.red))
        }
        course.pointRadius = 0
        course.strokeWidth = 2


        val lines = ArrayList<Line>()
        lines.add(course)

//        val axis = Axis()
//        val yAxis = Axis()
//        axis.values = axisValues
//        axis.textSize = 12
//        axis.textColor = Color.parseColor("#03A9F4")
//        yAxis.textColor = Color.parseColor("#03A9F4")
//        yAxis.textSize = 12

        val data = LineChartData()
        data.lines = lines

//        data.axisXBottom = axis
//        data.axisYLeft = yAxis

        val lineChartView = LineChartView(context)
        lineChartView.lineChartData = data
        if (list.isNullOrEmpty()) {
            lineChartView.visibility = View.GONE
        }
        return lineChartView
    }

}