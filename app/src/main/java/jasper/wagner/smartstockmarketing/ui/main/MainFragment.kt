package jasper.wagner.smartstockmarketing.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import jasper.wagner.smartstockmarketing.common.Constants.Bundle.STOCK_SYMBOL
import jasper.wagner.smartstockmarketing.databinding.MainFragmentBinding
import jasper.wagner.smartstockmarketing.domain.model.*
import jasper.wagner.smartstockmarketing.ui.adapter.StockItemAdapter
import jasper.wagner.smartstockmarketing.ui.search.SearchFragment
import jasper.wagner.smartstockmarketing.ui.stockinfo.StockInfoFragment
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.coroutines.*
import kotlin.collections.ArrayList


class MainFragment : Fragment(), StockItemAdapter.ListItemClickListener {

    private lateinit var binding: MainFragmentBinding

    private val stockList = ArrayList<StockDisplayItem>()
    private val nameList = ArrayList<String>()

    private lateinit var itemAdapter: StockItemAdapter


//    private val parentJob = Job()
//    private val coroutineExceptionHandler: CoroutineExceptionHandler =
//        CoroutineExceptionHandler { _, throwable ->
//            coroutineScope.launch(Dispatchers.Main) {
////                binding.errorContainer.visibility = View.VISIBLE
////                binding.errorContainer.text = throwable.message
//            }
//            GlobalScope.launch { println("Caught $throwable") }
//        }
//    private val coroutineScope =
//        CoroutineScope(Dispatchers.Main + parentJob + coroutineExceptionHandler)
//    private val scopeMainThread =
//        CoroutineScope(parentJob + Dispatchers.Main + coroutineExceptionHandler)

    private lateinit var viewModel: MainViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)




//        recyclerView = root.findViewById(R.id.recycler_view_data)
//        recyclerViewAdapter = RecyclerViewAdapter(
//            userActionClickListener,
//            ArrayList(0),
//            sharedViewModel
//        )
//
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        recyclerView.adapter = recyclerViewAdapter

        initAddButton()
        initRefreshButton()
        observeLiveDataChanges()
        viewModel.refreshData()
        itemAdapter = StockItemAdapter(this)
        binding.stockList.layoutManager = LinearLayoutManager(requireContext())
        binding.stockList.itemAnimator = DefaultItemAnimator()
        binding.stockList.adapter = itemAdapter
    }

    private fun observeLiveDataChanges() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer {
            if (it) showProgressbar()
            hideProgressbar()
        })

        viewModel.stockList.observe(viewLifecycleOwner, Observer { list ->

//            stockList = list
//            itemAdapter.submitList(it)
        })
        viewModel.newStockItem.observe(viewLifecycleOwner, Observer {
//            binding.progressBar.visibility = View.GONE
            stockList.add(it)
            itemAdapter.submitList(stockList)
        })
    }


    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            showProgressbar()
            stockList.clear()
            viewModel.refreshData()
        }
    }

    override fun onItemClick(item: StockDisplayItem, position: Int) {
        val bundle = Bundle().apply {
            putString(STOCK_SYMBOL, item.stockSymbol)
        }
        val stockInfoFragment = StockInfoFragment.newInstance()
        stockInfoFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(jasper.wagner.smartstockmarketing.R.id.container, stockInfoFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun initAddButton() {
        add_stock.setOnClickListener {
            val searchFragment = SearchFragment.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(jasper.wagner.smartstockmarketing.R.id.container, searchFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun initRefreshButton() {
        refresh_button.setOnClickListener {
            viewModel.updateStockData()
        }
    }



    private fun showProgressbar() {
//        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
//        }
    }

    private fun hideProgressbar() {
//        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.GONE
//        }
    }


    companion object {
        fun newInstance() = MainFragment()
        const val MAIN_FRAG_TAG = "MainFragment"
    }
}
