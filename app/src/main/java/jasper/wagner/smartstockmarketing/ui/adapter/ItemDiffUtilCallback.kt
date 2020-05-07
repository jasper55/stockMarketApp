package jasper.wagner.smartstockmarketing.ui.adapter

import androidx.recyclerview.widget.DiffUtil
import jasper.wagner.smartstockmarketing.domain.model.StockData

class ItemDiffUtilCallback : DiffUtil.ItemCallback<StockData>() {
    override fun areItemsTheSame(oldItem: StockData, newItem: StockData): Boolean = oldItem == newItem

    override fun areContentsTheSame(oldItem: StockData, newItem: StockData): Boolean = oldItem == newItem

}