package jasper.wagner.smartstockmarketing.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jasper.wagner.cryptotracking.common.MathOperation
import jasper.wagner.smartstockmarketing.R
import jasper.wagner.smartstockmarketing.domain.model.StockDisplayItem
import kotlinx.android.synthetic.main.stock_data_item.view.*
import kotlinx.android.synthetic.main.stock_data_item.view.stock_development_last_hour

class StockItemAdapter(private val listItemClickListener: ListItemClickListener)
    : ListAdapter<StockDisplayItem, RecyclerView.ViewHolder>(ListItemCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as StockDataViewHolder).bind(item, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stock_data_item, parent, false)
        return StockDataViewHolder(view)
    }


inner class StockDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var stockName = itemView.stock_name
    var stockHigh = itemView.high
    var stockOpen = itemView.open
    var stockLow = itemView.low
    var stockClose = itemView.close
    var stockVolume = itemView.volume
    var stockGrowth = itemView.stock_development_last_hour

    fun bind(item : StockDisplayItem, position : Int) {
        stockName.text = item.stockSymbol //TODO replace with name
        stockHigh.text = MathOperation.round(item.high).toString()
        stockOpen.text = MathOperation.round(item.open).toString()
        stockLow.text = MathOperation.round(item.low).toString()
        stockClose.text = MathOperation.round(item.close).toString()
        stockVolume.text = MathOperation.round(item.volume).toString()
        stockGrowth.text = "growth rate: ${MathOperation.round(item.growthLastHour)} %"

        if (item.growthLastHour >= 0)
            stockGrowth.setTextColor(Color.GREEN)
        else stockGrowth.setTextColor(Color.RED)

        itemView.setOnClickListener {
            listItemClickListener.onItemClick(item, position)
        }
    }
}

interface ListItemClickListener {
    fun onItemClick(item : StockDisplayItem, position : Int)
}

class ListItemCallback : DiffUtil.ItemCallback<StockDisplayItem>() {
    override fun areItemsTheSame(oldItem: StockDisplayItem, newItem: StockDisplayItem): Boolean {
        return oldItem.stockSymbol == newItem.stockSymbol
    }

    override fun areContentsTheSame(oldItem: StockDisplayItem, newItem: StockDisplayItem): Boolean {
        return oldItem.open == newItem.open &&
                oldItem.close == newItem.close &&
                oldItem.high == newItem.high &&
                oldItem.low == newItem.low &&
                oldItem.volume == newItem.volume
    }
}}