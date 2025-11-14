package com.community.app_1113

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.community.app_1113.databinding.DialogSharePageBinding
import com.community.app_1113.databinding.FragmentWebviewBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WebViewFragment : Fragment() {
    private var binding: FragmentWebviewBinding? = null
    private var startX: Float = 0f
    private var startY: Float = 0f
    private var currentIndex: Int = 0
    private var urls: List<String> = listOf()
    private var scrollListener: View.OnScrollChangeListener? = null

    companion object {
        private const val ARG_URLS = "urls"
        fun newInstance(urls: List<String>): WebViewFragment {
            val fragment = WebViewFragment()
            val args = Bundle()
            args.putStringArrayList(ARG_URLS, ArrayList(urls))
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getStringArrayList(ARG_URLS)?.let {
            urls = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewBinding = FragmentWebviewBinding.inflate(inflater, container, false)
        binding = viewBinding
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        setupSwipeRefresh()
        setupSwipeGesture()
        setupButtons()
        setupOnBackPressed()
        setupCoachMark()
    }

    // [수정된 부분]
    private fun setupCoachMark() {
        val prefs = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val isCoachMarkShown = prefs.getBoolean(AppConstants.KEY_COACH_MARK_SHOWN, false)

        if (!isCoachMarkShown) {
            binding?.coachMarkLayout?.root?.visibility = View.VISIBLE

            // 코치마크를 닫는 동작을 하는 리스너를 하나 만듭니다.
            val dismissListener = View.OnClickListener { dismissCoachMark() }

            // 배경, 버튼, 그리고 이미지에 동일한 리스너를 설정합니다.
            // 이렇게 하면 어디를 눌러도 동일하게 동작합니다.
            binding?.coachMarkLayout?.root?.setOnClickListener(dismissListener)
            binding?.coachMarkLayout?.buttonCloseCoachMark?.setOnClickListener(dismissListener)
            binding?.coachMarkLayout?.imageSwipeHint?.setOnClickListener(dismissListener)
        }
    }

    private fun dismissCoachMark() {
        binding?.coachMarkLayout?.let { coachMark ->
            // '그만 보기' 버튼을 눌렀으므로, 항상 '다시 보지 않기' 상태를 저장
            val prefs = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(AppConstants.KEY_COACH_MARK_SHOWN, true).apply()

            // 코치마크 숨기기
            coachMark.root.visibility = View.GONE
        }
    }

    private fun setupOnBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding?.let {
                    if (it.webView.canGoBack()) {
                        it.webView.goBack()
                    }
                    else if (urls.isNotEmpty()) {
                        goToPreviousSite()
                    }
                }
            }
        })
    }

    private fun setupWebView() {
        binding?.webView?.settings?.javaScriptEnabled = true
        binding?.webView?.settings?.domStorageEnabled = true
        binding?.webView?.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding?.progressBar?.visibility = View.VISIBLE
                binding?.progressBar?.progress = 0
                binding?.swipeRefreshLayout?.isRefreshing = false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding?.progressBar?.visibility = View.GONE
                binding?.swipeRefreshLayout?.isRefreshing = false
                
                // 페이지 히스토리에 추가
                url?.let { pageUrl ->
                    val pageTitle = view?.title ?: pageUrl
                    savePageToHistory(pageTitle, pageUrl)
                }
            }
        }
        binding?.webView?.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding?.progressBar?.progress = newProgress
                if (newProgress == 100) {
                    binding?.progressBar?.visibility = View.GONE
                    binding?.swipeRefreshLayout?.isRefreshing = false
                }
            }
        }
        if (urls.isNotEmpty()) {
            binding?.webView?.loadUrl(urls[currentIndex])
        }
    }

    private fun setupSwipeRefresh() {
        binding?.swipeRefreshLayout?.setOnRefreshListener {
            binding?.webView?.reload()
        }
        
        // WebView의 스크롤 위치를 확인하여 맨 위에 있을 때만 새로고침 가능하도록 설정
        updateSwipeRefreshState()
    }
    
    private fun updateSwipeRefreshState() {
        binding?.webView?.let { webView ->
            // 기존 리스너 제거
            scrollListener?.let { listener ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    webView.setOnScrollChangeListener(null)
                }
            }
            
            // 새 리스너 추가
            scrollListener = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                View.OnScrollChangeListener { _, _, scrollY, _, _ ->
                    val isAtTop = scrollY == 0
                    binding?.swipeRefreshLayout?.isEnabled = isAtTop
                }.also { listener ->
                    webView.setOnScrollChangeListener(listener)
                }
            } else {
                webView.viewTreeObserver.addOnScrollChangedListener {
                    val isAtTop = webView.scrollY == 0
                    binding?.swipeRefreshLayout?.isEnabled = isAtTop
                }
                null
            }
        }
    }

    private fun setupSwipeGesture() {
        binding?.webView?.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentX = event.x
                    val currentY = event.y
                    val deltaX = Math.abs(startX - currentX)
                    val deltaY = Math.abs(startY - currentY)
                    // 수평 스와이프가 수직 스와이프보다 크면 SwipeRefreshLayout 비활성화
                    if (deltaX > deltaY && deltaX > 50) {
                        binding?.swipeRefreshLayout?.isEnabled = false
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val endX = event.x
                    val endY = event.y
                    val deltaX = startX - endX
                    val deltaY = startY - endY
                    if (Math.abs(deltaX) > Math.abs(deltaY)) {
                        if (deltaX > 300 && urls.isNotEmpty()) {
                            goToNextSite()
                            // SwipeRefreshLayout 상태 업데이트
                            updateSwipeRefreshState()
                            return@setOnTouchListener true
                        }
                        else if (deltaX < -300 && urls.isNotEmpty()) {
                            goToPreviousSite()
                            // SwipeRefreshLayout 상태 업데이트
                            updateSwipeRefreshState()
                            return@setOnTouchListener true
                        }
                    }
                    // 스와이프가 아니면 SwipeRefreshLayout 상태 업데이트
                    updateSwipeRefreshState()
                }
            }
            false
        }
    }

    private fun goToNextSite() {
        currentIndex = (currentIndex + 1) % urls.size
        binding?.progressBar?.visibility = View.VISIBLE
        binding?.progressBar?.progress = 0
        binding?.webView?.loadUrl(urls[currentIndex])
    }

    private fun goToPreviousSite() {
        currentIndex = (currentIndex - 1 + urls.size) % urls.size
        binding?.progressBar?.visibility = View.VISIBLE
        binding?.progressBar?.progress = 0
        binding?.webView?.loadUrl(urls[currentIndex])
    }

    private fun goToFirstSite() {
        if (urls.isNotEmpty()) {
            currentIndex = 0
            binding?.progressBar?.visibility = View.VISIBLE
            binding?.progressBar?.progress = 0
            binding?.webView?.loadUrl(urls[currentIndex])
        }
    }

    private fun navigateToBookmark() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, BookmarkFragment(), "BookmarkFragment")
            .commit()
    }

    private fun navigateToPageHistory() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, PageHistoryFragment(), "PageHistoryFragment")
            .addToBackStack(null)
            .commit()
    }

    private fun savePageToHistory(title: String, url: String) {
        val prefs = requireContext().getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val historyJson = prefs.getString(AppConstants.KEY_PAGE_HISTORY, null)
        
        val gson = Gson()
        val historyList = if (historyJson != null) {
            val type = object : TypeToken<MutableList<PageHistoryItem>>() {}.type
            gson.fromJson<MutableList<PageHistoryItem>>(historyJson, type)
        } else {
            mutableListOf()
        }
        
        // 중복 제거 (같은 URL이 있으면 제거)
        historyList.removeAll { it.url == url }
        
        // 새 항목 추가
        historyList.add(PageHistoryItem(title, url))
        
        // 최대 100개까지만 저장
        if (historyList.size > 100) {
            historyList.removeAt(0)
        }
        
        // 저장
        val updatedJson = gson.toJson(historyList)
        prefs.edit().putString(AppConstants.KEY_PAGE_HISTORY, updatedJson).apply()
    }

    private fun setupButtons() {
        binding?.buttonHome?.setOnClickListener {
            goToFirstSite()
        }
        binding?.buttonBack?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding?.buttonForward?.setOnClickListener {
            if (binding?.webView?.canGoForward() == true) {
                binding?.webView?.goForward()
            }
        }
        binding?.buttonMenu?.setOnClickListener {
            showShareDialog()
        }
        binding?.buttonRefresh?.setOnClickListener {
            navigateToPageHistory()
        }
        binding?.buttonBookmark?.setOnClickListener {
            navigateToBookmark()
        }
    }

    private fun showShareDialog() {
        val pageTitle = binding?.webView?.title ?: ""
        val pageUrl = binding?.webView?.url ?: ""
        val dialogBinding = DialogSharePageBinding.inflate(layoutInflater)
        dialogBinding.textPageTitle.text = pageTitle.ifEmpty { "제목 없음" }
        dialogBinding.textUrl.text = pageUrl.ifEmpty { "주소 없음" }
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        dialogBinding.buttonSnsShare.setOnClickListener {
            shareToSns(pageTitle, pageUrl)
            dialog.dismiss()
        }
        dialogBinding.buttonCopyUrl.setOnClickListener {
            copyUrlToClipboard(pageUrl)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun shareToSns(title: String, url: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, url)
        }
        val chooserIntent = Intent.createChooser(shareIntent, "공유하기")
        startActivity(chooserIntent)
    }

    private fun copyUrlToClipboard(url: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("URL", url)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), "주소가 복사되었습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Fragment가 다시 보일 때 스와이프 제스처 재설정
        setupSwipeGesture()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            // Fragment가 다시 보일 때 스와이프 제스처 재설정
            setupSwipeGesture()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding?.webView?.destroy()
        binding = null
    }
}