package jasper.wagner.smartstockmarketing.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import jasper.wagner.cryptotracking.common.Common
import jasper.wagner.smartstockmarketing.R
import jasper.wagner.smartstockmarketing.data.local.StockDatabase
import jasper.wagner.smartstockmarketing.data.remote.USStockMarketApi
import jasper.wagner.smartstockmarketing.databinding.SearchFragmentBinding
import jasper.wagner.smartstockmarketing.domain.model.Stock
import jasper.wagner.smartstockmarketing.domain.model.StockApiCallParams
import jasper.wagner.smartstockmarketing.ui.adapter.SearchAdapter
import jasper.wagner.smartstockmarketing.ui.main.MainFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SearchFragment : Fragment(), SearchAdapter.ResultItemClickListener {

    private lateinit var binding: SearchFragmentBinding
    private lateinit var searchAdapter: SearchAdapter
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModel.Factory(requireActivity().applicationContext,Dispatchers.IO)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SearchFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.searchResult.observe(requireActivity(), Observer {
            handleSearchResult(it)
        })

        searchAdapter = SearchAdapter(this)
        binding.searchResult.adapter = searchAdapter
        searchAdapter.submitList(emptyList())

        binding.otherResultText.visibility = View.VISIBLE
        binding.searchResult.visibility = View.GONE
        binding.otherResultText.setText(R.string.not_enough_characters)
        binding.searchText.requestFocus()

        binding.searchText.doAfterTextChanged { editable ->
            lifecycleScope.launch {
                viewModel.queryChannel.send(editable.toString())
            }
        }
    }

    private fun handleSearchResult(it: SearchResult) {
        when (it) {
            is ValidResult -> {
                binding.otherResultText.visibility = View.GONE
                binding.searchResult.visibility = View.VISIBLE
                searchAdapter.submitList(it.result)
            }
            is ErrorResult -> {
                searchAdapter.submitList(emptyList())
                binding.otherResultText.visibility = View.VISIBLE
                binding.searchResult.visibility = View.GONE
                binding.otherResultText.setText(R.string.search_error)
            }
            is EmptyResult -> {
                searchAdapter.submitList(emptyList())
                binding.otherResultText.visibility = View.VISIBLE
                binding.searchResult.visibility = View.GONE
                binding.otherResultText.setText(R.string.empty_result)
            }
            is EmptyQuery -> {
                searchAdapter.submitList(emptyList())
                binding.otherResultText.visibility = View.VISIBLE
                binding.searchResult.visibility = View.GONE
                binding.otherResultText.setText(R.string.not_enough_characters)
            }
            is TerminalError -> {
                // Something wen't terribly wrong!
                println("Our Flow terminated unexpectedly, so we're bailing!")
                Toast.makeText(
                    requireActivity(),
                    "Unexpected error in SearchRepository!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onSearchResultItemClick(stockName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = StockDatabase.getInstance(requireActivity().applicationContext)

            if (!db.stockDao().getStoredStockNames().contains(stockName)) {
                val stockInfo = db.stockInfoDao().getStockInfoForStockName(stockName)
                val stockToStore = Stock(stockInfo.stockSymbol, stockInfo.stockName, null)
                db.stockDao().addStock(stockToStore)

                val apiParams = StockApiCallParams(
                    stockToStore.stockSymbol,
                    Common.Function.intraDay,
                    Common.Interval.min1,
                    Common.OutputSize.full
                )
                val stock = db.stockDao().getStockBySymbol(stockToStore.stockSymbol)
                val usStockMarketApi = USStockMarketApi()
                val valuesList = usStockMarketApi.fetchStockValuesList(stock.stockUID!!, apiParams)

                db.stockValuesDao().addList(valuesList)
            }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment.newInstance())
            .addToBackStack(null)
            .commit()
        }
    }


    companion object {
        fun newInstance(): SearchFragment {
            return SearchFragment()
        }
    }
}
