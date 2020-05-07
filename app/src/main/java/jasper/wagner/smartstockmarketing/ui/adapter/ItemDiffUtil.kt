package jasper.wagner.smartstockmarketing.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.domain.model.StockData

typealias FeedItemClass = Class<out Any>
typealias FeedItemBinder = FeedItemViewBinder<Any, RecyclerView.ViewHolder>

class ItemDiffUtil(val oldList: List<StockData>, val newList: List<StockData>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

}