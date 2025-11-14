package com.community.app_1113

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.community.app_1113.databinding.FragmentBookmarkBinding
import com.google.android.gms.ads.AdRequest

class BookmarkFragment : Fragment() {
    private var binding: FragmentBookmarkBinding? = null
    private val bookmarks = AppConstants.bookmarks
    private val checkBoxes = mutableListOf<CheckBox>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [수정] 광고 로드 함수 호출 추가
        loadBannerAd()

        setupCheckBoxes()
        setupListeners()
        restoreSelectedStates()
        updateAllCheckBoxText()
        setupStartButton()
    }

    // [추가] 배너 광고를 로드하는 함수
    private fun loadBannerAd() {
        binding?.adView?.let { adView ->
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        }
    }

    private fun setupCheckBoxes() {
        binding?.checkBoxContainer?.let { container ->
            checkBoxes.clear()

            // 테마 색상 가져오기 (머티리얼 속성 사용)
            val attrs = intArrayOf(
                com.google.android.material.R.attr.colorSurface,
                com.google.android.material.R.attr.colorSurfaceVariant,
                com.google.android.material.R.attr.colorPrimary
            )
            val typedArray = requireContext().obtainStyledAttributes(attrs)
            val colorSurface = typedArray.getColor(0, 0)
            val colorSurfaceVariant = typedArray.getColor(1, 0)
            val colorPrimary = typedArray.getColor(2, 0)
            typedArray.recycle()

            // dp를 픽셀로 변환
            val paddingPx = (16 * resources.displayMetrics.density).toInt()
            val headerPaddingPx = (12 * resources.displayMetrics.density).toInt()

            // 카테고리별로 그룹화
            val groupedBookmarks = bookmarks.groupBy { it.category }
            
            // 카테고리 순서 정의
            val categoryOrder = listOf(
                AppConstants.CATEGORY_HOT,
                AppConstants.CATEGORY_GAME_IT,
                AppConstants.CATEGORY_CHAT,
                AppConstants.CATEGORY_PHOTO,
                AppConstants.CATEGORY_ENTERTAINMENT,
                AppConstants.CATEGORY_ETC
            )

            categoryOrder.forEach { category ->
                val items = groupedBookmarks[category] ?: return@forEach
                // 카테고리 헤더 추가
                val headerView = TextView(requireContext()).apply {
                    text = category
                    textSize = 16f
                    setPadding(headerPaddingPx, paddingPx, headerPaddingPx, headerPaddingPx)
                    setBackgroundColor(colorPrimary)
                    setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                }
                container.addView(headerView)

                // 해당 카테고리의 체크박스들 추가
                items.forEachIndexed { itemIndex, bookmark ->
                    val checkBox = CheckBox(requireContext()).apply {
                        text = bookmark.name
                        textSize = 14f
                        setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
                        // 교대로 배경색 변경
                        setBackgroundColor(if (itemIndex % 2 == 0) colorSurface else colorSurfaceVariant)
                    }

                    container.addView(checkBox)
                    checkBoxes.add(checkBox)
                }
            }
        }
    }

    private fun setupListeners() {
        binding?.checkBoxAll?.setOnClickListener {
            val isChecked = (it as CheckBox).isChecked
            checkBoxes.forEach { checkBox ->
                checkBox.isChecked = isChecked
            }
            saveSelectedStates()
            updateAllCheckBoxText()
        }

        val individualClickListener = View.OnClickListener {
            binding?.checkBoxAll?.isChecked = checkBoxes.all { it.isChecked }
            saveSelectedStates()
            updateAllCheckBoxText()
        }

        checkBoxes.forEach { it.setOnClickListener(individualClickListener) }
    }

    private fun saveSelectedStates() {
        val prefs = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        checkBoxes.forEachIndexed { index, checkBox ->
            editor.putBoolean("${AppConstants.KEY_SELECTED_STATES}$index", checkBox.isChecked)
        }
        editor.apply()
    }

    private fun restoreSelectedStates() {
        val prefs = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        checkBoxes.forEachIndexed { index, checkBox ->
            checkBox.isChecked = prefs.getBoolean("${AppConstants.KEY_SELECTED_STATES}$index", false)
        }
        binding?.checkBoxAll?.isChecked = checkBoxes.all { it.isChecked }
    }

    private fun updateAllCheckBoxText() {
        val selectedCount = checkBoxes.count { it.isChecked }
        val totalCount = checkBoxes.size
        binding?.checkBoxAll?.text = "ALL( 선택 $selectedCount   |   사이트 $totalCount)"
    }

    private fun setupStartButton() {
        binding?.buttonStart?.setOnClickListener {
            val selectedUrls = getSelectedUrls()
            if (selectedUrls.isEmpty()) {
                Toast.makeText(requireContext(), "하나 이상의 사이트를 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                val prefs = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit().putBoolean(AppConstants.KEY_IS_FIRST_RUN, false).apply()
                navigateToWebView(selectedUrls)
            }
        }
    }

    private fun getSelectedUrls(): List<String> {
        return bookmarks.filterIndexed { index, _ -> checkBoxes[index].isChecked }.map { it.url }
    }

    private fun navigateToWebView(urls: List<String>) {
        binding?.adView?.visibility = View.GONE
        binding?.root?.visibility = View.GONE
        val webViewFragment = WebViewFragment.newInstance(urls)
        requireActivity().supportFragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(android.R.id.content, webViewFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            binding?.adView?.visibility = View.GONE
            binding?.adView?.pause()
        } else {
            binding?.adView?.visibility = View.VISIBLE
            binding?.adView?.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        binding?.adView?.pause()
    }
    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            binding?.adView?.resume()
            binding?.adView?.visibility = View.VISIBLE
            binding?.root?.visibility = View.VISIBLE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding?.adView?.destroy()
        binding = null
    }
}