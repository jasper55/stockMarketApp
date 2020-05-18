package jasper.wagner.smartstockmarketing.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jasper.wagner.smartstockmarketing.databinding.SearchResultItemBinding

class SearchAdapter(private val onResultItemClickListener: ResultItemClickListener) : ListAdapter<String, SearchViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SearchResultItemBinding.inflate(layoutInflater, parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.setOnClickListener {
            onResultItemClickListener.onItemClick(getItem(position))
        }
    }

    interface ResultItemClickListener {
        fun onItemClick(item : String)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
                oldItem == newItem
        }
    }
}

class SearchViewHolder(private val binding: SearchResultItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(text: String) {
        binding.resultText.text = text
    }
}