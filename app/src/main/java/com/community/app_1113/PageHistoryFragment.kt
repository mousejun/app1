package com.community.app_1113

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.community.app_1113.databinding.FragmentPageHistoryBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PageHistoryFragment : Fragment() {
    private var binding: FragmentPageHistoryBinding? = null
    private val historyList = mutableListOf<PageHistoryItem>()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewBinding = FragmentPageHistoryBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadHistory()
        setupRecyclerView()
        setupButtons()
    }

    private fun loadHistory() {
        val prefs = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val historyJson = prefs.getString(AppConstants.KEY_PAGE_HISTORY, null)
        
        if (historyJson != null) {
            val gson = Gson()
            val type = object : TypeToken<List<PageHistoryItem>>() {}.type
            val loadedHistory = gson.fromJson<List<PageHistoryItem>>(historyJson, type)
            historyList.clear()
            historyList.addAll(loadedHistory.reversed()) // 최신순으로 표시
        }
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(historyList) { item ->
            // 페이지 클릭 시 해당 URL로 이동
            val urls = listOf(item.url)
            val webViewFragment = WebViewFragment.newInstance(urls)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, webViewFragment)
                .addToBackStack(null)
                .commit()
        }
        
        binding?.recyclerView?.layoutManager = LinearLayoutManager(requireContext())
        binding?.recyclerView?.adapter = adapter
        
        if (historyList.isEmpty()) {
            binding?.emptyTextView?.visibility = View.VISIBLE
            binding?.recyclerView?.visibility = View.GONE
        } else {
            binding?.emptyTextView?.visibility = View.GONE
            binding?.recyclerView?.visibility = View.VISIBLE
        }
    }

    private fun setupButtons() {
        binding?.buttonClear?.setOnClickListener {
            clearHistory()
        }
        binding?.buttonBack?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun clearHistory() {
        val prefs = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(AppConstants.KEY_PAGE_HISTORY).apply()
        historyList.clear()
        adapter.notifyDataSetChanged()
        binding?.emptyTextView?.visibility = View.VISIBLE
        binding?.recyclerView?.visibility = View.GONE
        Toast.makeText(requireContext(), "히스토리가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private class HistoryAdapter(
        private val items: List<PageHistoryItem>,
        private val onItemClick: (PageHistoryItem) -> Unit
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val titleText: TextView = view.findViewById(R.id.titleText)
            val urlText: TextView = view.findViewById(R.id.urlText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_page_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.titleText.text = item.title.ifEmpty { item.url }
            holder.urlText.text = item.url
            holder.itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        override fun getItemCount() = items.size
    }
}

