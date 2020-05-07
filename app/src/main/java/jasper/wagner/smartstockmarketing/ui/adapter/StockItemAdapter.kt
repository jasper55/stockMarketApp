package jasper.wagner.smartstockmarketing.ui.adapter

import android.app.Activity
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jasper.wagner.cryptotracking.common.MathOperation
import jasper.wagner.smartstockmarketing.R
import jasper.wagner.smartstockmarketing.domain.model.StockData
import kotlinx.android.synthetic.main.stock_data_item.view.*



//class StockItemAdapter(recyclerView: RecyclerView, internal var activity: Activity, var items: ArrayList<StockData>) : RecyclerView.Adapter<StockDataViewHolder>() {
class StockItemAdapter(private val listItemClickListener: ListItemClickListener)
    : ListAdapter<StockData, RecyclerView.ViewHolder>(ListItemCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as StockDataViewHolder).bind(item, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stock_data_item, parent, false)
        return StockDataViewHolder(view)
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockDataViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.stock_data_item, parent, false)
//        return StockDataViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: StockDataViewHolder, position: Int) {
//
//        val stockDataModel = items[position]
//
//        holder.bindTo(getItem(position))
//
//        val item = holder as StockDataViewHolder
//
//        item.stockName.text = stockDataModel.stockName
//        item.stockHigh.text = MathOperation.round(stockDataModel.high).toString()
//        item.stockOpen.text = MathOperation.round(stockDataModel.open).toString()
//        item.stockLow.text = MathOperation.round(stockDataModel.low).toString()
//        item.stockClose.text = MathOperation.round(stockDataModel.close).toString()
//        item.stockVolume.text = MathOperation.round(stockDataModel.volume).toString()
//        item.stockGrowth.text = MathOperation.round(stockDataModel.growth).toString()
//
//        item.stockGrowth.setTextColor(if (stockDataModel.growth.toString().contains("-"))
//            Color.parseColor("#FF0000")
//        else
//            Color.parseColor("#32CD32")
//        )
//    }
//
//    fun setList(newList: ArrayList<StockData>) {
//        DiffUtil.calculateDiff(ItemDiffUtil(items, newList)).dispatchUpdatesTo(this)
//        items.clear()
//        items.addAll(newList)
//    }


    init {
//        val linearLayout = recyclerView.layoutManager as LinearLayoutManager
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                totalItemCount = linearLayout.itemCount
//                lastVisibleItem = linearLayout.findLastVisibleItemPosition()
//                if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
//                    if (loadMore != null)
//                        loadMore!!.onLoadMore()
//                    isLoading = true
//                }
//            }
//        })
    }

//    fun setLoadMore(loadMore: ILoadMore) {
//        this.loadMore = loadMore
//    }

//    override fun getItemCount(): Int {
//        return items.size
//    }
//
////    fun  setLoaded(){
////        isLoading = false
////    }
//
//    fun updateData(stockDataModels: ArrayList<StockData>)
//    {
//        this.items = stockDataModels
//        notifyDataSetChanged()
//    }



inner class StockDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var stockName = itemView.stock_name
    var stockHigh = itemView.high
    var stockOpen = itemView.open
    var stockLow = itemView.low
    var stockClose = itemView.close
    var stockVolume = itemView.volume
    var stockGrowth = itemView.stock_development_last_hour

    fun bind(item : StockData, position : Int) {
        stockName.text = item.stockName
        stockHigh.text = MathOperation.round(item.high).toString()
        stockOpen.text = MathOperation.round(item.open).toString()
        stockLow.text = MathOperation.round(item.low).toString()
        stockClose.text = MathOperation.round(item.close).toString()
        stockVolume.text = MathOperation.round(item.volume).toString()
        stockGrowth.text = MathOperation.round(item.growth).toString()



        itemView.setOnClickListener {
            listItemClickListener.onItemClick(item, position)
        }
//        Log.d("onclick", "onClick " + position + " " + tvContent.text)
    }
}

interface ListItemClickListener {
    fun onItemClick(item : StockData, position : Int)
}

class ListItemCallback : DiffUtil.ItemCallback<StockData>() {
    override fun areItemsTheSame(oldItem: StockData, newItem: StockData): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: StockData, newItem: StockData): Boolean {
        return true
//        return oldItem.text == newItem.text && oldItem.clicks == newItem.clicks
    }
}}